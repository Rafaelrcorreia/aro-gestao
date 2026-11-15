package com.aro.aro_gestao.security;

import com.aro.aro_gestao.entity.PapelUsuario;
import com.aro.aro_gestao.entity.Usuario;
import com.aro.aro_gestao.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Sobe a aplicacao inteira (usa o banco de desenvolvimento). @Transactional desfaz tudo ao final.
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SegurancaIntegracaoTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void criarUsuarioDeTeste() {
        Usuario usuario = new Usuario();
        usuario.setNome("Teste Seguranca");
        usuario.setLogin("teste.seguranca.integracao");
        usuario.setSenhaHash(passwordEncoder.encode("senha-correta-123"));
        usuario.setPapel(PapelUsuario.USUARIO);
        usuario.setAtivo(true);
        usuario.setDataCadastro(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    @Test
    void apiSemLoginRespondeNaoAutorizado() throws Exception {
        mvc.perform(get("/api/clientes")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/documentos/1/arquivo")).andExpect(status().isUnauthorized());
    }

    @Test
    void paginaSemLoginRedirecionaParaOLogin() throws Exception {
        mvc.perform(get("/pages/clientes.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login.html?voltar=%2Fpages%2Fclientes.html"));
    }

    @Test
    void telaDeLoginEStatusSaoPublicos() throws Exception {
        mvc.perform(get("/login.html")).andExpect(status().isOk());
        mvc.perform(get("/api/auth/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configurado").value(true));
    }

    @Test
    void deveEntrarComSenhaCorretaERecusarSenhaErrada() throws Exception {
        mvc.perform(post("/api/auth/login").with(csrf())
                        .param("login", "Teste.Seguranca.Integracao").param("senha", "senha-correta-123"))
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/auth/login").with(csrf())
                        .param("login", "teste.seguranca.integracao").param("senha", "senha-errada"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Login ou senha invalidos."));
    }

    @Test
    void alteracaoSemTokenCsrfERecusada() throws Exception {
        mvc.perform(post("/api/clientes").with(user("teste.seguranca.integracao"))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void somenteAdministradorGerenciaUsuarios() throws Exception {
        mvc.perform(get("/api/usuarios").with(user("teste.seguranca.integracao").roles("USUARIO")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void primeiroAcessoNaoFuncionaQuandoJaExisteUsuario() throws Exception {
        mvc.perform(post("/api/auth/primeiro-acesso").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Invasor\",\"login\":\"invasor\",\"senha\":\"12345678\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usuarioLogadoConsultaOsProprioDados() throws Exception {
        mvc.perform(get("/api/auth/me").with(user("teste.seguranca.integracao")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Teste Seguranca"))
                .andExpect(jsonPath("$.senhaHash").doesNotExist());
    }

    private static final String CONFIGURACAO_VALIDA =
            "{\"nomeEmpresa\":\"ARO Teste\",\"diasAvisoDocumentos\":45,\"diasAlertaPrazo\":5}";

    @Test
    void usuarioComumLeMasNaoAlteraConfiguracoes() throws Exception {
        mvc.perform(get("/api/configuracoes").with(user("teste.seguranca.integracao").roles("USUARIO")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diasAvisoDocumentos").isNumber());
        mvc.perform(put("/api/configuracoes").with(user("teste.seguranca.integracao").roles("USUARIO")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(CONFIGURACAO_VALIDA))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorAlteraConfiguracoesComValidacao() throws Exception {
        mvc.perform(put("/api/configuracoes").with(user("admin").roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(CONFIGURACAO_VALIDA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diasAvisoDocumentos").value(45))
                .andExpect(jsonPath("$.atualizadoPor").value("admin"));
        mvc.perform(put("/api/configuracoes").with(user("admin").roles("ADMIN")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nomeEmpresa\":\"\",\"diasAvisoDocumentos\":0,\"diasAlertaPrazo\":99}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros.nomeEmpresa").exists())
                .andExpect(jsonPath("$.erros.diasAvisoDocumentos").exists())
                .andExpect(jsonPath("$.erros.diasAlertaPrazo").exists());
    }
}
