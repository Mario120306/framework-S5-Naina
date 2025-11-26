package itu.sprint.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Affiche les classes et methodes annotees
 */
@WebServlet(name = "ScanServlet", urlPatterns = "/scan", loadOnStartup = 1)
public class ScanServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("text/plain; charset=UTF-8");
        PrintWriter out = resp.getWriter();

    Map<String, Map<Class<?>, Method>> urlMap = (Map<String, Map<Class<?>, Method>>) getServletContext().getAttribute(itu.sprint.ControllerScanner.CONTROLLERS_MAP_ATTR);

        out.println("=== MAPPINGS URL -> Controller#Method ===\n");
        out.println("URLs mappées : " + urlMap.size() + "\n");

        urlMap.forEach((url, classMethodMap) -> {
            out.println("URL : " + url);
            classMethodMap.forEach((cls, method) -> {
                out.println("  - " + cls.getName() + "#" + method.getName() + "()");
            });
            out.println();
        });
    }
}
