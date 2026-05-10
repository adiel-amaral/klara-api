package com.klaraapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String TIMESTAMP = "timestamp";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldDetail> fields = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldDetail(error.getField(), message(error)))
                .toList();

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(422), "Invalid data");
        problem.setTitle("Validation error");
        problem.setProperty("fields", fields);
        problem.setProperty(TIMESTAMP, LocalDateTime.now());

        return ResponseEntity.status(HttpStatusCode.valueOf(422)).body(problem);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusiness(BusinessException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle("Business rule violation");
        problem.setProperty(TIMESTAMP, LocalDateTime.now());

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Resource not found");
        problem.setProperty(TIMESTAMP, LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    private String message(FieldError error) {
        return error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value";
    }

    private record FieldDetail(String field, String message) {}
}
