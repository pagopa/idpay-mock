package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.service.isee.IseeMockService;
import it.gov.pagopa.mock.wsimport.inps.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.xml.datatype.DatatypeFactory;
import java.math.BigDecimal;

import static it.gov.pagopa.mock.wsimport.inps.EsitoEnum.*;
import static it.gov.pagopa.mock.wsimport.inps.SiNoEnum.SI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InpsMockSoapControllerIntegrationTest {

    private IseeMockService iseeMockService;
    private InpsMockSoapController controller;

    @BeforeEach
    void setUp() {
        iseeMockService = mock(IseeMockService.class);
        controller = new InpsMockSoapController(iseeMockService);
    }

    @Test
    void consultazioneSogliaIndicatore_ShouldReturnOk_WhenIseeBelowThreshold() {
        String cf = "RSSMRA80A01H501U";
        BigDecimal mockedIsee = BigDecimal.valueOf(9000);

        when(iseeMockService.retrieveIsee(cf, null)).thenReturn(mockedIsee);

        ConsultazioneSogliaIndicatore request = new ConsultazioneSogliaIndicatore();
        ConsultazioneSogliaIndicatoreRequestType reqType = new ConsultazioneSogliaIndicatoreRequestType();
        reqType.setCodiceFiscale(cf);
        request.setRequest(reqType);

        ConsultazioneSogliaIndicatoreResponse response = controller.consultazioneSogliaIndicatore(request);

        assertThat(response).isNotNull();
        ConsultazioneSogliaIndicatoreResponseType result = response.getConsultazioneSogliaIndicatoreResult();
        assertThat(result.getEsito()).isEqualTo(OK);
        assertThat(result.getDatiIndicatore()).isNotNull();
        assertThat(result.getDatiIndicatore().getSottoSoglia()).isEqualTo(SI);
    }

    @Test
    void consultazioneSogliaIndicatore_ShouldReturnInvalid_WhenIseeAboveThreshold() {
        String cf = "RSSMRA80A01H501U";
        BigDecimal mockedIsee = BigDecimal.valueOf(30000);

        when(iseeMockService.retrieveIsee(cf, null)).thenReturn(mockedIsee);

        ConsultazioneSogliaIndicatore request = new ConsultazioneSogliaIndicatore();
        ConsultazioneSogliaIndicatoreRequestType reqType = new ConsultazioneSogliaIndicatoreRequestType();
        reqType.setCodiceFiscale(cf);
        request.setRequest(reqType);

        ConsultazioneSogliaIndicatoreResponse response = controller.consultazioneSogliaIndicatore(request);

        assertThat(response).isNotNull();
        ConsultazioneSogliaIndicatoreResponseType result = response.getConsultazioneSogliaIndicatoreResult();
        assertThat(result.getEsito()).isEqualTo(RICHIESTA_INVALIDA);
        assertThat(result.getDescrizioneErrore()).isEqualTo("ISEE above the threshold of 25,000");
        assertThat(result.getDatiIndicatore()).isNull();
    }

    @Test
    void consultazioneSogliaIndicatore_ShouldReturnNoData_WhenIseeNotFound() {
        String cf = "RSSMRA80A01H501U";

        when(iseeMockService.retrieveIsee(cf, null)).thenReturn(null);

        ConsultazioneSogliaIndicatore request = new ConsultazioneSogliaIndicatore();
        ConsultazioneSogliaIndicatoreRequestType reqType = new ConsultazioneSogliaIndicatoreRequestType();
        reqType.setCodiceFiscale(cf);
        request.setRequest(reqType);

        ConsultazioneSogliaIndicatoreResponse response = controller.consultazioneSogliaIndicatore(request);

        assertThat(response).isNotNull();
        ConsultazioneSogliaIndicatoreResponseType result = response.getConsultazioneSogliaIndicatoreResult();
        assertThat(result.getEsito()).isEqualTo(DATI_NON_TROVATI);
        assertThat(result.getDatiIndicatore()).isNull();
    }

    @Test
    void buildXmlResult_ShouldReturnXmlContainingIsee() {
        BigDecimal isee = BigDecimal.valueOf(12345);

        byte[] xmlBytes = InpsMockSoapController.buildXmlResult(isee);

        assertThat(xmlBytes).isNotNull();
        String xml = new String(xmlBytes);
        assertThat(xml)
                .satisfies(s -> {
                    assertThat(s).contains("12345");
                    assertThat(s).contains("<Indicatore");
                });
    }

    @Test
    void toByteArray_ShouldSerializeTypeEsitoConsultazioneSogliaIndicatore() {
        TypeEsitoConsultazioneIndicatore indicatore = new TypeEsitoConsultazioneIndicatore();
        indicatore.setISEE(BigDecimal.valueOf(6789));

        byte[] xmlBytes = InpsMockSoapController.toByteArray(indicatore);

        assertThat(xmlBytes).isNotNull();
        String xml = new String(xmlBytes);
        assertThat(xml)
                .satisfies(s -> {
                    assertThat(s).contains("6789");
                    assertThat(s).contains("<Indicatore");
                });

    }

    @Test
    void toByteArray_ShouldThrowIllegalStateException_WhenMarshallerFails() throws Exception {
        try (MockedStatic<JAXBContext> mockedStatic = Mockito.mockStatic(JAXBContext.class)) {
            JAXBContext fakeContext = mock(JAXBContext.class);
            when(fakeContext.createMarshaller()).thenThrow(new JAXBException("boom"));

            mockedStatic.when(() -> JAXBContext.newInstance(TypeEsitoConsultazioneIndicatore.class))
                    .thenReturn(fakeContext);

            TypeEsitoConsultazioneIndicatore indicatore = new TypeEsitoConsultazioneIndicatore();
            indicatore.setISEE(BigDecimal.valueOf(1234));

            assertThatThrownBy(() -> InpsMockSoapController.toByteArray(indicatore))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot create mocked INPS response");
        }
    }

    @Test
    void buildDatiIndicatore_ShouldThrowIllegalStateException_WhenDatatypeFactoryFails() {
        controller = new InpsMockSoapController(iseeMockService);

        try (MockedStatic<DatatypeFactory> mockedFactory = Mockito.mockStatic(DatatypeFactory.class)) {
            mockedFactory.when(DatatypeFactory::newInstance).thenThrow(new RuntimeException("Error Test"));

            assertThatThrownBy(() -> {
                controller.getClass()
                        .getDeclaredMethod("buildDatiIndicatore", BigDecimal.class)
                        .setAccessible(true);

                java.lang.reflect.Method m = InpsMockSoapController.class
                        .getDeclaredMethod("buildDatiIndicatore", BigDecimal.class);
                m.setAccessible(true);
                m.invoke(controller, BigDecimal.valueOf(1234));
            })
                    .hasCauseInstanceOf(IllegalStateException.class)
                    .hasRootCauseMessage("Error Test");
        }
    }

    @Test
    void consultazioneSogliaIndicatore_ShouldReturnInvalid_WhenIseeEqualsThreshold() {
        String cf = "RSSMRA80A01H501U";
        BigDecimal mockedIsee = BigDecimal.valueOf(25000);

        when(iseeMockService.retrieveIsee(cf, null)).thenReturn(mockedIsee);

        ConsultazioneSogliaIndicatore request = new ConsultazioneSogliaIndicatore();
        ConsultazioneSogliaIndicatoreRequestType reqType = new ConsultazioneSogliaIndicatoreRequestType();
        reqType.setCodiceFiscale(cf);
        request.setRequest(reqType);

        ConsultazioneSogliaIndicatoreResponse response = controller.consultazioneSogliaIndicatore(request);

        assertThat(response).isNotNull();
        ConsultazioneSogliaIndicatoreResponseType result = response.getConsultazioneSogliaIndicatoreResult();
        assertThat(result.getEsito()).isEqualTo(RICHIESTA_INVALIDA);
        assertThat(result.getDescrizioneErrore()).isEqualTo("ISEE above the threshold of 25,000");
        assertThat(result.getDatiIndicatore()).isNull();
    }
}
