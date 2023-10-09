package it.gov.pagopa.mock.dto.anpr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Toponimo {

    private String denominazione;
    private String denominazioneToponimo;
    private String numeroCivico;
    private String specie;
    private String specieFonte;
}
