package it.gov.pagopa.mock.service.isee;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.mock.connector.encrypt.EncryptRestConnector;
import it.gov.pagopa.mock.dto.CFDTO;
import it.gov.pagopa.mock.dto.EncryptedCfDTO;
import it.gov.pagopa.mock.enums.IseeTypologyEnum;
import it.gov.pagopa.mock.model.MockedIsee;
import it.gov.pagopa.mock.wsimport.inps.TipoIndicatoreSinteticoEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IseeMockServiceImpl implements IseeMockService {

    private final MongoTemplate mongoTemplate;
    private final EncryptRestConnector encryptRestConnector;

    public IseeMockServiceImpl(MongoTemplate mongoTemplate,
                               EncryptRestConnector encryptRestConnector) {
        this.mongoTemplate = mongoTemplate;
        this.encryptRestConnector = encryptRestConnector;
    }

    @Override
    public BigDecimal retrieveIsee(String fiscalCode, TipoIndicatoreSinteticoEnum iseeType) {
        log.debug("[RETRIEVE_ISEE] Retrieve isee");
        String userId = encryptCF(fiscalCode);
        log.info("[RETRIEVE_ISEE] Retrieve isee for userId {}", userId);

        MockedIsee isee = searchMockCollection(userId);

        Map<IseeTypologyEnum, BigDecimal> iseeTypes;
        if (isee != null) {
            iseeTypes = isee.getIseeTypeMap();
        } else {
            iseeTypes = generateMockIsee(userId);
        }
        return iseeTypes.get(iseeTypesMapReverse.get(iseeType));
    }

    private Map<IseeTypologyEnum, BigDecimal> generateMockIsee(String userId) {
        log.info("[RERIEVE_ISEE] ISEE of user {} not found in collection mocked_isee, genereting a fake one",
                userId);

        Map<IseeTypologyEnum, BigDecimal> iseeMockMap = new EnumMap<>(IseeTypologyEnum.class);
        List<IseeTypologyEnum> iseeList = Arrays.asList(IseeTypologyEnum.values());

        int randomTypology = new Random(userId.hashCode()).nextInt(1, iseeList.size()+1);
        for (int i = 0; i < randomTypology; i++) {
            Random value = new Random((userId + iseeList.get(i)).hashCode());
            iseeMockMap.put(iseeList.get(i), new BigDecimal(value.nextInt(1_000, 100_000)));
        }

        return iseeMockMap;
    }

    @Override
    public MockedIsee saveIsee(String fiscalCode, Map<IseeTypologyEnum, BigDecimal> iseeTypes) {
        log.debug("[SAVE_ISEE] Save isee");
        String userId = encryptCF(fiscalCode);
        log.info("[SAVE_ISEE] Save isee for userId {}", userId);

        validateIseeTypes(iseeTypes);
        return mongoTemplate.save(new MockedIsee(userId, iseeTypes));
    }

    private static final Map<IseeTypologyEnum, TipoIndicatoreSinteticoEnum> iseeTypesMap = Map.of(
            IseeTypologyEnum.ORDINARIO, TipoIndicatoreSinteticoEnum.ORDINARIO,
            IseeTypologyEnum.MINORENNE, TipoIndicatoreSinteticoEnum.MINORENNE,
            IseeTypologyEnum.UNIVERSITARIO, TipoIndicatoreSinteticoEnum.UNIVERSITARIO,
            IseeTypologyEnum.SOCIOSANITARIO, TipoIndicatoreSinteticoEnum.SOCIO_SANITARIO,
            IseeTypologyEnum.DOTTORATO, TipoIndicatoreSinteticoEnum.DOTTORATO,
            IseeTypologyEnum.RESIDENZIALE, TipoIndicatoreSinteticoEnum.RESIDENZIALE
            // IseeTypologyEnum.CORRENTE not mapped
    );
    private static final Map<TipoIndicatoreSinteticoEnum, IseeTypologyEnum> iseeTypesMapReverse = iseeTypesMap.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    private void validateIseeTypes(Map<IseeTypologyEnum, BigDecimal> iseeTypes) {
        iseeTypes.keySet().forEach(i -> {
            if(iseeTypesMap.get(i)==null){
                throw new IllegalArgumentException("ISEE type not handled: " + i);
            }
        });
    }

    private MockedIsee searchMockCollection(String userId) {
        return mongoTemplate.findById(userId, MockedIsee.class);
    }

    private String encryptCF(String fiscalCode) {
        String userId;
        try {
            EncryptedCfDTO encryptedCfDTO = encryptRestConnector.upsertToken(new CFDTO(fiscalCode));
            userId = encryptedCfDTO.getToken();
        } catch (Exception e) {
            throw new ClientExceptionWithBody(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "INTERNAL SERVER ERROR",
                    "Error during encryption", e);
        }
        return userId;
    }

}
