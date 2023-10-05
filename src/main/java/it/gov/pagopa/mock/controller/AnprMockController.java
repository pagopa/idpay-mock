package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.AnprResponseDTO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Validated
@RequestMapping("/idpay/mock/anpr")
public interface AnprMockController {

    @PostMapping("/C001-servizioNotifica/v1/anpr-service-e002")
    AnprResponseDTO getAnprResidence(
            @Valid @RequestBody AnprRequestDTO body);

}
