package it.gov.pagopa.mock.dto.anpr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Localita {

    private String codiceStato;
    private String descrizioneLocalita;
    private String descrizioneStato;
    private String provinciaContea;
}
