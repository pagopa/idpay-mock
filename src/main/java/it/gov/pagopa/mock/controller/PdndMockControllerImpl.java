package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.Residence;
import it.gov.pagopa.mock.service.PdndApiMockService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
public class PdndMockControllerImpl implements PdndMockController {
    private final PdndApiMockService pdndApiMockService;

    public PdndMockControllerImpl(PdndApiMockService pdndApiMockService) {
        this.pdndApiMockService = pdndApiMockService;
    }


    @Override
    public ResponseEntity<Family> getFamilyForUser(String userId) {
        return ResponseEntity.ok(pdndApiMockService.getFamilyForUser(userId));
    }

    @Override
    public ResponseEntity<Residence> getResidenceForUser(String userId) {
        return ResponseEntity.ok(pdndApiMockService.getResidenceForUser(userId));
    }
}
