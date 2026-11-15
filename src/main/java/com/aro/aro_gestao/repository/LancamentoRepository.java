package com.aro.aro_gestao.repository;

import com.aro.aro_gestao.entity.Lancamento;
import com.aro.aro_gestao.entity.TipoLancamento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    boolean existsByClienteId(Long clienteId);

    boolean existsByProcessoId(Long processoId);

    @EntityGraph(attributePaths = {"cliente", "processo"})
    Optional<Lancamento> findComVinculosById(Long id);

    // Lancamentos do periodo: vencem no periodo OU foram pagos nele
    // (assim um pagamento atrasado aparece no mes em que entrou o dinheiro).
    // tipo, clienteId e busca sao opcionais.
    @EntityGraph(attributePaths = {"cliente", "processo"})
    @Query("""
            SELECT l FROM Lancamento l
            LEFT JOIN l.cliente c
            WHERE ((l.dataVencimento BETWEEN :inicio AND :fim)
                   OR (l.dataPagamento BETWEEN :inicio AND :fim))
              AND (:tipo IS NULL OR l.tipo = :tipo)
              AND (:clienteId IS NULL OR c.id = :clienteId)
              AND (:busca IS NULL
                   OR LOWER(l.descricao) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(l.observacoes) LIKE LOWER(CONCAT('%', :busca, '%'))
                   OR LOWER(c.nomeCompleto) LIKE LOWER(CONCAT('%', :busca, '%')))
            ORDER BY l.dataVencimento ASC, l.id ASC
            """)
    List<Lancamento> buscarNoPeriodo(@Param("inicio") LocalDate inicio,
                                     @Param("fim") LocalDate fim,
                                     @Param("tipo") TipoLancamento tipo,
                                     @Param("clienteId") Long clienteId,
                                     @Param("busca") String busca);

    // Total efetivamente pago/recebido no periodo
    @Query("""
            SELECT COALESCE(SUM(l.valor), 0) FROM Lancamento l
            WHERE l.tipo = :tipo AND l.dataPagamento BETWEEN :inicio AND :fim
            """)
    BigDecimal somarPagosNoPeriodo(@Param("tipo") TipoLancamento tipo,
                                   @Param("inicio") LocalDate inicio,
                                   @Param("fim") LocalDate fim);

    // Total em aberto que vence no periodo
    @Query("""
            SELECT COALESCE(SUM(l.valor), 0) FROM Lancamento l
            WHERE l.tipo = :tipo AND l.dataPagamento IS NULL
              AND l.dataVencimento BETWEEN :inicio AND :fim
            """)
    BigDecimal somarEmAbertoNoPeriodo(@Param("tipo") TipoLancamento tipo,
                                      @Param("inicio") LocalDate inicio,
                                      @Param("fim") LocalDate fim);

    // Total em aberto com vencimento ja passado (qualquer mes)
    @Query("""
            SELECT COALESCE(SUM(l.valor), 0) FROM Lancamento l
            WHERE l.tipo = :tipo AND l.dataPagamento IS NULL AND l.dataVencimento < :hoje
            """)
    BigDecimal somarAtrasados(@Param("tipo") TipoLancamento tipo, @Param("hoje") LocalDate hoje);
}
