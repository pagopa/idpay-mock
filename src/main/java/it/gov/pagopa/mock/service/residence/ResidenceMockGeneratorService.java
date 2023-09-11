package it.gov.pagopa.mock.service.residence;

import it.gov.pagopa.mock.dto.Residence;

public interface ResidenceMockGeneratorService {
    Residence generateResidence(String userId);
}
