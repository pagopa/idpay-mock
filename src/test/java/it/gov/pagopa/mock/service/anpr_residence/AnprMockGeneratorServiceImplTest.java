package it.gov.pagopa.mock.service.anpr_residence;

import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.AnprResponseDTO;
import it.gov.pagopa.mock.mapper.AnprMapperFaker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AnprMockGeneratorServiceImplTest {

    private AnprMockGeneratorService anprMockGeneratorService = new AnprMockGeneratorServiceImpl();
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void getResidence(boolean isCityMilano) {
        String expectedCity;
        String codiceFiscale;

        if(isCityMilano) {
            codiceFiscale = "AAAAAA95E08P000B";
            expectedCity="MILANO";
        } else {
            codiceFiscale = "BBBBBB01E07A000A";
            expectedCity="ROMA";
        }

        AnprRequestDTO anprRequestDTO = AnprMapperFaker.mockInstanceBuilder(codiceFiscale);
        AnprResponseDTO anprResponseDTO = anprMockGeneratorService.getAnprResidence(anprRequestDTO);

        Assertions.assertNotNull(anprResponseDTO);
        Assertions.assertEquals(expectedCity,
                anprResponseDTO.getListaSoggetti().getDatiSoggetto().get(0).getResidenza().get(0).getIndirizzo().getComune().getNomeComune());
    }

    @Test
    void getResidenceFiscalCodeInvalid(){
        AnprRequestDTO anprRequestDTO = AnprMapperFaker.mockInstanceBuilder("QWERTY");
        AnprResponseDTO anprResponseDTO = anprMockGeneratorService.getAnprResidence(anprRequestDTO);

        Assertions.assertNull(anprResponseDTO);

    }

}
