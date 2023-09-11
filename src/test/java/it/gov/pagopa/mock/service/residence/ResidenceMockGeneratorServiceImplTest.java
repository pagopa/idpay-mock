package it.gov.pagopa.mock.service.residence;

import it.gov.pagopa.mock.dto.Residence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Value;

import static org.junit.jupiter.api.Assertions.*;

class ResidenceMockGeneratorServiceImplTest {

    private ResidenceMockGeneratorService residenceMockGeneratorService= new ResidenceMockGeneratorServiceImpl();


    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void generateResidence(boolean isCityMilano) {
        String userId;
        String expectedCity;

        if(isCityMilano) {
            userId = "userId";
            expectedCity="Milano";
        } else {
            userId = "userId1";
            expectedCity="Roma";
        }

        Residence residence = residenceMockGeneratorService.generateResidence(userId);

        Assertions.assertNotNull(residence);
        Assertions.assertEquals(expectedCity, residence.getCity());
    }
}