package com.demoapp.model;

import jakarta.persistence.*;

/**
 * Línea de detalle dentro de un presupuesto.
 * Cada línea representa un concepto (mano de obra, pieza, etc.)
 * con cantidad, precio unitario e IVA propio.
 */
@Entity
@Table(name = "lineas_presupuesto")
public class LineaPresupuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "presupuesto_id")
    private Presupuesto presupuesto;

    @Column(nullable = false)
    private String concepto;

    @Column(nullable = false)
    private int cantidad;

    /** Precio unitario sin IVA (en euros) */
    @Column(nullable = false)
    private double precioUnitario;

    /** IVA aplicado a esta línea en porcentaje (ej: 21.0) */
    @Column(nullable = false)
    private double iva;

    public LineaPresupuesto() {}

    // ── Cálculos ──────────────────────────────────────────

    public double getSubtotal() {
        return cantidad * precioUnitario;
    }

    public double getImporteIva() {
        return getSubtotal() * (iva / 100.0);
    }

    public double getTotal() {
        return getSubtotal() + getImporteIva();
    }

    // ── Getters / Setters ─────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Presupuesto getPresupuesto() { return presupuesto; }
    public void setPresupuesto(Presupuesto presupuesto) { this.presupuesto = presupuesto; }

    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public double getIva() { return iva; }
    public void setIva(double iva) { this.iva = iva; }
}
