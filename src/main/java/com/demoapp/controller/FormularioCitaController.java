package com.demoapp.controller;

import com.demoapp.model.Cita;
import com.demoapp.model.Cliente;
import com.demoapp.service.CitaService;
import com.demoapp.service.ClienteService;
import com.demoapp.util.AsyncTask;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

@Controller
public class FormularioCitaController {

    @FXML private Label             lblTitulo;
    @FXML private DatePicker        campoFecha;
    @FXML private TextField         campoHora;
    @FXML private TextArea          campoDescripcion;
    @FXML private ComboBox<Cliente> comboCliente;
    @FXML private Label             lblError;
    @FXML private Button            btnGuardar;

    @Autowired private CitaService    citaService;
    @Autowired private ClienteService clienteService;

    private Cita     citaAEditar;
    private Runnable onGuardado;

    @FXML
    public void initialize() {
        AsyncTask.run(
            () -> {
                var clientes = clienteService.findAll();
                javafx.application.Platform.runLater(() -> {
                    comboCliente.getItems().add(null);
                    comboCliente.getItems().addAll(clientes);
                });
            },
            () -> {},
            e -> {}
        );

        comboCliente.setConverter(new StringConverter<>() {
            @Override public String toString(Cliente c) { return c == null ? "Sin cliente" : c.getName() + " " + c.getSurname(); }
            @Override public Cliente fromString(String s) { return null; }
        });
        campoFecha.setValue(LocalDate.now());
    }

    public void setCita(Cita cita) {
        this.citaAEditar = cita;
        if (cita != null) {
            lblTitulo.setText("Editar cita");
            campoFecha.setValue(cita.getFecha());
            campoHora.setText(cita.getHora().toString());
            campoDescripcion.setText(cita.getDescripcion());
            comboCliente.setValue(cita.getCliente());
        }
    }

    public void setOnGuardado(Runnable cb) { this.onGuardado = cb; }

    @FXML
    public void onGuardar() {
        LocalDate fecha    = campoFecha.getValue();
        String horaTexto   = campoHora.getText();
        String descripcion = campoDescripcion.getText();
        Cliente cliente    = comboCliente.getValue();

        if (descripcion == null || descripcion.isBlank()) { mostrarError("La descripción es obligatoria"); return; }

        LocalTime hora;
        try { hora = LocalTime.parse(horaTexto); }
        catch (DateTimeParseException e) { mostrarError("Formato de hora incorrecto (ej: 09:30)"); return; }

        setBloqueado(true);
        Long clienteId = cliente != null ? cliente.getId() : null;

        AsyncTask.run(
            () -> {
                if (citaAEditar == null)
                    citaService.crear(fecha, hora, descripcion, clienteId);
                else
                    citaService.actualizar(citaAEditar.getId(), fecha, hora, descripcion, clienteId);
            },
            () -> { if (onGuardado != null) onGuardado.run(); cerrar(); },
            error -> { setBloqueado(false); mostrarError(error); }
        );
    }

    @FXML public void onCancelar() { cerrar(); }

    private void mostrarError(String msg) { lblError.setText(msg); }
    private void setBloqueado(boolean b)  { if (btnGuardar != null) btnGuardar.setDisable(b); }
    private void cerrar() {
        Stage stage = (Stage) campoHora.getScene().getWindow();
        stage.close();
    }
}
