package br.com.TramityRH.TramityRH.exception;

import br.com.TramityRH.TramityRH.dto.ErroResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class TratadorDeErros {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErroResponse> tratarStatus(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body(new ErroResponse(e.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException e) {
        FieldError campo = e.getBindingResult().getFieldErrors().get(0);
        String mensagem = campo.getField() + ": " + campo.getDefaultMessage();
        return ResponseEntity.badRequest().body(new ErroResponse(mensagem));
    }
}
