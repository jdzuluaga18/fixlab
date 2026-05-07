package com.demoapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa un presupuesto/factura asociado
 * a una reparación y un cliente.
 */
@Entity
@Table(name = "presupuestos")
public class Presupuesto {

    public enum Estado { BORRADOR, ENVIADO, ACEPTADO, RECHAZADO, FACTURADO }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Número de factura legible, ej: FAC-2026-0001 */
    @Column(unique = true)
    private String numero;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Estado estado;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "reparacion_id")
    private Reparacion reparacion;

    /** Notas o condiciones que aparecen en la factura */
    @Column(length = 1000)
    private String notas;

    @OneToMany(mappedBy = "presupuesto", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LineaPresupuesto> lineas = new ArrayList<>();

    public Presupuesto() {}

    // ── Cálculos globales ─────────────────────────────────

    public double getBaseImponible() {
        return lineas.stream().mapToDouble(LineaPresupuesto::getSubtotal).sum();
    }

    public double getTotalIva() {
        return lineas.stream().mapToDouble(LineaPresupuesto::getImporteIva).sum();
    }

    public double getTotalFactura() {
        return lineas.stream().mapToDouble(LineaPresupuesto::getTotal).sum();
    }

    // ── Helper líneas ─────────────────────────────────────

    public void addLinea(LineaPresupuesto linea) {
        lineas.add(linea);
        linea.setPresupuesto(this);
    }

    public void removeLinea(LineaPresupuesto linea) {
        lineas.remove(linea);
        linea.setPresupuesto(null);
    }

    // ── Getters / Setters ─────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Reparacion getReparacion() { return reparacion; }
    public void setReparacion(Reparacion reparacion) { this.reparacion = reparacion; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public List<LineaPresupuesto> getLineas() { return lineas; }
    public void setLineas(List<LineaPresupuesto> lineas) { this.lineas = lineas; }
}
