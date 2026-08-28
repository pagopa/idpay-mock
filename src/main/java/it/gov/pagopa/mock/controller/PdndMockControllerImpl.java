package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.visuraimpresa.VisuraImpresa;
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

    private static final Pattern TAX_ID_PATTERN =
            Pattern.compile("^(?:[A-Z0-9]{16}|\\d{11})$");

    private final PdndMockService pdndMockService;

    public PdndMockControllerImpl(PdndMockService pdndMockService) {
        this.pdndMockService = pdndMockService;
    }

    @Override
    public ResponseEntity<ClientCredentialsResponseDTO> createToken(
            String clientAssertion,
            String clientAssertionType,
            String grantType,
            String clientId) {

        ClientCredentialsResponseDTO token = pdndMockService.createToken(
                clientAssertion,
                clientAssertionType,
                grantType,
                clientId);

        if (token != null) {
            log.info(
                    "[MOCK_PDND] Returning fake accessToken for clientId {}",
                    sanitizeForLog(clientId));
            return ResponseEntity.ok(token);
        }

        log.warn("[MOCK_PDND] Token creation rejected for clientId {}", sanitizeForLog(clientId));
        return ResponseEntity.badRequest().build();
    }

    @GetMapping(value = "/dettaglio/codicefiscale",
                produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<VisuraImpresa> getRawInstitutionDetail(
            @RequestParam("codiceFiscale") String codiceFiscale,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {

        String normalizedTaxId = normalize(codiceFiscale);

        if (!isValidTaxId(normalizedTaxId)) {
            log.warn("[MOCK_PDND] Invalid codiceFiscale format: {}", sanitizeForLog(normalizedTaxId));
            return ResponseEntity.badRequest().build();
        }

        log.info(
                "[MOCK_PDND] Returning fake visura for codiceFiscale {} (auth header present: {})",
                sanitizeForLog(normalizedTaxId),
                authorization != null && !authorization.isBlank());

        VisuraImpresa rawInstitutionDetail = pdndMockService.getRawInstitutionDetail(normalizedTaxId);

        return ResponseEntity.ok(rawInstitutionDetail);
    }

    @PostMapping(
            value = "/visura-impresa",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> saveVisuraImpresa(@RequestBody VisuraImpresa visuraImpresa) {

        String normalizedTaxId = normalize(visuraImpresa != null ? visuraImpresa.getCodiceFiscale() : null);
        if (!isValidTaxId(normalizedTaxId)) {
            log.warn("[MOCK_PDND] Invalid codiceFiscale format: {}", sanitizeForLog(normalizedTaxId));
            return ResponseEntity.badRequest().build();
        }

        pdndMockService.saveVisuraImpresa(visuraImpresa);

        log.info("[MOCK_PDND] Saved mocked visura for codiceFiscale {}", sanitizeForLog(normalizedTaxId));

        return ResponseEntity.ok().build();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private boolean isValidTaxId(String taxId) {
        return taxId != null && TAX_ID_PATTERN.matcher(taxId).matches();
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