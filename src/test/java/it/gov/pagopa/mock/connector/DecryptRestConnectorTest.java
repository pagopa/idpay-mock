package it.gov.pagopa.mock.connector;

import it.gov.pagopa.mock.connector.decrypt.DecryptRest;
import it.gov.pagopa.mock.connector.decrypt.DecryptRestConnectorImpl;
import it.gov.pagopa.mock.dto.DecryptCfDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {DecryptRestConnectorImpl.class, String.class})
@ExtendWith(SpringExtension.class)
class DecryptRestConnectorTest {
    @MockBean
    private DecryptRest decryptRest;

    @Autowired
    private DecryptRestConnectorImpl decryptRestConnector;

    @Test
    void testUpsertToken() {
        DecryptCfDTO decryptCfDTO = new DecryptCfDTO("ABC123");
        when(decryptRest.getPiiByToken(anyString(), anyString())).thenReturn(decryptCfDTO);
        assertSame(decryptCfDTO, decryptRestConnector.getPiiByToken("token"));
        verify(decryptRest).getPiiByToken(any(), any());
    }
}

