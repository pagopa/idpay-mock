package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.Residence;
import it.gov.pagopa.mock.dto.SaveIseeRequestDTO;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Validated
@RequestMapping("/idpay/mock")
public interface DataMockController {
    @GetMapping("/family/user/{userId}")
    Family getFamilyForUser(@PathVariable String userId);

    @PutMapping("/family")
    Family upsertFamilyUnit(@RequestParam(required = false) String familyId,
                                            @RequestBody @NotEmpty(message = "The list of userIds cannot be empty") Set<String> userIds);
    @GetMapping("/residence/user/{userId}")
    Residence getResidenceForUser(@PathVariable String userId);

    @PostMapping("/isee")
    void saveIsee(@RequestHeader("Fiscal-Code") String fiscalCode, @RequestBody SaveIseeRequestDTO iseeRequestDTO);
}
