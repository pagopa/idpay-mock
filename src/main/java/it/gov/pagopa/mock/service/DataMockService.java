package it.gov.pagopa.mock.service;

import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.Residence;

import java.util.Set;


public interface DataMockService {
    Family getFamilyForUser(String userId);
    Residence getResidenceForUser(String userId);
    Family upsertFamilyUnit(String familyId, Set<String> userIds);
}
