package it.gov.pagopa.mock.controller;

import it.gov.pagopa.common.config.JsonConfig;
import it.gov.pagopa.common.web.dto.ErrorDTO;
import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.common.web.mockmvc.MockMvcUtils;
import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.Residence;
import it.gov.pagopa.mock.dto.SaveIseeRequestDTO;
import it.gov.pagopa.mock.enums.IseeTypologyEnum;
import it.gov.pagopa.mock.service.DataMockService;
import it.gov.pagopa.mock.service.isee.IseeMockService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(value = DataMockControllerImpl.class,
        excludeAutoConfiguration = {UserDetailsServiceAutoConfiguration.class, SecurityAutoConfiguration.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(JsonConfig.class)
class DataMockControllerIntegrationTest {
    private static final String USER_ID = "USERID";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DataMockService dataMockService;

    @MockitoBean
    private IseeMockService iseeMockService;

    @Test
    void getFamilyForUser_familyPresentIntoDB() throws Exception {
        Family expectedFamily = Family.builder()
                .familyId("FAMILYID")
                .memberIds(Set.of(USER_ID, "USERID2"))
                .build();

        when(dataMockService.getFamilyForUser(USER_ID)).thenReturn(expectedFamily);

        Family familyResult = MockMvcUtils.extractResponse(getFamily(USER_ID), HttpStatus.OK, Family.class);
        assertNotNull(familyResult);
        assertEquals(expectedFamily, familyResult);
    }

    @Test
    void getFamilyForUser_familyNotPresentIntoDB() throws Exception {
        Family expectedFamily = Family.builder()
                .familyId("0123456789ABCDEF01234567")
                .memberIds(Set.of(USER_ID))
                .build();

        when(dataMockService.getFamilyForUser(USER_ID)).thenReturn(expectedFamily);

        Family familyResult = MockMvcUtils.extractResponse(getFamily(USER_ID), HttpStatus.OK, Family.class);
        assertNotNull(familyResult);
        assertEquals(Set.of(USER_ID), familyResult.getMemberIds());
        assertEquals(24, familyResult.getFamilyId().length());
    }

    @Test
    void getResidenceForUser() throws Exception {
        Residence expectedResidence = Residence.builder()
                .postalCode("00100")
                .cityCouncil("Roma")
                .province("RM")
                .city("Roma")
                .region("Lazio")
                .nation("IT")
                .build();

        when(dataMockService.getResidenceForUser(USER_ID)).thenReturn(expectedResidence);

        Residence residenceResult = MockMvcUtils.extractResponse(getResidence(USER_ID), HttpStatus.OK, Residence.class);
        assertNotNull(residenceResult);
        assertEquals(expectedResidence, residenceResult);
    }

    @Test
    void upsertFamilyUnit() throws Exception {
        Set<String> creationUserIds = Set.of("CF1", "CF2");
        Family createdFamily = Family.builder()
                .familyId("FAMILYID")
                .memberIds(Set.of("TOKENIZED_CF1", "TOKENIZED_CF2"))
                .build();

        when(dataMockService.upsertFamilyUnit(null, creationUserIds))
                .thenReturn(createdFamily)
                .thenThrow(new ClientExceptionWithBody(HttpStatus.BAD_REQUEST, "FAMILY_UNIT",
                        "The user TOKENIZED_CF2 is already member of the family unit FAMILYID"));

        Family firstResponse = MockMvcUtils.extractResponse(upsertFamily(null, creationUserIds), HttpStatus.OK, Family.class);
        assertNotNull(firstResponse);
        assertEquals(Set.of("TOKENIZED_CF1", "TOKENIZED_CF2"), firstResponse.getMemberIds());

        MvcResult badRequest = upsertFamilyBadRequest(null, creationUserIds);
        ErrorDTO duplicateError = objectMapper.readValue(badRequest.getResponse().getContentAsString(), ErrorDTO.class);
        assertEquals("FAMILY_UNIT", duplicateError.getCode());
        assertEquals("The user TOKENIZED_CF2 is already member of the family unit FAMILYID", duplicateError.getMessage());

        Set<String> updateUserIds = Set.of("CF1");
        Family updatedFamily = Family.builder()
                .familyId("FAMILYID")
                .memberIds(Set.of("TOKENIZED_CF1"))
                .build();
        when(dataMockService.upsertFamilyUnit("FAMILYID", updateUserIds)).thenReturn(updatedFamily);

        Family updateResponse = MockMvcUtils.extractResponse(upsertFamily("FAMILYID", updateUserIds), HttpStatus.OK, Family.class);
        assertNotNull(updateResponse);
        assertEquals(Set.of("TOKENIZED_CF1"), updateResponse.getMemberIds());
    }

    @Test
    void upsertFamilyUnit_emptyListUserIds() throws Exception {
        MvcResult result = upsertFamilyBadRequest(null, new HashSet<>());
        ErrorDTO error = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorDTO.class);
        assertEquals("CONSTRAINT_VIOLATION", error.getCode());
        assertEquals("The list of userIds cannot be empty", error.getMessage());
    }

    @Test
    void upsertFamilyUnit_emptyStringUserIds() throws Exception {
        Set<String> userIds = new HashSet<>();
        userIds.add(USER_ID);
        userIds.add("");

        when(dataMockService.upsertFamilyUnit(null, userIds))
                .thenThrow(new ClientExceptionWithBody(HttpStatus.BAD_REQUEST, "FAMILY_UNIT",
                        "The userIds cannot contain empty strings"));

        MvcResult result = upsertFamilyBadRequest(null, userIds);
        ErrorDTO error = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorDTO.class);
        assertEquals("FAMILY_UNIT", error.getCode());
        assertEquals("The userIds cannot contain empty strings", error.getMessage());
    }

    @Test
    void storeIsee() {
        SaveIseeRequestDTO request = new SaveIseeRequestDTO(Map.of(
                IseeTypologyEnum.ORDINARIO, BigDecimal.TEN,
                IseeTypologyEnum.MINORENNE, BigDecimal.TEN,
                IseeTypologyEnum.UNIVERSITARIO, BigDecimal.TEN,
                IseeTypologyEnum.SOCIOSANITARIO, BigDecimal.TEN,
                IseeTypologyEnum.DOTTORATO, BigDecimal.TEN,
                IseeTypologyEnum.RESIDENZIALE, BigDecimal.TEN
        ));

        storeIsee(mockMvc, objectMapper, "CF_OK", request);
        verify(iseeMockService).saveIsee(("CF_OK"), (request.getIseeTypeMap()));
    }

    protected MvcResult getFamily(String userId) throws Exception {
        return mockMvc.perform(get("/idpay/mock/family/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    protected MvcResult getResidence(String userId) throws Exception {
        return mockMvc.perform(get("/idpay/mock/residence/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    protected MvcResult upsertFamily(String familyId, Set<String> userIds) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.put("/idpay/mock/family")
                        .queryParam("familyId", familyId)
                        .content(objectMapper.writeValueAsString(userIds))
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    protected MvcResult upsertFamilyBadRequest(String familyId, Set<String> userIds) throws Exception {
        return mockMvc.perform(MockMvcRequestBuilders.put("/idpay/mock/family")
                        .queryParam("familyId", familyId)
                        .content(objectMapper.writeValueAsString(userIds))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andReturn();
    }

    public static void storeIsee(MockMvc mockMvc, ObjectMapper objectMapper, String cf, SaveIseeRequestDTO saveIseeRequestDTO) {
        try {
            mockMvc.perform(MockMvcRequestBuilders.post("/idpay/mock/isee")
                            .header("Fiscal-Code", cf)
                            .content(objectMapper.writeValueAsString(saveIseeRequestDTO))
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().is2xxSuccessful());
        } catch (Exception e) {
            throw new IllegalStateException("Something gone wrong storing isee", e);
        }
    }
}
