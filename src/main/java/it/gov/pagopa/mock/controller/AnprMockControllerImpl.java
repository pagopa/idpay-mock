package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.AnprResponseBase;
import it.gov.pagopa.mock.dto.anpr.AnprResponseDTO;
import it.gov.pagopa.mock.enums.FamilyResponseTypeEnum;
import it.gov.pagopa.mock.service.anpr_family.AnprMockFamilyGeneratorService;
import it.gov.pagopa.mock.service.anpr_residence.AnprMockGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AnprMockControllerImpl implements AnprMockController {

    private final AnprMockGeneratorService anprMockGeneratorService;
    private final AnprMockFamilyGeneratorService anprMockFamilyGeneratorService;
    private final FamilyResponseTypeEnum anprResponseType;

    public AnprMockControllerImpl(AnprMockGeneratorService anprMockGeneratorService,
                                  AnprMockFamilyGeneratorService anprMockFamilyGeneratorService,
                                  @Value("${mocks.pdnd.family.response-type}") FamilyResponseTypeEnum anprResponseType) {
        this.anprMockGeneratorService = anprMockGeneratorService;
        this.anprMockFamilyGeneratorService = anprMockFamilyGeneratorService;
        this.anprResponseType = anprResponseType;
    }

    @Override
    public AnprResponseDTO getAnprResidence(AnprRequestDTO body) {
        AnprResponseDTO anprResidence = anprMockGeneratorService.getAnprResidence(body);
        log.info("[MOCK_ANPR] Returning {}", anprResidence);
        return anprResidence;
    }

    /** ANPR user found ok*/

    @Override
    public ResponseEntity<AnprResponseBase> getAnprFamily(AnprRequestDTO body) {
        switch (anprResponseType){
            case OK_WITH_ANOMALY -> {
                log.info("[MOCK_FAMILY] Performing mock response: HTTP 200 with anomaly code");
                return  ResponseEntity.status(HttpStatus.OK)
                        .body(anprMockFamilyGeneratorService.getAnprAnomalyFamily());
            }
            case KO_404_ERROR_ANOMALY -> {
                log.info("[MOCK_FAMILY] Performing mock response: HTTP 404 with anomaly code");
                return  ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(anprMockFamilyGeneratorService.getAnprAnomalyError());
            }
            case TOO_MANY_REQUEST -> {
                log.info("[MOCK_FAMILY] Performing mock response: HTTP 429");
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();}
            default -> {
                log.info("[MOCK_FAMILY] Performing mock response: HTTP 200");
                return  ResponseEntity.status(HttpStatus.OK)
                        .body(anprMockFamilyGeneratorService.getAnprFamily(body));
            }
        }
    }

}
