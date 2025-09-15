package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.AnprResponseDTO;
import it.gov.pagopa.mock.service.anpr_family.AnprMockFamilyGeneratorService;
import it.gov.pagopa.mock.service.anpr_residence.AnprMockGeneratorService;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public AnprResponseDTO getAnprFamily(AnprRequestDTO body) {
        AnprResponseDTO anprResidence = anprMockFamilyGeneratorService.getAnprFamily(body);
        log.info("[MOCK_ANPR_FAMILY] Returning {}", anprResidence);
        return anprResidence;
    }
}
