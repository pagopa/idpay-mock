package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.AnprResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Validated
@RequestMapping("/idpay/anpr/mock")
public interface AnprMockController {

    @PostMapping("/residence")
    ResponseEntity<AnprResponseDTO> getAnprResidence(
            @Valid @RequestBody AnprRequestDTO body);

}
