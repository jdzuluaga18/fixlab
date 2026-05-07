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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
/**
 * Controlador de la vista de gestión de reparaciones.
 *
 * Gestiona:
 *  - La carga, filtrado y búsqueda de reparaciones.
 *  - La configuración de la tabla con badges de estado y acciones (editar/eliminar).
 *  - La apertura del formulario modal para crear o modificar reparaciones.
 *  - La actualización del contador total según los filtros aplicados.
 *  - La navegación entre pantallas y el cierre de sesión.
 *
 * Actúa como puente entre la interfaz JavaFX y ReparacionService,
 * permitiendo operaciones completas de administración de reparaciones.
 */

@Controller
public class ReparacionesController {

    // ── Topbar ────────────────────────────────────────────
    @FXML private Label lblReloj;
    @FXML private Label lblUsuario;

    // ── Filtros ───────────────────────────────────────────
    @FXML private TextField campoBusqueda;
    @FXML private ComboBox<Estado> filtroEstado;
    @FXML private Label lblTotal;

    // ── Tabla ─────────────────────────────────────────────
    @FXML private TableView<Reparacion>              tablaReparaciones;
    @FXML private TableColumn<Reparacion, String>    colId;
    @FXML private TableColumn<Reparacion, String>    colCliente;
    @FXML private TableColumn<Reparacion, String>    colDispositivo;
    @FXML private TableColumn<Reparacion, String>    colEstado;
    @FXML private TableColumn<Reparacion, String>    colFechaEntrada;
    @FXML private TableColumn<Reparacion, String>    colFechaSalida;
    @FXML private TableColumn<Reparacion, Void>      colAcciones;

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
        configurarTabla();
        cargarReparaciones();
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        if (lblUsuario != null && usuario != null) {
            lblUsuario.setText(usuario.getUsername().toUpperCase());
        }
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

        // Estado con badge de color
        colEstado.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEstado().name()));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    String etiqueta = traducirEstado(estado);
                    Label badge = new Label(etiqueta);
                    badge.getStyleClass().add("badge");
                    badge.getStyleClass().add(switch (estado) {
                        case "EN_REPARACION"      -> "badge-reparacion";
                        case "LISTO_PARA_RETIRAR" -> "badge-listo";
                        case "ESPERANDO_REPUESTO" -> "badge-espera";
                        case "ENTREGADO"          -> "badge-entregado";
                        case "DIAGNOSTICO"        -> "badge-diagnostico";
                        default                   -> "badge-entregado";
                    });
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        colFechaEntrada.setCellValueFactory(data -> {
            LocalDate fecha = data.getValue().getFechaEntrada();
            return new SimpleStringProperty(fecha != null ? fecha.format(DATE_FMT) : "-");
        });

        colFechaSalida.setCellValueFactory(data -> {
            LocalDate fecha = data.getValue().getFechaSalida();
            return new SimpleStringProperty(fecha != null ? fecha.format(DATE_FMT) : "-");
        });

        // Columna acciones
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar   = new Button("✏");
            private final Button btnEliminar = new Button("🗑");

            {
                btnEditar.setStyle(
                        "-fx-background-color: #21262d; -fx-text-fill: #58a6ff;" +
                                "-fx-background-radius: 4px; -fx-cursor: hand; -fx-padding: 4px 8px;");
                btnEliminar.setStyle(
                        "-fx-background-color: #21262d; -fx-text-fill: #ff4757;" +
                                "-fx-background-radius: 4px; -fx-cursor: hand; -fx-padding: 4px 8px;");

                btnEditar.setOnAction(e -> {
                    Reparacion r = getTableView().getItems().get(getIndex());
                    abrirFormulario(r);
                });
                btnEliminar.setOnAction(e -> {
                    Reparacion r = getTableView().getItems().get(getIndex());
                    confirmarEliminar(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(6, btnEditar, btnEliminar));
            }
        });
    }

    private void cargarReparaciones() {
        AsyncTask.run(
            () -> reparacionService.findAll(),
            lista -> {
                tablaReparaciones.setItems(FXCollections.observableArrayList(lista));
                actualizarContador(lista.size());
            }
        );
    }

    private void actualizarContador(int total) {
        lblTotal.setText(total + " reparación" + (total != 1 ? "es" : ""));
    }

    // ── Filtros ───────────────────────────────────────────

    @FXML
    public void onBuscar() {
        String texto = campoBusqueda.getText();
        List<Reparacion> resultados = reparacionService.buscarPorDispositivo(texto);
        tablaReparaciones.setItems(FXCollections.observableArrayList(resultados));
        actualizarContador(resultados.size());
    }

    @FXML
    public void onFiltrarEstado() {
        Estado estado = filtroEstado.getValue();
        if (estado == null) return;
        List<Reparacion> resultados = reparacionService.findByEstado(estado);
        tablaReparaciones.setItems(FXCollections.observableArrayList(resultados));
        actualizarContador(resultados.size());
    }

    @FXML
    public void onVerTodas() {
        filtroEstado.setValue(null);
        campoBusqueda.clear();
        cargarReparaciones();
    }

    // ── Formulario modal ──────────────────────────────────

    @FXML
    public void onNuevaReparacion() {
        abrirFormulario(null);
    }

    private void abrirFormulario(Reparacion reparacionAEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/formulario_reparacion.fxml"));
            loader.setControllerFactory(springContext::getBean);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle(reparacionAEditar == null ? "Nueva reparación" : "Editar reparación");
            modal.setResizable(false);

            Parent root = loader.load();

            FormularioReparacionController controller = loader.getController();
            controller.setReparacion(reparacionAEditar);
            controller.setOnGuardado(this::cargarReparaciones);

            modal.setScene(new Scene(root));
            modal.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void confirmarEliminar(Reparacion reparacion) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar reparación");
        alert.setHeaderText(null);
        alert.setContentText("¿Seguro que quieres eliminar la reparación de \"" +
                reparacion.getDispositivo() + "\"?");

        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/dashboard.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                reparacionService.eliminar(reparacion.getId());
                cargarReparaciones();
            }
        });
    }

    // ── Helper traducción de estados ──────────────────────

    private String traducirEstado(String estado) {
        return switch (estado) {
            case "EN_REPARACION"      -> "En reparación";
            case "LISTO_PARA_RETIRAR" -> "Listo para retirar";
            case "ESPERANDO_REPUESTO" -> "Esperando repuesto";
            case "ENTREGADO"          -> "Entregado";
            case "DIAGNOSTICO"        -> "Diagnóstico";
            default                   -> estado;
        };
    }

    // ── Navegación ────────────────────────────────────────

    @FXML public void irDashboard(ActionEvent e)    { Navigation.goTo("dashboard.fxml"); }
    @FXML public void irClientes(ActionEvent e)     { Navigation.goTo("clientes.fxml"); }
    @FXML public void irReparaciones(ActionEvent e) { /* ya estás */ }
    @FXML public void irPresupuestos(ActionEvent e) { Navigation.goTo("presupuestos.fxml"); }
    @FXML public void irAgenda(ActionEvent e)       { Navigation.goTo("agenda_tecnico.fxml");}

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
                javafx.application.Platform.exit();
                System.exit(0);
            }
        });
    }
}