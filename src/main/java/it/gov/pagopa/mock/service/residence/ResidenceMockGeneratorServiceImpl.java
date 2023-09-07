package it.gov.pagopa.mock.service.residence;

import it.gov.pagopa.mock.dto.Residence;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ResidenceMockGeneratorServiceImpl implements ResidenceMockGeneratorService {

    public static final String MILANO = "Milano";
    @Override
    public Residence generateResidence(String userId) {
        return userIdBasedIntegerGenerator(userId).nextInt(0, 2) == 0
                ? Residence.builder()
                .city(MILANO)
                .cityCouncil(MILANO)
                .province(MILANO)
                .region("Lombardia")
                .postalCode("20124")
                .nation("Italia")
                .build()
                : Residence.builder()
                .city("Roma")
                .cityCouncil("Roma")
                .province("Roma")
                .region("Lazio")
                .postalCode("00187")
                .nation("Italia")
                .build();
    }

    private static Random userIdBasedIntegerGenerator(String userId) {
        @SuppressWarnings("squid:S2245")
        Random random = new Random(userId.hashCode());
        return random;
    }
}
