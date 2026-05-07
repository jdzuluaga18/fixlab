package com.demoapp.controller;

import com.demoapp.model.Cliente;
import com.demoapp.service.ClienteService;
import com.demoapp.util.AsyncTask;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class FormularioClienteController {

    @FXML private Label     lblTitulo;
    @FXML private TextField campoNombre;
    @FXML private TextField campoApellidos;
    @FXML private TextField campoTelefono;
    @FXML private TextField campoEmail;
    @FXML private TextField campoDireccion;
    @FXML private TextArea  campoNotas;
    @FXML private Label     lblError;
    @FXML private Button    btnGuardar;

    @Autowired private ClienteService clienteService;

    private Cliente  clienteAEditar;
    private Runnable onGuardado;

    public void setCliente(Cliente cliente) {
        this.clienteAEditar = cliente;
        if (cliente != null) {
            lblTitulo.setText("Editar cliente");
            campoNombre.setText(cliente.getName());
            campoApellidos.setText(cliente.getSurname());
            campoTelefono.setText(cliente.getNumPhone());
            campoEmail.setText(cliente.getEmail() != null ? cliente.getEmail() : "");
            campoDireccion.setText(cliente.getDireccion() != null ? cliente.getDireccion() : "");
            campoNotas.setText(cliente.getNotas() != null ? cliente.getNotas() : "");
        }
    }

    public void setOnGuardado(Runnable cb) { this.onGuardado = cb; }

    @FXML
    public void onGuardar() {
        String nombre    = campoNombre.getText();
        String apellidos = campoApellidos.getText();
        String telefono  = campoTelefono.getText();
        String email     = campoEmail.getText();
        String direccion = campoDireccion.getText();
        String notas     = campoNotas.getText();

        if (nombre == null || nombre.isBlank())    { mostrarError("El nombre es obligatorio"); return; }
        if (apellidos == null || apellidos.isBlank()){ mostrarError("Los apellidos son obligatorios"); return; }
        if (telefono == null || telefono.isBlank()) { mostrarError("El teléfono es obligatorio"); return; }

        setBloqueado(true);

        AsyncTask.run(
            () -> {
                if (clienteAEditar == null)
                    clienteService.crear(nombre, apellidos, telefono, email, direccion, notas);
                else
                    clienteService.actualizar(clienteAEditar.getId(), nombre, apellidos,
                            telefono, email, direccion, notas);
            },
            () -> {
                if (onGuardado != null) onGuardado.run();
                cerrar();
            },
            error -> {
                setBloqueado(false);
                mostrarError(error);
            }
        );
    }

    @FXML public void onCancelar() { cerrar(); }

    private void mostrarError(String msg)   { lblError.setText(msg); }
    private void setBloqueado(boolean b)    { if (btnGuardar != null) btnGuardar.setDisable(b); }
    private void cerrar() {
        Stage stage = (Stage) campoNombre.getScene().getWindow();
        stage.close();
    }
}
