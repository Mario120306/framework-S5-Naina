package itu.sprint;

import java.lang.reflect.Method;
import java.util.Map;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ContextInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[Sprint] Scan des contrôleurs...");

    Map<String, Map<Class<?>, Method>> urlMap = ControllerScanner.scanControllers(sce.getServletContext());
    sce.getServletContext().setAttribute(ControllerScanner.CONTROLLERS_MAP_ATTR, urlMap);

        System.out.println("[Sprint] URLs mappées : " + urlMap.size());
        urlMap.forEach((url, classMethodMap) -> {
            classMethodMap.forEach((cls, method) -> {
                System.out.println("[Sprint]   " + url + " -> " + cls.getSimpleName() + "#" + method.getName());
            });
        });
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[Sprint] Arrêt du framework");
    }
}
