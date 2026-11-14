package com.aro.aro_gestao.repository;

import com.aro.aro_gestao.entity.Cliente;
import com.aro.aro_gestao.entity.StatusCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    long countByStatus(StatusCliente status);

    Optional<Cliente> findByCpfCnpj(String cpfCnpj);

    @Query("""
            SELECT c FROM Cliente c
            WHERE LOWER(c.nomeCompleto) LIKE LOWER(CONCAT('%', :busca, '%'))
               OR c.cpfCnpj LIKE CONCAT('%', :busca, '%')
               OR c.telefone LIKE CONCAT('%', :busca, '%')
               OR c.whatsapp LIKE CONCAT('%', :busca, '%')
            """)
    List<Cliente> buscarPorTexto(@Param("busca") String busca);
}
