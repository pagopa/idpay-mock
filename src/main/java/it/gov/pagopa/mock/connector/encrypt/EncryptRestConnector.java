package it.gov.pagopa.mock.connector.encrypt;

import it.gov.pagopa.mock.dto.CFDTO;
import it.gov.pagopa.mock.dto.EncryptedCfDTO;
import org.springframework.web.bind.annotation.RequestBody;

public interface EncryptRestConnector {

    EncryptedCfDTO upsertToken(@RequestBody CFDTO body);

}
