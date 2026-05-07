package com.demoapp;

/**
 * Punto de entrada principal de FixLab.
 *
 * Esta clase existe porque JavaFX requiere que el método main()
 * esté en una clase separada que NO extienda javafx.application.Application
 * cuando se empaqueta como JAR.
 */
public class Launcher {

    public static void main(String[] args) {
        javafx.application.Application.launch(JavaFxApplication.class, args);
    }
}
