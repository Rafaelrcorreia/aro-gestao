package com.aro.aro_gestao.repository;

import com.aro.aro_gestao.entity.Imovel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ImovelRepository extends JpaRepository<Imovel, Long> {

    boolean existsByClienteId(Long clienteId);

    // clienteId e busca sao opcionais: quando vierem nulos, o filtro correspondente e ignorado
    @EntityGraph(attributePaths = "cliente")
    @Query("""
            SELECT i FROM Imovel i
            WHERE (:clienteId IS NULL OR i.cliente.id = :clienteId)
              AND (:busca IS NULL
                   OR LOWER(i.denominacao) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(i.municipio) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(i.matricula) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(i.codigoCar) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(i.codigoCcir) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(i.nirf) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(i.cliente.nomeCompleto) LIKE LOWER(CONCAT('%', :busca, '%')))
            ORDER BY i.denominacao ASC, i.id ASC
            """)
    List<Imovel> buscar(@Param("clienteId") Long clienteId, @Param("busca") String busca);
}
