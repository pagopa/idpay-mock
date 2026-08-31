package it.gov.pagopa.mock.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.query.Query;

import it.gov.pagopa.mock.dto.visuraimpresa.ClassificazioneAteco;
import it.gov.pagopa.mock.dto.visuraimpresa.InfoAttivita;
import it.gov.pagopa.mock.dto.visuraimpresa.VisuraImpresa;
import it.gov.pagopa.mock.mapper.MockedVisuraImpresaMapperImpl;
import it.gov.pagopa.mock.mapper.VisuraImpresaMapperImpl;
import it.gov.pagopa.mock.model.MockedClassificazioneAteco;
import it.gov.pagopa.mock.model.MockedVisuraImpresa;
import it.gov.pagopa.mock.openapi.pdnd.dto.ClientCredentialsResponseDTO;
import it.gov.pagopa.mock.openapi.pdnd.dto.TokenTypeDTO;
import it.gov.pagopa.mock.service.pdnd.PdndMockServiceImpl;

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
                mongoTemplate,
                new VisuraImpresaMapperImpl(),
                new MockedVisuraImpresaMapperImpl()
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
    void getRawInstitutionDetail_validTaxCode_returnsVisuraImpresa() {
        VisuraImpresa result = pdndMockService.getRawInstitutionDetail("12345678901");

        assertNotNull(result);
                assertEquals("12345678901", result.getCodiceFiscale());
                assertNotNull(result.getInfoAttivita());
                assertNotNull(result.getInfoAttivita().getClassificazioniAteco());
                assertEquals(2, result.getInfoAttivita().getClassificazioniAteco().size());

                ClassificazioneAteco first = result.getInfoAttivita().getClassificazioniAteco().getFirst();
                assertEquals("47.11.10", first.getCodiceAttivita());
                assertEquals("Commercio al dettaglio", first.getAttivita());
                assertEquals("1", first.getCodiceImportanza());

                ClassificazioneAteco second = result.getInfoAttivita().getClassificazioniAteco().get(1);
                assertEquals("56.10.11", second.getCodiceAttivita());
                assertEquals("Ristorazione", second.getAttivita());
                assertEquals("2", second.getCodiceImportanza());
    }

    @Test
    void getRawInstitutionDetail_visuraPersistedForTaxCode() {
        MockedVisuraImpresa visura = MockedVisuraImpresa.builder()
                .taxCode("3463457457")
                .classificazioniAteco(List.of(MockedClassificazioneAteco.builder()
                        .codiceAttivita("3.2.1")
                        .descrizioneAttivita("Attivita di esempio")
                        .codiceImportanza("1")
                        .build()))
                .build();
        when(mongoTemplate.findOne(any(Query.class), eq(MockedVisuraImpresa.class)))
                .thenReturn(visura);

        VisuraImpresa result = pdndMockService.getRawInstitutionDetail("3463457457");

        assertNotNull(result);
        assertEquals("3463457457", result.getCodiceFiscale());
        assertNotNull(result.getInfoAttivita());
        assertNotNull(result.getInfoAttivita().getClassificazioniAteco());
        assertEquals(1, result.getInfoAttivita().getClassificazioniAteco().size());

        ClassificazioneAteco classification = result.getInfoAttivita().getClassificazioniAteco().getFirst();
        assertEquals("3.2.1", classification.getCodiceAttivita());
        assertEquals("Attivita di esempio", classification.getAttivita());
        assertEquals("1", classification.getCodiceImportanza());
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