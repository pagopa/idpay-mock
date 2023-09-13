package it.gov.pagopa.mock.exception;

import it.gov.pagopa.common.web.dto.ErrorDTO;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ControllerExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorDTO> handleQueryValidationExceptions(ConstraintViolationException ex) {
        List<String> errors = new ArrayList<>();

        ex.getConstraintViolations().forEach(error ->
                errors.add(error.getMessage())
        );
        String message = String.join(" - ", errors);
        return new ResponseEntity<>(
                new ErrorDTO("CONSTRAINT_VIOLATION", message),
                HttpStatus.BAD_REQUEST);
    }

}
