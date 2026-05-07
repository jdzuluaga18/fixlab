package com.demoapp.controller;

import com.demoapp.Navigation;
import com.demoapp.model.Usuario;
import com.demoapp.service.UsuarioService;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class RegisterController {

    @FXML private TextField             usernameField;
    @FXML private PasswordField         passwordField;
    @FXML private PasswordField         confirmPasswordField;
    @FXML private ComboBox<Usuario.Rol> rolComboBox;
    @FXML private Label                 errorLabel;

    @Autowired
    private UsuarioService userService;

    @FXML
    public void initialize() {
        rolComboBox.getItems().setAll(Usuario.Rol.values());
        rolComboBox.setValue(Usuario.Rol.ADMIN);
    }

    @FXML
    public void onRegister() {
        String username = usernameField.getText();
        String pass     = passwordField.getText();
        String confirm  = confirmPasswordField.getText();
        Usuario.Rol rol = rolComboBox.getValue();

        if (username == null || username.isBlank()) {
            mostrarError("El usuario es obligatorio"); return;
        }
        if (pass == null || pass.isBlank()) {
            mostrarError("La contraseña es obligatoria"); return;
        }
        if (!pass.equals(confirm)) {
            mostrarError("Las contraseñas no coinciden"); return;
        }
        if (rol == null) {
            mostrarError("Selecciona un rol"); return;
        }

        try {
            userService.register(username, pass, rol);
            userService.login(username, pass).ifPresentOrElse(
                    this::iniciarSesion,
                    () -> mostrarError("Error al iniciar sesión automáticamente")
            );
        } catch (Exception e) {
            mostrarError("Error: " + e.getMessage());
        }
    }

    @FXML
    public void onBackToLogin() {
        Navigation.goTo("login.fxml");
    }

    private void iniciarSesion(Usuario usuario) {
        Navigation.setUsuarioActual(usuario);
        Navigation.goTo("dashboard.fxml");
    }

    private void mostrarError(String mensaje) {
        errorLabel.setText(mensaje);
    }
}
