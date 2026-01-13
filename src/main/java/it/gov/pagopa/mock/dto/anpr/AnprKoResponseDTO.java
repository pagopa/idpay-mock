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
public class AnprKoResponseDTO {

    private String idOperazioneANPR;
    private List<ErroriAnomalia> listaErrori;

}
