package com.aro.aro_gestao.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Executa as consultas dos relatorios de verdade no banco de desenvolvimento (somente leitura).
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@WithMockUser
class RelatorioIntegracaoTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void relatorioFinanceiroResponde() throws Exception {
        mvc.perform(get("/api/relatorios/financeiro").param("inicio", "2026-01-01").param("fim", "2026-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inicio").value("2026-01-01"))
                .andExpect(jsonPath("$.totalRecebido").exists())
                .andExpect(jsonPath("$.aReceberNoPeriodo").exists())
                .andExpect(jsonPath("$.porCategoria").isArray());
    }

    @Test
    void relatorioFinanceiroValidaDatas() throws Exception {
        mvc.perform(get("/api/relatorios/financeiro").param("inicio", "2026-12-31").param("fim", "2026-01-01"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/relatorios/financeiro").param("inicio", "31/12/2026").param("fim", "2026-01-01"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void relatorioDeProcessosResponde() throws Exception {
        mvc.perform(get("/api/relatorios/processos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porStatus.length()").value(7))
                .andExpect(jsonPath("$.processosEmAndamento").isArray());
    }

    @Test
    void relatorioDeDocumentosResponde() throws Exception {
        mvc.perform(get("/api/relatorios/documentos").param("dias", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dias").value(60))
                .andExpect(jsonPath("$.documentos").isArray());
    }

    @Test
    void carteiraDeClientesResponde() throws Exception {
        mvc.perform(get("/api/relatorios/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClientes").isNumber())
                .andExpect(jsonPath("$.clientes").isArray());
    }

    @Test
    void relatoriosExigemLogin() throws Exception {
        // Sem o usuario simulado: requisicao anonima
        mvc.perform(get("/api/relatorios/clientes").with(anonymous()))
                .andExpect(status().isUnauthorized());
    }
}
