package it.gov.pagopa.mock.service.pdnd;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.gov.pagopa.mock.openapi.pdnd.dto.ClientCredentialsResponseDTO;
import it.gov.pagopa.mock.openapi.pdnd.dto.TokenTypeDTO;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PdndMockServiceImpl implements PdndMockService {

    public static final String EXPECTED_CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
    public static final String EXPECTED_GRANT_TYPE = "client_credentials";

    public static final String FAKE_JWT_HEADER = "eyJ0eXAiOiJhdCtqd3QiLCJhbGciOiJSUzI1NiIsInVzZSI6InNpZyIsImtpZCI6IjMyZDhhMzIxLTE1NjgtNDRmNS05NTU4LWE5MDcyZjUxOWQyZCJ9";
    public static final String FAKE_JWT_SIGN = "Lq-b9r9LHgbAFNyFcCHiIvbvBh9YznIrw3Cr-kpcCC4qflshsEYbhfNlXn4d5n_bwAsFPaFpwbi64zfUn60Ly5vuQTRs_QL01CciIrA1F-XYhgy6n3qYgUI5rQA0w9yxo0k2iOVViX2yXo27W9Cv0rTDsT4Pa6KcfV7-Q1o0JtJZfNulf38hv99hGm8AyNLCcLMFGOpPZzzXBE8TqTtmfQsoxFCUNcniHFIyRoMpI1hWlWRE0SzWAVqbpq4gEcCUKNpCtNF4FVGR0kJ52eob5IPa2bqByFtec4aL-KEI1Kh4InMtMDelQE9vrTJGTmua8YY4e_VW-aH9weFNammSkg";

    private final ObjectMapper objectMapper;
    private final String expectedAudience;

    public PdndMockServiceImpl(
            @Value("${mocks.pdnd.expected.audience}") String expectedAudience,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.expectedAudience = expectedAudience;
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
        @SuppressWarnings("unchecked") Map<String, Object> clientAssertionClaims = objectMapper.readValue(Base64.getDecoder().decode(clientAssertionSplits[1]), Map.class);

        validateExpectedClaim(clientAssertionClaims, "iss", clientId);
        validateExpectedClaim(clientAssertionClaims, "sub", clientId);
        validateExpectedClaim(clientAssertionClaims, "aud", expectedAudience);

        @SuppressWarnings("unchecked") Map<String, String> signedDigest = (Map<String, String>) readMandatoryClaim(clientAssertionClaims, "digest");
        String signedPurposeId = (String) readMandatoryClaim(clientAssertionClaims, "purposeId");
        Integer exp = (Integer) readMandatoryClaim(clientAssertionClaims, "exp");
        Integer iat = (Integer) readMandatoryClaim(clientAssertionClaims, "iat");

        return Map.of(
                "exp", exp+"",
                "iat", iat+"",
                "nbf", iat+"",
                "digest", objectMapper.writeValueAsString(signedDigest),
                "purposeId", signedPurposeId,
                "sub", clientId,
                "client_id", clientId
        );
    }

    private static void validateExpectedClaim(Map<String, Object> clientAssertionClaims, String claimName, String expectedClaimValue) {
        Object claimValue = clientAssertionClaims.get(claimName);
        if (!expectedClaimValue.equals(claimValue)) {
            throw new IllegalArgumentException("[PDND_MOCK] Unexpected clientAssertion claims: " + claimName + " doesn't match: " + claimValue);
        }
    }

    @NonNull
    private static Object readMandatoryClaim(Map<String, Object> clientAssertionClaims, String claimName) {
        Object claimValue = clientAssertionClaims.get(claimName);
        if (claimValue == null) {
            throw new IllegalArgumentException("[PDND_MOCK] Unexpected clientAssertion claims: " + claimName + " not provided");
        }
        return claimValue;
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

}
