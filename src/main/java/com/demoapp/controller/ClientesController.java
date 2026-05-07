package com.demoapp.controller;

import com.demoapp.Navigation;
import com.demoapp.model.Cliente;
import com.demoapp.model.Usuario;
import com.demoapp.service.ClienteService;
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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class ClientesController {

    @FXML private Label     lblReloj;
    @FXML private Label     lblUsuario;
    @FXML private TextField campoBusqueda;
    @FXML private Label     lblTotal;

    @FXML private TableView<Cliente>           tablaClientes;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colApellidos;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private TableColumn<Cliente, String> colDireccion;
    @FXML private TableColumn<Cliente, Void>   colAcciones;

    @Autowired private ClienteService    clienteService;
    @Autowired private ApplicationContext springContext;

    private Timeline relojTimeline;
    private Usuario usuarioActual;

    @FXML
    public void initialize() {
        iniciarReloj();
        configurarTabla();
        cargarClientes();
    }

    public void setUsuario(Usuario usuario) {
        this.usuarioActual = usuario;
        if (lblUsuario != null && usuario != null)
            lblUsuario.setText(usuario.getUsername().toUpperCase());
    }

    // ── Reloj ─────────────────────────────────────────────

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
        colNombre.setCellValueFactory(d    -> new SimpleStringProperty(d.getValue().getName()));
        colApellidos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSurname()));
        colTelefono.setCellValueFactory(d  -> new SimpleStringProperty(d.getValue().getNumPhone()));
        colEmail.setCellValueFactory(d     -> new SimpleStringProperty(
                d.getValue().getEmail() != null ? d.getValue().getEmail() : "-"));
        colDireccion.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDireccion() != null ? d.getValue().getDireccion() : "-"));

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar   = new Button("✏");
            private final Button btnEliminar = new Button("🗑");
            {
                btnEditar.setStyle("-fx-background-color:#21262d;-fx-text-fill:#58a6ff;" +
                        "-fx-background-radius:4px;-fx-cursor:hand;-fx-padding:4px 8px;");
                btnEliminar.setStyle("-fx-background-color:#21262d;-fx-text-fill:#ff4757;" +
                        "-fx-background-radius:4px;-fx-cursor:hand;-fx-padding:4px 8px;");
                btnEditar.setOnAction(e   -> abrirFormulario(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> confirmarEliminar(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : new HBox(6, btnEditar, btnEliminar));
            }
        });
    }

    private void cargarClientes() {
        AsyncTask.run(
            () -> clienteService.findAll(),
            lista -> {
                tablaClientes.setItems(FXCollections.observableArrayList(lista));
                actualizarContador(lista.size());
            }
        );
    }

    private void actualizarContador(int total) {
        lblTotal.setText(total + " cliente" + (total != 1 ? "s" : ""));
    }

    // ── Búsqueda potente ──────────────────────────────────
    // Busca en tiempo real por: nombre, apellidos, teléfono, email, dirección y notas

    @FXML
    public void onBuscar() {
        String texto = campoBusqueda.getText();
        List<Cliente> resultados = clienteService.buscar(texto);
        tablaClientes.setItems(FXCollections.observableArrayList(resultados));
        actualizarContador(resultados.size());

        // Indicador visual: borde azul si hay filtro activo
        if (texto != null && !texto.isBlank()) {
            campoBusqueda.setStyle(campoBusqueda.getStyle() +
                    "-fx-border-color: #58a6ff;");
        } else {
            campoBusqueda.setStyle(campoBusqueda.getStyle()
                    .replace("-fx-border-color: #58a6ff;", ""));
        }
    }

    // ── Formulario modal ──────────────────────────────────

    @FXML public void onNuevoCliente() { abrirFormulario(null); }

    private void abrirFormulario(Cliente clienteAEditar) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/formulario_cliente.fxml"));
            loader.setControllerFactory(springContext::getBean);

            Stage modal = new Stage();
            modal.initModality(Modality.APPLICATION_MODAL);
            modal.setTitle(clienteAEditar == null ? "Nuevo cliente" : "Editar cliente");
            modal.setResizable(false);

            Parent root = loader.load();
            FormularioClienteController ctrl = loader.getController();
            ctrl.setCliente(clienteAEditar);
            ctrl.setOnGuardado(this::cargarClientes);

            modal.setScene(new Scene(root));
            modal.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void confirmarEliminar(Cliente cliente) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar cliente");
        alert.setHeaderText(null);
        alert.setContentText("¿Seguro que quieres eliminar a " +
                cliente.getName() + " " + cliente.getSurname() + "?");
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/dashboard.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                clienteService.eliminar(cliente.getId());
                cargarClientes();
            }
        });
    }

    // ── Navegación ────────────────────────────────────────

    @FXML public void irDashboard(ActionEvent e)    { Navigation.goTo("dashboard.fxml"); }
    @FXML public void irClientes(ActionEvent e)     { /* ya estás */ }
    @FXML public void irReparaciones(ActionEvent e) { Navigation.goTo("reparaciones.fxml"); }
    @FXML public void irPresupuestos(ActionEvent e) { Navigation.goTo("presupuestos.fxml"); }
    @FXML public void irAgenda(ActionEvent e)       { Navigation.goTo("agenda_tecnico.fxml"); }

    @FXML
    public void onLogout(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cerrar sesión");
        alert.setHeaderText(null);
        alert.setContentText("¿Estás seguro de que quieres salir?");
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/css/dashboard.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("dialog-pane");
        alert.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                javafx.application.Platform.exit();
                System.exit(0);
            }
        });
    }
}
