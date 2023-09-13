package it.gov.pagopa.mock.connector;

import it.gov.pagopa.mock.connector.encrypt.EncryptRest;
import it.gov.pagopa.mock.connector.encrypt.EncryptRestConnectorImpl;
import it.gov.pagopa.mock.dto.CFDTO;
import it.gov.pagopa.mock.dto.EncryptedCfDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {EncryptRestConnectorImpl.class, String.class})
@ExtendWith(SpringExtension.class)
class EncryptRestConnectorTest {
    @MockBean
    private EncryptRest encryptRest;

    @Autowired
    private EncryptRestConnectorImpl encryptRestConnectorImpl;

    @Test
    void testUpsertToken() {
        EncryptedCfDTO encryptedCfDTO = new EncryptedCfDTO("ABC123");
        when(encryptRest.upsertToken(any(), any())).thenReturn(encryptedCfDTO);
        assertSame(encryptedCfDTO, encryptRestConnectorImpl.upsertToken(new CFDTO("Pii")));
        verify(encryptRest).upsertToken(any(), any());
    }
}

