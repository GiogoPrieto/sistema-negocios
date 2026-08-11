package com.sistema.SistemaNegocios.repository;

import com.sistema.SistemaNegocios.model.Caja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // <-- Agregar import

import java.util.List;
import java.util.Optional;

@Repository // <-- Agregar anotación
public interface ICajaRepository extends JpaRepository<Caja, Long> {

    Optional<Caja> findByEstado(String estado);
    List<Caja> findByEstadoOrderByFechaAperturaDesc(String estado);
}