package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.AnprResponseDTO;
import it.gov.pagopa.mock.service.anpr_residence.AnprMockGeneratorService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnprMockControllerImpl implements AnprMockController {

    private final AnprMockGeneratorService anprMockGeneratorService;

    public AnprMockControllerImpl(AnprMockGeneratorService anprMockGeneratorService) {
        this.anprMockGeneratorService = anprMockGeneratorService;
    }

    @Override
    public AnprResponseDTO getAnprResidence(AnprRequestDTO body) {
        return anprMockGeneratorService.getAnprResidence(body);
    }
}
