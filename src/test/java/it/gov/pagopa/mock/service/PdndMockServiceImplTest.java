package it.gov.pagopa.mock.service;

import it.gov.pagopa.mock.openapi.pdnd.dto.ClientCredentialsResponseDTO;
import it.gov.pagopa.mock.openapi.pdnd.dto.TokenTypeDTO;
import it.gov.pagopa.mock.service.pdnd.PdndMockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdndMockServiceImplTest {

    @Mock
    private tools.jackson.databind.ObjectMapper objectMapper;

    private PdndMockServiceImpl pdndMockService;

    @BeforeEach
    void setUp() {
        pdndMockService = new PdndMockServiceImpl(
                "expectedAudience",
                objectMapper
        );
    }

    @Test
    void createToken_validRequest_returnsToken() throws Exception {

        String clientId = "validClientId";

        String payload = Base64.getEncoder()
                .encodeToString("{\"dummy\":\"value\"}"
                        .getBytes(StandardCharsets.UTF_8));

        String clientAssertion = "header." + payload + ".signature";

        Map<String, Object> claims = Map.of(
                "iss", clientId,
                "sub", clientId,
                "aud", "expectedAudience",
                "digest", Map.of("key", "value"),
                "purposeId", "purpose",
                "exp", 1234567890,
                "iat", 1234567890
        );

        when(objectMapper.readValue(any(byte[].class), eq(Map.class)))
                .thenReturn(claims);

        when(objectMapper.writeValueAsString(any()))
                .thenReturn("{\"key\":\"value\"}");

        ClientCredentialsResponseDTO response =
                pdndMockService.createToken(
                        clientAssertion,
                        PdndMockServiceImpl.EXPECTED_CLIENT_ASSERTION_TYPE,
                        PdndMockServiceImpl.EXPECTED_GRANT_TYPE,
                        clientId
                );

        assertNotNull(response);
        assertEquals(TokenTypeDTO.BEARER, response.getTokenType());
        assertEquals(600, response.getExpiresIn());
        assertNotNull(response.getAccessToken());
        assertTrue(
                response.getAccessToken()
                        .startsWith(PdndMockServiceImpl.FAKE_JWT_HEADER)
        );
    }

    @Test
    void createToken_invalidGrantType_returnsNull() {

        String payload = Base64.getEncoder()
                .encodeToString("{}".getBytes(StandardCharsets.UTF_8));

        String clientAssertion = "header." + payload + ".signature";

        ClientCredentialsResponseDTO response =
                pdndMockService.createToken(
                        clientAssertion,
                        PdndMockServiceImpl.EXPECTED_CLIENT_ASSERTION_TYPE,
                        "invalidGrantType",
                        "clientId"
                );

        assertNull(response);
    }

    @Test
    void createToken_invalidClientAssertionType_returnsNull() {

        String payload = Base64.getEncoder()
                .encodeToString("{}".getBytes(StandardCharsets.UTF_8));

        String clientAssertion = "header." + payload + ".signature";

        ClientCredentialsResponseDTO response =
                pdndMockService.createToken(
                        clientAssertion,
                        "invalidType",
                        PdndMockServiceImpl.EXPECTED_GRANT_TYPE,
                        "clientId"
                );

        assertNull(response);
    }

    @Test
    void createToken_missingClaims_returnsNull() throws Exception {

        String payload = Base64.getEncoder()
                .encodeToString("{}".getBytes(StandardCharsets.UTF_8));

        String clientAssertion = "header." + payload + ".signature";

        when(objectMapper.readValue(any(byte[].class), eq(Map.class)))
                .thenReturn(Collections.emptyMap());

        ClientCredentialsResponseDTO response =
                pdndMockService.createToken(
                        clientAssertion,
                        PdndMockServiceImpl.EXPECTED_CLIENT_ASSERTION_TYPE,
                        PdndMockServiceImpl.EXPECTED_GRANT_TYPE,
                        "clientId"
                );

        assertNull(response);
    }

    @Test
    void getRawInstitutionDetail_validTaxCode_returnsXml() {

        byte[] result =
                pdndMockService.getRawInstitutionDetail("12345678901");

        assertNotNull(result);

        String xml = new String(result, StandardCharsets.UTF_8);

        assertTrue(xml.contains("<VisuraImpresa>"));
        assertTrue(xml.contains("classificazione-ateco"));
        assertTrue(xml.contains("47.11.10"));
        assertTrue(xml.contains("56.10.11"));
    }

    @Test
    void getRawInstitutionDetail_emptyTaxCode_returnsXml() {

        byte[] result =
                pdndMockService.getRawInstitutionDetail("");

        assertNotNull(result);

        String xml = new String(result, StandardCharsets.UTF_8);

        assertTrue(xml.contains("<VisuraImpresa>"));
        assertTrue(xml.contains("classificazione-ateco"));
    }
}