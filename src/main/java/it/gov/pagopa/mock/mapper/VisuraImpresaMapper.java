package it.gov.pagopa.mock.mapper;

import it.gov.pagopa.mock.dto.visuraimpresa.VisuraImpresa;
import it.gov.pagopa.mock.model.MockedVisuraImpresa;

public interface VisuraImpresaMapper {
    VisuraImpresa mapMockedVisuraImpresa(MockedVisuraImpresa visura);
}