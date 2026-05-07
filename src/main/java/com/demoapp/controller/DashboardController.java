package com.demoapp.controller;

import com.demoapp.Navigation;
import com.demoapp.model.Reparacion;
import com.demoapp.model.Usuario;
import com.demoapp.service.ClienteService;
import com.demoapp.service.ReparacionService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import com.demoapp.util.AsyncTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador del panel principal (Dashboard) de FixLab.
 *
 * Gestiona:
 *  - La visualización del usuario y el reloj en tiempo real.
 *  - Los KPIs de actividad: órdenes activas, clientes y reparaciones del día.
 *  - La tabla con las reparaciones recientes, incluyendo badges de estado.
 *  - La navegación hacia otras secciones de la aplicación.
 *
 * Actúa como punto de entrada visual para el técnico o administrador,
 * mostrando un resumen rápido del estado general del sistema.
 */

@Controller
public class DashboardController {


    //TOPBAR: reloj en vivo + nombre del usuario logueado
    @FXML private Label lblReloj;
    @FXML private Label lblUsuario;

    // KPIs: números rápidos del dashboard
    @FXML private Label lblOrdenesActivas;
    @FXML private Label lblClientes;
    @FXML private Label lblReparacionesHoy;

    // TABLA: últimas reparaciones recientes
    @FXML private TableView<Reparacion>              tablaRecientes;
    @FXML private TableColumn<Reparacion, String>    colId;
    @FXML private TableColumn<Reparacion, String>    colCliente;
    @FXML private TableColumn<Reparacion, String>    colDispositivo;
    @FXML private TableColumn<Reparacion, String>    colEstado;

    // Servicios inyectados por Spring
    @Autowired private ClienteService clienteService;
    @Autowired private ReparacionService reparacionService;

    // Reloj en vivo
    private Timeline relojTimeline;

    // Usuario actual (se asigna desde Navigation)
    private Usuario usuario;

    // CICLO DE VIDA DEL CONTROLADOR
    // Se ejecuta automáticamente al cargar el FXML

    @FXML
    public void initialize() {
        iniciarReloj();
        configurarTabla();
        cargarDatos();
    }
    // Se llama desde Navigation al cambiar de pantalla
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        if (lblUsuario != null && usuario != null) {
            lblUsuario.setText(usuario.getUsername().toUpperCase());
        }
    }

    // RELOJ EN VIVO (Timeline actualiza cada segundo)

    private void iniciarReloj() {
        if (relojTimeline != null) relojTimeline.stop();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        relojTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e ->
                lblReloj.setText(LocalTime.now().format(formatter))
        ));
        relojTimeline.setCycleCount(Animation.INDEFINITE);
        relojTimeline.play();
    }

    // CARGA DE DATOS REALES PARA EL DASHBOARD
    // KPIs + últimas reparaciones activas

    private void cargarDatos() {
        AsyncTask.run(
            () -> {
                long activas = reparacionService.contarActivas();
                long clientes = clienteService.findAll().size();
                long hoy = reparacionService.contarHoy();
                List<Reparacion> recientes = reparacionService.findActivas().stream()
                        .sorted((a, b) -> b.getFechaEntrada().compareTo(a.getFechaEntrada()))
                        .limit(5).toList();
                return new Object[]{activas, clientes, hoy, recientes};
            },
            datos -> {
                lblOrdenesActivas.setText(String.valueOf(datos[0]));
                lblClientes.setText(String.valueOf(datos[1]));
                lblReparacionesHoy.setText(String.valueOf(datos[2]));
                @SuppressWarnings("unchecked")
                List<Reparacion> recientes = (List<Reparacion>) datos[3];
                tablaRecientes.setItems(FXCollections.observableArrayList(recientes));
            }
        );
    }

    // CONFIGURACIÓN DE LA TABLA
    // Ajustes de columnas + badges de estado

    private void configurarTabla() {
        // Ajuste automático de columnas
        // Columna ID fija
        colId.setMaxWidth(80);
        colId.setMinWidth(80);

        // Distribución proporcional del resto
        colCliente.prefWidthProperty().bind(
                tablaRecientes.widthProperty().subtract(80).multiply(0.30));
        colDispositivo.prefWidthProperty().bind(
                tablaRecientes.widthProperty().subtract(80).multiply(0.35));
        colEstado.prefWidthProperty().bind(
                tablaRecientes.widthProperty().subtract(80).multiply(0.35));

        // ID con formato "#123"
        colId.setCellValueFactory(data ->
                new SimpleStringProperty("#" + data.getValue().getId()));

        // Cliente: nombre + apellido
        colCliente.setCellValueFactory(data -> {
            var c = data.getValue().getCliente();
            return new SimpleStringProperty(c.getName() + " " + c.getSurname());
        });

        // Dispositivo
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
                    String etiqueta = switch (estado) {
                        case "EN_REPARACION"      -> "En reparación";
                        case "LISTO_PARA_RETIRAR" -> "Listo para retirar";
                        case "ESPERANDO_REPUESTO" -> "Esperando repuesto";
                        case "ENTREGADO"          -> "Entregado";
                        case "DIAGNOSTICO"        -> "Diagnóstico";
                        default                   -> estado;
                    };
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
    }

    // NAVEGACIÓN ENTRE PANTALLAS

    @FXML public void irDashboard(ActionEvent e)    {  }
    @FXML public void irClientes(ActionEvent e)     { Navigation.goTo("clientes.fxml"); }
    @FXML public void irReparaciones(ActionEvent e) { Navigation.goTo("reparaciones.fxml"); }
    @FXML public void irReparacionesLink(javafx.scene.input.MouseEvent e) {Navigation.goTo("reparaciones.fxml");}
    @FXML public void irPresupuestos(ActionEvent e) { Navigation.goTo("presupuestos.fxml"); }
    @FXML public void irAgenda(ActionEvent e) { Navigation.goTo("agenda_tecnico.fxml"); }

    @FXML
    public void onLogout(ActionEvent e) {

        //Crea un cuadro de dialogo.
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
                Platform.exit();
                System.exit(0);
            }
        });
    }
}