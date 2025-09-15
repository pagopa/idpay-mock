package it.gov.pagopa.mock.service.anpr_family;

import it.gov.pagopa.mock.connector.decrypt.DecryptRestConnector;
import it.gov.pagopa.mock.connector.encrypt.EncryptRestConnector;
import it.gov.pagopa.mock.dto.CFDTO;
import it.gov.pagopa.mock.dto.DecryptCfDTO;
import it.gov.pagopa.mock.dto.EncryptedCfDTO;
import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.AnprResponseDTO;
import it.gov.pagopa.mock.dto.anpr.CriteriRicerca;
import it.gov.pagopa.mock.dto.anpr.DatiSoggetto;
import it.gov.pagopa.mock.service.family.FamilyMockGeneratorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnprMockFamilyGeneratorServiceImplTest {

    public static final String ENCRYPTED_TOKEN = "encrypted-token";
    public static final String PII = "BBBBBB01E07A000A";
    @Mock
    private FamilyMockGeneratorService familyMockGeneratorService;
    @Mock
    private EncryptRestConnector encryptRestConnector;
    @Mock
    private DecryptRestConnector decryptRestConnector;

    @InjectMocks
    private AnprMockFamilyGeneratorServiceImpl service;

    @Test
    void testGetAnprFamily_whenFamilyExists() {
        // given
        String cf = PII;
        AnprRequestDTO request = AnprRequestDTO.builder()
                .criteriRicerca(CriteriRicerca.builder().codiceFiscale(cf).build())
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
        String cf = PII;
        AnprRequestDTO request = AnprRequestDTO.builder()
                .criteriRicerca(CriteriRicerca.builder().codiceFiscale(cf).build())
                .build();

        EncryptedCfDTO encrypted = new EncryptedCfDTO(ENCRYPTED_TOKEN);

        when(encryptRestConnector.upsertToken(any(CFDTO.class))).thenReturn(encrypted);
        when(familyMockGeneratorService.retrieveFamily(ENCRYPTED_TOKEN)).thenReturn(null);

        // when
        AnprResponseDTO response = service.getAnprFamily(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getListaSoggetti().getDatiSoggetto()).isNotEmpty();
        assertEquals(1L,response.getListaSoggetti().getDatiSoggetto().size());
        verify(encryptRestConnector).upsertToken(any(CFDTO.class));
        verify(familyMockGeneratorService).retrieveFamily(ENCRYPTED_TOKEN);
    }

    @Test
    void testGenerateFamily_returnsValidResponse() {
        // given
        String cf = PII;

        // when
        AnprResponseDTO response = service.generateFamily(cf, "ROMA", "00100", "RM");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getListaSoggetti().getDatiSoggetto()).isNotEmpty();
        DatiSoggetto soggetto = response.getListaSoggetti().getDatiSoggetto().get(0);
        assertThat(soggetto.getGeneralita().getNome()).isNotBlank();
        assertThat(soggetto.getGeneralita().getCognome()).isNotBlank();
        assertThat(soggetto.getGeneralita().getSesso()).isIn("M", "F");
    }
}