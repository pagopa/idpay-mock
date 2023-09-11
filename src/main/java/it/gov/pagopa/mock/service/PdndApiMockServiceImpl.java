package it.gov.pagopa.mock.service;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.Residence;
import it.gov.pagopa.mock.service.family.FamilyMockGeneratorService;
import it.gov.pagopa.mock.service.residence.ResidenceMockGeneratorService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Set;

import org.springframework.stereotype.Service;

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
        userIds.forEach(userId -> {
            if (userId.equals("")){
                throw new ClientExceptionWithBody(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.value(), "The userIds cannot be empty strings");
            }
        });
        return familyMockGeneratorService.upsertFamilyUnit(familyId, userIds);
    }
}
