package it.gov.pagopa.mock.service.anpr_family;

import it.gov.pagopa.mock.connector.decrypt.DecryptRestConnector;
import it.gov.pagopa.mock.connector.encrypt.EncryptRestConnector;
import it.gov.pagopa.mock.dto.CFDTO;
import it.gov.pagopa.mock.dto.DecryptCfDTO;
import it.gov.pagopa.mock.dto.EncryptedCfDTO;
import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.anpr.*;
import it.gov.pagopa.mock.service.family.FamilyMockGeneratorService;
import it.gov.pagopa.mock.utils.Utilities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;

@Service
@Slf4j
public class AnprMockFamilyGeneratorServiceImpl implements AnprMockFamilyGeneratorService{
    public static final String FISCAL_CODE_REGEX = "^([A-Za-z]{6}[0-9lmnpqrstuvLMNPQRSTUV]{2}[abcdehlmprstABCDEHLMPRST][0-9lmnpqrstuvLMNPQRSTUV]{2}[A-Za-z][0-9lmnpqrstuvLMNPQRSTUV]{3}[A-Za-z])$";

    private static final List<String> NAME = List.of("MARIO", "LUCA", "GIUSEPPE", "ANDREA", "PAOLO", "ELENA", "MARIA", "LUCIA");
    private static final List<String> SURNAME = List.of("ROSSI", "BIANCHI", "VERDI", "FERRARI", "RICCI");
    private static final List<String> GENDER = List.of("M", "F");

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final FamilyMockGeneratorService familyMockGeneratorService;
    private final EncryptRestConnector encryptRestConnector;
    private final DecryptRestConnector decryptRestConnector;

    public AnprMockFamilyGeneratorServiceImpl(FamilyMockGeneratorService familyMockGeneratorService, EncryptRestConnector encryptRestConnector, DecryptRestConnector decryptRestConnector) {
        this.familyMockGeneratorService = familyMockGeneratorService;
        this.encryptRestConnector = encryptRestConnector;
        this.decryptRestConnector = decryptRestConnector;
    }

    @Override
    public AnprResponseDTO getAnprFamily(AnprRequestDTO anprRequestDTO) {
        if(anprRequestDTO == null || anprRequestDTO.getCriteriRicerca() == null ||
                !anprRequestDTO.getCriteriRicerca().getCodiceFiscale().matches(FISCAL_CODE_REGEX)){
            log.info("[MOCK_ANPR_FAMILY] Fiscal code is not valid");
            return null;
        }
        String codiceFiscale = anprRequestDTO.getCriteriRicerca().getCodiceFiscale();

        EncryptedCfDTO encryptedCfDTO = encryptRestConnector.upsertToken(new CFDTO(codiceFiscale));

        Family family = familyMockGeneratorService.retrieveFamily(encryptedCfDTO.getToken());

        return family == null ? generateFamily(codiceFiscale, "ROMA", "00100", "RM")
                : decryptFamily(family);

    }

    private AnprResponseDTO decryptFamily(Family family) {
        List<DatiSoggetto> familyMembers = family.getMemberIds().stream().map(user -> {
            DecryptCfDTO decrypted = decryptRestConnector.getPiiByToken(user);
            return createDatiSoggetto(decrypted.getPii(), "MILANO", "20121", "MI");
        }).toList();
        return AnprResponseDTO.builder()
                .listaSoggetti(ListaSoggetti.builder()
                        .datiSoggetto(familyMembers)
                        .build())
                .idOperazioneANPR("87654321")
                .build();
    }


    public AnprResponseDTO generateFamily(String codiceFiscale, String nomeComune, String cap, String siglaProvincia){
        return AnprResponseDTO.builder()
                .listaSoggetti(ListaSoggetti.builder()
                        .datiSoggetto(List.of(createDatiSoggetto(codiceFiscale, nomeComune, cap, siglaProvincia)))
                        .build())
                .idOperazioneANPR("87654321")
                .build();
    }

    private static String getRandom(List<String> lista) {
        int index = SECURE_RANDOM.nextInt(lista.size());
        return lista.get(index);
    }

    private DatiSoggetto createDatiSoggetto(String codiceFiscale, String nomeComune, String cap, String siglaProvincia){
        Comune comune = Comune.builder()
                .codiceIstat("123456")
                .nomeComune(nomeComune)
                .siglaProvinciaIstat(siglaProvincia)
                .build();
        Generalita generalita = Generalita.builder()
                .codiceFiscale(CodiceFiscale.builder().codFiscale(codiceFiscale).validitaCF("1").build())
                .cognome(getRandom(SURNAME))
                .dataNascita(Utilities.calculateBirthDateFromFiscalCode(codiceFiscale).toString())
                .idSchedaSoggettoANPR("12345678")
                .luogoNascita(LuogoNascita.builder().comune(comune).build())
                .nome(getRandom(NAME))
                .sesso(getRandom(GENDER))
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
        return DatiSoggetto.builder()
                .generalita(generalita)
                .identificativi(identificativi)
                .infoSoggettoEnte(List.of(infoSoggettoEnte))
                .residenza(List.of(residenza)).build();
    }
}
