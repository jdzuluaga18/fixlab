package com.demoapp.util;

import javafx.application.Platform;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Ejecuta operaciones de base de datos en un hilo separado
 * y actualiza la UI de vuelta en el hilo de JavaFX.
 * Evita el deadlock entre el hilo de JavaFX y Hibernate/SQLite.
 */
public class AsyncTask {

    /** Versión sin valor de retorno */
    public static void run(Runnable tarea, Runnable onExito,
                           Consumer<String> onError) {
        Thread hilo = new Thread(() -> {
            try {
                tarea.run();
                Platform.runLater(onExito);
            } catch (Throwable e) {
                String msg = e.getMessage() != null ? e.getMessage() : "Error inesperado";
                Platform.runLater(() -> onError.accept(msg));
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }

    /** Versión con valor de retorno */
    public static <T> void run(Supplier<T> tarea, Consumer<T> onExito) {
        Thread hilo = new Thread(() -> {
            try {
                T resultado = tarea.get();
                Platform.runLater(() -> onExito.accept(resultado));
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
        hilo.setDaemon(true);
        hilo.start();
    }
}
