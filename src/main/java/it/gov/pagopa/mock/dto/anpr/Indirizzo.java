package it.gov.pagopa.mock.dto.anpr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Indirizzo {
    private String cap;
    private Comune comune;
    private NumeroCivico numeroCivico;
    private Toponimo toponimo;


}
