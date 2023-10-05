package it.gov.pagopa.mock.dto;

import it.gov.pagopa.admissibility.generated.soap.ws.client.TipoIndicatoreSinteticoEnum;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaveIseeRequestDTO {
    private Map<TipoIndicatoreSinteticoEnum, BigDecimal> iseeTypeMap;
}
