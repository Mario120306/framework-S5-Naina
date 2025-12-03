package itu.sprint.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import itu.sprint.ControllerScanner;
import itu.sprint.annotation.PathVariable;
import itu.sprint.annotation.RequestParam;
import itu.sprint.util.UrlMapping;
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
        List<UrlMapping> mappings = (List<UrlMapping>) getServletContext().getAttribute(ControllerScanner.CONTROLLERS_MAP_ATTR);

        // 1. Ressource statique ? On délègue tout de suite.
        if (isStaticResource(resourcePath)) {
            forwardToDefaultServlet(req, resp);
            return;
        }

        // 2. Contrôleur dynamique ?
        UrlMapping matchedMapping = findMatchingMapping(mappings, resourcePath);
        if (matchedMapping != null) {
            handleDynamic(resourcePath, matchedMapping, req, resp);
            return;
        }

        // 3. Rien trouvé => 404
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Aucun mapping pour l'URL: " + resourcePath);
    }

    private void handleDynamic(String url, UrlMapping mapping, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // On suppose qu'il n'y a qu'un seul contrôleur/méthode par URL  blabla
        System.out.println("[Sprint][DEBUG] handleDynamic appelé pour URL : " + url);
        Map<String, String> pathParams = mapping.getPattern().extractParams(url);
        for (Map.Entry<Class<?>, Method> entry : mapping.getClassMethodMap().entrySet()) {
            Class<?> cls = entry.getKey();
            Method method = entry.getValue();
            try {
                System.out.println("[Sprint][DEBUG] Appel du contrôleur : " + cls.getName() + "#" + method.getName());
                Object controllerInstance = cls.getDeclaredConstructor().newInstance();
                Object[] args = buildArgs(method, req, resp, pathParams);
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
     * HttpServletRequest / HttpServletResponse / PathVariable si présents, sinon vide.
     */
    private Object[] buildArgs(Method method, HttpServletRequest req, HttpServletResponse resp, Map<String, String> pathParams) {
        Parameter[] parameters = method.getParameters();
        if (parameters.length == 0) return new Object[0];
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            Class<?> pt = param.getType();
            if (HttpServletRequest.class.isAssignableFrom(pt)) {
                args[i] = req;
            } else if (HttpServletResponse.class.isAssignableFrom(pt)) {
                args[i] = resp;
            } else if (param.isAnnotationPresent(PathVariable.class)) {
                PathVariable pv = param.getAnnotation(PathVariable.class);
                String paramName = pv.value().isEmpty() ? param.getName() : pv.value();
                String value = pathParams.get(paramName);
                if (value != null) {
                    args[i] = convertValue(value, pt);
                } else {
                    args[i] = null;
                }
            } else if (param.isAnnotationPresent(RequestParam.class)) {
                RequestParam rp = param.getAnnotation(RequestParam.class);
                String paramName = rp.value();
                String value = req.getParameter(paramName);
                if (value != null) {
                    args[i] = convertValue(value, pt);
                } else {
                    args[i] = null;
                }
            } else {
                // Type non géré : laisser null (IllegalArgumentException possible si primitif)
                args[i] = null;
            }
        }
        return args;
    }

    private Object convertValue(String value, Class<?> type) {
        if (type == String.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return 0;
            }
        } else if (type == long.class || type == Long.class) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return 0L;
            }
        } else if (type == double.class || type == Double.class) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else {
            // Pour d'autres types, retourner la chaîne
            return value;
        }
    }

    private UrlMapping findMatchingMapping(List<UrlMapping> mappings, String url) {
        if (mappings == null) return null;
        for (UrlMapping mapping : mappings) {
            if (mapping.getPattern().matches(url)) {
                return mapping;
            }
        }
        return null;
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