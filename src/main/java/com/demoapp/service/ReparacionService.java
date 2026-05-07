package com.demoapp.service;

import com.demoapp.model.Cliente;
import com.demoapp.model.Reparacion;
import com.demoapp.model.Reparacion.Estado;
import com.demoapp.model.Usuario;
import com.demoapp.repository.ClienteRepository;
import com.demoapp.repository.ReparacionRepository;
import com.demoapp.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
/**
 * Servicio encargado de gestionar la lógica de negocio relacionada con las reparaciones.
 *
 * Proporciona operaciones para:
 *  - Crear, actualizar y eliminar reparaciones.
 *  - Consultar reparaciones por cliente, estado, técnico o fechas.
 *  - Obtener reparaciones activas y pendientes de asignación.
 *  - Validar relaciones con Cliente y Usuario antes de guardar.
 *
 * Actúa como capa intermedia entre los controladores y los repositorios,
 * centralizando las reglas de negocio del flujo de reparaciones en FixLab.
 */

@Service
public class ReparacionService {

    @Autowired private ReparacionRepository reparacionRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    // ── Obtener todas ─────────────────────────────────────
    public List<Reparacion> findAll() {
        return reparacionRepository.findAll();
    }

    // ── Buscar por ID ─────────────────────────────────────
    public Optional<Reparacion> findById(Long id) {
        return reparacionRepository.findById(id);
    }

    // ── Reparaciones de un cliente ────────────────────────
    public List<Reparacion> findByCliente(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return reparacionRepository.findByCliente(cliente);
    }

    // ── Filtrar por estado ────────────────────────────────
    public List<Reparacion> findByEstado(Estado estado) {
        return reparacionRepository.findByEstado(estado);
    }

    // ── Reparaciones activas (no entregadas) ──────────────
    public List<Reparacion> findActivas() {
        return reparacionRepository.findByEstadoNot(Estado.ENTREGADO);
    }

    // ── Sin técnico asignado ──────────────────────────────
    public List<Reparacion> findSinTecnico() {
        return reparacionRepository.findByTecnicoIsNull();
    }

    // ── Buscar por dispositivo ────────────────────────────
    public List<Reparacion> buscarPorDispositivo(String texto) {
        if (texto == null || texto.isBlank()) return findAll();
        return reparacionRepository.findByDispositivoContainingIgnoreCase(texto);
    }

    // ── Agenda del técnico (activas asignadas a él) ───────
    public List<Reparacion> findAgendaTecnico(Usuario tecnico) {
        return reparacionRepository
                .findByTecnicoAndEstadoNotOrderByFechaEntradaAsc(tecnico, Estado.ENTREGADO);
    }

    // ── KPIs para el dashboard ────────────────────────────
    public long contarActivas() {
        return reparacionRepository.countByEstadoNot(Estado.ENTREGADO);
    }

    public long contarHoy() {
        return reparacionRepository.findByFechaEntrada(LocalDate.now()).size();
    }

    // ── Crear reparación ──────────────────────────────────
    public Reparacion crear(Long clienteId, String dispositivo,
                            String descripcion, Estado estado, Long tecnicoId) {

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        if (dispositivo == null || dispositivo.isBlank()) {
            throw new RuntimeException("El dispositivo es obligatorio");
        }

        Reparacion reparacion = new Reparacion();
        reparacion.setCliente(cliente);
        reparacion.setDispositivo(dispositivo.trim());
        reparacion.setDescripcion(descripcion != null ? descripcion.trim() : null);
        reparacion.setEstado(estado != null ? estado : Estado.DIAGNOSTICO);
        reparacion.setFechaEntrada(LocalDate.now());

        // Asignar técnico si se proporcionó
        if (tecnicoId != null) {
            Usuario tecnico = usuarioRepository.findById(tecnicoId)
                    .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));
            reparacion.setTecnico(tecnico);
        }

        return reparacionRepository.save(reparacion);
    }

    // ── Actualizar reparación ─────────────────────────────
    public Reparacion actualizar(Long id, String dispositivo, String descripcion,
                                 Estado estado, Long tecnicoId) {

        Reparacion reparacion = reparacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reparación no encontrada"));

        if (dispositivo == null || dispositivo.isBlank()) {
            throw new RuntimeException("El dispositivo es obligatorio");
        }

        reparacion.setDispositivo(dispositivo.trim());
        reparacion.setDescripcion(descripcion != null ? descripcion.trim() : null);

        if (estado == Estado.ENTREGADO && reparacion.getEstado() != Estado.ENTREGADO) {
            reparacion.setFechaSalida(LocalDate.now());
        }
        if (estado != Estado.ENTREGADO) {
            reparacion.setFechaSalida(null);
        }
        reparacion.setEstado(estado);

        // Actualizar técnico
        if (tecnicoId != null) {
            Usuario tecnico = usuarioRepository.findById(tecnicoId)
                    .orElseThrow(() -> new RuntimeException("Técnico no encontrado"));
            reparacion.setTecnico(tecnico);
        } else {
            reparacion.setTecnico(null);
        }

        return reparacionRepository.save(reparacion);
    }

    // ── Cambiar solo el estado ────────────────────────────
    public Reparacion cambiarEstado(Long id, Estado nuevoEstado) {
        Reparacion reparacion = reparacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reparación no encontrada"));

        if (nuevoEstado == Estado.ENTREGADO && reparacion.getEstado() != Estado.ENTREGADO) {
            reparacion.setFechaSalida(LocalDate.now());
        }
        if (nuevoEstado != Estado.ENTREGADO) {
            reparacion.setFechaSalida(null);
        }

        reparacion.setEstado(nuevoEstado);
        return reparacionRepository.save(reparacion);
    }

    // ── Eliminar reparación ───────────────────────────────
    public void eliminar(Long id) {
        if (!reparacionRepository.existsById(id)) {
            throw new RuntimeException("Reparación no encontrada");
        }
        reparacionRepository.deleteById(id);
    }
}