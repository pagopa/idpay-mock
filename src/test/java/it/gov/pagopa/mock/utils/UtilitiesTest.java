package it.gov.pagopa.mock.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilitiesTest {

    @Test
    void testPageable(){
        Pageable pageable = PageRequest.of(1,5);
        Pageable result = Utilities.getPageable(pageable);

        assertEquals(result, pageable);
    }
    @Test
    void testPageable_null(){
        Pageable result = Utilities.getPageable(null);
        Utilities.performanceLog(System.currentTimeMillis(), "");

        assertEquals(result, PageRequest.of(0, 15, Sort.by("updateDate")));
    }

    @Test
    void testBirthDateFromFiscalCodeAndAge() {
        Map<String, LocalDate> mapFiscalCodesBirthDates = Map.of(
                "PTRGNL73S51X000Q", LocalDate.of(1973, 11, 11),
                "BLBGRC57A05X000D", LocalDate.of(1957, 1, 5),
                "DSUFLV00R19X000P", LocalDate.of(2000, 10, 19),
                "BCCVCR01E65X000T", LocalDate.of(2001, 5, 25)
        );

        for(String fc : mapFiscalCodesBirthDates.keySet()) {
            LocalDate result = Utilities.calculateBirthDateFromFiscalCode(fc);

            Assertions.assertEquals(mapFiscalCodesBirthDates.get(fc), result);
        }

        int age = Utilities.getAge(LocalDate.now().minusYears(20).plusDays(1));
        Assertions.assertEquals(19, age);
    }
}
