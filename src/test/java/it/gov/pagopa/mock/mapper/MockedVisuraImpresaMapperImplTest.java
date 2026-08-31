package it.gov.pagopa.mock.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.gov.pagopa.mock.dto.visuraimpresa.ClassificazioneAteco;
import it.gov.pagopa.mock.dto.visuraimpresa.InfoAttivita;
import it.gov.pagopa.mock.dto.visuraimpresa.VisuraImpresa;
import it.gov.pagopa.mock.model.MockedClassificazioneAteco;
import it.gov.pagopa.mock.model.MockedVisuraImpresa;

class MockedVisuraImpresaMapperImplTest {

    private final MockedVisuraImpresaMapperImpl mapper = new MockedVisuraImpresaMapperImpl();

    @Test
    void mapVisuraImpresa_mapsTaxCodeAndClassifications() {
        VisuraImpresa visura = new VisuraImpresa(
                "ABCDEF12G34H567I",
                new InfoAttivita(List.of(
                        new ClassificazioneAteco("47.11.10", "Commercio al dettaglio", "1"),
                        new ClassificazioneAteco("56.10.11", "Ristorazione", "2"))));

        MockedVisuraImpresa result = mapper.mapVisuraImpresa(visura);

        assertNotNull(result);
        assertEquals("ABCDEF12G34H567I", result.getTaxCode());
        List<MockedClassificazioneAteco> classifications = result.getClassificazioniAteco();
        assertEquals(2, classifications.size());
        assertEquals("47.11.10", classifications.get(0).getCodiceAttivita());
        assertEquals("Commercio al dettaglio", classifications.get(0).getDescrizioneAttivita());
        assertEquals("1", classifications.get(0).getCodiceImportanza());
        assertEquals("56.10.11", classifications.get(1).getCodiceAttivita());
        assertEquals("Ristorazione", classifications.get(1).getDescrizioneAttivita());
        assertEquals("2", classifications.get(1).getCodiceImportanza());
    }

    @Test
    void mapVisuraImpresa_nullInfoAttivita_returnsEmptyList() {
        MockedVisuraImpresa result = mapper.mapVisuraImpresa(new VisuraImpresa("ABCDEF12G34H567I", null));

        assertEquals(Collections.emptyList(), result.getClassificazioniAteco());
    }
}