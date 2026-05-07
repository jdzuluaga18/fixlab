package com.demoapp.controller;

import com.demoapp.model.*;
import com.demoapp.model.Reparacion.Estado;
import com.demoapp.service.ClienteService;
import com.demoapp.service.ReparacionService;
import com.demoapp.service.UsuarioService;
import com.demoapp.util.AsyncTask;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class FormularioReparacionController {

    @FXML private Label             lblTitulo;
    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private TextField         campoDispositivo;
    @FXML private TextArea          campoDescripcion;
    @FXML private ComboBox<Estado>  comboEstado;
    @FXML private ComboBox<Usuario> comboTecnico;
    @FXML private Label             lblError;
    @FXML private Button            btnGuardar;

    @Autowired private ReparacionService reparacionService;
    @Autowired private ClienteService    clienteService;
    @Autowired private UsuarioService    usuarioService;

    private Reparacion reparacionAEditar;
    private Runnable   onGuardado;

    @FXML
    public void initialize() {
        AsyncTask.run(
            () -> {
                List<Cliente> clientes = clienteService.findAll();
                List<Usuario> tecnicos = usuarioService.findAll();
                javafx.application.Platform.runLater(() -> {
                    comboCliente.getItems().setAll(clientes);
                    comboTecnico.getItems().add(null);
                    comboTecnico.getItems().addAll(tecnicos);
                });
            },
            () -> {},
            e -> {}
        );

        comboCliente.setConverter(new StringConverter<>() {
            @Override public String toString(Cliente c) { return c == null ? "" : c.getName() + " " + c.getSurname(); }
            @Override public Cliente fromString(String s) { return null; }
        });
        comboEstado.getItems().setAll(Estado.values());
        comboEstado.setConverter(new StringConverter<>() {
            @Override public String toString(Estado e) {
                if (e == null) return "";
                return switch (e) {
                    case EN_REPARACION      -> "En reparación";
                    case LISTO_PARA_RETIRAR -> "Listo para retirar";
                    case ESPERANDO_REPUESTO -> "Esperando repuesto";
                    case ENTREGADO          -> "Entregado";
                    case DIAGNOSTICO        -> "Diagnóstico";
                };
            }
            @Override public Estado fromString(String s) { return null; }
        });
        comboEstado.setValue(Estado.DIAGNOSTICO);
        comboTecnico.setConverter(new StringConverter<>() {
            @Override public String toString(Usuario u) { return u == null ? "Sin asignar" : u.getUsername(); }
            @Override public Usuario fromString(String s) { return null; }
        });
    }

    public void setReparacion(Reparacion r) {
        this.reparacionAEditar = r;
        if (r != null) {
            lblTitulo.setText("Editar reparación");
            comboCliente.setValue(r.getCliente());
            comboCliente.setDisable(true);
            campoDispositivo.setText(r.getDispositivo());
            campoDescripcion.setText(r.getDescripcion() != null ? r.getDescripcion() : "");
            comboEstado.setValue(r.getEstado());
            comboTecnico.setValue(r.getTecnico());
        }
    }

    public void setOnGuardado(Runnable cb) { this.onGuardado = cb; }

    @FXML
    public void onGuardar() {
        Cliente cliente    = comboCliente.getValue();
        String dispositivo = campoDispositivo.getText();
        String descripcion = campoDescripcion.getText();
        Estado estado      = comboEstado.getValue();
        Usuario tecnico    = comboTecnico.getValue();

        if (cliente == null)                        { mostrarError("Selecciona un cliente"); return; }
        if (dispositivo == null || dispositivo.isBlank()) { mostrarError("El dispositivo es obligatorio"); return; }
        if (estado == null)                         { mostrarError("Selecciona un estado"); return; }

        setBloqueado(true);
        Long tecnicoId = tecnico != null ? tecnico.getId() : null;

        AsyncTask.run(
            () -> {
                if (reparacionAEditar == null)
                    reparacionService.crear(cliente.getId(), dispositivo, descripcion, estado, tecnicoId);
                else
                    reparacionService.actualizar(reparacionAEditar.getId(), dispositivo, descripcion, estado, tecnicoId);
            },
            () -> { if (onGuardado != null) onGuardado.run(); cerrar(); },
            error -> { setBloqueado(false); mostrarError(error); }
        );
    }

    @FXML public void onCancelar() { cerrar(); }

    private void mostrarError(String msg) { lblError.setText(msg); }
    private void setBloqueado(boolean b)  { if (btnGuardar != null) btnGuardar.setDisable(b); }
    private void cerrar() {
        Stage stage = (Stage) campoDispositivo.getScene().getWindow();
        stage.close();
    }
}
