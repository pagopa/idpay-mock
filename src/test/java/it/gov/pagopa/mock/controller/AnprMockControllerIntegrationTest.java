package it.gov.pagopa.mock.controller;

import tools.jackson.databind.ObjectMapper;
import it.gov.pagopa.common.config.JsonConfig;
import it.gov.pagopa.common.web.mockmvc.MockMvcUtils;
import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.AnprResponseDTO;
import it.gov.pagopa.mock.mapper.AnprMapperFaker;
import it.gov.pagopa.mock.service.anpr_family.AnprMockFamilyGeneratorService;
import it.gov.pagopa.mock.service.anpr_residence.AnprMockGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(value={AnprMockController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({JsonConfig.class})
class AnprMockControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AnprMockGeneratorService anprMockGeneratorService;

    @MockitoBean
    private AnprMockFamilyGeneratorService anprMockFamilyGeneratorService;


    @Test
    void getAnprResidence() throws Exception {

        AnprRequestDTO request =
                AnprMapperFaker.mockInstanceBuilder("AAAAAA01E65B000B");

        AnprResponseDTO response = new AnprResponseDTO();

        when(anprMockGeneratorService.getAnprResidence(any()))
                .thenReturn(response);

        MvcResult result = mockMvc.perform(
                        post("/idpay/mock/anpr/C001-servizioNotifica/v1/anpr-service-e002")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        AnprResponseDTO residenceResult =
                MockMvcUtils.extractResponse(result, HttpStatus.OK, AnprResponseDTO.class);

        assertNotNull(residenceResult);
    }
}