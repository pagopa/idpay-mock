package it.gov.pagopa.mock.service.anpr_family;

import it.gov.pagopa.mock.dto.anpr.AnprKoResponseDTO;
import it.gov.pagopa.mock.dto.anpr.AnprRequestDTO;
import it.gov.pagopa.mock.dto.anpr.AnprResponseDTO;

public interface AnprMockFamilyGeneratorService {
    AnprResponseDTO getAnprFamily(AnprRequestDTO anprRequestDTO);
    AnprResponseDTO getAnprAnomalyFamily();
    AnprKoResponseDTO getAnprAnomalyError();
}
