package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.Residence;
import it.gov.pagopa.mock.dto.SaveIseeRequestDTO;
import it.gov.pagopa.mock.model.MockedIsee;
import it.gov.pagopa.mock.service.PdndApiMockService;
import it.gov.pagopa.mock.service.isee.IseeMockService;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class PdndMockControllerImpl implements PdndMockController {
    private final PdndApiMockService pdndApiMockService;
    private final IseeMockService iseeMockService;

    public PdndMockControllerImpl(PdndApiMockService pdndApiMockService, IseeMockService iseeMockService) {
        this.pdndApiMockService = pdndApiMockService;
        this.iseeMockService = iseeMockService;
    }


    @Override
    public Family getFamilyForUser(String userId) {
        return pdndApiMockService.getFamilyForUser(userId);
    }

    @Override
    public Family upsertFamilyUnit(String familyId, Set<String> userIds){
        return pdndApiMockService.upsertFamilyUnit(familyId, userIds);
    }


    @Override
    public Residence getResidenceForUser(String userId) {
        return pdndApiMockService.getResidenceForUser(userId);
    }

    @Override
    public MockedIsee saveIsee(String fiscalCode, SaveIseeRequestDTO iseeRequestDTO) {
        return iseeMockService.saveIsee(fiscalCode, iseeRequestDTO.getIseeTypeMap());
    }
}
