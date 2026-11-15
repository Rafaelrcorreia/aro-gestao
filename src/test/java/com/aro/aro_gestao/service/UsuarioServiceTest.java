package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.AlterarSenhaRequestDTO;
import com.aro.aro_gestao.dto.PrimeiroAcessoRequestDTO;
import com.aro.aro_gestao.dto.UsuarioRequestDTO;
import com.aro.aro_gestao.dto.UsuarioResponseDTO;
import com.aro.aro_gestao.entity.PapelUsuario;
import com.aro.aro_gestao.entity.Usuario;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SessionRegistry sessionRegistry;

    // Encoder de verdade (com custo baixo para o teste ser rapido)
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    private UsuarioService usuarioService;

    @BeforeEach
    void configurar() {
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder, sessionRegistry);
    }

    private Usuario usuario(long id, String login, PapelUsuario papel, boolean ativo, String senha) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Pessoa " + id);
        usuario.setLogin(login);
        usuario.setPapel(papel);
        usuario.setAtivo(ativo);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        return usuario;
    }

    private UsuarioRequestDTO dados(String login, PapelUsuario papel, boolean ativo) {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNome("  Maria Souza ");
        dto.setLogin(login);
        dto.setPapel(papel);
        dto.setAtivo(ativo);
        return dto;
    }

    @Test
    void deveCriarPrimeiroAdministradorComSenhaCriptografadaELoginMinusculo() {
        when(usuarioRepository.count()).thenReturn(0L);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        PrimeiroAcessoRequestDTO dto = new PrimeiroAcessoRequestDTO();
        dto.setNome(" Rafael ");
        dto.setLogin(" Rafael.Admin ");
        dto.setSenha("senha-forte-123");

        UsuarioResponseDTO criado = usuarioService.criarPrimeiroAdministrador(dto);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("rafael.admin", criado.getLogin());
        assertEquals(PapelUsuario.ADMIN, criado.getPapel());
        assertNotEquals("senha-forte-123", captor.getValue().getSenhaHash());
        assertTrue(passwordEncoder.matches("senha-forte-123", captor.getValue().getSenhaHash()));
    }

    @Test
    void naoDevePermitirPrimeiroAcessoQuandoJaExisteUsuario() {
        when(usuarioRepository.count()).thenReturn(1L);
        PrimeiroAcessoRequestDTO dto = new PrimeiroAcessoRequestDTO();
        dto.setNome("Invasor");
        dto.setLogin("invasor");
        dto.setSenha("12345678");

        assertThrows(BusinessException.class, () -> usuarioService.criarPrimeiroAdministrador(dto));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void naoDeveCriarLoginRepetidoNemSemSenha() {
        UsuarioRequestDTO dto = dados("Maria", PapelUsuario.USUARIO, true);
        assertThrows(BusinessException.class, () -> usuarioService.criar(dto));

        dto.setSenha("12345678");
        when(usuarioRepository.existsByLogin("maria")).thenReturn(true);
        assertThrows(BusinessException.class, () -> usuarioService.criar(dto));
    }

    @Test
    void deveAlterarPropriaSenhaSomenteComSenhaAtualCorreta() {
        Usuario maria = usuario(2, "maria", PapelUsuario.USUARIO, true, "senha-antiga");
        when(usuarioRepository.findByLogin("maria")).thenReturn(Optional.of(maria));
        AlterarSenhaRequestDTO dto = new AlterarSenhaRequestDTO();
        dto.setSenhaAtual("errada");
        dto.setNovaSenha("senha-nova-123");

        assertThrows(BusinessException.class, () -> usuarioService.alterarPropriaSenha("maria", dto));

        dto.setSenhaAtual("senha-antiga");
        usuarioService.alterarPropriaSenha("maria", dto);
        assertTrue(passwordEncoder.matches("senha-nova-123", maria.getSenhaHash()));
    }

    @Test
    void naoDevePermitirDesativarOProprioUsuario() {
        Usuario admin = usuario(1, "admin", PapelUsuario.ADMIN, true, "x");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertThrows(BusinessException.class,
                () -> usuarioService.atualizar(1L, dados("admin", PapelUsuario.ADMIN, false), "admin"));
    }

    @Test
    void deveManterPeloMenosUmAdministradorAtivo() {
        Usuario unicoAdmin = usuario(1, "admin", PapelUsuario.ADMIN, true, "x");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(unicoAdmin));
        when(usuarioRepository.countByPapelAndAtivoTrue(PapelUsuario.ADMIN)).thenReturn(1L);

        // Rebaixar o unico administrador (feito por ele mesmo)
        assertThrows(BusinessException.class,
                () -> usuarioService.atualizar(1L, dados("admin", PapelUsuario.USUARIO, true), "admin"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void deveEncerrarSessoesDeQuemFoiDesativado() {
        Usuario maria = usuario(2, "maria", PapelUsuario.USUARIO, true, "x");
        SessionInformation sessao = new SessionInformation("maria", "sessao-1", new Date());
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(maria));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));
        when(sessionRegistry.getAllSessions(any(), anyBoolean())).thenReturn(List.of(sessao));

        UsuarioResponseDTO resultado = usuarioService.atualizar(2L, dados("maria", PapelUsuario.USUARIO, false), "admin");

        assertEquals(false, resultado.isAtivo());
        assertEquals("Maria Souza", resultado.getNome());
        assertTrue(sessao.isExpired());
    }

    @Test
    void deveEncerrarSessoesAoRedefinirSenhaDeOutraPessoa() {
        Usuario maria = usuario(2, "maria", PapelUsuario.USUARIO, true, "antiga");
        SessionInformation sessao = new SessionInformation("maria", "sessao-1", new Date());
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(maria));
        when(sessionRegistry.getAllSessions(any(), anyBoolean())).thenReturn(List.of(sessao));
        AlterarSenhaRequestDTO dto = new AlterarSenhaRequestDTO();
        dto.setNovaSenha("nova-senha-123");

        usuarioService.redefinirSenha(2L, dto, "admin");

        assertTrue(passwordEncoder.matches("nova-senha-123", maria.getSenhaHash()));
        assertTrue(sessao.isExpired());
    }
}
