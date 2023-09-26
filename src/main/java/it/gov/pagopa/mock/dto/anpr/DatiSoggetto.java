package it.gov.pagopa.mock.dto.anpr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DatiSoggetto {

    private Generalita generalita;
    private Identificativi identificativi;
    private List<InfoSoggettoEnte> infoSoggettoEnte;
    private List<Residenza> residenza;
}
