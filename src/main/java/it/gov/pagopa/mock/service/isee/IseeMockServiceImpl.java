package it.gov.pagopa.mock.service.isee;

import it.gov.pagopa.admissibility.generated.soap.ws.client.TipoIndicatoreSinteticoEnum;
import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.mock.connector.encrypt.EncryptRestConnector;
import it.gov.pagopa.mock.dto.CFDTO;
import it.gov.pagopa.mock.dto.EncryptedCfDTO;
import it.gov.pagopa.mock.enums.IseeTypologyEnum;
import it.gov.pagopa.mock.model.MockedIsee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
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

        if (isee != null) {
            return isee.getIseeTypeMap().get(iseeType);
        } else {
            return null;
        }
    }

    @Override
    public MockedIsee saveIsee(String fiscalCode, Map<IseeTypologyEnum, BigDecimal> iseeTypes) {
        log.debug("[SAVE_ISEE] Save isee");
        String userId = encryptCF(fiscalCode);
        log.info("[SAVE_ISEE] Save isee for userId {}", userId);

        return mongoTemplate.save(new MockedIsee(userId, transcodeIseeTypes(iseeTypes)));
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
    private Map<TipoIndicatoreSinteticoEnum, BigDecimal> transcodeIseeTypes(Map<IseeTypologyEnum, BigDecimal> iseeTypes) {
        return iseeTypes.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> iseeTypesMap.get(e.getKey()),
                        Map.Entry::getValue
                ));
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
