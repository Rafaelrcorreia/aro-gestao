package com.aro.aro_gestao.repository;

import com.aro.aro_gestao.entity.StatusTarefa;
import com.aro.aro_gestao.entity.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDate;
import java.util.List;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    @EntityGraph(attributePaths = "cliente")
    List<Tarefa> findByPrazoBetweenOrderByPrazoAscIdAsc(LocalDate inicio, LocalDate fim);

    @EntityGraph(attributePaths = "cliente")
    List<Tarefa> findByClienteIdAndPrazoBetweenOrderByPrazoAscIdAsc(
            Long clienteId, LocalDate inicio, LocalDate fim);

    long countByStatus(StatusTarefa status);

    long countByStatusNotAndPrazoBefore(StatusTarefa status, LocalDate data);

    long countByStatusNotAndPrazoGreaterThanEqual(StatusTarefa status, LocalDate data);

    List<Tarefa> findTop5ByStatusNotAndPrazoGreaterThanEqualOrderByPrazoAscIdAsc(
            StatusTarefa status, LocalDate data);

    List<Tarefa> findByClienteIdOrderByPrazoAsc(Long clienteId);

    List<Tarefa> findAllByOrderByPrazoAsc();

    boolean existsByClienteId(Long clienteId);
}
