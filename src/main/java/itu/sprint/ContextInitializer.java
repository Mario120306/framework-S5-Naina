package itu.sprint;

import java.lang.reflect.Method;
import java.util.List;

import itu.sprint.util.UrlMapping;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ContextInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[Sprint] Scan des contrôleurs...");

        List<UrlMapping> mappings = ControllerScanner.scanControllers(sce.getServletContext());
        sce.getServletContext().setAttribute(ControllerScanner.CONTROLLERS_MAP_ATTR, mappings);

        System.out.println("[Sprint] URLs mappées : " + mappings.size());
        mappings.forEach(mapping -> {
            mapping.getClassMethodMap().forEach((cls, method) -> {
                System.out.println("[Sprint]   " + mapping.getPattern().getPattern() + " -> " + cls.getSimpleName() + "#" + method.getName());
            });
        });
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("[Sprint] Arrêt du framework");
    }
}
