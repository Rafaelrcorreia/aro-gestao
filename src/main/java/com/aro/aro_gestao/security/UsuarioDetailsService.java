package com.aro.aro_gestao.security;

import com.aro.aro_gestao.entity.Usuario;
import com.aro.aro_gestao.repository.UsuarioRepository;
import com.aro.aro_gestao.service.UsuarioService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Diz ao Spring Security como achar um usuario pelo login
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByLogin(UsuarioService.normalizarLogin(login))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado"));

        return User.withUsername(usuario.getLogin())
                .password(usuario.getSenhaHash())
                .roles(usuario.getPapel().name())
                .disabled(!usuario.isAtivo())
                .build();
    }
}
