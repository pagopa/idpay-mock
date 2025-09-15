package it.gov.pagopa.mock.connector.decrypt;

import it.gov.pagopa.mock.dto.DecryptCfDTO;
import org.springframework.stereotype.Service;

@Service
public interface DecryptRestConnector {

  DecryptCfDTO getPiiByToken(String token);
}
