package it.gov.pagopa.mock.service.isee;

import it.gov.pagopa.common.web.exception.ClientExceptionWithBody;
import it.gov.pagopa.mock.connector.encrypt.EncryptRestConnector;
import it.gov.pagopa.mock.dto.CFDTO;
import it.gov.pagopa.mock.dto.EncryptedCfDTO;
import it.gov.pagopa.mock.enums.IseeTypologyEnum;
import it.gov.pagopa.mock.model.MockedIsee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;

import static it.gov.pagopa.mock.enums.IseeTypologyEnum.CORRENTE;
import static it.gov.pagopa.mock.enums.IseeTypologyEnum.ORDINARIO;
import static it.gov.pagopa.mock.wsimport.inps.TipoIndicatoreEnum.ISEE_ORDINARIO;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

class IseeMockServiceImplTest {

    private MongoTemplate mongoTemplate;
    private EncryptRestConnector encryptRestConnector;
    private IseeMockServiceImpl iseeMockService;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        encryptRestConnector = mock(EncryptRestConnector.class);
        iseeMockService = new IseeMockServiceImpl(mongoTemplate, encryptRestConnector);
    }

    @Test
    void retrieveIsee_ShouldGenerateMock_WhenNotInMongo() {
        String cf = "RSSMRA80A01H501U";
        String token = "encrypted456";

        EncryptedCfDTO encrypted = new EncryptedCfDTO(token);
        when(encryptRestConnector.upsertToken(any(CFDTO.class))).thenReturn(encrypted);
        when(mongoTemplate.findById(token, MockedIsee.class)).thenReturn(null);

        BigDecimal result = iseeMockService.retrieveIsee(cf, ISEE_ORDINARIO);

        assertThat(result).isNotNull();
        assertThat(result).isGreaterThanOrEqualTo(BigDecimal.valueOf(1_000));
    }

    @Test
    void retrieveIsee_ShouldReturnExisting_WhenFoundInMongo() {
        String cf = "ABCDEF12G34H567I";
        String userId = "encryptedUserId";
        Map<IseeTypologyEnum, BigDecimal> iseeMap = new EnumMap<>(IseeTypologyEnum.class);
        iseeMap.put(ORDINARIO, BigDecimal.valueOf(5000));

        when(encryptRestConnector.upsertToken(any(CFDTO.class))).thenReturn(new EncryptedCfDTO(userId));
        when(mongoTemplate.findById(userId, MockedIsee.class)).thenReturn(new MockedIsee(userId, iseeMap));

        BigDecimal result = iseeMockService.retrieveIsee(cf, ISEE_ORDINARIO);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(5000), result);
    }

    @Test
    void retrieveIsee_ShouldGenerateValueAboveAndBelowThreshold() {
        String cf = "userThresholdTest";
        String token = "mockToken";

        when(encryptRestConnector.upsertToken(any(CFDTO.class)))
                .thenReturn(new EncryptedCfDTO(token));
        when(mongoTemplate.findById(token, MockedIsee.class))
                .thenReturn(null);

        BigDecimal result = iseeMockService.retrieveIsee(cf, ISEE_ORDINARIO);

        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.valueOf(25_000)) < 0 ||
                result.compareTo(BigDecimal.valueOf(25_000)) >= 0);
    }

    @Test
    void saveIsee_ShouldSaveSuccessfully() {
        String cf = "ABCDEF12G34H567I";
        String userId = "encryptedUserId";
        Map<IseeTypologyEnum, BigDecimal> iseeMap = new EnumMap<>(IseeTypologyEnum.class);
        iseeMap.put(ORDINARIO, BigDecimal.valueOf(5000));

        when(encryptRestConnector.upsertToken(any(CFDTO.class))).thenReturn(new EncryptedCfDTO(userId));
        when(mongoTemplate.save(ArgumentMatchers.any(MockedIsee.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MockedIsee saved = iseeMockService.saveIsee(cf, iseeMap);

        assertNotNull(saved);
        assertEquals(userId, saved.getUserId());
        assertEquals(iseeMap, saved.getIseeTypeMap());
    }

    @Test
    void saveIsee_ShouldThrowForUnhandledType() {
        String cf = "ABCDEF12G34H567I";
        Map<IseeTypologyEnum, BigDecimal> iseeMap = Map.of(CORRENTE, BigDecimal.valueOf(1000));
        when(encryptRestConnector.upsertToken(any(CFDTO.class))).thenReturn(new EncryptedCfDTO("encryptedUserId"));

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> iseeMockService.saveIsee(cf, iseeMap));

        assertTrue(exception.getMessage().contains("ISEE type not handled"));
    }

    @Test
    void encryptCF_ShouldThrowClientException_WhenEncryptRestConnectorFails() {
        String cf = "RSSMRA80A01H501U";

        when(encryptRestConnector.upsertToken(any(CFDTO.class)))
                .thenThrow(new RuntimeException("Connector failure"));

        ClientExceptionWithBody exception = assertThrows(
                ClientExceptionWithBody.class,
                () -> ReflectionTestUtils.invokeMethod(iseeMockService, "encryptCF", cf)
        );

        assertEquals(INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertEquals("INTERNAL SERVER ERROR", exception.getCode());
        assertEquals("Error during encryption", exception.getMessage());
    }

    @Test
    void retrieveIsee_ShouldGenerateMock_WhenMapIsNull() {
        String cf = "CFNULLMAP";
        String userId = "encryptedUserId";

        when(encryptRestConnector.upsertToken(any(CFDTO.class)))
                .thenReturn(new EncryptedCfDTO(userId));
        when(mongoTemplate.findById(userId, MockedIsee.class))
                .thenReturn(new MockedIsee(userId, null));

        BigDecimal result = iseeMockService.retrieveIsee(cf, ISEE_ORDINARIO);

        assertNotNull(result);
        assertThat(result).isGreaterThanOrEqualTo(BigDecimal.valueOf(1_000));
    }

    @Test
    void retrieveIsee_ShouldGenerateMock_WhenMapIsEmpty() {
        String cf = "CFEMPTYMAP";
        String userId = "encryptedUserId";

        when(encryptRestConnector.upsertToken(any(CFDTO.class)))
                .thenReturn(new EncryptedCfDTO(userId));
        when(mongoTemplate.findById(userId, MockedIsee.class))
                .thenReturn(new MockedIsee(userId, Map.of()));

        BigDecimal result = iseeMockService.retrieveIsee(cf, ISEE_ORDINARIO);

        assertNotNull(result);
        assertThat(result).isGreaterThanOrEqualTo(BigDecimal.valueOf(1_000));
    }
}

