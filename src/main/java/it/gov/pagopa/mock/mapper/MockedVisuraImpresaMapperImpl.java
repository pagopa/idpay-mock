package it.gov.pagopa.mock.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import it.gov.pagopa.mock.dto.visuraimpresa.InfoAttivita;
import it.gov.pagopa.mock.dto.visuraimpresa.VisuraImpresa;
import it.gov.pagopa.mock.model.MockedClassificazioneAteco;
import it.gov.pagopa.mock.model.MockedVisuraImpresa;

@Component
public class MockedVisuraImpresaMapperImpl implements MockedVisuraImpresaMapper {

    @Override
    public MockedVisuraImpresa mapVisuraImpresa(VisuraImpresa visuraImpresa) {
        return MockedVisuraImpresa.builder()
                .taxCode(visuraImpresa.getCodiceFiscale())
                .classificazioniAteco(toMockedClassificazioniAteco(visuraImpresa.getInfoAttivita()))
                .build();
    }

    private List<MockedClassificazioneAteco> toMockedClassificazioniAteco(InfoAttivita infoAttivita) {
        if (infoAttivita == null || CollectionUtils.isEmpty(infoAttivita.getClassificazioniAteco())) {
            return Collections.emptyList();
        }
        return infoAttivita.getClassificazioniAteco().stream()
                .filter(java.util.Objects::nonNull)
                .map(c -> MockedClassificazioneAteco.builder()
                        .codiceAttivita(c.getCodiceAttivita())
                        .descrizioneAttivita(c.getAttivita())
                        .codiceImportanza(c.getCodiceImportanza())
                        .build())
                .toList();
    }
}