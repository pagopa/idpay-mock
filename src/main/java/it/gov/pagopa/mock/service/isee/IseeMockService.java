package it.gov.pagopa.mock.service.isee;

import it.gov.pagopa.admissibility.generated.soap.ws.client.TipoIndicatoreSinteticoEnum;
import it.gov.pagopa.mock.model.MockedIsee;

import java.math.BigDecimal;
import java.util.Map;

public interface IseeMockService {
    BigDecimal retrieveIsee(String fiscalCode, TipoIndicatoreSinteticoEnum iseeType);
    MockedIsee saveIsee(String fiscalCode, Map<TipoIndicatoreSinteticoEnum, BigDecimal> iseeTypes);
}
