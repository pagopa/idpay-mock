package it.gov.pagopa.mock.dto.anpr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DatiRichiesta {

    private String dataRiferimentoRichiesta;
    private String motivoRichiesta;
    private String casoUso;
}
