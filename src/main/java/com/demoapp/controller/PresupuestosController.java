package com.demoapp.controller;

import com.demoapp.Navigation;
import com.demoapp.model.Presupuesto;
import com.demoapp.model.Presupuesto.Estado;
import com.demoapp.model.Usuario;
import com.demoapp.service.PresupuestoService;
import javafx.animation.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import com.demoapp.util.AsyncTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class PresupuestosController {

    @FXML private Label lblReloj;
    @FXML private Label lblUsuario;
    @FXML private ComboBox<Estado> filtroEstado;
    @FXML private Label lblTotal;

    @FXML private TableView<Presupuesto>              tablaPresupuestos;
    @FXML private TableColumn<Presupuesto, String>    colNumero;
    @FXML private TableColumn<Presupuesto, String>    colCliente;
    @FXML private TableColumn<Presupuesto, String>    colFecha;
    @FXML private TableColumn<Presupuesto, String>    colEstado;
    @FXML private TableColumn<Presupuesto, String>    colBase;
    @FXML private TableColumn<Presupuesto, String>    colIva;
    @FXML private TableColumn<Presupuesto, String>    colTotal;
    @FXML private TableColumn<Presupuesto, Void>      colAcciones;

    @Autowired private PresupuestoService presupuestoService;
    @Autowired private ApplicationContext springContext;

    private Timeline relojTimeline;
    private Usuario usuarioActual;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        iniciarReloj();
        filtroEstado.getItems().setAll(Estado.values());
        filtroEstado.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Estado e) { return e == null ? "" : traducir(e); }
            @Override public Estado fromString(String s) { return null; }
        });
        configurarTabla();
        cargarPresupuestos();
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        if (lblUsuario != null && usuario != null)
            lblUsuario.setText(usuario.getUsername().toUpperCase());
    }

    private void iniciarReloj() {
        if (relojTimeline != null) relojTimeline.stop();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        relojTimeline = new Timeline(new KeyFrame(Duration.seconds(1),
                e -> lblReloj.setText(LocalTime.now().format(fmt))));
        relojTimeline.setCycleCount(Animation.INDEFINITE);
        relojTimeline.play();
    }

    // ── Tabla ─────────────────────────────────────────────

    private void configurarTabla() {
        colNumero.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue().getNumero()));
        colCliente.setCellValueFactory(d -> {
            var c = d.getValue().getCliente();
            return new SimpleStringProperty(c.getName() + " " + c.getSurname());
        });
        colFecha.setCellValueFactory(d   -> new SimpleStringProperty(
                d.getValue().getFecha().format(DATE_FMT)));
        colBase.setCellValueFactory(d    -> new SimpleStringProperty(
                String.format("%.2f €", d.getValue().getBaseImponible())));
        colIva.setCellValueFactory(d     -> new SimpleStringProperty(
                String.format("%.2f €", d.getValue().getTotalIva())));
        colTotal.setCellValueFactory(d   -> new SimpleStringProperty(
                String.format("%.2f €", d.getValue().getTotalFactura())));

        // Badge de estado
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().name()));
        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setGraphic(null); return; }
                Label badge = new Label(traducir(Estado.valueOf(estado)));
                badge.getStyleClass().addAll("badge", badgeClass(estado));
                setGraphic(badge); setText(null);
            }
        });

        // Acciones
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnVer      = new Button("👁");
            private final Button btnEliminar = new Button("🗑");
            {
                btnVer.setStyle("-fx-background-color:#21262d;-fx-text-fill:#58a6ff;" +
                        "-fx-background-radius:4px;-fx-cursor:hand;-fx-padding:4px 8px;");
                btnEliminar.setStyle("-fx-background-color:#21262d;-fx-text-fill:#ff4757;" +
                        "-fx-background-radius:4px;-fx-cursor:hand;-fx-padding:4px 8px;");
                btnVer.setOnAction(e -> {
                    Presupuesto p = getTableView().getItems().get(getIndex());
                    abrirFormulario(p);
                });
                btnEliminar.setOnAction(e -> {
                    Presupuesto p = getTableView().getItems().get(getIndex());
                    confirmarEliminar(p);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(6, btnVer, btnEliminar));
            }
        });
    }

    private void cargarPresupuestos() {
        AsyncTask.run(
            () -> presupuestoService.findAll(),
            lista -> {
                tablaPresupuestos.setItems(FXCollections.observableArrayList(lista));
                lblTotal.setText(lista.size() + " presupuesto" + (lista.size() != 1 ? "s" : ""));
            }
        );
    }

    @FXML public void onFiltrarEstado() {
        Estado e = filtroEstado.getValue();
        if (e == null) return;
        List<Presupuesto> res = presupuestoService.findByEstado(e);
        tablaPresupuestos.setItems(FXCollections.observableArrayList(res));
        lblTotal.setText(res.size() + " presupuesto" + (res.size() != 1 ? "s" : ""));
    }

    @FXML public void onVerTodos() {
        filtroEstado.setValue(null);
        cargarPresupuestos();
    }

    // ── Formulario ────────────────────────────────────────

    @FXML public void onNuevoPresupuesto() { abrirFormulario(null); }

    private void abrirFormulario(Presupuesto presupuesto) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/formulario_presupuesto.fxml"));
            loader.setControllerFactory(springContext::getBean);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle(presupuesto == null ? "Nuevo presupuesto" : "Presupuesto " + presupuesto.getNumero());
            modal.setResizable(true);
            modal.setMinWidth(720);
            modal.setMinHeight(600);

            Parent root = loader.load();
            FormularioPresupuestoController ctrl = loader.getController();
            ctrl.setPresupuesto(presupuesto);
            ctrl.setOnGuardado(this::cargarPresupuestos);

            modal.setScene(new Scene(root));
            modal.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void confirmarEliminar(Presupuesto p) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar presupuesto");
        alert.setHeaderText(null);
        alert.setContentText("¿Eliminar el presupuesto " + p.getNumero() + "?");
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/dashboard.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                presupuestoService.eliminar(p.getId());
                cargarPresupuestos();
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────

    private String traducir(Estado e) {
        return switch (e) {
            case BORRADOR   -> "Borrador";
            case ENVIADO    -> "Enviado";
            case ACEPTADO   -> "Aceptado";
            case RECHAZADO  -> "Rechazado";
            case FACTURADO  -> "Facturado";
        };
    }

    private String badgeClass(String e) {
        return switch (e) {
            case "BORRADOR"  -> "badge-diagnostico";
            case "ENVIADO"   -> "badge-reparacion";
            case "ACEPTADO"  -> "badge-listo";
            case "RECHAZADO" -> "badge-entregado";
            case "FACTURADO" -> "badge-espera";
            default          -> "badge-entregado";
        };
    }

    // ── Navegación ────────────────────────────────────────

    @FXML public void irDashboard(ActionEvent e)      { Navigation.goTo("dashboard.fxml"); }
    @FXML public void irClientes(ActionEvent e)       { Navigation.goTo("clientes.fxml"); }
    @FXML public void irReparaciones(ActionEvent e)   { Navigation.goTo("reparaciones.fxml"); }
    @FXML public void irAgenda(ActionEvent e)         { Navigation.goTo("agenda_tecnico.fxml"); }
    @FXML public void irPresupuestos(ActionEvent e)   { /* ya estás */ }

    @FXML public void onLogout(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar sesión");
        alert.setHeaderText(null);
        alert.setContentText("¿Estás seguro de que quieres salir?");
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/dashboard.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) { javafx.application.Platform.exit(); System.exit(0); }
        });
    }
}
