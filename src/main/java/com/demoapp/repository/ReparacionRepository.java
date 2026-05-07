package com.demoapp.repository;

import com.demoapp.model.Cliente;
import com.demoapp.model.Reparacion;
import com.demoapp.model.Reparacion.Estado;
import com.demoapp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
/**
 * Repositorio JPA para la entidad Reparacion.
 *
 * Proporciona consultas especializadas para:
 *  - Obtener reparaciones por cliente, estado o fecha de entrada.
 *  - Buscar reparaciones activas (todas excepto ENTREGADO).
 *  - Realizar búsquedas por dispositivo con coincidencia parcial.
 *  - Contar reparaciones por estado o activas.
 *  - Consultar reparaciones asignadas a un técnico, filtradas u ordenadas.
 *  - Listar reparaciones pendientes de asignación (sin técnico).
 *
 * Es utilizado por los servicios para gestionar el ciclo completo
 * de las reparaciones y facilitar consultas eficientes en la base de datos.
 */

public interface ReparacionRepository extends JpaRepository<Reparacion, Long> {

    // Todas las reparaciones de un cliente
    List<Reparacion> findByCliente(Cliente cliente);

    // Filtrar por estado
    List<Reparacion> findByEstado(Estado estado);

    // Reparaciones entradas hoy
    List<Reparacion> findByFechaEntrada(LocalDate fecha);

    // Reparaciones activas (todo menos ENTREGADO)
    List<Reparacion> findByEstadoNot(Estado estado);

    // Buscar por dispositivo
    List<Reparacion> findByDispositivoContainingIgnoreCase(String dispositivo);

    // Contar por estado
    long countByEstado(Estado estado);

    // Contar activas
    long countByEstadoNot(Estado estado);

    // ── Consultas por técnico ─────────────────────────────

    // Todas las reparaciones asignadas a un técnico
    List<Reparacion> findByTecnico(Usuario tecnico);

    // Reparaciones activas de un técnico (su agenda de trabajo)
    List<Reparacion> findByTecnicoAndEstadoNotOrderByFechaEntradaAsc(
            Usuario tecnico, Estado estado);

    // Reparaciones de un técnico filtradas por estado
    List<Reparacion> findByTecnicoAndEstado(Usuario tecnico, Estado estado);

    // Reparaciones sin técnico asignado (pendientes de asignar)
    List<Reparacion> findByTecnicoIsNull();
}