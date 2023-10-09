package it.gov.pagopa.mock.controller;

import it.gov.pagopa.common.web.mockmvc.MockMvcUtils;
import it.gov.pagopa.mock.BaseIntegrationTest;
import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.AnprResponseDTO;
import it.gov.pagopa.mock.mapper.AnprMapperFaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class AnprMockControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getAnprResidence() {
        AnprResponseDTO residenceResult = null;

        AnprRequestDTO anprRequestDTO = AnprMapperFaker.mockInstanceBuilder("AAAAAA01E65B000B");

        try {
            residenceResult = MockMvcUtils.extractResponse(getAnprResidence(anprRequestDTO), HttpStatus.OK, AnprResponseDTO.class);
        } catch (Exception e) {
            Assertions.fail();
        }

        assertNotNull(residenceResult);
    }

    protected MvcResult getAnprResidence(AnprRequestDTO anprRequestDTO) throws Exception {
        return mockMvc
                .perform(post("/idpay/mock/anpr/C001-servizioNotifica/v1/anpr-service-e002", anprRequestDTO)
                        .content(objectMapper.writeValueAsString(anprRequestDTO))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andReturn();
    }
}
