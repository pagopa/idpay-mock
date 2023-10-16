package it.gov.pagopa.mock.dto.anpr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnprRequestDTO {

    private String idOperazioneClient;
    private CriteriRicerca criteriRicerca;
    private DatiRichiesta datiRichiesta;

}