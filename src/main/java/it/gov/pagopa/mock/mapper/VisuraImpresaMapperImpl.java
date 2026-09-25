package it.gov.pagopa.mock.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import it.gov.pagopa.mock.dto.visuraimpresa.ClassificazioneAteco;
import it.gov.pagopa.mock.dto.visuraimpresa.InfoAttivita;
import it.gov.pagopa.mock.dto.visuraimpresa.VisuraImpresa;
import it.gov.pagopa.mock.model.MockedVisuraImpresa;

@Component
public class VisuraImpresaMapperImpl implements VisuraImpresaMapper {

    @Override
    public VisuraImpresa mapMockedVisuraImpresa(MockedVisuraImpresa visura) {
        return new VisuraImpresa(
                visura.getTaxCode(),
                new InfoAttivita(getClassificazioniAteco(visura))
        );
    }

    private List<ClassificazioneAteco> getClassificazioniAteco(MockedVisuraImpresa visura) {
        if (visura == null || visura.getClassificazioniAteco() == null) {
            return List.of();
        }

        return visura.getClassificazioniAteco().stream()
                .map(c -> ClassificazioneAteco.builder()
                        .codiceAttivita(c.getCodiceAttivita())
                        .attivita(c.getDescrizioneAttivita())
                        .codiceImportanza(c.getCodiceImportanza())
                        .build())
                .toList();
    }
}
