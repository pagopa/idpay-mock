package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.anpr.AnprKoResponseDTO;
import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.AnprResponseDTO;
import it.gov.pagopa.mock.service.anpr_family.AnprMockFamilyGeneratorService;
import it.gov.pagopa.mock.service.anpr_residence.AnprMockGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class AnprMockControllerImpl implements AnprMockController {

    private final AnprMockGeneratorService anprMockGeneratorService;
    private final AnprMockFamilyGeneratorService anprMockFamilyGeneratorService;

    public AnprMockControllerImpl(AnprMockGeneratorService anprMockGeneratorService, AnprMockFamilyGeneratorService anprMockFamilyGeneratorService) {
        this.anprMockGeneratorService = anprMockGeneratorService;
        this.anprMockFamilyGeneratorService = anprMockFamilyGeneratorService;
    }

    @Override
    public AnprResponseDTO getAnprResidence(AnprRequestDTO body) {
        AnprResponseDTO anprResidence = anprMockGeneratorService.getAnprResidence(body);
        log.info("[MOCK_ANPR] Returning {}", anprResidence);
        return anprResidence;
    }
    /** ANPR user found ok*/

        @Override
    public AnprResponseDTO getAnprFamily(AnprRequestDTO body) {
       AnprResponseDTO anprResidence = anprMockFamilyGeneratorService.getAnprFamily(body);
        log.info("[MOCK_ANPR_FAMILY] Returning {}", anprResidence);
        return anprResidence;
    }


    /** ANPR user not 200 found found

    @Override
    public AnprResponseDTO getAnprFamily(AnprRequestDTO body) {
       AnprResponseDTO anprResidence = anprMockFamilyGeneratorService.getAnprAnomalyFamily();
        log.info("[MOCK_ANPR_FAMILY][ANOMALY_FOUND] Returning {}", anprResidence);
        return anprResidence;
    }

    */



    /** ANPR Too Many Request */
    /*
    @Override
    public ResponseEntity<AnprResponseDTO> getAnprFamily(AnprRequestDTO body) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }
     */

    /** ANPR user not found found HTTP
    @Override
    public ResponseEntity<AnprKoResponseDTO> getAnprFamily(AnprRequestDTO body) {
        AnprKoResponseDTO anprResidence = anprMockFamilyGeneratorService.getAnprAnomalyError();
        log.info("[MOCK_ANPR_FAMILY][ANOMALY_FOUND] Returning {}", anprResidence);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(anprResidence);
    }
*/

}
