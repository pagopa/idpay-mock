package it.gov.pagopa.mock.service.family;

import it.gov.pagopa.mock.dto.Family;

import java.util.Set;


public interface FamilyMockGeneratorService {
    Family retrieveFamily(String userId);

    Family upsertFamilyUnit(String familyId, Set<String> userIds);
}
