package com.demoapp.controller;

import com.demoapp.Navigation;
import com.demoapp.model.Reparacion;
import com.demoapp.model.Reparacion.Estado;
import com.demoapp.model.Usuario;
import com.demoapp.service.ReparacionService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import javafx.util.StringConverter;
import com.demoapp.util.AsyncTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
/**
 * Controlador de la vista Agenda del técnico.
 *
 * Gestiona:
 *  - La carga de reparaciones asignadas al técnico actual.
 *  - Los filtros por estado y la actualización del contador.
 *  - La tabla con badges de estado y cambio de estado en línea.
 *  - El reloj en tiempo real y la visualización del usuario.
 *  - La navegación entre pantallas y el cierre de sesión.
 *
 * Es el puente entre la interfaz JavaFX y la lógica de negocio
 * proporcionada por ReparacionService.
 */

@Controller
public class AgendaController {

    // ── Topbar ────────────────────────────────────────────
    @FXML private Label lblReloj;
    @FXML private Label lblUsuario;
    @FXML private Label lblTituloAgenda;

    // ── Filtros ───────────────────────────────────────────
    @FXML private ComboBox<Estado> filtroEstado;
    @FXML private Label lblTotal;

    // ── Tabla ─────────────────────────────────────────────
    @FXML private TableView<Reparacion>           tablaAgenda;
    @FXML private TableColumn<Reparacion, String> colId;
    @FXML private TableColumn<Reparacion, String> colCliente;
    @FXML private TableColumn<Reparacion, String> colDispositivo;
    @FXML private TableColumn<Reparacion, String> colDescripcion;
    @FXML private TableColumn<Reparacion, String> colEstado;
    @FXML private TableColumn<Reparacion, String> colFecha;
    @FXML private TableColumn<Reparacion, Void>   colCambiarEstado;

    @Autowired private ReparacionService reparacionService;
    @Autowired private ApplicationContext springContext;

    private Timeline relojTimeline;
    private Usuario usuarioActual;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ── Ciclo de vida ─────────────────────────────────────

    @FXML
    public void initialize() {
        iniciarReloj();
        filtroEstado.getItems().setAll(Estado.values());
        filtroEstado.setConverter(new StringConverter<>() {
            @Override public String toString(Estado e) { return e == null ? "" : traducir(e); }
            @Override public Estado fromString(String s) { return null; }
        });
        configurarTabla();
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        if (lblUsuario != null && usuario != null) {
            lblUsuario.setText(usuario.getUsername().toUpperCase());
        }
        // Cargar datos una vez que tenemos el usuario
        cargarAgenda();
    }

    // ── Reloj ─────────────────────────────────────────────

