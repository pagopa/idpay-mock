package it.gov.pagopa.mock.service.family;

import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.model.MockedFamily;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

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
}
