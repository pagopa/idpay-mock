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

import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Slf4j
@Endpoint
public class InpsMockSoapController {
    private static final String NAMESPACE_URI = "http://inps.it/ConsultazioneISEE";

    private final IseeMockService iseeMockService;
    private final ConsultazioneIndicatoreResponse noIseeResult;

    public InpsMockSoapController(IseeMockService iseeMockService) {
        this.iseeMockService = iseeMockService;

        noIseeResult = new ConsultazioneIndicatoreResponse();
        ConsultazioneIndicatoreResponseType value = new ConsultazioneIndicatoreResponseType();
        value.setEsito(EsitoEnum.DATI_NON_TROVATI);
        noIseeResult.setConsultazioneIndicatoreResult(value);
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "ConsultazioneIndicatore")
    @ResponsePayload
    public ConsultazioneIndicatoreResponse consultazioneIndicatore(@RequestPayload ConsultazioneIndicatore request) {
        BigDecimal isee = iseeMockService.retrieveIsee(
                request.getRequest().getRicercaCF().getCodiceFiscale(),
                request.getRequest().getTipoIndicatore()
        );
        if(isee!=null){
            log.info("[MOCK_INPS] Returning mocked ISEE for fiscalCode={} iseeType={}: {}", request.getRequest().getRicercaCF().getCodiceFiscale(), request.getRequest().getTipoIndicatore(), isee);
            ConsultazioneIndicatoreResponse result = new ConsultazioneIndicatoreResponse();
            ConsultazioneIndicatoreResponseType value = new ConsultazioneIndicatoreResponseType();
            value.setEsito(EsitoEnum.OK);
            value.setXmlEsitoIndicatore(buildXmlResult(isee));
            result.setConsultazioneIndicatoreResult(value);
            return result;
        } else {
            return noIseeResult;
        }
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "ConsultazioneAttestazione")
    @ResponsePayload
    public ConsultazioneAttestazioneResponse consultazioneAttestazione(@RequestPayload ConsultazioneAttestazione request) {
        throw new IllegalStateException("NOT IMPLEMENTED");
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
}
