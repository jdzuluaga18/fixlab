package com.demoapp.service;

import com.demoapp.model.Cita;
import com.demoapp.model.Cliente;
import com.demoapp.repository.CitaRepository;
import com.demoapp.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
/**
 * Servicio encargado de gestionar la lógica de negocio relacionada con las citas.
 *
 * Proporciona operaciones para:
 *  - Crear, actualizar y eliminar citas.
 *  - Consultar citas por fecha, cliente o rango temporal.
 *  - Validar disponibilidad y evitar solapamientos.
 *
 * Actúa como capa intermedia entre los controladores y el repositorio,
 * centralizando las reglas de negocio de la agenda del sistema.
 */

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // ── Obtener todas ordenadas ───────────────────────────
    public List<Cita> findAll() {
        return citaRepository.findAllByOrderByFechaAscHoraAsc();
    }

    // ── Próximas citas (desde hoy) ────────────────────────
    public List<Cita> findProximas() {
        return citaRepository.findByFechaGreaterThanEqualOrderByFechaAscHoraAsc(LocalDate.now());
    }

    // ── Citas de un día concreto ──────────────────────────
    public List<Cita> findByFecha(LocalDate fecha) {
        return citaRepository.findByFechaOrderByHoraAsc(fecha);
    }

    // ── Buscar por ID ─────────────────────────────────────
    public Optional<Cita> findById(Long id) {
        return citaRepository.findById(id);
    }

    // ── Crear cita ────────────────────────────────────────
    public Cita crear(LocalDate fecha, LocalTime hora,
                      String descripcion, Long clienteId) {

        validar(fecha, hora, descripcion);

        Cita cita = new Cita();
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setDescripcion(descripcion.trim());

        // Cliente opcional
        if (clienteId != null) {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            cita.setCliente(cliente);
        }

        return citaRepository.save(cita);
    }

    // ── Actualizar cita ───────────────────────────────────
    public Cita actualizar(Long id, LocalDate fecha, LocalTime hora,
                           String descripcion, Long clienteId) {

        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        validar(fecha, hora, descripcion);

        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setDescripcion(descripcion.trim());

        if (clienteId != null) {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            cita.setCliente(cliente);
        } else {
            cita.setCliente(null); // permite desvincular el cliente
        }

        return citaRepository.save(cita);
    }

    // ── Eliminar cita ─────────────────────────────────────
    public void eliminar(Long id) {
        if (!citaRepository.existsById(id)) {
            throw new RuntimeException("Cita no encontrada");
        }
        citaRepository.deleteById(id);
    }

    // ── Validaciones ──────────────────────────────────────
    private void validar(LocalDate fecha, LocalTime hora, String descripcion) {
        if (fecha == null) {
            throw new RuntimeException("La fecha es obligatoria");
        }
        if (hora == null) {
            throw new RuntimeException("La hora es obligatoria");
        }
        if (descripcion == null || descripcion.isBlank()) {
            throw new RuntimeException("La descripción es obligatoria");
        }
    }
}
