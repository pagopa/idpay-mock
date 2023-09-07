package it.gov.pagopa.mock.service;

import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.Residence;
import it.gov.pagopa.mock.service.family.FamilyMockGeneratorService;
import it.gov.pagopa.mock.service.residence.ResidenceMockGeneratorService;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PdndApiMockServiceImpl implements PdndApiMockService {
    private final FamilyMockGeneratorService familyMockGeneratorService;
    private final ResidenceMockGeneratorService residenceMockGeneratorService;

    public PdndApiMockServiceImpl(FamilyMockGeneratorService familyMockGeneratorService, ResidenceMockGeneratorService residenceMockGeneratorService) {
        this.familyMockGeneratorService = familyMockGeneratorService;
        this.residenceMockGeneratorService = residenceMockGeneratorService;
    }

    @Override
    public Family getFamilyForUser(String userId) {
        return familyMockGeneratorService.retrieveFamily(userId);
    }

    @Override
    public Residence getResidenceForUser(String userId) {
        return residenceMockGeneratorService.generateResidence(userId);
    }

    @Override
    public Family upsertFamilyUnit(String familyId, Set<String> userIds){
        return familyMockGeneratorService.upsertFamilyUnit(familyId, userIds);
    }
}
