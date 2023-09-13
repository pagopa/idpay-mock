package it.gov.pagopa.mock.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Residence {
    private String postalCode;
    private String cityCouncil;
    private String province;
    private String city;
    private String region;
    private String nation;
}