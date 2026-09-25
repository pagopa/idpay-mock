package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.anpr.AnprKoResponseDTO;
import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.AnprResponseBase;
import it.gov.pagopa.mock.dto.anpr.AnprResponseDTO;
import it.gov.pagopa.mock.enums.FamilyResponseTypeEnum;
import it.gov.pagopa.mock.service.anpr_family.AnprMockFamilyGeneratorService;
import it.gov.pagopa.mock.service.anpr_residence.AnprMockGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnprMockControllerImplTest {

    @Mock
    private AnprMockGeneratorService anprMockGeneratorService;

    @Mock
    private AnprMockFamilyGeneratorService anprMockFamilyGeneratorService;

    @Test
    void getAnprResidence_shouldDelegateToResidenceService() {
        AnprRequestDTO request = AnprRequestDTO.builder()
                .idOperazioneClient("operation-id")
                .build();
        AnprResponseDTO expectedResponse = AnprResponseDTO.builder()
                .idOperazioneANPR("anpr-operation-id")
                .build();
        when(anprMockGeneratorService.getAnprResidence(request)).thenReturn(expectedResponse);

        AnprMockControllerImpl controller = buildController(FamilyResponseTypeEnum.OK);

        AnprResponseDTO response = controller.getAnprResidence(request);

        assertSame(expectedResponse, response);
        verify(anprMockGeneratorService).getAnprResidence(request);
        verifyNoInteractions(anprMockFamilyGeneratorService);
    }

    @Test
    void getAnprFamily_shouldReturnOkFamilyResponse_whenResponseTypeIsDefaultOk() {
        AnprRequestDTO request = AnprRequestDTO.builder()
                .idOperazioneClient("operation-id")
                .build();
        AnprResponseDTO expectedBody = AnprResponseDTO.builder()
                .idOperazioneANPR("ok-family")
                .build();
        when(anprMockFamilyGeneratorService.getAnprFamily(request)).thenReturn(expectedBody);

        AnprMockControllerImpl controller = buildController(FamilyResponseTypeEnum.OK);

        ResponseEntity<AnprResponseBase> response = controller.getAnprFamily(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedBody, response.getBody());
        verify(anprMockFamilyGeneratorService).getAnprFamily(request);
    }

    @Test
    void getAnprFamily_shouldReturnOkAnomalyResponse_whenResponseTypeIsOkWithAnomaly() {
        AnprRequestDTO request = AnprRequestDTO.builder()
                .idOperazioneClient("operation-id")
                .build();
        AnprResponseDTO expectedBody = AnprResponseDTO.builder()
                .idOperazioneANPR("ok-with-anomaly")
                .build();
        when(anprMockFamilyGeneratorService.getAnprAnomalyFamily()).thenReturn(expectedBody);

        AnprMockControllerImpl controller = buildController(FamilyResponseTypeEnum.OK_WITH_ANOMALY);

        ResponseEntity<AnprResponseBase> response = controller.getAnprFamily(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedBody, response.getBody());
        verify(anprMockFamilyGeneratorService).getAnprAnomalyFamily();
    }

    @Test
    void getAnprFamily_shouldReturnNotFoundAnomalyError_whenResponseTypeIsKo404ErrorAnomaly() {
        AnprRequestDTO request = AnprRequestDTO.builder()
                .idOperazioneClient("operation-id")
                .build();
        AnprKoResponseDTO expectedBody = AnprKoResponseDTO.builder()
                .idOperazioneANPR("ko-anomaly")
                .build();
        when(anprMockFamilyGeneratorService.getAnprAnomalyError()).thenReturn(expectedBody);

        AnprMockControllerImpl controller = buildController(FamilyResponseTypeEnum.KO_404_ERROR_ANOMALY);

        ResponseEntity<AnprResponseBase> response = controller.getAnprFamily(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertSame(expectedBody, response.getBody());
        verify(anprMockFamilyGeneratorService).getAnprAnomalyError();
    }

    @Test
    void getAnprFamily_shouldReturnTooManyRequestsWithoutBody_whenResponseTypeIsTooManyRequest() {
        AnprRequestDTO request = AnprRequestDTO.builder()
                .idOperazioneClient("operation-id")
                .build();

        AnprMockControllerImpl controller = buildController(FamilyResponseTypeEnum.TOO_MANY_REQUEST);

        ResponseEntity<AnprResponseBase> response = controller.getAnprFamily(request);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertNull(response.getBody());
        verifyNoInteractions(anprMockFamilyGeneratorService);
    }

    private AnprMockControllerImpl buildController(FamilyResponseTypeEnum responseType) {
        return new AnprMockControllerImpl(
                anprMockGeneratorService,
                anprMockFamilyGeneratorService,
                responseType
        );
    }
}

