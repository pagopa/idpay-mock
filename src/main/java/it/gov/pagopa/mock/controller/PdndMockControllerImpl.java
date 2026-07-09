package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.openapi.pdnd.api.TokenOauth2Api;
import it.gov.pagopa.mock.openapi.pdnd.dto.ClientCredentialsResponseDTO;
import it.gov.pagopa.mock.service.pdnd.PdndMockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/idpay/mock/pdnd")
public class PdndMockControllerImpl implements TokenOauth2Api {

    private static final Pattern CODICE_FISCALE_PATTERN = Pattern.compile("^[A-Z0-9]{16}$");

    private final PdndMockService pdndMockService;

    public PdndMockControllerImpl(PdndMockService pdndMockService) {
        this.pdndMockService = pdndMockService;
    }

    @Override
    public ResponseEntity<ClientCredentialsResponseDTO> createToken(String clientAssertion, String clientAssertionType, String grantType, String clientId) {
        ClientCredentialsResponseDTO token = pdndMockService.createToken(clientAssertion, clientAssertionType, grantType, clientId);
        if(token!=null){
            log.info("[MOCK_PDND] Returning PDND fake accessToken for clientId {} and clientAssertion {}", clientId, clientAssertion);
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(value = "/dettaglio/codicefiscale", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<byte[]> getRawInstitutionDetail(
            @RequestParam("codiceFiscale") String codiceFiscale,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {

        String normalizedCodiceFiscale = codiceFiscale == null ? null : codiceFiscale.trim().toUpperCase();
        if (!isValidCodiceFiscale(normalizedCodiceFiscale)) {
            return ResponseEntity.badRequest().build();
        }

        String sanitizedCodiceFiscaleForLog = sanitizeForLog(normalizedCodiceFiscale);
        log.info("[MOCK_PDND] Returning PDND fake visura for codiceFiscale {}", sanitizedCodiceFiscaleForLog);
        byte[] xml = pdndMockService.getRawInstitutionDetail(normalizedCodiceFiscale);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(xml);
    }

    private boolean isValidCodiceFiscale(String codiceFiscale) {
        return codiceFiscale != null && CODICE_FISCALE_PATTERN.matcher(codiceFiscale).matches();
    }

    private String sanitizeForLog(String value) {
        if (value == null) {
            return "<null>";
        }

        String sanitized = value
                .replace('\n', '_')
                .replace('\r', '_')
                .replace('\t', '_')
                .replace('\f', '_')
                .replace('\u0000', '_');

        if (sanitized.length() > 256) {
            sanitized = sanitized.substring(0, 256) + "...";
        }

        return sanitized;
    }
}
