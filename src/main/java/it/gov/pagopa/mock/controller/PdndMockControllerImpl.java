package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.openapi.pdnd.api.TokenOauth2Api;
import it.gov.pagopa.mock.openapi.pdnd.dto.ClientCredentialsResponseDTO;
import it.gov.pagopa.mock.service.pdnd.PdndMockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/idpay/mock/pdnd")
public class PdndMockControllerImpl implements TokenOauth2Api {

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
}
