package it.gov.pagopa.mock.mapper;

import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.CriteriRicerca;
import it.gov.pagopa.mock.dto.anpr.DatiRichiesta;

import java.time.LocalDate;

public class AnprMapperFaker {

    public static AnprRequestDTO mockInstanceBuilder(String codiceFiscale){
        return AnprRequestDTO.builder()
                .idOperazioneClient("1")
                .criteriRicerca(CriteriRicerca.builder().codiceFiscale(codiceFiscale).build())
                .datiRichiesta(DatiRichiesta.builder()
                        .dataRiferimentoRichiesta(LocalDate.now().toString())
                        .casoUso("Cxxx")
                        .motivoRichiesta("X")
                        .build())
                .build();

    }
}
