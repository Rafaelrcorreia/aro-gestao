package com.aro.aro_gestao.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Regras de acesso:
 * - login.html, CSS, JS e as rotas de status/primeiro acesso sao publicas;
 * - /api/usuarios so para administradores;
 * - todo o resto exige estar logado.
 * Sem login, a API responde 401 (JSON) e as paginas redirecionam para /login.html.
 * A sessao fica num cookie; o CSRF usa o cookie XSRF-TOKEN + cabecalho X-XSRF-TOKEN.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Permite encerrar as sessoes de um usuario desativado ou com senha redefinida
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    // Avisa o SessionRegistry quando uma sessao termina
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public SecurityFilterChain filtroDeSeguranca(HttpSecurity http, SessionRegistry sessionRegistry) throws Exception {
        RequestMatcher api = PathPatternRequestMatcher.withDefaults().matcher("/api/**");
        AuthenticationEntryPoint naoAutorizado = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);
        // Leva junto a pagina pedida (?voltar=...) para voltar a ela depois de entrar
        AuthenticationEntryPoint paginaDeLogin = (requisicao, resposta, erro) ->
                resposta.sendRedirect(enderecoDoLogin(requisicao));

        http
                .authorizeHttpRequests(regras -> regras
                        .requestMatchers("/login.html", "/css/**", "/js/**", "/favicon.ico", "/error").permitAll()
                        .requestMatchers("/api/auth/status", "/api/auth/primeiro-acesso").permitAll()
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        .anyRequest().authenticated())

                .formLogin(login -> login
                        .loginPage("/login.html")
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("login")
                        .passwordParameter("senha")
                        .successHandler((requisicao, resposta, autenticacao) ->
                                resposta.setStatus(HttpStatus.NO_CONTENT.value()))
                        .failureHandler((requisicao, resposta, erro) -> RespostaJsonSeguranca.escrever(resposta,
                                401, "Unauthorized", erro instanceof DisabledException
                                        ? "Este usuario esta desativado. Fale com o administrador."
                                        : "Login ou senha invalidos."))
                        .permitAll())

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)))

                .csrf(csrf -> csrf.spa())

                .sessionManagement(sessao -> sessao
                        .maximumSessions(-1)
                        .sessionRegistry(sessionRegistry)
                        .expiredSessionStrategy(evento -> {
                            HttpServletRequest requisicao = evento.getRequest();
                            if (api.matches(requisicao)) {
                                RespostaJsonSeguranca.escrever(evento.getResponse(), 401, "Unauthorized",
                                        "Sua sessao foi encerrada. Entre novamente.");
                            } else {
                                evento.getResponse().sendRedirect(enderecoDoLogin(requisicao));
                            }
                        }))

                .exceptionHandling(erros -> erros
                        // Sem login: a API responde 401; paginas redirecionam para o login
                        .authenticationEntryPoint((requisicao, resposta, erro) -> {
                            if (api.matches(requisicao)) {
                                naoAutorizado.commence(requisicao, resposta, erro);
                            } else {
                                paginaDeLogin.commence(requisicao, resposta, erro);
                            }
                        })
                        .accessDeniedHandler((requisicao, resposta, erro) -> RespostaJsonSeguranca.escrever(resposta,
                                403, "Forbidden", erro instanceof CsrfException
                                        ? "Sua sessao expirou ou a pagina esta desatualizada. Recarregue a pagina."
                                        : "Voce nao tem permissao para esta acao.")));

        return http.build();
    }

    // "/login.html?voltar=/pages/financeiro.html%3Fmes%3D2026-11" (so para GET; a raiz volta para o inicio)
    private static String enderecoDoLogin(HttpServletRequest requisicao) {
        String login = requisicao.getContextPath() + "/login.html";
        String caminho = requisicao.getRequestURI().substring(requisicao.getContextPath().length());
        if (!"GET".equals(requisicao.getMethod()) || caminho.isEmpty() || "/".equals(caminho)) {
            return login;
        }
        if (requisicao.getQueryString() != null) {
            caminho += "?" + requisicao.getQueryString();
        }
        return login + "?voltar=" + URLEncoder.encode(caminho, StandardCharsets.UTF_8);
    }
}
