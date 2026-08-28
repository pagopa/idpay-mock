package it.gov.pagopa.mock.service.pdnd;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import it.gov.pagopa.mock.dto.visuraimpresa.ClassificazioneAteco;
import it.gov.pagopa.mock.dto.visuraimpresa.InfoAttivita;
import it.gov.pagopa.mock.dto.visuraimpresa.VisuraImpresa;
import it.gov.pagopa.mock.model.MockedClassificazioneAteco;
import it.gov.pagopa.mock.model.MockedVisuraImpresa;
import it.gov.pagopa.mock.openapi.pdnd.dto.ClientCredentialsResponseDTO;
import it.gov.pagopa.mock.openapi.pdnd.dto.TokenTypeDTO;
import it.gov.pagopa.mock.utils.Utilities;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class PdndMockServiceImpl implements PdndMockService {

    public static final String EXPECTED_CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
    public static final String EXPECTED_GRANT_TYPE = "client_credentials";

    public static final String FAKE_JWT_HEADER = "eyJ0eXAiOiJhdCtqd3QiLCJhbGciOiJSUzI1NiIsInVzZSI6InNpZyIsImtpZCI6IjMyZDhhMzIxLTE1NjgtNDRmNS05NTU4LWE5MDcyZjUxOWQyZCJ9";
    public static final String FAKE_JWT_SIGN = "Lq-b9r9LHgbAFNyFcCHiIvbvBh9YznIrw3Cr-kpcCC4qflshsEYbhfNlXn4d5n_bwAsFPaFpwbi64zfUn60Ly5vuQTRs_QL01CciIrA1F-XYhgy6n3qYgUI5rQA0w9yxo0k2iOVViX2yXo27W9Cv0rTDsT4Pa6KcfV7-Q1o0JtJZfNulf38hv99hGm8AyNLCcLMFGOpPZzzXBE8TqTtmfQsoxFCUNcniHFIyRoMpI1hWlWRE0SzWAVqbpq4gEcCUKNpCtNF4FVGR0kJ52eob5IPa2bqByFtec4aL-KEI1Kh4InMtMDelQE9vrTJGTmua8YY4e_VW-aH9weFNammSkg";

    private final ObjectMapper objectMapper;
    private final String expectedAudience;
    private final MongoTemplate mongoTemplate;

    public PdndMockServiceImpl(
            @Value("${mocks.pdnd.expected.audience}") String expectedAudience,
            ObjectMapper objectMapper,
            MongoTemplate mongoTemplate) {
        this.objectMapper = objectMapper;
        this.expectedAudience = expectedAudience;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public ClientCredentialsResponseDTO createToken(String clientAssertion, String clientAssertionType, String grantType, String clientId) {
        Map<String, String> claims = validateAndExtractClaims(clientAssertion, clientAssertionType, grantType, clientId);
        if (!CollectionUtils.isEmpty(claims)) {
            log.info("[PDND_MOCK] createToken request OK: clientId={} clientAssertion={}",
                    clientId, clientAssertion);
            return new ClientCredentialsResponseDTO(FAKE_JWT_HEADER + "." + createClaims(claims) + "." + FAKE_JWT_SIGN, TokenTypeDTO.BEARER, 600);
        } else {
            log.error("[PDND_MOCK] Unexpected createToken request: clientId={} clientAssertion={} clientAssertionType={} grantType={}",
                    clientId, clientAssertion,
                    clientAssertionType, grantType);
            return null;
        }
    }

    @SuppressWarnings("squid:S1121")
    private Map<String, String> validateAndExtractClaims(String clientAssertion, String clientAssertionType, String grantType, String clientId) {
        try {
            String[] clientAssertionSplits;
            if (StringUtils.isNotEmpty(clientId) &&
                    StringUtils.isNotEmpty(clientAssertion) &&
                    (clientAssertionSplits = clientAssertion.split("\\.")).length == 3 &&
                    EXPECTED_CLIENT_ASSERTION_TYPE.equals(clientAssertionType) &&
                    EXPECTED_GRANT_TYPE.equals(grantType)) {
                return validateClaims(clientId, clientAssertionSplits);
            } else {
                return Collections.emptyMap();
            }
        } catch (IOException e) {
            log.error("[PDND_MOCK] Unexpected clientAssertion claims", e);
            return Collections.emptyMap();
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return Collections.emptyMap();
        } catch (Exception e) {
            log.error("Something gone wrong validating request", e);
            return Collections.emptyMap();
        }
    }

    private Map<String, String> validateClaims(String clientId, String[] clientAssertionSplits) throws IOException {
        @SuppressWarnings("unchecked") Map<String, Object> clientAssertionClaims = objectMapper.readValue(decodeJwtPart(clientAssertionSplits[1]), Map.class);

        validateExpectedClaim(clientAssertionClaims, "iss", clientId);
        validateExpectedClaim(clientAssertionClaims, "sub", clientId);
        validateExpectedAudience(clientAssertionClaims.get("aud"));

        @SuppressWarnings("unchecked") Map<String, String> signedDigest = (Map<String, String>) clientAssertionClaims.get("digest");
        String signedPurposeId = (String) readMandatoryClaim(clientAssertionClaims, "purposeId");
        long exp = readMandatoryNumericClaim(clientAssertionClaims, "exp");
        long iat = readMandatoryNumericClaim(clientAssertionClaims, "iat");

        Map<String, String> claims = new LinkedHashMap<>();
        claims.put("exp", exp + "");
        claims.put("iat", iat + "");
        claims.put("nbf", iat + "");
        if (!CollectionUtils.isEmpty(signedDigest)) {
            claims.put("digest", objectMapper.writeValueAsString(signedDigest));
        }
        claims.put("purposeId", signedPurposeId);
        claims.put("sub", clientId);
        claims.put("client_id", clientId);
        return claims;
    }

    private static void validateExpectedClaim(Map<String, Object> clientAssertionClaims, String claimName, String expectedClaimValue) {
        Object claimValue = clientAssertionClaims.get(claimName);
        if (!expectedClaimValue.equals(claimValue)) {
            throw new IllegalArgumentException("[PDND_MOCK] Unexpected clientAssertion claims: " + claimName + " doesn't match: " + claimValue);
        }
    }

    private void validateExpectedAudience(Object audienceClaim) {
        if (expectedAudience.equals(audienceClaim)) {
            return;
        }

        if (audienceClaim instanceof Collection<?> audienceValues && audienceValues.contains(expectedAudience)) {
            return;
        }

        throw new IllegalArgumentException("[PDND_MOCK] Unexpected clientAssertion claims: aud doesn't match: " + audienceClaim);
    }

    @NonNull
    private static Object readMandatoryClaim(Map<String, Object> clientAssertionClaims, String claimName) {
        Object claimValue = clientAssertionClaims.get(claimName);
        if (claimValue == null) {
            throw new IllegalArgumentException("[PDND_MOCK] Unexpected clientAssertion claims: " + claimName + " not provided");
        }
        return claimValue;
    }

    private static long readMandatoryNumericClaim(Map<String, Object> clientAssertionClaims, String claimName) {
        Object claimValue = readMandatoryClaim(clientAssertionClaims, claimName);
        if (claimValue instanceof Number number) {
            return number.longValue();
        }

        throw new IllegalArgumentException("[PDND_MOCK] Unexpected clientAssertion claims: " + claimName + " is not numeric");
    }

    private static byte[] decodeJwtPart(String jwtPart) {
        try {
            return Base64.getUrlDecoder().decode(jwtPart);
        } catch (IllegalArgumentException _) {
            return Base64.getDecoder().decode(jwtPart);
        }
    }

    private String createClaims(Map<String, String> claimsFromRequest) {
        return Base64.getEncoder().encodeToString("""
                        {
                          "aud": "REQUESTED_PDND_SERVICE_AUDIENCE",
                          "iss": "uat.interop.pagopa.it",
                          "jti": "691cc4a4-7c4a-4bea-9bfb-9934dca00901",
                          %s
                        }
                        """.formatted(
                                claimsFromRequest.entrySet().stream()
                                        .sorted(Map.Entry.comparingByKey())
                                        .map(e -> "\"%s\": \"%s\"".formatted(e.getKey(), e.getValue()))
                                        .collect(Collectors.joining(",\n"))
                        )
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public VisuraImpresa getRawInstitutionDetail(String taxCode) {
        MockedVisuraImpresa visura = mongoTemplate.findOne(
                Query.query(Criteria.where("taxCode").is(taxCode)),
                MockedVisuraImpresa.class
        );

        if (visura == null) {
            return makeDefaultVisura(taxCode);
        }
        if (visura.getClassificazioniAteco() == null) {
            visura.setClassificazioniAteco(Collections.emptyList());
        }
        return mapMockedVisuraImpresa(taxCode, visura);
    }

    @Override
    public void saveVisuraImpresa(VisuraImpresa visuraImpresa) {
        if (visuraImpresa == null || StringUtils.isBlank(visuraImpresa.getCodiceFiscale())) {
            throw new IllegalArgumentException("[PDND_MOCK] codice-fiscale is mandatory to save a VisuraImpresa");
        }

        MockedVisuraImpresa toSave = MockedVisuraImpresa.builder()
                .taxCode(visuraImpresa.getCodiceFiscale())
                .classificazioniAteco(toMockedClassificazioniAteco(visuraImpresa.getInfoAttivita()))
                .build();

        log.info("[PDND_MOCK] Saving mocked VisuraImpresa for taxCode {}", 
                    Utilities.sanitizeForLog(toSave.getTaxCode()));
        mongoTemplate.save(toSave);
    }

    private List<MockedClassificazioneAteco> toMockedClassificazioniAteco(InfoAttivita infoAttivita) {
        if (infoAttivita == null || CollectionUtils.isEmpty(infoAttivita.getClassificazioniAteco())) {
            return Collections.emptyList();
        }
        return infoAttivita.getClassificazioniAteco().stream()
                .filter(Objects::nonNull)
                .map(c -> MockedClassificazioneAteco.builder()
                        .codiceAttivita(c.getCodiceAttivita())
                        .descrizioneAttivita(c.getAttivita())
                        .codiceImportanza(c.getCodiceImportanza())
                        .build())
                .toList();
    }

    private VisuraImpresa mapMockedVisuraImpresa(String taxCode, MockedVisuraImpresa visura) {
        return new VisuraImpresa(
                taxCode,
                new InfoAttivita(visura.getClassificazioniAteco().stream()
                        .map(c -> ClassificazioneAteco.builder()
                                .codiceAttivita(c.getCodiceAttivita())
                                .attivita(c.getDescrizioneAttivita())
                                .codiceImportanza(c.getCodiceImportanza())
                                .build())
                        .toList())
        );
    }

    private VisuraImpresa makeDefaultVisura(String taxCode) {
        return new VisuraImpresa(
                taxCode,
                new InfoAttivita(List.of(
                        ClassificazioneAteco.builder()
                                .codiceAttivita("47.11.10")
                                .attivita("Commercio al dettaglio")
                                .codiceImportanza("1")
                                .build(),
                        ClassificazioneAteco.builder()
                                .codiceAttivita("56.10.11")
                                .attivita("Ristorazione")
                                .codiceImportanza("2")
                                .build()
                ))
        );
    }

}
