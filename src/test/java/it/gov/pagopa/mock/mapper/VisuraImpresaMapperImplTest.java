package it.gov.pagopa.mock.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.gov.pagopa.mock.dto.visuraimpresa.ClassificazioneAteco;
import it.gov.pagopa.mock.dto.visuraimpresa.VisuraImpresa;
import it.gov.pagopa.mock.model.MockedClassificazioneAteco;
import it.gov.pagopa.mock.model.MockedVisuraImpresa;

class VisuraImpresaMapperImplTest {

    private final VisuraImpresaMapperImpl mapper = new VisuraImpresaMapperImpl();

    @Test
    void mapMockedVisuraImpresa_mapsTaxCodeAndClassifications() {
        MockedVisuraImpresa visura = MockedVisuraImpresa.builder()
                .taxCode("ABCDEF12G34H567I")
                .classificazioniAteco(List.of(
                        MockedClassificazioneAteco.builder()
                                .codiceAttivita("47.11.10")
                                .descrizioneAttivita("Commercio al dettaglio")
                                .codiceImportanza("1")
                                .build(),
                        MockedClassificazioneAteco.builder()
                                .codiceAttivita("56.10.11")
                                .descrizioneAttivita("Ristorazione")
                                .codiceImportanza("2")
                                .build()))
                .build();

        VisuraImpresa result = mapper.mapMockedVisuraImpresa(visura);

        assertNotNull(result);
        assertEquals("ABCDEF12G34H567I", result.getCodiceFiscale());
        assertNotNull(result.getInfoAttivita());
        List<ClassificazioneAteco> classifications = result.getInfoAttivita().getClassificazioniAteco();
        assertEquals(2, classifications.size());
        assertEquals("47.11.10", classifications.get(0).getCodiceAttivita());
        assertEquals("Commercio al dettaglio", classifications.get(0).getAttivita());
        assertEquals("1", classifications.get(0).getCodiceImportanza());
        assertEquals("56.10.11", classifications.get(1).getCodiceAttivita());
        assertEquals("Ristorazione", classifications.get(1).getAttivita());
        assertEquals("2", classifications.get(1).getCodiceImportanza());
    }

    @Test
    void mapMockedVisuraImpresa_nullClassifications_returnsEmptyList() {
        MockedVisuraImpresa visura = MockedVisuraImpresa.builder()
                .taxCode("ABCDEF12G34H567I")
                .classificazioniAteco(null)
                .build();

        VisuraImpresa result = mapper.mapMockedVisuraImpresa(visura);

        assertNotNull(result.getInfoAttivita());
        assertEquals(Collections.emptyList(), result.getInfoAttivita().getClassificazioniAteco());
    }

    @Test
    void mapMockedVisuraImpresa_emptyClassifications_returnsEmptyList() {
        MockedVisuraImpresa visura = MockedVisuraImpresa.builder()
                .taxCode("ABCDEF12G34H567I")
                .classificazioniAteco(Collections.emptyList())
                .build();

        VisuraImpresa result = mapper.mapMockedVisuraImpresa(visura);

        assertEquals(Collections.emptyList(), result.getInfoAttivita().getClassificazioniAteco());
    }
}
