package it.gov.pagopa.mock.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.admissibility.generated.soap.ws.client.TipoIndicatoreSinteticoEnum;
import it.gov.pagopa.common.utils.TestUtils;
import it.gov.pagopa.common.web.dto.ErrorDTO;
import it.gov.pagopa.common.web.mockmvc.MockMvcUtils;
import it.gov.pagopa.mock.BaseIntegrationTest;
import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.Residence;
import it.gov.pagopa.mock.dto.SaveIseeRequestDTO;
import it.gov.pagopa.mock.model.MockedFamily;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

class DataMockControllerIntegrationTest extends BaseIntegrationTest {
    private final String userId = "USERID";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        mongoTemplate.remove(new Query(), "mocked_families");
    }

    @Test
    void getFamilyForUser_familyPresentIntoDB() throws Exception {
        MockedFamily mockedFamily = MockedFamily.builder()
                .familyId("FAMILYID")
                .memberIds(Set.of(userId, "USERID2")).build();
        mongoTemplate.save(mockedFamily, "mocked_families");

        Family expectedFamily = Family.builder().familyId(mockedFamily.getFamilyId())
                .memberIds(mockedFamily.getMemberIds())
                .build();


        Family familyResult = MockMvcUtils.extractResponse(getFamily(userId), HttpStatus.OK, Family.class);

        assertNotNull(familyResult);
        assertEquals(expectedFamily, familyResult);

    }

    @Test
    void getFamilyForUser_familyNotPresentIntoDB() throws Exception {

        Family familyResult = MockMvcUtils.extractResponse(getFamily(userId), HttpStatus.OK, Family.class);

        assertNotNull(familyResult);
        assertEquals(Set.of(userId), familyResult.getMemberIds());
        System.out.println(familyResult.getFamilyId());
        assertEquals(24, familyResult.getFamilyId().length());


    }


    @Test
    void getResidenceForUser() {

        Residence residenceResult = null;

        try {
            residenceResult = MockMvcUtils.extractResponse(getResidence(userId), HttpStatus.OK, Residence.class);
        } catch (Exception e) {
            Assertions.fail();
        }

        assertNotNull(residenceResult);
        TestUtils.checkNotNullFields(residenceResult);
    }

    @SneakyThrows
    @Test
    void upsertFamilyUnit() {
        Set<String> userIds = Set.of("CF1", "CF2");

        //Creating the familyUnit from scratch
        Family upsertedFamily = MockMvcUtils.extractResponse(upsertFamily(null, userIds), HttpStatus.OK, Family.class);
        assertNotNull(upsertedFamily);
        MockedFamily storedFamily = checkStoredMockedFamily(upsertedFamily);
        assertEquals(2, storedFamily.getMemberIds().size());
        assertEquals(Set.of("TOKENIZED_CF1", "TOKENIZED_CF2"), storedFamily.getMemberIds());

        //Trying to create another family unit with the same members from before, throws exception and returns a bad request
        MvcResult result = upsertFamily_badRequest(null, userIds);
        ErrorDTO error = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorDTO.class);
        assertEquals("FAMILY_UNIT", error.getCode());
        assertEquals(String.format("The user TOKENIZED_CF2 is already member of the family unit %s", storedFamily.getFamilyId()), error.getMessage());

        //Trying to update the same family unit with just one of the members of the family unit
        userIds= Set.of("CF1");
        upsertedFamily = MockMvcUtils.extractResponse(upsertFamily(storedFamily.getFamilyId(), userIds), HttpStatus.OK, Family.class);
        assertNotNull(upsertedFamily);
        storedFamily = checkStoredMockedFamily(upsertedFamily);
        assertEquals(1, storedFamily.getMemberIds().size());
        assertEquals(Set.of("TOKENIZED_CF1"), storedFamily.getMemberIds());

    }

    private MockedFamily checkStoredMockedFamily(Family upsertedFamily) {
        String familyId = upsertedFamily.getFamilyId();
        assertNotNull(familyId);

        MockedFamily storedFamily = mongoTemplate.findById(familyId, MockedFamily.class);
        Assertions.assertNotNull(storedFamily);
        return storedFamily;
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

    @Test
    void storeIsee() {
        storeIsee(mockMvc, objectMapper, "CF_OK", new SaveIseeRequestDTO(Map.of(TipoIndicatoreSinteticoEnum.ORDINARIO, BigDecimal.TEN)));
    }

    public static void storeIsee(MockMvc mockMvc, ObjectMapper objectMapper, String cf, SaveIseeRequestDTO saveIseeRequestDTO) {
        try {
            mockMvc
                    .perform(MockMvcRequestBuilders.post("/idpay/mock/isee")
                            .header("Fiscal-Code", cf)
                            .content(objectMapper.writeValueAsString(saveIseeRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON)
                    )
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
        } catch (Exception e) {
            throw new IllegalStateException("Something gone wrong storing isee", e);
        }
    }
}
