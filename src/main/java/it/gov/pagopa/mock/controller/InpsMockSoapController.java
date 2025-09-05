package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.wsimport.inps.*;
import it.gov.pagopa.mock.service.isee.IseeMockService;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.GregorianCalendar;
import java.util.UUID;

import static it.gov.pagopa.mock.wsimport.inps.EsitoEnum.OK;
import static it.gov.pagopa.mock.wsimport.inps.EsitoEnum.RICHIESTA_INVALIDA;
import static it.gov.pagopa.mock.wsimport.inps.SiNoEnum.NO;
import static it.gov.pagopa.mock.wsimport.inps.SiNoEnum.SI;
import static it.gov.pagopa.mock.wsimport.inps.TipoIndicatoreEnum.ISEE_ORDINARIO;

@Slf4j
@Endpoint
public class InpsMockSoapController {
    private static final String NAMESPACE_URI = "http://inps.it/ConsultazioneISEE";

    private final IseeMockService iseeMockService;
    private final ConsultazioneSogliaIndicatoreResponse noIseeResult;

    public InpsMockSoapController(IseeMockService iseeMockService) {
        this.iseeMockService = iseeMockService;

        noIseeResult = new ConsultazioneSogliaIndicatoreResponse();
        ConsultazioneSogliaIndicatoreResponseType value = new ConsultazioneSogliaIndicatoreResponseType();
        value.setEsito(EsitoEnum.DATI_NON_TROVATI);
        noIseeResult.setConsultazioneSogliaIndicatoreResult(value);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "ConsultazioneIndicatore")
    @ResponsePayload
    public ConsultazioneSogliaIndicatoreResponse consultazioneIndicatore(
            @RequestPayload ConsultazioneSogliaIndicatore request) {

        BigDecimal isee = iseeMockService.retrieveIsee(
                request.getRequest().getCodiceFiscale(),
                null
        );

        if (isee != null) {
            log.info("[MOCK_INPS] Returning mocked ISEE:{} for CF:{}",
                    isee, request.getRequest().getCodiceFiscale());

            ConsultazioneSogliaIndicatoreResponse result = new ConsultazioneSogliaIndicatoreResponse();
            ConsultazioneSogliaIndicatoreResponseType value = new ConsultazioneSogliaIndicatoreResponseType();

            if (isee.compareTo(BigDecimal.valueOf(25000)) < 0) {
                value.setEsito(OK);
                value.setDatiIndicatore(buildDatiIndicatore(isee));
            } else {
                value.setEsito(RICHIESTA_INVALIDA);
                value.setDescrizioneErrore("ISEE above the threshold of 25,000");
            }

            result.setConsultazioneSogliaIndicatoreResult(value);
            return result;
        } else {
            return noIseeResult;
        }
    }


    public static byte[] buildXmlResult(BigDecimal isee) {
        TypeEsitoConsultazioneIndicatore xmlResult = new TypeEsitoConsultazioneIndicatore();
        xmlResult.setISEE(isee);

        return toByteArray(xmlResult);
    }

    public static byte[] toByteArray(TypeEsitoConsultazioneIndicatore inpsResult) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TypeEsitoConsultazioneIndicatore.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            JAXBElement<TypeEsitoConsultazioneIndicatore> je = new ObjectFactory().createIndicatore(inpsResult);
            StringWriter sw = new StringWriter();

            marshaller.marshal(je, sw);

            return sw.toString().getBytes(StandardCharsets.UTF_8);
        } catch (JAXBException e){
            throw new IllegalStateException("Cannot create mocked INPS response", e);
        }
    }

    private DatiIndicatoreType buildDatiIndicatore(BigDecimal isee) {
        DatiIndicatoreType dati = new DatiIndicatoreType();
        dati.setTipoIndicatore(ISEE_ORDINARIO);
        dati.setSottoSoglia(isee.compareTo(BigDecimal.valueOf(25000)) < 0 ? SI : NO);
        dati.setProtocolloDSU(UUID.randomUUID().toString());
        try {
            GregorianCalendar gc = new GregorianCalendar();
            XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
            dati.setDataPresentazioneDSU(xmlDate);
        } catch (Exception e) {
            throw new IllegalStateException("Error in creating the date", e);
        }
        dati.setPresenzaDifformita(NO);
        return dati;
    }
}
