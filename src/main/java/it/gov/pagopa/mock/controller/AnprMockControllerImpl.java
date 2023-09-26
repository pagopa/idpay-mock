package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.AnprResponseDTO;
import it.gov.pagopa.mock.service.anpr_residence.AnprMockGeneratorService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class AnprMockControllerImpl implements AnprMockController {

    private final AnprMockGeneratorService anprMockGeneratorService;

    public AnprMockControllerImpl(AnprMockGeneratorService anprMockGeneratorService) {
        this.anprMockGeneratorService = anprMockGeneratorService;
    }

    @Override
    public ResponseEntity<AnprResponseDTO> getAnprResidence(AnprRequestDTO body) {
        return ResponseEntity.ok(anprMockGeneratorService.getAnprResidence(body));
    }
}
