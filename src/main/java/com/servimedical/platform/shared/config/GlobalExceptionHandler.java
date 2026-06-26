package com.servimedical.platform.shared.config;

import com.servimedical.platform.aph.domain.exception.AphNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    List<String> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getDefaultMessage())
            .collect(Collectors.toList());
    return ResponseEntity.badRequest().body(new ErrorResponse("Faltan datos por llenar", errors));
  }

  @ExceptionHandler(AphNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(AphNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage(), List.of()));
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
    String msg = ex.getMessage();
    if (msg != null && msg.startsWith("Faltan datos por llenar")) {
      return ResponseEntity.badRequest().body(new ErrorResponse(msg, List.of()));
    }
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("Error interno: " + msg, List.of()));
  }

  public record ErrorResponse(String message, List<String> fields) {}
}
