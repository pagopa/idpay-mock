package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.model.Family;
import it.gov.pagopa.mock.dto.Residence;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/idpay/mock")
public interface PdndMockController {
    @GetMapping("/family/user/{userId}")
    ResponseEntity<Family> getFamilyForUser(@PathVariable String userId);

    @GetMapping("/residence/user/{userId}")
    ResponseEntity<Residence> getResidenceForUser(@PathVariable String userId);
}
