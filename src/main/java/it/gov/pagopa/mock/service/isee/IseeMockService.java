package it.gov.pagopa.mock.service.isee;

import it.gov.pagopa.mock.wsimport.inps.TipoIndicatoreEnum;
import it.gov.pagopa.mock.enums.IseeTypologyEnum;
import it.gov.pagopa.mock.model.MockedIsee;

import java.math.BigDecimal;
import java.util.Map;

public interface IseeMockService {
    BigDecimal retrieveIsee(String fiscalCode, TipoIndicatoreEnum  iseeType);
    MockedIsee saveIsee(String fiscalCode, Map<IseeTypologyEnum, BigDecimal> iseeTypes);
}
