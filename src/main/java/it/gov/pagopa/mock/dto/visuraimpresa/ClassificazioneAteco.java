package it.gov.pagopa.mock.dto.visuraimpresa;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD) 
public class ClassificazioneAteco {

    @XmlAttribute(name = "c-attivita")
    private String codiceAttivita;

    @XmlAttribute
    private String attivita;

    @XmlAttribute(name = "c-importanza")
    private String codiceImportanza;

    public ClassificazioneAteco() {
    }

    public ClassificazioneAteco(String codiceAttivita, String attivita, String codiceImportanza) {
        this.codiceAttivita = codiceAttivita;
        this.attivita = attivita;
        this.codiceImportanza = codiceImportanza;
    }
}