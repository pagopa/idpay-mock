package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.Residence;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Validated
@RequestMapping("/idpay/mock")
public interface PdndMockController {
    @GetMapping("/family/user/{userId}")
    ResponseEntity<Family> getFamilyForUser(@PathVariable String userId);

    @PutMapping("/family")
    ResponseEntity<Family> upsertFamilyUnit(@RequestParam(required = false) String familyId,
                                            @RequestBody @NotEmpty(message = "The list of userIds cannot be empty") Set<String> userIds);
    @GetMapping("/residence/user/{userId}")
    ResponseEntity<Residence> getResidenceForUser(@PathVariable String userId);
}
