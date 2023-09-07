package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.Residence;
import it.gov.pagopa.mock.service.PdndApiMockService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    public ResponseEntity<Family> upsertFamilyUnit(String familyId, List<String> userIds){
        return ResponseEntity.ok(pdndApiMockService.upsertFamilyUnit(familyId, new HashSet<>(userIds)));
    }

    @Override
    public ResponseEntity<Residence> getResidenceForUser(String userId) {
        return ResponseEntity.ok(pdndApiMockService.getResidenceForUser(userId));
    }
}
