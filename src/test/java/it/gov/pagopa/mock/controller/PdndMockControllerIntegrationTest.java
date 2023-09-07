package it.gov.pagopa.mock.controller;

import it.gov.pagopa.common.utils.TestUtils;
import it.gov.pagopa.common.web.mockmvc.MockMvcUtils;
import it.gov.pagopa.mock.BaseIntegrationTest;
import it.gov.pagopa.mock.model.Family;
import it.gov.pagopa.mock.dto.Residence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class PdndMockControllerIntegrationTest extends BaseIntegrationTest {
    private final String userId = "USERID";

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getFamilyForUser(boolean existFamilyInDB) {
        Family family;
        if (existFamilyInDB) {
            family = Family.builder()
                    .familyId("FAMILYID")
                    .memberIds(Set.of(userId, "USERID2")).build();
            mongoTemplate.save(family, "mocked_families");
        } else {
            family = Family.builder()
                    .familyId("FAMILYID_" + userId)
                    .memberIds(new HashSet<>(List.of(userId)))
                    .build();
        }

        Family familyResult=null;

        try {
            familyResult = MockMvcUtils.extractResponse(getFamily(userId), HttpStatus.OK, Family.class);
        } catch (Exception e) {
            Assertions.fail();
        }

        Assertions.assertNotNull(familyResult);
        Assertions.assertEquals(family,familyResult);

        if (existFamilyInDB) {
            mongoTemplate.remove(
                    new Query(Criteria.where("_id").is(family.getFamilyId())),
                    "mocked_families");
        }
    }


    @Test
    void getResidenceForUser() {

        Residence residenceResult=null;

        try {
            residenceResult = MockMvcUtils.extractResponse(getResidence(userId), HttpStatus.OK, Residence.class);
        } catch (Exception e) {
            Assertions.fail();
        }

        Assertions.assertNotNull(residenceResult);
        TestUtils.checkNotNullFields(residenceResult);
    }

    private MvcResult getFamily(String userId) throws Exception {
        return mockMvc
                .perform(get("/idpay/mock/family/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn();
    }

    private MvcResult getResidence(String userId) throws Exception {
        return mockMvc
                .perform(get("/idpay/mock/residence/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn();
    }
}
