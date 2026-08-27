package it.gov.pagopa.mock.dto.visuraimpresa;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "VisuraImpresa")
@XmlAccessorType(XmlAccessType.FIELD) 
public class VisuraImpresa {

    @XmlElement(name = "codice-fiscale")
    private String codiceFiscale;

    @XmlElement(name = "info-attivita")
    private InfoAttivita infoAttivita;

    public VisuraImpresa() {
    }

    public VisuraImpresa(String codiceFiscale, InfoAttivita infoAttivita) {
        this.codiceFiscale = codiceFiscale;
        this.infoAttivita = infoAttivita;
    }

}