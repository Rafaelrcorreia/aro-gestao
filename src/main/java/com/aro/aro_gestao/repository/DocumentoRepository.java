package com.aro.aro_gestao.repository;

import com.aro.aro_gestao.entity.Documento;
import com.aro.aro_gestao.entity.TipoDocumento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    boolean existsByClienteId(Long clienteId);

    boolean existsByImovelId(Long imovelId);

    boolean existsByProcessoId(Long processoId);

    @EntityGraph(attributePaths = {"cliente", "imovel", "processo"})
    Optional<Documento> findComVinculosById(Long id);

    // Todos os filtros sao opcionais: quando vierem nulos, sao ignorados.
    @EntityGraph(attributePaths = {"cliente", "imovel", "processo"})
    @Query("""
            SELECT d FROM Documento d
            LEFT JOIN d.imovel i
            WHERE (:clienteId IS NULL OR d.cliente.id = :clienteId)
              AND (:tipo IS NULL OR d.tipo = :tipo)
              AND (:busca IS NULL
                   OR LOWER(d.titulo) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(d.numero) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(d.arquivoNomeOriginal) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(d.cliente.nomeCompleto) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(i.denominacao) LIKE LOWER(CONCAT('%', :busca, '%')))
            ORDER BY d.dataCadastro DESC, d.id DESC
            """)
    List<Documento> buscar(@Param("clienteId") Long clienteId,
                           @Param("tipo") TipoDocumento tipo,
                           @Param("busca") String busca);
}
