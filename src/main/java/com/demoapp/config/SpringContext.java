package com.demoapp.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Clase auxiliar que permite acceder al ApplicationContext de Spring
 * desde cualquier parte de la aplicación, incluso en clases que no son
 * gestionadas por Spring (por ejemplo, controladores JavaFX cargados por FXMLLoader).
 *
 * Funciona almacenando el ApplicationContext en una variable estática
 * cuando Spring inicializa la aplicación. Luego, mediante el método
 * getBean(), se pueden obtener beans de Spring manualmente.
 *
 * Es especialmente útil en aplicaciones híbridas JavaFX + Spring,
 * donde los controladores FXML no pueden ser inyectados automáticamente
 * y necesitan acceder a servicios o componentes de Spring.
 */

@Component
public class SpringContext  implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.context = applicationContext;

    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}
