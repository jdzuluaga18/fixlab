package com.demoapp.controller;

import com.demoapp.Navigation;
import com.demoapp.model.Usuario;
import com.demoapp.service.UsuarioService;
import javafx.fxml.FXML;

import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
/**
 * Controlador de la pantalla de inicio de sesión.
 *
 * Gestiona:
 *  - La validación de credenciales introducidas por el usuario.
 *  - La comunicación con UsuarioService para autenticar.
 *  - El manejo de errores cuando el login falla.
 *  - El inicio de sesión y la propagación del usuario mediante Navigation.
 *  - El acceso a la pantalla de registro.
 *
 * Es la puerta de entrada a la aplicación y define el flujo inicial
 * antes de cargar el dashboard.
 */

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ApplicationContext springContext;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    public void onLogin() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        usuarioService.login(user, pass).ifPresentOrElse(
                this::iniciarSesion,
                () -> errorLabel.setText("Usuario o contraseña incorrecta")
        );
    }

    @FXML
    public void onRegister() {
        Navigation.goTo("register.fxml");
    }


    private void iniciarSesion(Usuario usuario) {
        // Guardar el usuario en sesión para que Navigation lo propague
        Navigation.setUsuarioActual(usuario);
        Navigation.goTo("dashboard.fxml");
    }

}