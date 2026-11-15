package com.aro.aro_gestao.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

// Respostas de erro da seguranca no mesmo formato do GlobalExceptionHandler
final class RespostaJsonSeguranca {

    private RespostaJsonSeguranca() {
    }

    static void escrever(HttpServletResponse resposta, int status, String erro, String mensagem) throws IOException {
        resposta.setStatus(status);
        resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resposta.setContentType("application/json");
        // As mensagens sao fixas (definidas no codigo), entao nao precisam de escape
        resposta.getWriter().write("{\"status\":" + status
                + ",\"erro\":\"" + erro + "\""
                + ",\"mensagem\":\"" + mensagem + "\""
                + ",\"timestamp\":\"" + LocalDateTime.now() + "\"}");
    }
}
