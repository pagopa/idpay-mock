package it.gov.pagopa.mock.dto.anpr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Residenza {

    private LocalitaEstera localitaEstera;
    private Indirizzo indirizzo;
    private String tipoIndirizzo;
}
