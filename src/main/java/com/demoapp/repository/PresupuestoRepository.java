package com.demoapp.repository;

import com.demoapp.model.Cliente;
import com.demoapp.model.Presupuesto;
import com.demoapp.model.Presupuesto.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PresupuestoRepository extends JpaRepository<Presupuesto, Long> {

    List<Presupuesto> findByCliente(Cliente cliente);
    List<Presupuesto> findByEstado(Estado estado);
    List<Presupuesto> findAllByOrderByFechaDescIdDesc();
    Optional<Presupuesto> findByNumero(String numero);

    // Obtener el último número de secuencia del año actual
    @Query("SELECT MAX(p.numero) FROM Presupuesto p WHERE p.numero LIKE CONCAT('FAC-', :anio, '-%')")
    Optional<String> findUltimoNumeroDelAnio(String anio);
}
