package com.aro.aro_gestao.repository;

import com.aro.aro_gestao.entity.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.time.LocalDate;

import java.util.List;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {

    @EntityGraph(attributePaths = "cliente")
    List<Atendimento> findByProximoContatoBetweenOrderByProximoContatoAscIdAsc(
            LocalDate inicio, LocalDate fim);

    @EntityGraph(attributePaths = "cliente")
    List<Atendimento> findByClienteIdAndProximoContatoBetweenOrderByProximoContatoAscIdAsc(
            Long clienteId, LocalDate inicio, LocalDate fim);

    List<Atendimento> findTop5ByOrderByDataHoraDescIdDesc();

    List<Atendimento> findByClienteIdOrderByDataHoraDesc(Long clienteId);

    boolean existsByClienteId(Long clienteId);
}
