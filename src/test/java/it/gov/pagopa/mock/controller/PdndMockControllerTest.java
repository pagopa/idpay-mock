package it.gov.pagopa.mock.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import it.gov.pagopa.common.config.JsonConfig;
import it.gov.pagopa.mock.dto.visuraimpresa.ClassificazioneAteco;
import it.gov.pagopa.mock.dto.visuraimpresa.InfoAttivita;
import it.gov.pagopa.mock.dto.visuraimpresa.VisuraImpresa;
import it.gov.pagopa.mock.openapi.pdnd.dto.ClientCredentialsResponseDTO;
import it.gov.pagopa.mock.openapi.pdnd.dto.TokenTypeDTO;
import it.gov.pagopa.mock.service.pdnd.PdndMockService;
import it.gov.pagopa.mock.service.pdnd.PdndMockServiceImpl;


@WebMvcTest(value={PdndMockControllerImpl.class})
@AutoConfigureMockMvc(addFilters = false)
@Import({JsonConfig.class})
class PdndMockControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PdndMockService pdndMockService;

    private static final String EXPECTED_ACCESS_TOKEN = "eyJ0eXAiOiJhdCtqd3QiLCJhbGciOiJSUzI1NiIsInVzZSI6InNpZyIsImtpZCI6IjMyZDhhMzIxLTE1NjgtNDRmNS05NTU4LWE5MDcyZjUxOWQyZCJ9.ewogICJhdWQiOiAiUkVRVUVTVEVEX1BETkRfU0VSVklDRV9BVURJRU5DRSIsCiAgImlzcyI6ICJ1YXQuaW50ZXJvcC5wYWdvcGEuaXQiLAogICJqdGkiOiAiNjkxY2M0YTQtN2M0YS00YmVhLTliZmItOTkzNGRjYTAwOTAxIiwKICAiY2xpZW50X2lkIjogIkNMSUVOVElEIiwKImRpZ2VzdCI6ICJ7InZhbHVlIjoiRkIwODVGQ0JGRjgyMDZDMjE0Q0IzQjQyM0I1QkJEN0QyMTQ2MDc4MTlGNzI5OUYwNzYyRUU3MjkwMjM5RDc1MyIsImFsZyI6IlNIQTI1NiJ9IiwKImV4cCI6ICIxNjk2MDEwMzQ1IiwKImlhdCI6ICIxNjk2MDA5OTI1IiwKIm5iZiI6ICIxNjk2MDA5OTI1IiwKInB1cnBvc2VJZCI6ICJQVVJQT1NFSUQiLAoic3ViIjogIkNMSUVOVElEIgp9Cg==.Lq-b9r9LHgbAFNyFcCHiIvbvBh9YznIrw3Cr-kpcCC4qflshsEYbhfNlXn4d5n_bwAsFPaFpwbi64zfUn60Ly5vuQTRs_QL01CciIrA1F-XYhgy6n3qYgUI5rQA0w9yxo0k2iOVViX2yXo27W9Cv0rTDsT4Pa6KcfV7-Q1o0JtJZfNulf38hv99hGm8AyNLCcLMFGOpPZzzXBE8TqTtmfQsoxFCUNcniHFIyRoMpI1hWlWRE0SzWAVqbpq4gEcCUKNpCtNF4FVGR0kJ52eob5IPa2bqByFtec4aL-KEI1Kh4InMtMDelQE9vrTJGTmua8YY4e_VW-aH9weFNammSkg";

    private static final String CLIENT_ASSERTION = "eyJraWQiOiJyM2VlOHdaMzlmeHE3MUxpbmJZRGdwb0hleXdidXpMeWM0eW5WbGRFQUtZIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJDTElFTlRJRCIsImF1ZCI6ImF1dGgudWF0LmludGVyb3AucGFnb3BhLml0L2NsaWVudC1hc3NlcnRpb24iLCJkaWdlc3QiOnsidmFsdWUiOiJGQjA4NUZDQkZGODIwNkMyMTRDQjNCNDIzQjVCQkQ3RDIxNDYwNzgxOUY3Mjk5RjA3NjJFRTcyOTAyMzlENzUzIiwiYWxnIjoiU0hBMjU2In0sImlzcyI6IkNMSUVOVElEIiwicHVycG9zZUlkIjoiUFVSUE9TRUlEIiwiZXhwIjoxNjk2MDEwMzQ1LCJpYXQiOjE2OTYwMDk5MjUsImp0aSI6IjVmNGEwOTNjLWQwYmMtNDAzMi05MWRjLTI1ZWM5MTRkOGU0YSJ9.bah-zp6lbBf8F9vEaGjyv7gE-DfF8iOnTlBde-NBZlIU3adrs5Nvvgccqc_DCzd5jsEHMOg2Z1dWjGdVcBOWUFDlQFuVRVUNJaxiYpisUPJmTxezV9sOCxGlIebrQJunC2u9wH8PYxqH_xQelvecoxIT9QX7naI6JtnX6uKUUzKNET4JiUB3NkKZtuL37ff7PBh6g-iuTWQO6MWijXB9SKCCrhWUuV64FKhE4_mgTfRdlI0zGcAjq71oplQ6OdoHq-KN4HeTF3Z-w0t2QLEHhAHzkYuhCG0UnfgNnJe4IsnEDomX1mZI5vroP7MFzlW7Q6yTq8ToVjqKt21u9fE-yg";
    private static final String CLIENT_ASSERTION_VISURA_STYLE = buildJwtLikeToken(
            """
                    {
                      "kid": "visura-kid",
                      "typ": "JWT",
                      "alg": "RS256"
                    }
                    """,
            """
                    {
                      "sub": "CLIENTID",
                      "aud": ["auth.uat.interop.pagopa.it/client-assertion"],
                      "iss": "CLIENTID",
                      "purposeId": "PURPOSEID",
                      "exp": 1893456000,
                      "iat": 1893452400,
                      "jti": "5f4a093c-d0bc-4032-91dc-25ec914d8e4a"
                    }
                    """
    );

    @Test
    void createToken_ok() throws Exception {
        mockTokenResponse(CLIENT_ASSERTION, EXPECTED_ACCESS_TOKEN);
        MvcResult result = performRequest(CLIENT_ASSERTION, PdndMockServiceImpl.EXPECTED_CLIENT_ASSERTION_TYPE, PdndMockServiceImpl.EXPECTED_GRANT_TYPE, 200);
        Assertions.assertEquals("{\"access_token\":\"" + EXPECTED_ACCESS_TOKEN + "\",\"token_type\":\"Bearer\",\"expires_in\":600}",
                result.getResponse().getContentAsString());
    }

    @Test
    void createToken_okVisuraStyleAssertion() throws Exception {
        mockTokenResponse(CLIENT_ASSERTION_VISURA_STYLE, "mock-access-token");
        MvcResult result = performRequest(CLIENT_ASSERTION_VISURA_STYLE, PdndMockServiceImpl.EXPECTED_CLIENT_ASSERTION_TYPE, PdndMockServiceImpl.EXPECTED_GRANT_TYPE, 200);
        String response = result.getResponse().getContentAsString();

        Assertions.assertTrue(response.contains("\"access_token\":\""));
        Assertions.assertTrue(response.contains("\"token_type\":\"Bearer\""));
        Assertions.assertTrue(response.contains("\"expires_in\":600"));
        Assertions.assertFalse(response.contains("\\\"digest\\\""));
    }

    @Test
    void createToken_koInvalidClientAssertion() throws Exception {
        MvcResult result = performRequest("CLIENT_ASSERTION", PdndMockServiceImpl.EXPECTED_CLIENT_ASSERTION_TYPE, PdndMockServiceImpl.EXPECTED_GRANT_TYPE, 400);
        Assertions.assertEquals(0L, result.getResponse().getContentLength());
    }

    @Test
    void createToken_koInvalidClientAssertionContent() throws Exception {
        MvcResult result = performRequest("CLIENT.e30=.ASSERTION", PdndMockServiceImpl.EXPECTED_CLIENT_ASSERTION_TYPE, PdndMockServiceImpl.EXPECTED_GRANT_TYPE, 400);
        Assertions.assertEquals(0L, result.getResponse().getContentLength());
    }

    @Test
    void createToken_koInvalidFixedRequestValue() throws Exception {
        MvcResult result = performRequest(CLIENT_ASSERTION, "CLIENT_ASSERTION_TYPE", "GRANT_TYPE", 400);
        Assertions.assertEquals(0L, result.getResponse().getContentLength());
    }

    private MvcResult performRequest(String clientAssertion, String clientAssertionType, String grantType, int expectedStatus) throws Exception {
        return mockMvc.perform(
                        post("/idpay/mock/pdnd/token.oauth2")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("client_id", "CLIENTID")
                                .param("client_assertion", clientAssertion)
                                .param("client_assertion_type", clientAssertionType)
                                .param("grant_type", grantType)
                )
                .andExpect(MockMvcResultMatchers.status().is(expectedStatus))
                .andReturn();
    }

    private void mockTokenResponse(String clientAssertion, String accessToken) {
      when(pdndMockService.createToken(
            clientAssertion,
            PdndMockServiceImpl.EXPECTED_CLIENT_ASSERTION_TYPE,
            PdndMockServiceImpl.EXPECTED_GRANT_TYPE,
            "CLIENTID"))
          .thenReturn(new ClientCredentialsResponseDTO(accessToken, TokenTypeDTO.BEARER, 600));
    }

    @Test
    void getRawInstitutionDetail_ok() throws Exception {

      VisuraImpresa visuraImpresa = new VisuraImpresa(
        "ABCDEF12G34H567I",
        new InfoAttivita(java.util.List.of(
          new ClassificazioneAteco("47.11.10", "Commercio al dettaglio", "1")
        ))
      );
      when(pdndMockService.getRawInstitutionDetail("ABCDEF12G34H567I"))
        .thenReturn(visuraImpresa);

        MvcResult result = mockMvc.perform(
                        get("/idpay/mock/pdnd/dettaglio/codicefiscale")
                                .param("codiceFiscale", "ABCDEF12G34H567I")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer validToken")
                                .accept(MediaType.APPLICATION_XML)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        Assertions.assertTrue(result.getResponse().getContentAsString().contains("<VisuraImpresa>"));
    }

    @Test
    void getRawInstitutionDetail_invalidCodiceFiscale() throws Exception {
        String invalidCodiceFiscale = "INVALID_CF";

        mockMvc.perform(
                        get("/idpay/mock/pdnd/dettaglio/codicefiscale")
                                .param("codiceFiscale", invalidCodiceFiscale)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer validToken")
                                .accept(MediaType.APPLICATION_XML)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void getRawInstitutionDetail_missingCodiceFiscale() throws Exception {
        mockMvc.perform(
                        get("/idpay/mock/pdnd/dettaglio/codicefiscale")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer validToken")
                                .accept(MediaType.APPLICATION_XML)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    void saveVisuraImpresa_ok() throws Exception {
        String requestBody = """
                {
                  "codiceFiscale": "ABCDEF12G34H567I",
                  "infoAttivita": {
                    "classificazioniAteco": [
                      {
                        "codiceAttivita": "47.11.10",
                        "attivita": "Commercio al dettaglio",
                        "codiceImportanza": "1"
                      }
                    ]
                  }
                }
                """;

        mockMvc.perform(
                        post("/idpay/mock/pdnd/visura-impresa")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    void saveVisuraImpresa_invalidTaxId_returnsBadRequest() throws Exception {
        String requestBody = """
                {
                  "codiceFiscale": "INVALID_CF",
                  "infoAttivita": {
                    "classificazioniAteco": [
                      {
                        "codiceAttivita": "47.11.10",
                        "attivita": "Commercio al dettaglio",
                        "codiceImportanza": "1"
                      }
                    ]
                  }
                }
                """;

        mockMvc.perform(
                        post("/idpay/mock/pdnd/visura-impresa")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    private static String buildJwtLikeToken(String headerJson, String payloadJson) {
        return encodeBase64Url(headerJson) + "." + encodeBase64Url(payloadJson) + ".signature-with-url_safe-characters";
    }

    private static String encodeBase64Url(String value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
    
}
