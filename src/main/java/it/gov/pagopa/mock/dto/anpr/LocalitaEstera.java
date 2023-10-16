package it.gov.pagopa.mock.dto.anpr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocalitaEstera {

    private Consolato consolato;
    private IndirizzoEstero indirizzoEstero;

}
