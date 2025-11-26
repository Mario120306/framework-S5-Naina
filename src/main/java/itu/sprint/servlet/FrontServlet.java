package itu.sprint.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import itu.sprint.ControllerScanner;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet frontal qui gère toutes les requêtes et délègue aux contrôleurs appropriés.
 */
@WebServlet(name = "FrontServlet", urlPatterns = "/")
public class FrontServlet extends HttpServlet {

    private static final String DEFAULT_SERVLET_NAME = "default";

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String resourcePath = extractResourcePath(req); // ex: /hello
        Map<String, Map<Class<?>, Method>> urlMap = (Map<String, Map<Class<?>, Method>>) getServletContext().getAttribute(ControllerScanner.CONTROLLERS_MAP_ATTR);

        // 1. Ressource statique ? On délègue tout de suite.
        if (isStaticResource(resourcePath)) {
            forwardToDefaultServlet(req, resp);
            return;
        }

        // 2. Contrôleur dynamique ?
        if (urlMap != null && urlMap.containsKey(resourcePath)) {
            handleDynamic(resourcePath, urlMap.get(resourcePath), req, resp);
            return;
        }

        // 3. Rien trouvé => 404
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Aucun mapping pour l'URL: " + resourcePath);
    }

    private void handleDynamic(String url, Map<Class<?>, Method> classMethodMap, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // On suppose qu'il n'y a qu'un seul contrôleur/méthode par URL  blabla
        System.out.println("[Sprint][DEBUG] handleDynamic appelé pour URL : " + url);
        for (Map.Entry<Class<?>, Method> entry : classMethodMap.entrySet()) {
            Class<?> cls = entry.getKey();
            Method method = entry.getValue();
            try {
                System.out.println("[Sprint][DEBUG] Appel du contrôleur : " + cls.getName() + "#" + method.getName());
                Object controllerInstance = cls.getDeclaredConstructor().newInstance();
                Object[] args = buildArgs(method, req, resp);
                method.setAccessible(true);
                Object returnValue = method.invoke(controllerInstance, args);
                System.out.println("[Sprint][DEBUG] Retour du contrôleur : " + (returnValue != null ? returnValue.getClass().getName() : "null"));
                // Si retour ModelView, on gère l'affichage JSP
                if (returnValue != null && returnValue.getClass().getSimpleName().equals("ModelView")) {
                    // On récupère les attributs et la vue
                    try {
                        java.lang.reflect.Method getAttributes = returnValue.getClass().getMethod("getAttributes");
                        java.util.Map<String, Object> attributes = (java.util.Map<String, Object>) getAttributes.invoke(returnValue);
                        if (attributes != null) {
                            System.out.println("[Sprint][DEBUG] Attributs ModelView avant forward :");
                            for (Map.Entry<String, Object> att : attributes.entrySet()) {
                                System.out.println("[Sprint][DEBUG]   " + att.getKey() + " = " + att.getValue());
                                req.setAttribute(att.getKey(), att.getValue());
                            }
                        }
                        java.lang.reflect.Method getView = returnValue.getClass().getMethod("getView");
                        String view = (String) getView.invoke(returnValue);
                        if (view != null) {
                            RequestDispatcher dispatcher = req.getRequestDispatcher(view);
                            dispatcher.forward(req, resp);
                            return;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Erreur ModelView: " + e.getMessage(), e);
                    }
                } else {
                    // Sinon, on affiche le retour en texte brut
                    resp.setContentType("text/plain; charset=UTF-8");
                    PrintWriter out = resp.getWriter();
                    out.println("[Sprint] Dispatch URL: " + url);
                    out.println("-> " + cls.getName() + "#" + method.getName() + "()");
                    if (returnValue != null) {
                        out.println("   <= Retour: " + returnValue);
                    }
                }
            } catch (ReflectiveOperationException | IllegalArgumentException e) {
                resp.setContentType("text/plain; charset=UTF-8");
                PrintWriter out;
                try { out = resp.getWriter(); } catch (IOException ex) { throw new RuntimeException(ex); }
                out.println("   !! Erreur invocation: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            }
        }
    }

    /**
     * Construit le tableau d'arguments pour la méthode : injection basique
     * HttpServletRequest / HttpServletResponse si présents, sinon vide.
     */
    private Object[] buildArgs(Method method, HttpServletRequest req, HttpServletResponse resp) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 0) return new Object[0];
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> pt = paramTypes[i];
            if (HttpServletRequest.class.isAssignableFrom(pt)) {
                args[i] = req;
            } else if (HttpServletResponse.class.isAssignableFrom(pt)) {
                args[i] = resp;
            } else {
                // Type non géré : laisser null (IllegalArgumentException possible si primitif)
                args[i] = null;
            }
        }
        return args;
    }

    /**
     * Extrait le chemin de la ressource depuis la requête.
     */
    private String extractResourcePath(HttpServletRequest req) {
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();

        return requestURI.substring(contextPath.length());
    }

    /**
     * Vérifie si la ressource est un fichier statique.
     */
    private boolean isStaticResource(String resourcePath) {
        try {
            URL resource = getServletContext().getResource(resourcePath);
            return resource != null;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    /**
     * Délègue au servlet par défaut pour les ressources statiques.
     */
    private void forwardToDefaultServlet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        RequestDispatcher defaultServlet = getServletContext().getNamedDispatcher(DEFAULT_SERVLET_NAME);

        if (defaultServlet != null) {
            defaultServlet.forward(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp);
    }
}