package it.gov.pagopa.mock.service.anpr_family;

import it.gov.pagopa.mock.connector.decrypt.DecryptRestConnector;
import it.gov.pagopa.mock.connector.encrypt.EncryptRestConnector;
import it.gov.pagopa.mock.dto.CFDTO;
import it.gov.pagopa.mock.dto.DecryptCfDTO;
import it.gov.pagopa.mock.dto.EncryptedCfDTO;
import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.anpr.*;
import it.gov.pagopa.mock.service.family.FamilyMockGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AnprMockFamilyGeneratorServiceImpl implements AnprMockFamilyGeneratorService {

    private static final List<String> NAME = List.of("MARIO", "LUCA", "GIUSEPPE", "ANDREA", "PAOLO", "ELENA", "MARIA", "LUCIA");
    private static final List<String> SURNAME = List.of("ROSSI", "BIANCHI", "VERDI", "FERRARI", "RICCI");
    private static final List<String> GENDER = List.of("M", "F");

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
        String codiceFiscale = anprRequestDTO.getCriteriRicerca().getCodiceFiscale();

        EncryptedCfDTO encryptedCfDTO = encryptRestConnector.upsertToken(new CFDTO(codiceFiscale));

        Family family = familyMockGeneratorService.retrieveFamily(encryptedCfDTO.getToken());

        return family == null ? generateFamily(codiceFiscale, "ROMA", "00100", "RM", true)
                : decryptFamily(family);

    }

    private AnprResponseDTO decryptFamily(Family family) {
        log.info("[FAMILY_MOCKED] Retrieved family data from the database {}", family);
        List<DatiSoggetto> familyMembers = family.getMemberIds().stream().map(user -> {
            DecryptCfDTO decrypted = decryptRestConnector.getPiiByToken(user);
            return createDatiSoggetto(decrypted.getPii(), "MILANO", "20121", "MI", true);
        }).toList();
        List<DatiSoggetto> allFamilyMembers = new ArrayList<>(familyMembers);

        if (family.getMinorMemberIds() != null && !family.getMinorMemberIds().isEmpty()) {
            log.info("[FAMILY_MOCKED] Family with ID {} has {} minor members", family.getFamilyId(), familyMembers.size());
            List<DatiSoggetto> familyMinorMembers = family.getMinorMemberIds().stream().map(user -> {
                DecryptCfDTO decrypted = decryptRestConnector.getPiiByToken(user);
                return createDatiSoggetto(decrypted.getPii(), "MILANO", "20121", "MI", false);
            }).toList();
            allFamilyMembers.addAll(familyMinorMembers);
        }

        return AnprResponseDTO.builder()
                .listaSoggetti(ListaSoggetti.builder()
                        .datiSoggetto(allFamilyMembers)
                        .build())
                .idOperazioneANPR(family.getFamilyId())
                .build();
    }


    public AnprResponseDTO generateFamily(String codiceFiscale, String nomeComune, String cap, String siglaProvincia, boolean isOver18) {
        return AnprResponseDTO.builder()
                .listaSoggetti(ListaSoggetti.builder()
                        .datiSoggetto(List.of(createDatiSoggetto(codiceFiscale, nomeComune, cap, siglaProvincia, isOver18)))
                        .build())
                .idOperazioneANPR(String.valueOf(System.currentTimeMillis()))
                .build();
    }

    private static String getRandom(List<String> lista) {
        int index = SECURE_RANDOM.nextInt(lista.size());
        return lista.get(index);
    }

    private DatiSoggetto createDatiSoggetto(String codiceFiscale, String nomeComune, String cap, String siglaProvincia, boolean isOver18) {
        Comune comune = Comune.builder()
                .codiceIstat("123456")
                .nomeComune(nomeComune)
                .siglaProvinciaIstat(siglaProvincia)
                .build();
        Generalita generalita = Generalita.builder()
                .codiceFiscale(CodiceFiscale.builder().codFiscale(codiceFiscale).validitaCF("1").build())
                .cognome(getRandom(SURNAME))
                .dataNascita(randomBirthDate(isOver18))
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

    public static String randomBirthDate(boolean isOver18) {
        OffsetDateTime now = OffsetDateTime.now();

        OffsetDateTime over18Max = now.minusYears(18);
        OffsetDateTime minDate = now.minusYears(100);

        SecureRandom random = new SecureRandom();

        OffsetDateTime randomDate;
        if (isOver18) {
            long minEpoch = minDate.toEpochSecond();
            long maxEpoch = over18Max.toEpochSecond();

            long randomEpoch = minEpoch + (long) (random.nextDouble() * (maxEpoch - minEpoch));
            randomDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(randomEpoch), now.getOffset());
        } else {
            long minEpoch = over18Max.toEpochSecond();
            long maxEpoch = now.toEpochSecond();

            long randomEpoch = minEpoch + (long) (random.nextDouble() * (maxEpoch - minEpoch));
            randomDate = OffsetDateTime.ofInstant(Instant.ofEpochSecond(randomEpoch), now.getOffset());
        }

        return randomDate.format(FORMATTER);
    }

    @Override
    public AnprResponseDTO getAnprAnomalyFamily() {
        AnprResponseDTO response = new AnprResponseDTO();
        response.setIdOperazioneANPR(String.valueOf(System.currentTimeMillis()));

        ErroriAnomalia anomalia = ErroriAnomalia.builder()
                .codiceErroreAnomalia("TEST001")
                .testoErroreAnomalia("Anomaly test")
                .tipoErroreAnomalia("A").build();
        response.setListaAnomalie(List.of(anomalia));

        return response;
    }

    @Override
    public AnprKoResponseDTO getAnprAnomalyError() {
        AnprKoResponseDTO response = new AnprKoResponseDTO();
        response.setIdOperazioneANPR(String.valueOf(System.currentTimeMillis()));

        ErroriAnomalia anomalia = ErroriAnomalia.builder()
                .codiceErroreAnomalia("TEST001")
                .testoErroreAnomalia("Anomaly test")
                .tipoErroreAnomalia("E").build();
        response.setListaErrori(List.of(anomalia));

        return response;
    }
}
