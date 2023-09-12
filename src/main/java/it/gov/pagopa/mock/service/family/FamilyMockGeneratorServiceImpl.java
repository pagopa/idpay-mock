package it.gov.pagopa.mock.service.family;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.mock.connector.encrypt.EncryptRestConnector;
import it.gov.pagopa.mock.dto.CFDTO;
import it.gov.pagopa.mock.dto.EncryptedCfDTO;
import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.model.MockedFamily;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class FamilyMockGeneratorServiceImpl implements FamilyMockGeneratorService {

    private final MongoTemplate mongoTemplate;
    private final EncryptRestConnector encryptRestConnector;

    public FamilyMockGeneratorServiceImpl(MongoTemplate mongoTemplate,
                                          EncryptRestConnector encryptRestConnector) {
        this.mongoTemplate = mongoTemplate;
        this.encryptRestConnector = encryptRestConnector;
    }

    @Override
    public Family retrieveFamily(String userId) {
        log.info("[RETRIEVE_FAMILY] Retrieve family for user {}", userId);
        Family family = searchMockCollection(userId);

        if (family != null) {
            return family;
        }

        return Family.builder()
                .familyId("FAMILYID_" + userId)
                .memberIds(new HashSet<>(List.of(userId)))
                .build();
    }

    @Override
    public Family upsertFamilyUnit(String familyId, Set<String> userCFs) {

        Set<String> userIds = new HashSet<>();
        for (String userId: userCFs){
            userIds.add(encryptCF(userId));
        }

        userIds.forEach(userId -> {
            Family retrievedFamily = searchMockCollection(userId);
            if (retrievedFamily != null && !retrievedFamily.getFamilyId().equals(familyId)) {
                log.error("[UPSERT_FAMILY_UNIT] Error during family unit upsert. User {} is already in the family unit {}", userId, retrievedFamily.getFamilyId());
                throw new ClientExceptionWithBody(HttpStatus.BAD_REQUEST, "FAMILY_UNIT", String.format("The user %s is already member of the family unit %s", userId, retrievedFamily.getFamilyId()));
            }
        });


        MockedFamily upsertedFamilyUnit = saveFamilyUnit(MockedFamily.builder()
                .familyId(familyId)
                .memberIds(userIds)
                .build());

        log.info("[UPSERT_FAMILY_UNIT] Upserted family unit with id {} and family members {}", upsertedFamilyUnit.getFamilyId(), upsertedFamilyUnit.getMemberIds());

        return Family.builder().familyId(upsertedFamilyUnit.getFamilyId()).build();
    }

    private Family searchMockCollection(String userId) {
        try{
            MockedFamily mockedFamily = mongoTemplate.find(
                    new Query(Criteria.where("memberIds").is(userId)),
                    MockedFamily.class
            ).get(0);

            return Family.builder().familyId(mockedFamily.getFamilyId())
                    .memberIds(mockedFamily.getMemberIds())
                    .build();
        } catch (IndexOutOfBoundsException e){
            return null;
        }
    }

    private String encryptCF(String fiscalCode) {
        String userId;
        try {
            EncryptedCfDTO encryptedCfDTO = encryptRestConnector.upsertToken(new CFDTO(fiscalCode));
            userId = encryptedCfDTO.getToken();
        } catch (Exception e) {
            throw new ClientExceptionWithBody(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "INTERNAL SERVER ERROR",
                    "Error during encryption");
        }
        return userId;
    }

    private MockedFamily saveFamilyUnit(MockedFamily mockedFamily){
        return mongoTemplate.save(mockedFamily);
    }
}
