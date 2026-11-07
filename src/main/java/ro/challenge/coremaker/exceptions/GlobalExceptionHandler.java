package ro.challenge.coremaker.exceptions;

import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ro.challenge.coremaker.dto.ErrorResponse;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage());
    }

    @ExceptionHandler(LedgerRuleViolation.class)
    public ResponseEntity<ErrorResponse> handleRuleViolation(LedgerRuleViolation ex) {
        return error(HttpStatus.CONFLICT, "LEDGER_RULE_VIOLATION", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        FieldError field = ex.getBindingResult().getFieldError();
        String pointer = field == null ? "/ " : "/ " + field.getField();
        String detail = field == null ? "Invalid request" : field.getDefaultMessage();
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", pointer + detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Malformed JSON body");
    }

    private static ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message) {
        ErrorResponse body = new ErrorResponse()
            .code(code)
            .message(message)
            .timestamp(OffsetDateTime.now(ZoneOffset.UTC));
        return ResponseEntity.status(status).body(body);
    }
}