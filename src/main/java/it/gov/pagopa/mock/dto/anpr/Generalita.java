package it.gov.pagopa.mock.dto.anpr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Generalita {

    private String annoEspatrio;
    private CodiceFiscale codiceFiscale;
    private String cognome;
    private String dataNascita;
    private String idSchedaSoggettoANPR;
    private LuogoNascita luogoNascita;
    private String nome;
    private String sesso;
    private String soggettoAIRE;

}
