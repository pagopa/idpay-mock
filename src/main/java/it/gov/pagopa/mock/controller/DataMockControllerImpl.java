package it.gov.pagopa.mock.controller;

import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.dto.Residence;
import it.gov.pagopa.mock.dto.SaveIseeRequestDTO;
import it.gov.pagopa.mock.service.DataMockService;
import it.gov.pagopa.mock.service.isee.IseeMockService;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class DataMockControllerImpl implements DataMockController {
    private final DataMockService dataMockService;
    private final IseeMockService iseeMockService;

    public DataMockControllerImpl(DataMockService dataMockService, IseeMockService iseeMockService) {
        this.dataMockService = dataMockService;
        this.iseeMockService = iseeMockService;
    }


    @Override
    public Family getFamilyForUser(String userId) {
        return dataMockService.getFamilyForUser(userId);
    }

    @Override
    public Family upsertFamilyUnit(String familyId, Set<String> userIds){
        return dataMockService.upsertFamilyUnit(familyId, userIds);
    }


    @Override
    public Residence getResidenceForUser(String userId) {
        return dataMockService.getResidenceForUser(userId);
    }

    @Override
    public void saveIsee(String fiscalCode, SaveIseeRequestDTO iseeRequestDTO) {
        iseeMockService.saveIsee(fiscalCode, iseeRequestDTO.getIseeTypeMap());
    }
}
