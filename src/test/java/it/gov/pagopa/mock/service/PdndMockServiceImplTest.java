package it.gov.pagopa.mock.service;

import it.gov.pagopa.mock.dto.visuraimpresa.ClassificazioneAteco;
import it.gov.pagopa.mock.dto.visuraimpresa.InfoAttivita;
import it.gov.pagopa.mock.dto.visuraimpresa.VisuraImpresa;
import it.gov.pagopa.mock.model.MockedClassificazioneAteco;
import it.gov.pagopa.mock.model.MockedVisuraImpresa;
import it.gov.pagopa.mock.openapi.pdnd.dto.ClientCredentialsResponseDTO;
import it.gov.pagopa.mock.openapi.pdnd.dto.TokenTypeDTO;
import it.gov.pagopa.mock.service.pdnd.PdndMockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PdndMockServiceImplTest {

    @Mock
    private tools.jackson.databind.ObjectMapper objectMapper;

    @Mock
    private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    private PdndMockServiceImpl pdndMockService;

    @BeforeEach
    void setUp() {
        pdndMockService = new PdndMockServiceImpl(
                "expectedAudience",
                objectMapper,
                mongoTemplate
        );
    }

    @Test
    void createToken_validRequest_returnsToken() {

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
    void createToken_visuraStyleRequestWithoutDigest_returnsToken() {

        String clientId = "validClientId";

        String payload = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString("{\"dummy\":\"value\"}".getBytes(StandardCharsets.UTF_8));

        String clientAssertion = "header." + payload + ".signature-with-url_safe-characters";

        Map<String, Object> claims = Map.of(
                "iss", clientId,
                "sub", clientId,
                "aud", List.of("expectedAudience"),
                "purposeId", "purpose",
                "exp", 1234567890L,
                "iat", 1234567890L
        );

        when(objectMapper.readValue(any(byte[].class), eq(Map.class)))
                .thenReturn(claims);

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
    void createToken_missingClaims_returnsNull() {

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

    @Test
    void saveVisuraImpresa_persistsMappedFields() {
        String taxCode = "ABCDEF12G34H567I";
        VisuraImpresa input = new VisuraImpresa(
                taxCode,
                new InfoAttivita(List.of(
                        new ClassificazioneAteco("47.11.10", "Commercio al dettaglio", "1"),
                        new ClassificazioneAteco("56.10.11", "Ristorazione", "2")
                ))
        );

        when(mongoTemplate.save(any(MockedVisuraImpresa.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        pdndMockService.saveVisuraImpresa(input);

        ArgumentCaptor<MockedVisuraImpresa> captor = ArgumentCaptor.forClass(MockedVisuraImpresa.class);
        verify(mongoTemplate).save(captor.capture());

        MockedVisuraImpresa saved = captor.getValue();
        assertNotNull(saved);
        assertEquals(taxCode, saved.getTaxCode());
        assertEquals(2, saved.getClassificazioniAteco().size());

        MockedClassificazioneAteco first = saved.getClassificazioniAteco().get(0);
        assertEquals("47.11.10", first.getCodiceAttivita());
        assertEquals("Commercio al dettaglio", first.getDescrizioneAttivita());
        assertEquals("1", first.getCodiceImportanza());

        MockedClassificazioneAteco second = saved.getClassificazioniAteco().get(1);
        assertEquals("56.10.11", second.getCodiceAttivita());
        assertEquals("Ristorazione", second.getDescrizioneAttivita());
        assertEquals("2", second.getCodiceImportanza());
    }

}