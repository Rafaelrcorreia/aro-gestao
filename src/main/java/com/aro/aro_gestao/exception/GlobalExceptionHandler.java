package com.aro.aro_gestao.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> tratarResourceNotFound(ResourceNotFoundException ex) {
        Map<String, Object> corpo = criarCorpoBasico(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(corpo);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> tratarBusinessException(BusinessException ex) {
        Map<String, Object> corpo = criarCorpoBasico(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> tratarValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (FieldError erro : ex.getBindingResult().getFieldErrors()) {
            erros.put(erro.getField(), erro.getDefaultMessage());
        }

        Map<String, Object> corpo = criarCorpoBasico(HttpStatus.BAD_REQUEST, "Dados invalidos");
        corpo.put("erros", erros);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> tratarParametroInvalido(MethodArgumentTypeMismatchException ex) {
        Map<String, Object> corpo = criarCorpoBasico(HttpStatus.BAD_REQUEST,
                "Valor invalido para o parametro '" + ex.getName() + "'");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> tratarArquivoGrande(MaxUploadSizeExceededException ex) {
        Map<String, Object> corpo = criarCorpoBasico(HttpStatus.PAYLOAD_TOO_LARGE,
                "O arquivo e grande demais. O tamanho maximo e 20 MB.");
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(corpo);
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<Map<String, Object>> tratarParteAusente(MissingServletRequestPartException ex) {
        Map<String, Object> corpo = criarCorpoBasico(HttpStatus.BAD_REQUEST,
                "Parte obrigatoria ausente no envio: '" + ex.getRequestPartName() + "'");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> tratarCorpoInvalido(HttpMessageNotReadableException ex) {
        Map<String, Object> corpo = criarCorpoBasico(HttpStatus.BAD_REQUEST,
                "Os dados enviados estao em formato invalido");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> tratarErroGenerico(Exception ex) {
        // Registra a causa real no console, porque a resposta ao navegador e generica
        log.error("Erro inesperado ao processar a requisicao", ex);

        Map<String, Object> corpo = criarCorpoBasico(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno no servidor"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(corpo);
    }

    private Map<String, Object> criarCorpoBasico(HttpStatus status, String mensagem) {
        Map<String, Object> corpo = new HashMap<>();
        corpo.put("timestamp", LocalDateTime.now());
        corpo.put("status", status.value());
        corpo.put("erro", status.getReasonPhrase());
        corpo.put("mensagem", mensagem);
        return corpo;
    }
}