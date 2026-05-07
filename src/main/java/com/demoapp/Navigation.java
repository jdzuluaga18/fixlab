package com.demoapp;

import com.demoapp.controller.*;
import com.demoapp.model.Usuario;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;

public class Navigation {

    private static Stage stage;
    private static ApplicationContext springContext;
    private static Usuario usuarioActual;

    public static void setStage(Stage s)                      { stage = s; }
    public static void setSpringContext(ApplicationContext c)  { springContext = c; }
    public static void setUsuarioActual(Usuario u)            { usuarioActual = u; }
    public static Usuario getUsuarioActual()                  { return usuarioActual; }

    public static void goTo(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Navigation.class.getResource("/fxml/" + fxml));
            loader.setControllerFactory(springContext::getBean);

            Parent root = loader.load();

            // Propagar usuario al controlador destino
            propagarUsuario(loader.getController());

            Scene scene = stage.getScene();

            if (scene == null) {
                // Primera carga (solo ocurre en login)
                stage.setScene(new Scene(root,
                        stage.getWidth(), stage.getHeight()));
            } else {
                // ── Clave: cambiar SOLO el root de la Scene existente ──────
                // Esto evita el flash, mantiene el tamaño y conserva
                // el estado maximizado sin tener que restaurarlo.
                scene.setRoot(root);
            }

            stage.show();

        } catch (Exception e) {
            System.err.println("[Navigation] Error al navegar a: " + fxml);
            e.printStackTrace();
        }
    }

    private static void propagarUsuario(Object controller) {
        if (usuarioActual == null || controller == null) return;
        if      (controller instanceof DashboardController    dc) dc.setUsuario(usuarioActual);
        else if (controller instanceof ClientesController     cc) cc.setUsuario(usuarioActual);
        else if (controller instanceof ReparacionesController rc) rc.setUsuario(usuarioActual);
        else if (controller instanceof AgendaController       ac) ac.setUsuario(usuarioActual);
        else if (controller instanceof PresupuestosController pc) pc.setUsuario(usuarioActual);
    }
}
