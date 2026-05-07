package com.demoapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        new java.io.File(System.getProperty("user.home") + "/.fixlab").mkdirs();
        springContext = new SpringApplicationBuilder(DemoApplication.class)
                .headless(false)
                .run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        Navigation.setStage(stage);
        Navigation.setSpringContext(springContext);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        loader.setControllerFactory(springContext::getBean);

        Scene scene = new Scene(loader.load(), 1100, 700);
        stage.setScene(scene);
        stage.setTitle("FixLab — Gestión de Reparaciones");
        stage.setMinWidth(900);
        stage.setMinHeight(620);

        // Arrancar maximizado en pantallas grandes, centrado en pequeñas
        javafx.geometry.Rectangle2D screen =
                javafx.stage.Screen.getPrimary().getVisualBounds();
        if (screen.getWidth() >= 1280) {
            stage.setMaximized(true);
        } else {
            stage.setWidth(Math.min(1100, screen.getWidth()));
            stage.setHeight(Math.min(700, screen.getHeight()));
            stage.centerOnScreen();
        }

        stage.show();
    }

    @Override
    public void stop() {
        springContext.close();
    }
}
