package com.demoapp.controller;

import com.demoapp.model.*;
import com.demoapp.model.Presupuesto.Estado;
import com.demoapp.service.ClienteService;
import com.demoapp.service.PresupuestoService;
import com.demoapp.service.ReparacionService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class FormularioPresupuestoController {

    // ── Cabecera ──────────────────────────────────────────
    @FXML private Label              lblNumero;
    @FXML private Label              lblFecha;
    @FXML private ComboBox<Cliente>  comboCliente;
    @FXML private ComboBox<Reparacion> comboReparacion;
    @FXML private ComboBox<Estado>   comboEstado;
    @FXML private TextArea           campoNotas;

    // ── Tabla de líneas ───────────────────────────────────
    @FXML private TableView<LineaPresupuesto>              tablaLineas;
    @FXML private TableColumn<LineaPresupuesto, String>    colConcepto;
    @FXML private TableColumn<LineaPresupuesto, String>    colCantidad;
    @FXML private TableColumn<LineaPresupuesto, String>    colPrecio;
    @FXML private TableColumn<LineaPresupuesto, String>    colIva;
    @FXML private TableColumn<LineaPresupuesto, String>    colSubtotal;
    @FXML private TableColumn<LineaPresupuesto, Void>      colEliminar;

    // ── Formulario nueva línea ────────────────────────────
    @FXML private TextField campoConcepto;
    @FXML private TextField campoCantidad;
    @FXML private TextField campoPrecio;
    @FXML private ComboBox<Double> comboIva;

    // ── Totales ───────────────────────────────────────────
    @FXML private Label lblBaseImponible;
    @FXML private Label lblTotalIva;
    @FXML private Label lblTotalFactura;

    @FXML private Label lblError;

    @Autowired private PresupuestoService presupuestoService;
    @Autowired private ClienteService     clienteService;
    @Autowired private ReparacionService  reparacionService;

    private Presupuesto presupuestoAEditar;
    private Runnable onGuardado;

    private final ObservableList<LineaPresupuesto> lineas = FXCollections.observableArrayList();

    // ── Inicialización ────────────────────────────────────

    @FXML
    public void initialize() {
        // Clientes
        comboCliente.getItems().setAll(clienteService.findAll());
        comboCliente.setConverter(new StringConverter<>() {
            @Override public String toString(Cliente c) {
                return c == null ? "" : c.getName() + " " + c.getSurname();
            }
            @Override public Cliente fromString(String s) { return null; }
        });

        // Al cambiar cliente, filtrar sus reparaciones
        comboCliente.setOnAction(e -> cargarReparacionesCliente());

        // Reparaciones (vacío hasta elegir cliente)
        comboReparacion.setConverter(new StringConverter<>() {
            @Override public String toString(Reparacion r) {
                return r == null ? "Sin reparación asociada" :
                        "#" + r.getId() + " — " + r.getDispositivo();
            }
            @Override public Reparacion fromString(String s) { return null; }
        });
        comboReparacion.getItems().add(null);

        // Estados
        comboEstado.getItems().setAll(Estado.values());
        comboEstado.setValue(Estado.BORRADOR);
        comboEstado.setConverter(new StringConverter<>() {
            @Override public String toString(Estado e) {
                if (e == null) return "";
                return switch (e) {
                    case BORRADOR -> "Borrador"; case ENVIADO -> "Enviado";
                    case ACEPTADO -> "Aceptado"; case RECHAZADO -> "Rechazado";
                    case FACTURADO -> "Facturado";
                };
            }
            @Override public Estado fromString(String s) { return null; }
        });

        // IVA predefinido
        comboIva.getItems().setAll(0.0, 4.0, 10.0, 21.0);
        comboIva.setValue(21.0);
        comboIva.setConverter(new StringConverter<>() {
            @Override public String toString(Double d) { return d == null ? "" : d.intValue() + "%"; }
            @Override public Double fromString(String s) { return null; }
        });

        // Tabla de líneas
        configurarTablaLineas();
        tablaLineas.setItems(lineas);
    }

    private void cargarReparacionesCliente() {
        Cliente c = comboCliente.getValue();
        comboReparacion.getItems().clear();
        comboReparacion.getItems().add(null);
        if (c != null) {
            comboReparacion.getItems().addAll(reparacionService.findByCliente(c.getId()));
        }
        comboReparacion.setValue(null);
    }

    public void setPresupuesto(Presupuesto p) {
        this.presupuestoAEditar = p;
        if (p != null) {
            lblNumero.setText(p.getNumero());
            lblFecha.setText(p.getFecha().toString());
            comboCliente.setValue(p.getCliente());
            cargarReparacionesCliente();
            comboReparacion.setValue(p.getReparacion());
            comboEstado.setValue(p.getEstado());
            campoNotas.setText(p.getNotas() != null ? p.getNotas() : "");
            lineas.setAll(p.getLineas());
            actualizarTotales();
        }
    }

    public void setOnGuardado(Runnable onGuardado) { this.onGuardado = onGuardado; }

    // ── Tabla de líneas ───────────────────────────────────

    private void configurarTablaLineas() {
        colConcepto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getConcepto()));
        colCantidad.setCellValueFactory(d -> new SimpleStringProperty(
                String.valueOf(d.getValue().getCantidad())));
        colPrecio.setCellValueFactory(d   -> new SimpleStringProperty(
                String.format("%.2f €", d.getValue().getPrecioUnitario())));
        colIva.setCellValueFactory(d      -> new SimpleStringProperty(
                (int) d.getValue().getIva() + "%"));
        colSubtotal.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%.2f €", d.getValue().getTotal())));

        colEliminar.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✕");
            { btn.setStyle("-fx-background-color:#ff475733;-fx-text-fill:#ff4757;" +
                    "-fx-background-radius:4px;-fx-cursor:hand;-fx-padding:3px 8px;");
              btn.setOnAction(e -> {
                  lineas.remove(getTableView().getItems().get(getIndex()));
                  actualizarTotales();
              });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    // ── Añadir línea ──────────────────────────────────────

    @FXML
    public void onAnadirLinea() {
        String concepto = campoConcepto.getText();
        String cantidadTxt = campoCantidad.getText();
        String precioTxt   = campoPrecio.getText();
        Double iva = comboIva.getValue();

        if (concepto == null || concepto.isBlank()) {
            mostrarError("El concepto es obligatorio"); return;
        }
        int cantidad;
        double precio;
        try { cantidad = Integer.parseInt(cantidadTxt.trim()); }
        catch (NumberFormatException e) { mostrarError("Cantidad no válida"); return; }
        try { precio = Double.parseDouble(precioTxt.trim().replace(",", ".")); }
        catch (NumberFormatException e) { mostrarError("Precio no válido"); return; }

        LineaPresupuesto linea = new LineaPresupuesto();
        linea.setConcepto(concepto.trim());
        linea.setCantidad(cantidad);
        linea.setPrecioUnitario(precio);
        linea.setIva(iva != null ? iva : 21.0);
        lineas.add(linea);

        // Limpiar campos
        campoConcepto.clear();
        campoCantidad.setText("1");
        campoPrecio.clear();
        lblError.setText("");
        actualizarTotales();
    }

    // ── Totales ───────────────────────────────────────────

    private void actualizarTotales() {
        double base  = lineas.stream().mapToDouble(LineaPresupuesto::getSubtotal).sum();
        double iva   = lineas.stream().mapToDouble(LineaPresupuesto::getImporteIva).sum();
        double total = base + iva;
        lblBaseImponible.setText(String.format("%.2f €", base));
        lblTotalIva.setText(String.format("%.2f €", iva));
        lblTotalFactura.setText(String.format("%.2f €", total));
    }

    // ── Guardar ───────────────────────────────────────────

    @FXML
    public void onGuardar() {
        Cliente cliente = comboCliente.getValue();
        if (cliente == null) { mostrarError("Debes seleccionar un cliente"); return; }
        if (lineas.isEmpty()) { mostrarError("Añade al menos una línea al presupuesto"); return; }

        try {
            List<Map<String, Object>> lineasData = new ArrayList<>();
            for (LineaPresupuesto l : lineas) {
                Map<String, Object> m = new HashMap<>();
                m.put("concepto",       l.getConcepto());
                m.put("cantidad",       l.getCantidad());
                m.put("precioUnitario", l.getPrecioUnitario());
                m.put("iva",            l.getIva());
                lineasData.add(m);
            }

            Reparacion rep = comboReparacion.getValue();
            Long repId = rep != null ? rep.getId() : null;
            String notas = campoNotas.getText();
            Estado estado = comboEstado.getValue();

            if (presupuestoAEditar == null) {
                presupuestoService.crear(cliente.getId(), repId, notas, lineasData);
            } else {
                presupuestoService.actualizar(presupuestoAEditar.getId(), notas, estado, lineasData);
            }

            if (onGuardado != null) onGuardado.run();
            cerrar();
        } catch (Exception e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML public void onCancelar() { cerrar(); }

    private void mostrarError(String msg) { lblError.setText(msg); }

    private void cerrar() {
        Stage stage = (Stage) comboCliente.getScene().getWindow();
        stage.close();
    }
}
