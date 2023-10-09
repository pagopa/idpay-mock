package it.gov.pagopa.mock.dto;

import it.gov.pagopa.mock.enums.IseeTypologyEnum;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaveIseeRequestDTO {
    private Map<IseeTypologyEnum, BigDecimal> iseeTypeMap;
}
