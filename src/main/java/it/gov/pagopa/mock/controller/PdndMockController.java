package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.Residence;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;


@RequestMapping("/idpay/mock")
public interface PdndMockController {
    @GetMapping("/family/user/{userId}")
    ResponseEntity<Family> getFamilyForUser(@PathVariable String userId);

    @PutMapping("/family")
    ResponseEntity<Family> upsertFamilyUnit(@RequestParam(required = false) String familyId,
                                            @RequestBody @NotEmpty Set<String> userIds);

    @GetMapping("/residence/user/{userId}")
    ResponseEntity<Residence> getResidenceForUser(@PathVariable String userId);
}
