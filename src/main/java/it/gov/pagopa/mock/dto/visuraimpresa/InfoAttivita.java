package it.gov.pagopa.mock.dto.visuraimpresa;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD) 
public class InfoAttivita {

    @XmlElementWrapper(name = "classificazioni-ateco")
    @XmlElement(name = "classificazione-ateco")
    private List<ClassificazioneAteco> classificazioniAteco;

    public InfoAttivita() {
    }

    public InfoAttivita(List<ClassificazioneAteco> classificazioniAteco) {
        this.classificazioniAteco = classificazioniAteco;
    }
}