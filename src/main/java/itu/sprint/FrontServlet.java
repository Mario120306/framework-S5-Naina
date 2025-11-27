package itu.sprint;

import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontServlet extends HttpServlet {

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();

        // Chemin relatif
        String relativePath = requestURI.substring(contextPath.length());

        // Détection si l'URL correspond à une ressource statique
        boolean isResource = relativePath.matches(".*\\.(html|jsp|css|js|png|jpg|jpeg|gif|ico)$");

        if (!isResource) {
            // Cas 1 : ce n’est pas une ressource => afficher juste l’URL
            resp.setContentType("text/plain");
            resp.getWriter().println("URL demandée (pas une ressource) : " + requestURI);
        } else {
            // Cas 2 : ressource statique
            ServletContext context = getServletContext();
            try (InputStream resourceStream = context.getResourceAsStream(relativePath)) {
                if (resourceStream == null) {
                    // Cas 2a : ressource inexistante
                    resp.setContentType("text/plain");
                    resp.getWriter().println("Ressource inexistante : " + requestURI);
                } else {
                    resourceStream.transferTo(resp.getOutputStream());
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        service(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        service(req, resp);
    }
}