    private void iniciarReloj() {
        if (relojTimeline != null) relojTimeline.stop();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        relojTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e ->
                lblReloj.setText(java.time.LocalTime.now().format(formatter))
        ));
        relojTimeline.setCycleCount(Animation.INDEFINITE);
        relojTimeline.play();
    }

    // ── Carga de datos ────────────────────────────────────

    private void cargarAgenda() {
        if (usuarioActual == null) return;
        List<Reparacion> reparaciones = reparacionService.findAgendaTecnico(usuarioActual);
        tablaAgenda.setItems(FXCollections.observableArrayList(reparaciones));
        actualizarContador(reparaciones.size());
    }

    private void actualizarContador(int total) {
        lblTotal.setText(total + " reparación" + (total != 1 ? "es" : "") + " activa" + (total != 1 ? "s" : ""));
    }

    // ── Tabla ─────────────────────────────────────────────

    private void configurarTabla() {
        colId.setCellValueFactory(data ->
                new SimpleStringProperty("#" + data.getValue().getId()));

        colCliente.setCellValueFactory(data -> {
            var c = data.getValue().getCliente();
            return new SimpleStringProperty(c.getName() + " " + c.getSurname());
        });

        colDispositivo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDispositivo()));

        colDescripcion.setCellValueFactory(data -> {
            String desc = data.getValue().getDescripcion();
            return new SimpleStringProperty(desc != null ? desc : "-");
        });

        colFecha.setCellValueFactory(data -> {
            LocalDate fecha = data.getValue().getFechaEntrada();
            return new SimpleStringProperty(fecha != null ? fecha.format(DATE_FMT) : "-");
        });

        // Badge de estado
        colEstado.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEstado().name()));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) { setGraphic(null); return; }
                Label badge = new Label(traducir(Estado.valueOf(estado)));
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add(badgeClass(estado));
                setGraphic(badge);
                setText(null);
            }
        });

        // Columna cambiar estado — ComboBox directo en la fila
        colCambiarEstado.setCellFactory(col -> new TableCell<>() {
            private final ComboBox<Estado> combo = new ComboBox<>();

            {
                combo.getItems().setAll(Estado.values());
                combo.setConverter(new StringConverter<>() {
                    @Override public String toString(Estado e) { return e == null ? "" : traducir(e); }
                    @Override public Estado fromString(String s) { return null; }
                });
                combo.setStyle("-fx-background-color: #21262d; -fx-text-fill: #c9d1d9;" +
                        "-fx-border-color: #21262d; -fx-background-radius: 4px;");

                combo.setOnAction(e -> {
                    Estado nuevoEstado = combo.getValue();
                    if (nuevoEstado == null) return;
                    Reparacion r = getTableView().getItems().get(getIndex());
                    if (nuevoEstado == r.getEstado()) return;
                    reparacionService.cambiarEstado(r.getId(), nuevoEstado);
                    cargarAgenda(); // recargar para reflejar cambios
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Reparacion r = getTableView().getItems().get(getIndex());
                    combo.setValue(r.getEstado());
                    setGraphic(combo);
                }
            }
        });
    }

    // ── Filtros ───────────────────────────────────────────

    @FXML
    public void onFiltrarEstado() {
        if (usuarioActual == null) return;
        Estado estado = filtroEstado.getValue();
        if (estado == null) return;
        List<Reparacion> resultados = reparacionService.findAgendaTecnico(usuarioActual)
                .stream().filter(r -> r.getEstado() == estado).toList();
        tablaAgenda.setItems(FXCollections.observableArrayList(resultados));
        actualizarContador(resultados.size());
    }

    @FXML
    public void onVerTodas() {
        filtroEstado.setValue(null);
        cargarAgenda();
    }

    // ── Helpers ───────────────────────────────────────────

    private String traducir(Estado e) {
        return switch (e) {
            case EN_REPARACION      -> "En reparación";
            case LISTO_PARA_RETIRAR -> "Listo para retirar";
            case ESPERANDO_REPUESTO -> "Esperando repuesto";
            case ENTREGADO          -> "Entregado";
            case DIAGNOSTICO        -> "Diagnóstico";
        };
    }

    private String badgeClass(String estado) {
        return switch (estado) {
            case "EN_REPARACION"      -> "badge-reparacion";
            case "LISTO_PARA_RETIRAR" -> "badge-listo";
            case "ESPERANDO_REPUESTO" -> "badge-espera";
            case "ENTREGADO"          -> "badge-entregado";
            case "DIAGNOSTICO"        -> "badge-diagnostico";
            default                   -> "badge-entregado";
        };
    }

    // ── Navegación ────────────────────────────────────────

    @FXML public void irDashboard(ActionEvent e)    { Navigation.goTo("dashboard.fxml"); }
    @FXML public void irClientes(ActionEvent e)     { Navigation.goTo("clientes.fxml"); }
    @FXML public void irReparaciones(ActionEvent e) { Navigation.goTo("reparaciones.fxml"); }
    @FXML public void irPresupuestos(ActionEvent e) { Navigation.goTo("presupuestos.fxml"); }
    @FXML public void irAgenda(ActionEvent e)       { /* ya estás */ }

    @FXML
    public void onLogout(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar sesión");
        alert.setHeaderText(null);
        alert.setContentText("¿Estás seguro de que quieres salir?");

        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/dashboard.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Navigation.setUsuarioActual(null);
                javafx.application.Platform.exit();
                System.exit(0);
            }
        });
    }

    public void onNuevaCita(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/formulario_cita.fxml"));
            loader.setControllerFactory(springContext::getBean);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle("Nueva cita");
            modal.setResizable(false);

            Parent root = loader.load();
            modal.setScene(new Scene(root));
            modal.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}