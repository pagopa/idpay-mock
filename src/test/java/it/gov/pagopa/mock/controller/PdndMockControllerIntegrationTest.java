package it.gov.pagopa.mock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.common.utils.TestUtils;
import it.gov.pagopa.common.web.dto.ErrorDTO;
import it.gov.pagopa.common.web.mockmvc.MockMvcUtils;
import it.gov.pagopa.mock.BaseIntegrationTest;
import it.gov.pagopa.mock.connector.encrypt.EncryptRestConnector;
import it.gov.pagopa.mock.connector.encrypt.EncryptRestConnectorImpl;
import it.gov.pagopa.mock.dto.CFDTO;
import it.gov.pagopa.mock.dto.EncryptedCfDTO;
import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.model.MockedFamily;
import it.gov.pagopa.mock.dto.Residence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class PdndMockControllerIntegrationTest extends BaseIntegrationTest {
    private final String userId = "USERID";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    EncryptRestConnector encryptRestConnector;

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

        assertNotNull(familyResult);
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

        assertNotNull(residenceResult);
        TestUtils.checkNotNullFields(residenceResult);
    }

    @Disabled
    void upsertFamilyUnit() {
        String userId2 = "USERID2";
        Set<String> userIds = Set.of("CF1", "CF2");
        CFDTO cfdto1 = new CFDTO("CF1");
        CFDTO cfdto2 = new CFDTO("CF2");

        Mockito.when(encryptRestConnector.upsertToken(Mockito.eq(cfdto1))).thenReturn(new EncryptedCfDTO(userId));
        Mockito.when(encryptRestConnector.upsertToken(Mockito.eq(cfdto2))).thenReturn(new EncryptedCfDTO(userId2));

        try{
            //Creating the familyUnit from scratch
            Family upsertedFamily = MockMvcUtils.extractResponse(upsertFamily(null, userIds), HttpStatus.OK, Family.class);
            assertNotNull(upsertedFamily);
            assertEquals(2, upsertedFamily.getMemberIds().size());
            assertTrue(upsertedFamily.getMemberIds().contains(userId));
            assertTrue(upsertedFamily.getMemberIds().contains(userId2));
            //Trying to create another family unit with the same members from before, throws exception and returns a bad request
            MvcResult result = upsertFamily_badRequest(null, userIds);
            ErrorDTO error = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorDTO.class);
            assertEquals(HttpStatus.BAD_REQUEST.value(), error.getCode());
            assertEquals(String.format("The user %s is already member of the family unit %s", userId, upsertedFamily.getFamilyId()), error.getMessage());
            //Trying to update the same family unit with just one of the members of the family unit
            userIds.clear();
            userIds.add(userId);
            upsertedFamily = MockMvcUtils.extractResponse(upsertFamily(upsertedFamily.getFamilyId(), userIds), HttpStatus.OK, Family.class);
            assertNotNull(upsertedFamily);
            assertEquals(1, upsertedFamily.getMemberIds().size());
        } catch (Exception e){
            Assertions.fail(e);
        }


    }

    @Test
    void upsertFamilyUnit_emptyListUserIds() throws Exception {
        MvcResult result = upsertFamily_badRequest(null, new HashSet<>());

        ErrorDTO error = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorDTO.class);

        assertEquals("CONSTRAINT_VIOLATION", error.getCode());
        assertEquals("The list of userIds cannot be empty", error.getMessage());

    }

    @Test
    void upsertFamilyUnit_emptyStringUserIds() throws Exception {
        Set<String> userIds = new HashSet<>();
        userIds.add(userId);
        userIds.add("");
        MvcResult result = upsertFamily_badRequest(null, userIds);

        ErrorDTO error = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorDTO.class);

        assertEquals("FAMILY_UNIT", error.getCode());
        assertEquals("The userIds cannot contain empty strings", error.getMessage());

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
