package it.gov.pagopa.mock.dto.anpr;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class AnprResponseDTO extends AnprResponseBase{

    private ListaSoggetti listaSoggetti;
    private List<ErroriAnomalia> listaAnomalie;

}
