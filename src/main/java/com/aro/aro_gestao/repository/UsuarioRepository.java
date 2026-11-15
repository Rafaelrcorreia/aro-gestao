package com.aro.aro_gestao.repository;

import com.aro.aro_gestao.entity.PapelUsuario;
import com.aro.aro_gestao.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    boolean existsByLogin(String login);

    long countByPapelAndAtivoTrue(PapelUsuario papel);

    List<Usuario> findAllByOrderByNomeAsc();
}
