package it.gov.pagopa.mock.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
@Document("mocked_visura_impresa")
public class MockedVisuraImpresa {

    @Id
    private String taxCode;
    private List<MockedClassificazioneAteco> classificazioniAteco;
}
