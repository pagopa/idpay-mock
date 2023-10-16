package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.BaseIntegrationTest;
import it.gov.pagopa.mock.service.pdnd.PdndMockServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class PdndMockControllerTest extends BaseIntegrationTest {

    private static final String CLIENT_ASSERTION = "eyJraWQiOiJyM2VlOHdaMzlmeHE3MUxpbmJZRGdwb0hleXdidXpMeWM0eW5WbGRFQUtZIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJDTElFTlRJRCIsImF1ZCI6ImF1dGgudWF0LmludGVyb3AucGFnb3BhLml0L2NsaWVudC1hc3NlcnRpb24iLCJkaWdlc3QiOnsidmFsdWUiOiJGQjA4NUZDQkZGODIwNkMyMTRDQjNCNDIzQjVCQkQ3RDIxNDYwNzgxOUY3Mjk5RjA3NjJFRTcyOTAyMzlENzUzIiwiYWxnIjoiU0hBMjU2In0sImlzcyI6IkNMSUVOVElEIiwicHVycG9zZUlkIjoiUFVSUE9TRUlEIiwiZXhwIjoxNjk2MDEwMzQ1LCJpYXQiOjE2OTYwMDk5MjUsImp0aSI6IjVmNGEwOTNjLWQwYmMtNDAzMi05MWRjLTI1ZWM5MTRkOGU0YSJ9.bah-zp6lbBf8F9vEaGjyv7gE-DfF8iOnTlBde-NBZlIU3adrs5Nvvgccqc_DCzd5jsEHMOg2Z1dWjGdVcBOWUFDlQFuVRVUNJaxiYpisUPJmTxezV9sOCxGlIebrQJunC2u9wH8PYxqH_xQelvecoxIT9QX7naI6JtnX6uKUUzKNET4JiUB3NkKZtuL37ff7PBh6g-iuTWQO6MWijXB9SKCCrhWUuV64FKhE4_mgTfRdlI0zGcAjq71oplQ6OdoHq-KN4HeTF3Z-w0t2QLEHhAHzkYuhCG0UnfgNnJe4IsnEDomX1mZI5vroP7MFzlW7Q6yTq8ToVjqKt21u9fE-yg";

    @Test
    void createToken_ok() throws Exception {
        MvcResult result = performRequest(CLIENT_ASSERTION, PdndMockServiceImpl.EXPECTED_CLIENT_ASSERTION_TYPE, PdndMockServiceImpl.EXPECTED_GRANT_TYPE, 200);
        Assertions.assertEquals("{\"access_token\":\"eyJ0eXAiOiJhdCtqd3QiLCJhbGciOiJSUzI1NiIsInVzZSI6InNpZyIsImtpZCI6IjMyZDhhMzIxLTE1NjgtNDRmNS05NTU4LWE5MDcyZjUxOWQyZCJ9.ewogICJhdWQiOiAiUkVRVUVTVEVEX1BETkRfU0VSVklDRV9BVURJRU5DRSIsCiAgImlzcyI6ICJ1YXQuaW50ZXJvcC5wYWdvcGEuaXQiLAogICJqdGkiOiAiNjkxY2M0YTQtN2M0YS00YmVhLTliZmItOTkzNGRjYTAwOTAxIiwKICAiY2xpZW50X2lkIjogIkNMSUVOVElEIiwKImRpZ2VzdCI6ICJ7InZhbHVlIjoiRkIwODVGQ0JGRjgyMDZDMjE0Q0IzQjQyM0I1QkJEN0QyMTQ2MDc4MTlGNzI5OUYwNzYyRUU3MjkwMjM5RDc1MyIsImFsZyI6IlNIQTI1NiJ9IiwKImV4cCI6ICIxNjk2MDEwMzQ1IiwKImlhdCI6ICIxNjk2MDA5OTI1IiwKIm5iZiI6ICIxNjk2MDA5OTI1IiwKInB1cnBvc2VJZCI6ICJQVVJQT1NFSUQiLAoic3ViIjogIkNMSUVOVElEIgp9Cg==.Lq-b9r9LHgbAFNyFcCHiIvbvBh9YznIrw3Cr-kpcCC4qflshsEYbhfNlXn4d5n_bwAsFPaFpwbi64zfUn60Ly5vuQTRs_QL01CciIrA1F-XYhgy6n3qYgUI5rQA0w9yxo0k2iOVViX2yXo27W9Cv0rTDsT4Pa6KcfV7-Q1o0JtJZfNulf38hv99hGm8AyNLCcLMFGOpPZzzXBE8TqTtmfQsoxFCUNcniHFIyRoMpI1hWlWRE0SzWAVqbpq4gEcCUKNpCtNF4FVGR0kJ52eob5IPa2bqByFtec4aL-KEI1Kh4InMtMDelQE9vrTJGTmua8YY4e_VW-aH9weFNammSkg\",\"token_type\":\"Bearer\",\"expires_in\":600}",
                result.getResponse().getContentAsString());
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

}
