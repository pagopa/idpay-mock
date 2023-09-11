package it.gov.pagopa.mock.service.family;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.model.MockedFamily;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FamilyMockGeneratorServiceImpl implements FamilyMockGeneratorService {

    private final MongoTemplate mongoTemplate;

    public FamilyMockGeneratorServiceImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Family retrieveFamily(String userId) {

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
    public Family upsertFamilyUnit(String familyId, Set<String> userIds) {

        userIds.forEach(userId -> {
                    Family retrievedFamily = searchMockCollection(userId);
                    if (retrievedFamily != null && !retrievedFamily.getFamilyId().equals(familyId)) {
                        throw new ClientExceptionWithBody(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), String.format("The user %s is already member of the family unit %s", userId, retrievedFamily.getFamilyId()));
                    }
                });


        MockedFamily upsertedFamilyUnit = saveFamilyUnit(MockedFamily.builder()
                .familyId(familyId)
                .memberIds(userIds)
                .build());

        return Family.builder().familyId(upsertedFamilyUnit.getFamilyId()).memberIds(upsertedFamilyUnit.getMemberIds()).build();
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

    private MockedFamily saveFamilyUnit(MockedFamily mockedFamily){
        return mongoTemplate.save(mockedFamily);
    }

}
