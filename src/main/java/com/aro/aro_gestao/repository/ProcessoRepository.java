package com.aro.aro_gestao.repository;

import com.aro.aro_gestao.entity.Processo;
import com.aro.aro_gestao.entity.StatusProcesso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ProcessoRepository extends JpaRepository<Processo, Long> {

    boolean existsByClienteId(Long clienteId);

    boolean existsByImovelId(Long imovelId);

    @EntityGraph(attributePaths = {"cliente", "imovel"})
    List<Processo> findByPrazoPrevistoBetweenOrderByPrazoPrevistoAscIdAsc(LocalDate inicio, LocalDate fim);

    @EntityGraph(attributePaths = {"cliente", "imovel"})
    List<Processo> findByClienteIdAndPrazoPrevistoBetweenOrderByPrazoPrevistoAscIdAsc(
            Long clienteId, LocalDate inicio, LocalDate fim);

    long countByStatusNotIn(Collection<StatusProcesso> status);

    long countByStatusNotInAndPrazoPrevistoBefore(Collection<StatusProcesso> status, LocalDate data);

    @EntityGraph(attributePaths = {"cliente", "imovel"})
    @Query("SELECT p FROM Processo p")
    List<Processo> buscarTodosComVinculos();

    // Relatorio de clientes: [clienteId, quantidade] de processos ainda em andamento
    @Query("""
            SELECT p.cliente.id, COUNT(p) FROM Processo p
            WHERE p.status NOT IN :finalizados
            GROUP BY p.cliente.id
            """)
    List<Object[]> contarEmAndamentoPorCliente(@Param("finalizados") Collection<StatusProcesso> finalizados);

    // Todos os filtros sao opcionais: quando vierem nulos, sao ignorados.
    // Mais recentes primeiro.
    @EntityGraph(attributePaths = {"cliente", "imovel"})
    @Query("""
            SELECT p FROM Processo p
            LEFT JOIN p.imovel i
            WHERE (:clienteId IS NULL OR p.cliente.id = :clienteId)
              AND (:status IS NULL OR p.status = :status)
              AND (:busca IS NULL
                   OR LOWER(p.numeroProtocolo) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(p.orgao) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(p.descricao) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(p.cliente.nomeCompleto) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(i.denominacao) LIKE LOWER(CONCAT('%', :busca, '%')))
            ORDER BY p.dataAbertura DESC, p.id DESC
            """)
    List<Processo> buscar(@Param("clienteId") Long clienteId,
                          @Param("status") StatusProcesso status,
                          @Param("busca") String busca);
}
