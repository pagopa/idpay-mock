package it.gov.pagopa.mock.service.anpr_family;

import it.gov.pagopa.mock.connector.decrypt.DecryptRestConnector;
import it.gov.pagopa.mock.connector.encrypt.EncryptRestConnector;
import it.gov.pagopa.mock.dto.CFDTO;
import it.gov.pagopa.mock.dto.DecryptCfDTO;
import it.gov.pagopa.mock.dto.EncryptedCfDTO;
import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.anpr.AnprKoResponseDTO;
import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.AnprResponseDTO;
import it.gov.pagopa.mock.dto.anpr.CriteriRicerca;
import it.gov.pagopa.mock.dto.anpr.DatiSoggetto;
import it.gov.pagopa.mock.service.family.FamilyMockGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnprMockFamilyGeneratorServiceImplTest {

    public static final String ENCRYPTED_TOKEN = "encrypted-token";
    public static final String PII = "BBBBBB01E07A000A";
    private static final String FAMILY_ANOMALY_CODE = "FAMILY_ANOMALY";
    @Mock
    private FamilyMockGeneratorService familyMockGeneratorService;
    @Mock
    private EncryptRestConnector encryptRestConnector;
    @Mock
    private DecryptRestConnector decryptRestConnector;

    private AnprMockFamilyGeneratorServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AnprMockFamilyGeneratorServiceImpl(
                familyMockGeneratorService,
                encryptRestConnector,
                decryptRestConnector,
                FAMILY_ANOMALY_CODE
        );
    }

    @Test
    void testGetAnprFamily_whenFamilyExists() {
        // given
        AnprRequestDTO request = AnprRequestDTO.builder()
                .criteriRicerca(CriteriRicerca.builder().codiceFiscale(PII).build())
                .build();

        EncryptedCfDTO encrypted = new EncryptedCfDTO(ENCRYPTED_TOKEN);
        when(encryptRestConnector.upsertToken(any(CFDTO.class))).thenReturn(encrypted);

        Family family = Family.builder().memberIds(Set.of("token-1", "token-2")).build();
        when(familyMockGeneratorService.retrieveFamily(ENCRYPTED_TOKEN)).thenReturn(family);

        when(decryptRestConnector.getPiiByToken("token-1")).thenReturn(new DecryptCfDTO("BBBBBB01E07A000B"));
        when(decryptRestConnector.getPiiByToken("token-2")).thenReturn(new DecryptCfDTO("BBBBBB01E07A000C"));

        // when
        AnprResponseDTO response = service.getAnprFamily(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getListaSoggetti().getDatiSoggetto()).hasSize(2);
        verify(encryptRestConnector).upsertToken(any(CFDTO.class));
        verify(familyMockGeneratorService).retrieveFamily(ENCRYPTED_TOKEN);
    }

    @Test
    void testGetAnprFamily_whenFamilyDoesNotExist() {
        // given
        AnprRequestDTO request = AnprRequestDTO.builder()
                .criteriRicerca(CriteriRicerca.builder().codiceFiscale(PII).build())
                .build();

        EncryptedCfDTO encrypted = new EncryptedCfDTO(ENCRYPTED_TOKEN);

        when(encryptRestConnector.upsertToken(any(CFDTO.class))).thenReturn(encrypted);
        when(familyMockGeneratorService.retrieveFamily(ENCRYPTED_TOKEN)).thenReturn(null);

        // when
        AnprResponseDTO response = service.getAnprFamily(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getListaSoggetti().getDatiSoggetto()).isNotEmpty();
        Assertions.assertEquals(1L,response.getListaSoggetti().getDatiSoggetto().size());
        DatiSoggetto familyMember = response.getListaSoggetti().getDatiSoggetto().getFirst();
        Assertions.assertNotNull(familyMember.getGeneralita());
        String dataNascita = familyMember.getGeneralita().getDataNascita();
        Assertions.assertNotNull(dataNascita);
        Assertions.assertTrue(dataNascita.matches("\\d{4}-\\d{2}-\\d{2}"));
        verify(encryptRestConnector).upsertToken(any(CFDTO.class));
        verify(familyMockGeneratorService).retrieveFamily(ENCRYPTED_TOKEN);
    }

    @Test
    void testGenerateFamily_returnsValidResponse() {
        // given
        // when
        AnprResponseDTO response = service.generateFamily(PII, "ROMA", "00100", "RM", true);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getListaSoggetti().getDatiSoggetto()).isNotEmpty();
        DatiSoggetto soggetto = response.getListaSoggetti().getDatiSoggetto().getFirst();
        assertThat(soggetto.getGeneralita().getNome()).isNotBlank();
        assertThat(soggetto.getGeneralita().getCognome()).isNotBlank();
        assertThat(soggetto.getGeneralita().getSesso()).isIn("M", "F");
    }

    @Test
    void testGetAnprFamily_whenFamilyExistsWithChildren() {
        // given
        AnprRequestDTO request = AnprRequestDTO.builder()
                .criteriRicerca(CriteriRicerca.builder().codiceFiscale(PII).build())
                .build();

        EncryptedCfDTO encrypted = new EncryptedCfDTO(ENCRYPTED_TOKEN);
        when(encryptRestConnector.upsertToken(any(CFDTO.class))).thenReturn(encrypted);

        Family family = Family.builder().memberIds(Set.of("token-1", "token-2"))
                .minorMemberIds(Set.of("token-child-1")).build();
        when(familyMockGeneratorService.retrieveFamily(ENCRYPTED_TOKEN)).thenReturn(family);

        when(decryptRestConnector.getPiiByToken("token-1")).thenReturn(new DecryptCfDTO("BBBBBB01E07A000B"));
        when(decryptRestConnector.getPiiByToken("token-2")).thenReturn(new DecryptCfDTO("BBBBBB01E07A000C"));
        when(decryptRestConnector.getPiiByToken("token-child-1")).thenReturn(new DecryptCfDTO("BBBBBB01E07A000D"));

        // when
        AnprResponseDTO response = service.getAnprFamily(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getListaSoggetti().getDatiSoggetto()).hasSize(3);
        verify(encryptRestConnector).upsertToken(any(CFDTO.class));
        verify(familyMockGeneratorService).retrieveFamily(ENCRYPTED_TOKEN);
    }

    @Test
    void testRandomBirthDate_whenUserIsOver18() {
        LocalDate birthDate = LocalDate.parse(AnprMockFamilyGeneratorServiceImpl.randomBirthDate(true));

        assertThat(birthDate).isBeforeOrEqualTo(LocalDate.now().minusYears(18));
    }

    @Test
    void testRandomBirthDate_whenUserIsUnder18() {
        LocalDate birthDate = LocalDate.parse(AnprMockFamilyGeneratorServiceImpl.randomBirthDate(false));

        assertThat(birthDate).isAfterOrEqualTo(LocalDate.now().minusYears(18));
    }

    @Test
    void testGetAnprAnomalyFamily_returnsExpectedAnomalyResponse() {
        AnprResponseDTO response = service.getAnprAnomalyFamily();

        assertThat(response).isNotNull();
        assertThat(response.getIdOperazioneANPR()).isNotBlank();
        assertThat(response.getListaAnomalie()).hasSize(1);
        assertThat(response.getListaAnomalie().getFirst().getCodiceErroreAnomalia()).isEqualTo(FAMILY_ANOMALY_CODE);
        assertThat(response.getListaAnomalie().getFirst().getTestoErroreAnomalia()).isEqualTo("Anomaly test");
        assertThat(response.getListaAnomalie().getFirst().getTipoErroreAnomalia()).isEqualTo("A");
    }

    @Test
    void testGetAnprAnomalyError_returnsExpectedErrorResponse() {
        AnprKoResponseDTO response = service.getAnprAnomalyError();

        assertThat(response).isNotNull();
        assertThat(response.getIdOperazioneANPR()).isNotBlank();
        assertThat(response.getListaErrori()).hasSize(1);
        assertThat(response.getListaErrori().getFirst().getCodiceErroreAnomalia()).isEqualTo(FAMILY_ANOMALY_CODE);
        assertThat(response.getListaErrori().getFirst().getTestoErroreAnomalia()).isEqualTo("Anomaly test");
        assertThat(response.getListaErrori().getFirst().getTipoErroreAnomalia()).isEqualTo("E");
    }
}