package it.gov.pagopa.mock.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
public class MockedClassificazioneAteco {
    private String codiceAttivita;
    private String descrizioneAttivita;
    private String codiceImportanza;
}
