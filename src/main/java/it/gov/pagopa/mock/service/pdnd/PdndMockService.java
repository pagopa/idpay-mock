package it.gov.pagopa.mock.service.pdnd;

import it.gov.pagopa.mock.dto.visuraimpresa.VisuraImpresa;
import it.gov.pagopa.mock.openapi.pdnd.dto.ClientCredentialsResponseDTO;

public interface PdndMockService {
    ClientCredentialsResponseDTO createToken(String clientAssertion, String clientAssertionType, String grantType, String clientId);

    byte[] getRawInstitutionDetail(String taxCode);

    void saveVisuraImpresa(VisuraImpresa visuraImpresa);
}
