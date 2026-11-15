package com.aro.aro_gestao.service;

import com.aro.aro_gestao.dto.AlterarSenhaRequestDTO;
import com.aro.aro_gestao.dto.PrimeiroAcessoRequestDTO;
import com.aro.aro_gestao.dto.UsuarioRequestDTO;
import com.aro.aro_gestao.dto.UsuarioResponseDTO;
import com.aro.aro_gestao.entity.PapelUsuario;
import com.aro.aro_gestao.entity.Usuario;
import com.aro.aro_gestao.exception.BusinessException;
import com.aro.aro_gestao.exception.ResourceNotFoundException;
import com.aro.aro_gestao.repository.UsuarioRepository;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionRegistry sessionRegistry;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                          SessionRegistry sessionRegistry) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionRegistry = sessionRegistry;
    }

    /* ---------- Primeiro acesso ---------- */

    public boolean existeAlgumUsuario() {
        return usuarioRepository.count() > 0;
    }

    // synchronized: evita que duas pessoas criem o "primeiro" administrador ao mesmo tempo
    @Transactional
    public synchronized UsuarioResponseDTO criarPrimeiroAdministrador(PrimeiroAcessoRequestDTO dto) {
        if (existeAlgumUsuario()) {
            throw new BusinessException("O sistema ja possui usuarios. Entre com o seu login.");
        }
        Usuario usuario = novoUsuario(dto.getNome(), dto.getLogin(), dto.getSenha(), PapelUsuario.ADMIN);
        return new UsuarioResponseDTO(usuarioRepository.save(usuario));
    }

    /* ---------- Usuario logado ---------- */

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorLogin(String login) {
        return new UsuarioResponseDTO(buscarPorLoginOuFalhar(login));
    }

    @Transactional
    public void alterarPropriaSenha(String login, AlterarSenhaRequestDTO dto) {
        Usuario usuario = buscarPorLoginOuFalhar(login);
        if (dto.getSenhaAtual() == null || !passwordEncoder.matches(dto.getSenhaAtual(), usuario.getSenhaHash())) {
            throw new BusinessException("A senha atual nao confere.");
        }
        usuario.setSenhaHash(passwordEncoder.encode(dto.getNovaSenha()));
        usuarioRepository.save(usuario);
    }

    /* ---------- Gestao (somente administrador) ---------- */

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAllByOrderByNomeAsc().stream().map(UsuarioResponseDTO::new).toList();
    }

    @Transactional
    public UsuarioResponseDTO criar(UsuarioRequestDTO dto) {
        if (dto.getSenha() == null || dto.getSenha().isBlank()) {
            throw new BusinessException("Informe a senha inicial do usuario.");
        }
        Usuario usuario = novoUsuario(dto.getNome(), dto.getLogin(), dto.getSenha(), dto.getPapel());
        usuario.setAtivo(dto.getAtivo() == null || dto.getAtivo());
        return new UsuarioResponseDTO(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto, String loginDeQuemAltera) {
        Usuario usuario = buscarPorIdOuFalhar(id);
        String loginAntigo = usuario.getLogin();
        String loginNovo = normalizarLogin(dto.getLogin());
        boolean ativo = dto.getAtivo() == null || dto.getAtivo();
        boolean ehOProprio = loginAntigo.equals(loginDeQuemAltera);

        if (ehOProprio && !ativo) {
            throw new BusinessException("Voce nao pode desativar o seu proprio usuario.");
        }
        if (!loginNovo.equals(loginAntigo) && usuarioRepository.existsByLogin(loginNovo)) {
            throw new BusinessException("Ja existe um usuario com o login \"" + loginNovo + "\".");
        }
        // Sempre precisa sobrar pelo menos um administrador ativo
        boolean deixaDeSerAdminAtivo = usuario.getPapel() == PapelUsuario.ADMIN && usuario.isAtivo()
                && (dto.getPapel() != PapelUsuario.ADMIN || !ativo);
        if (deixaDeSerAdminAtivo && usuarioRepository.countByPapelAndAtivoTrue(PapelUsuario.ADMIN) <= 1) {
            throw new BusinessException("O sistema precisa ter pelo menos um administrador ativo.");
        }

        boolean perdeAcessoOuPapel = !ativo || dto.getPapel() != usuario.getPapel() || !loginNovo.equals(loginAntigo);
        usuario.setNome(dto.getNome().trim());
        usuario.setLogin(loginNovo);
        usuario.setPapel(dto.getPapel());
        usuario.setAtivo(ativo);
        Usuario salvo = usuarioRepository.save(usuario);

        // Quem foi desativado ou mudou de papel/login sai na hora (exceto quem fez a alteracao)
        if (perdeAcessoOuPapel && !ehOProprio) {
            encerrarSessoes(loginAntigo);
        }
        return new UsuarioResponseDTO(salvo);
    }

    @Transactional
    public void redefinirSenha(Long id, AlterarSenhaRequestDTO dto, String loginDeQuemAltera) {
        Usuario usuario = buscarPorIdOuFalhar(id);
        usuario.setSenhaHash(passwordEncoder.encode(dto.getNovaSenha()));
        usuarioRepository.save(usuario);
        // Com a senha nova, sessoes abertas com a antiga deixam de valer
        if (!usuario.getLogin().equals(loginDeQuemAltera)) {
            encerrarSessoes(usuario.getLogin());
        }
    }

    /* ---------- Auxiliares ---------- */

    private Usuario novoUsuario(String nome, String login, String senha, PapelUsuario papel) {
        String loginNormalizado = normalizarLogin(login);
        if (usuarioRepository.existsByLogin(loginNormalizado)) {
            throw new BusinessException("Ja existe um usuario com o login \"" + loginNormalizado + "\".");
        }
        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setLogin(loginNormalizado);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setPapel(papel);
        usuario.setAtivo(true);
        usuario.setDataCadastro(LocalDateTime.now());
        return usuario;
    }

    public static String normalizarLogin(String login) {
        return login == null ? "" : login.trim().toLowerCase(Locale.ROOT);
    }

    private void encerrarSessoes(String login) {
        // User.equals compara apenas o username, entao serve para achar as sessoes
        User principal = new User(login, "", List.of());
        for (SessionInformation sessao : sessionRegistry.getAllSessions(principal, false)) {
            sessao.expireNow();
        }
    }

    private Usuario buscarPorIdOuFalhar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario com id " + id + " nao encontrado"));
    }

    private Usuario buscarPorLoginOuFalhar(String login) {
        return usuarioRepository.findByLogin(normalizarLogin(login))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}
