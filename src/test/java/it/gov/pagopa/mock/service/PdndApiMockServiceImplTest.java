package it.gov.pagopa.mock.service;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.mock.dto.Family;
import it.gov.pagopa.mock.service.family.FamilyMockGeneratorService;
import it.gov.pagopa.mock.service.residence.ResidenceMockGeneratorService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = PdndApiMockServiceImpl.class)
public class PdndApiMockServiceImplTest {

    @MockBean
    ResidenceMockGeneratorService residenceMockGeneratorService;

    @MockBean
    FamilyMockGeneratorService familyMockGeneratorService;

    @Autowired
    PdndApiMockService pdndApiMockService;

    @Test
    void upsertFamilyUnit(){
        Set<String> userIds = new HashSet<>();
        userIds.add("CF1");

        Family family = Family.builder()
                .familyId("FamilyId")
                .memberIds(userIds)
                .build();

        when(familyMockGeneratorService.upsertFamilyUnit(any(), any())).thenReturn(family);

        Family result = pdndApiMockService.upsertFamilyUnit(null, userIds);

        Assertions.assertEquals(1, result.getMemberIds().size());
    }

    @Test
    void upsertFamilyUnit_emptyUserIds(){
        Set<String> userIds = new HashSet<>();
        userIds.add("");

        try{
            pdndApiMockService.upsertFamilyUnit(null, userIds);
        }catch (ClientExceptionWithBody e){
            Assertions.assertEquals(HttpStatus.BAD_REQUEST, e.getHttpStatus());
        }
    }

}
