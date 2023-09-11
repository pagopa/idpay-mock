package it.gov.pagopa.mock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.common.utils.TestUtils;
import it.gov.pagopa.common.web.dto.ErrorDTO;
import it.gov.pagopa.common.web.mockmvc.MockMvcUtils;
import it.gov.pagopa.mock.BaseIntegrationTest;
import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.model.MockedFamily;
import it.gov.pagopa.mock.dto.Residence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class PdndMockControllerIntegrationTest extends BaseIntegrationTest {
    private final String userId = "USERID";

    @Autowired
    ObjectMapper objectMapper;

    @BeforeEach
    public void setUp(){
        mongoTemplate.remove(new Query(), "mocked_families");
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getFamilyForUser(boolean existFamilyInDB) {
        MockedFamily mockedFamily;
        if (existFamilyInDB) {
            mockedFamily = MockedFamily.builder()
                    .familyId("FAMILYID")
                    .memberIds(Set.of(userId, "USERID2")).build();
            mongoTemplate.save(mockedFamily, "mocked_families");
        } else {
            mockedFamily = MockedFamily.builder()
                    .familyId("FAMILYID_" + userId)
                    .memberIds(new HashSet<>(List.of(userId)))
                    .build();
        }

        Family familyResult =null;

        try {
            familyResult = MockMvcUtils.extractResponse(getFamily(userId), HttpStatus.OK, Family.class);
        } catch (Exception e) {
            Assertions.fail();
        }

        Family expectedFamily = Family.builder().familyId(mockedFamily.getFamilyId())
                .memberIds(mockedFamily.getMemberIds())
                .build();

        Assertions.assertNotNull(familyResult);
        assertEquals(expectedFamily, familyResult);
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

    @Test
    void upsertFamilyUnit() {
        String userId2 = "USERID2";
        Set<String> userIds = new HashSet<>();
        userIds.add(userId);
        userIds.add(userId2);

        Family upsertedFamily = null;
        ErrorDTO error = null;
        try{
            //Creating the familyUnit from scratch
            upsertedFamily = MockMvcUtils.extractResponse(upsertFamily(null, userIds), HttpStatus.OK, Family.class);
            //Trying to create another family unit with the same members from before, throws exception and returns a bad request
            MvcResult result = upsertFamily_badRequest(null, userIds);
            error = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorDTO.class);
        } catch (Exception e){
            Assertions.fail(e);
        }

        assertEquals(2, upsertedFamily.getMemberIds().size());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getCode());
        assertEquals(String.format("The user %s is already member of the family unit %s", userId, upsertedFamily.getFamilyId()), error.getMessage());

    }

    @Test
    void upsertFamilyUnit_emptyListUserIds() throws Exception {
        MvcResult result = upsertFamily_badRequest(null, new HashSet<>());

        ErrorDTO error = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getCode());
        assertEquals("The list of userIds cannot be empty", error.getMessage());

    }

    @Test
    void upsertFamilyUnit_emptyStringkUserIds() throws Exception {
        MvcResult result = upsertFamily_badRequest(null, new HashSet<>());

        ErrorDTO error = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorDTO.class);

        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getCode());
        assertEquals("The list of userIds cannot be empty", error.getMessage());

    }

    protected MvcResult getFamily(String userId) throws Exception {
        return mockMvc
                .perform(get("/idpay/mock/family/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn();
    }

    protected MvcResult getResidence(String userId) throws Exception {
        return mockMvc
                .perform(get("/idpay/mock/residence/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn();
    }

    protected MvcResult upsertFamily(String familyId, Set<String> userIds) throws Exception {
        return mockMvc
                .perform(MockMvcRequestBuilders.put("/idpay/mock/family")
                        .queryParam("familyId", familyId)
                        .content(objectMapper.writeValueAsString(userIds))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn();
    }

    protected MvcResult upsertFamily_badRequest(String familyId, Set<String> userIds) throws Exception {
        return mockMvc
                .perform(MockMvcRequestBuilders.put("/idpay/mock/family")
                        .queryParam("familyId", familyId)
                        .content(objectMapper.writeValueAsString(userIds))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }
}
