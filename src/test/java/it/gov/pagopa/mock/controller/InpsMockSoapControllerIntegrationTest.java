package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.wsimport.inps.*;
import it.gov.pagopa.mock.BaseIntegrationTest;
import it.gov.pagopa.mock.dto.SaveIseeRequestDTO;
import it.gov.pagopa.mock.enums.IseeTypologyEnum;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class InpsMockSoapControllerIntegrationTest extends BaseIntegrationTest {

    private static final XMLInputFactory xmlFactory = XMLInputFactory.newFactory();
    private static final JAXBContext jaxbContext;

    static {
        try {
            jaxbContext = JAXBContext.newInstance(TypeEsitoConsultazioneIndicatore.class);
        } catch (JAXBException e) {
            throw new IllegalStateException("Something gone wrong while configuring JAXB serializer", e);
        }
    }

    @Autowired
    private InpsMockSoapController inpsMockSoapController;

    @Test
    void test(){
        DataMockControllerIntegrationTest.storeIsee(mockMvc, objectMapper, "CF", new SaveIseeRequestDTO(Map.of(IseeTypologyEnum.ORDINARIO, BigDecimal.TEN)));

        ConsultazioneIndicatoreResponse result = inpsMockSoapController.consultazioneIndicatore(buildRequest("CF", TipoIndicatoreSinteticoEnum.ORDINARIO));
        Assertions.assertNotNull(result);
        Assertions.assertEquals(EsitoEnum.OK, result.getConsultazioneIndicatoreResult().getEsito());
        Assertions.assertEquals(BigDecimal.TEN, getIseeFromResponse(result.getConsultazioneIndicatoreResult()));
    }

    private static ConsultazioneIndicatore buildRequest(String cf, TipoIndicatoreSinteticoEnum iseeType) {
        ConsultazioneIndicatore out = new ConsultazioneIndicatore();
        ConsultazioneIndicatoreRequestType value = new ConsultazioneIndicatoreRequestType();
        RicercaCFType ricercaCF = new RicercaCFType();
        ricercaCF.setCodiceFiscale(cf);
        value.setRicercaCF(ricercaCF);
        value.setTipoIndicatore(iseeType);
        out.setRequest(value);
        return out;
    }

    private BigDecimal getIseeFromResponse(ConsultazioneIndicatoreResponseType inpsResponse) {
        if (inpsResponse.getXmlEsitoIndicatore() != null && inpsResponse.getXmlEsitoIndicatore().length > 0) {
            try {
                String inpsResultString = new String(inpsResponse.getXmlEsitoIndicatore(), StandardCharsets.UTF_8);

                TypeEsitoConsultazioneIndicatore inpsResult = readResultFromXmlString(inpsResultString);
                return inpsResult.getISEE();
            } catch (Exception e) {
                return null;
            }
        } else {
            return null;
        }
    }

    public static TypeEsitoConsultazioneIndicatore readResultFromXmlString(String inpsResultString) {
        try (StringReader sr = new StringReader(inpsResultString)) {
            XMLStreamReader xsr = xmlFactory.createXMLStreamReader(sr);

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

            JAXBElement<TypeEsitoConsultazioneIndicatore> je = unmarshaller.unmarshal(xsr, TypeEsitoConsultazioneIndicatore.class);
            return je.getValue();
        } catch (JAXBException | XMLStreamException e) {
            throw new IllegalStateException("[ONBOARDING_REQUEST][INPS_INVOCATION] Cannot read XmlEsitoIndicatore to get ISEE from INPS response", e);
        }
    }
}
