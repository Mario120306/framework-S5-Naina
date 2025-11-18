package itu.sprint.util;

import itu.sprint.annotation.WebRoute;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Scanner {

    /**
     * Scanne un package et retourne toutes les classes trouvées.
     */
    public static List<Class<?>> findClassesInPackage(String packageName)
            throws IOException, URISyntaxException, ClassNotFoundException {

        List<Class<?>> classes = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(packagePath);

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            if (resource.getProtocol().equals("file")) {
                // Développement : dossier src
                Path path = Paths.get(resource.toURI());
                classes.addAll(scanDirectory(packageName, path));
            } else if (resource.getProtocol().equals("jar")) {
                // JAR déployé
                JarURLConnection jarConn = (JarURLConnection) resource.openConnection();
                try (JarFile jarFile = jarConn.getJarFile()) {
                    classes.addAll(scanJar(packageName, jarFile));
                }
            }
        }
        return classes;
    }

    private static List<Class<?>> scanDirectory(String packageName, Path directory)
            throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!Files.isDirectory(directory))
            return classes;

        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".class")) {
                    String className = buildClassName(packageName, directory, file);
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return classes;
    }

    private static List<Class<?>> scanJar(String packageName, JarFile jarFile) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');

        for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            if (name.startsWith(packagePath) && name.endsWith(".class") && !entry.isDirectory()) {
                String className = name.replace('/', '.').substring(0, name.length() - 6);
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }

    private static String buildClassName(String packageName, Path root, Path file) {
        String relative = root.relativize(file).toString();
        return packageName + "." + relative.replace(File.separator, ".").substring(0, relative.length() - 6);
    }

    // ------------------------------------------------------------------
    // Méthode principale à appeler
    // ------------------------------------------------------------------
    public static void printWebRouteAnnotations(String packageName) {
        try {
            List<Class<?>> classes = findClassesInPackage(packageName);
            System.out.println("=== Scan du package : " + packageName + " ===");
            boolean found = false;

            for (Class<?> clazz : classes) {
                for (Method method : clazz.getDeclaredMethods()) {
                    WebRoute annotation = method.getAnnotation(WebRoute.class);
                    if (annotation != null) {
                        found = true;
                        System.out.println("Classe : " + clazz.getName());
                        System.out.println("  Méthode : " + method.getName());
                        System.out.println("  URL     : " + annotation.url());
                        System.out.println("  ---");
                    }
                }
            }

            if (!found) {
                System.out.println("Aucune annotation trouvée dans le package.");
            }

        } catch (Exception e) {
            System.err.println("Erreur lors du scan : " + e.getMessage());
            e.printStackTrace();
        }
    }
    public static void executeWebRoute(String packageName, String requestedUrl) {
        try {
            List<Class<?>> classes = findClassesInPackage(packageName);
            boolean found = false;

            for (Class<?> clazz : classes) {
                for (Method method : clazz.getDeclaredMethods()) {
                    WebRoute route = method.getAnnotation(WebRoute.class);
                    if (route != null && route.url().equals(requestedUrl)) {
                        found = true;
                        System.out.println("→ Méthode trouvée : " + clazz.getName() + "." + method.getName());
                        System.out.println("→ Exécution...");
                        
                        if (Modifier.isStatic(method.getModifiers())) {
                            method.invoke(null);
                        } else {
                            Object instance = clazz.getDeclaredConstructor().newInstance();
                            method.invoke(instance);
                        }
                        break;
                    }
                }
                if (found) break;
            }

            if (!found) {
                System.out.println("❌ Aucune méthode trouvée pour l'URL : " + requestedUrl);
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de l'exécution de la route : " + e.getMessage());
            e.printStackTrace();
        }
    }
}

