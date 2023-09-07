package it.gov.pagopa.mock.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document("mocked_families")
public class MockedFamily {
    @MongoId
    private String id;
    private String familyId;
    private Set<String> memberIds;
}
