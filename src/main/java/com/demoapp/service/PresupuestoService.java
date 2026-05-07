package com.demoapp.service;

import com.demoapp.model.*;
import com.demoapp.model.Presupuesto.Estado;
import com.demoapp.repository.ClienteRepository;
import com.demoapp.repository.PresupuestoRepository;
import com.demoapp.repository.ReparacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PresupuestoService {

    @Autowired private PresupuestoRepository presupuestoRepository;
    @Autowired private ClienteRepository     clienteRepository;
    @Autowired private ReparacionRepository  reparacionRepository;

    // ── Consultas ─────────────────────────────────────────

    public List<Presupuesto> findAll() {
        return presupuestoRepository.findAllByOrderByFechaDescIdDesc();
    }

    public Optional<Presupuesto> findById(Long id) {
        return presupuestoRepository.findById(id);
    }

    public List<Presupuesto> findByEstado(Estado estado) {
        return presupuestoRepository.findByEstado(estado);
    }

    // ── Crear presupuesto ─────────────────────────────────

    @Transactional
    public Presupuesto crear(Long clienteId, Long reparacionId, String notas,
                             List<Map<String, Object>> lineasData) {

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Presupuesto p = new Presupuesto();
        p.setNumero(generarNumero());
        p.setFecha(LocalDate.now());
        p.setEstado(Estado.BORRADOR);
        p.setCliente(cliente);
        p.setNotas(notas);

        if (reparacionId != null) {
            reparacionRepository.findById(reparacionId)
                    .ifPresent(p::setReparacion);
        }

        agregarLineas(p, lineasData);
        return presupuestoRepository.save(p);
    }

    // ── Actualizar presupuesto ────────────────────────────

    @Transactional
    public Presupuesto actualizar(Long id, String notas, Estado estado,
                                  List<Map<String, Object>> lineasData) {

        Presupuesto p = presupuestoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));

        p.setNotas(notas);
        p.setEstado(estado);
        p.getLineas().clear();
        agregarLineas(p, lineasData);

        return presupuestoRepository.save(p);
    }

    // ── Cambiar estado ────────────────────────────────────

    @Transactional
    public void cambiarEstado(Long id, Estado nuevoEstado) {
        Presupuesto p = presupuestoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));
        p.setEstado(nuevoEstado);
        presupuestoRepository.save(p);
    }

    // ── Eliminar ──────────────────────────────────────────

    public void eliminar(Long id) {
        if (!presupuestoRepository.existsById(id))
            throw new RuntimeException("Presupuesto no encontrado");
        presupuestoRepository.deleteById(id);
    }

    // ── KPIs para dashboard ───────────────────────────────

    public long contarPorEstado(Estado estado) {
        return presupuestoRepository.findByEstado(estado).size();
    }

    // ── Helpers ───────────────────────────────────────────

    private void agregarLineas(Presupuesto p, List<Map<String, Object>> lineasData) {
        if (lineasData == null) return;
        for (Map<String, Object> data : lineasData) {
            LineaPresupuesto linea = new LineaPresupuesto();
            linea.setConcepto((String) data.get("concepto"));
            linea.setCantidad(((Number) data.get("cantidad")).intValue());
            linea.setPrecioUnitario(((Number) data.get("precioUnitario")).doubleValue());
            linea.setIva(((Number) data.get("iva")).doubleValue());
            p.addLinea(linea);
        }
    }

    private String generarNumero() {
        String anio = String.valueOf(LocalDate.now().getYear());
        Optional<String> ultimo = presupuestoRepository.findUltimoNumeroDelAnio(anio);

        int siguiente = 1;
        if (ultimo.isPresent()) {
            // Formato: FAC-2026-0042  → extraer 0042
            String[] partes = ultimo.get().split("-");
            if (partes.length == 3) {
                try { siguiente = Integer.parseInt(partes[2]) + 1; }
                catch (NumberFormatException ignored) {}
            }
        }
        return String.format("FAC-%s-%04d", anio, siguiente);
    }
}
