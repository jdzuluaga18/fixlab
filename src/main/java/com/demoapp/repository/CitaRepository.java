package com.demoapp.repository;

import com.demoapp.model.Cita;
import com.demoapp.model.Cliente;
import com.demoapp.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;
/**
 * Repositorio JPA para la entidad Cita.
 *
 * Proporciona consultas especializadas para:
 *  - Obtener todas las citas ordenadas por fecha y hora.
 *  - Listar citas de un día concreto.
 *  - Consultar citas asociadas a un cliente.
 *  - Recuperar las próximas citas desde una fecha dada.
 *
 * Es utilizado por los servicios para gestionar la agenda
 * y facilitar búsquedas eficientes en la base de datos.
 */

public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Todas las citas ordenadas por fecha y hora
    List<Cita> findAllByOrderByFechaAscHoraAsc();

    // Citas de un día concreto
    List<Cita> findByFechaOrderByHoraAsc(LocalDate fecha);

    // Citas de un cliente
    List<Cita> findByClienteOrderByFechaAscHoraAsc(Cliente cliente);

    // Citas desde hoy en adelante (próximas)
    List<Cita> findByFechaGreaterThanEqualOrderByFechaAscHoraAsc(LocalDate fecha);
}
