package com.aro.aro_gestao.repository;

import com.aro.aro_gestao.entity.Compromisso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CompromissoRepository extends JpaRepository<Compromisso, Long> {

    @EntityGraph(attributePaths = "cliente")
    List<Compromisso> findByDataBetweenOrderByDataAscIdAsc(LocalDate inicio, LocalDate fim);

    @EntityGraph(attributePaths = "cliente")
    List<Compromisso> findByClienteIdAndDataBetweenOrderByDataAscIdAsc(
            Long clienteId, LocalDate inicio, LocalDate fim);

    boolean existsByClienteId(Long clienteId);
}
