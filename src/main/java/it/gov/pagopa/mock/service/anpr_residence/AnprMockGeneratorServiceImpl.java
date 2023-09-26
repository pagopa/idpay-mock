package it.gov.pagopa.mock.service.anpr_residence;

import it.gov.pagopa.mock.dto.anpr.*;
import it.gov.pagopa.mock.utils.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AnprMockGeneratorServiceImpl implements AnprMockGeneratorService {

    public static final String FISCAL_CODE_REGEX = "^([A-Za-z]{6}[0-9lmnpqrstuvLMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST][0-9lmnpqrstuvLMNPQRSTUV]{2}[A-Za-z][0-9lmnpqrstuvLMNPQRSTUV]{3}[A-Za-z])$";


    @Override
    public AnprResponseDTO getAnprResidence(AnprRequestDTO anprRequestDTO) {
        if(anprRequestDTO == null || anprRequestDTO.getCriteriRicerca() == null ||
                !anprRequestDTO.getCriteriRicerca().getCodiceFiscale().matches(FISCAL_CODE_REGEX)){
            log.info("[MOCK RESIDENCE] Fiscal code is not valid");
            return null;
        }
        String codiceFiscale = anprRequestDTO.getCriteriRicerca().getCodiceFiscale();
        char residenceCode = codiceFiscale.charAt(11);

        return (Character.toString(residenceCode).matches("[A-Ma-m]")) ?
                mapper(codiceFiscale, "ROMA", "00100", "RM") :
                mapper(codiceFiscale, "MILANO", "20121", "MI");
    }

    public AnprResponseDTO mapper(String codiceFiscale, String nomeComune, String cap, String siglaProvincia){
        Comune comune = Comune.builder()
                .codiceIstat("123456")
                .nomeComune(nomeComune)
                .siglaProvinciaIstat(siglaProvincia)
                .build();
        Generalita generalita = Generalita.builder()
                .codiceFiscale(CodiceFiscale.builder().codFiscale(codiceFiscale).validitaCF("1").build())
                .cognome("ROSSI")
                .dataNascita(Utilities.calculateBirthDateFromFiscalCode(codiceFiscale).toString())
                .idSchedaSoggettoANPR("12345678")
                .luogoNascita(LuogoNascita.builder().comune(comune).build())
                .nome("MARIO")
                .sesso("M")
                .build();
        Identificativi identificativi = Identificativi.builder().idANPR("AB12345CD").build();
        InfoSoggettoEnte infoSoggettoEnte = InfoSoggettoEnte.builder()
                .chiave("Verifica esistenza in vita").id("1234").valore("S").build();
        Residenza residenza = Residenza.builder()
                .indirizzo(Indirizzo.builder()
                        .cap(cap)
                        .comune(comune)
                        .numeroCivico(NumeroCivico.builder()
                                .civicoInterno(CivicoInterno.builder().interno1("5").scala("A").build())
                                .numero("10")
                                .build())
                        .toponimo(Toponimo.builder()
                                .denominazioneToponimo("AMERIGO VESPUCCI")
                                .specie("VIA")
                                .specieFonte("1")
                                .build())
                        .build())
                .tipoIndirizzo("1")
                .build();
        return AnprResponseDTO.builder()
                .listaSoggetti(ListaSoggetti.builder()
                        .datiSoggetto(List.of(DatiSoggetto.builder()
                                .generalita(generalita)
                                .identificativi(identificativi)
                                .infoSoggettoEnte(List.of(infoSoggettoEnte))
                                .residenza(List.of(residenza)).build()))
                        .build())
                .idOperazioneANPR("87654321")
                .build();
    }

}
