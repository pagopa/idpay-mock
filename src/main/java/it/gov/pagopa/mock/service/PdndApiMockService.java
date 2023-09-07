package it.gov.pagopa.mock.service;

import it.gov.pagopa.mock.model.Family;
import it.gov.pagopa.mock.dto.Residence;


public interface PdndApiMockService {
    Family getFamilyForUser(String userId);
    Residence getResidenceForUser(String userId);
}
