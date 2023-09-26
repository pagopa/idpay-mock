package it.gov.pagopa.mock.service.anpr_residence;

import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.AnprResponseDTO;

public interface AnprMockGeneratorService {
    AnprResponseDTO getAnprResidence(AnprRequestDTO anprRequestDTO);
}
