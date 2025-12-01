package itu.sprint;

import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

import itu.sprint.annotation.MapURL;
import itu.sprint.util.UrlMapping;
import itu.sprint.util.UrlPattern;
import jakarta.servlet.ServletContext;

public final class ControllerScanner {

    public static final String CONTROLLERS_MAP_ATTR = "sprint.controllersMap";
    private static final String CONTROLLER_ANNOTATION = "AnnotationController";
    private static final String MAP_URL_ANNOTATION = "MapURL";
    private static final String CLASS_EXTENSION = ".class";

    private ControllerScanner() {
    }

    /**
     * Scanne tous les contrôleurs avec leurs méthodes mappées.
     */
    public static List<UrlMapping> scanControllers() {
        return scanControllers(null);
    }

    /**
     * Scans controllers using both the JVM classpath and, when available, the
     * web application's WEB-INF/classes directory provided by the ServletContext.
     */
    public static List<UrlMapping> scanControllers(ServletContext servletContext) {
        Set<Class<?>> candidates = findAnnotatedClasses();

        // If we're running inside a servlet container, try to scan WEB-INF/classes
        if (servletContext != null) {
            try {
                String classesPath = servletContext.getRealPath("/WEB-INF/classes");
                if (classesPath != null) {
                    File classesDir = new File(classesPath);
                    if (classesDir.exists() && classesDir.isDirectory()) {
                        findClassesInDirectory(classesDir, "", CONTROLLER_ANNOTATION, candidates);
                    }
                }
            } catch (Throwable ignored) {
                // Ignore, fall back to classpath-only scanning
            }
        }
        List<UrlMapping> mappings = new ArrayList<>();

        for (Class<?> cls : candidates) {
            List<Method> mapped = extractMappedMethods(cls);
            for (Method m : mapped) {
                String url = extractUrl(m);
                if (url == null || url.isEmpty()) {
                    // Fallback: utiliser le nom de la méthode précédé d'un slash
                    url = "/" + m.getName();
                }
                // Normaliser : toujours commencer par '/'
                if (!url.startsWith("/")) {
                    url = "/" + url;
                }

                UrlPattern pattern = new UrlPattern(url);
                UrlMapping mapping = findOrCreateMapping(mappings, pattern);
                // En cas de duplication classe->méthode sur la même URL, on écrase l'ancien (convention simple)
                mapping.addMethod(cls, m);
            }
        }

        return mappings;
    }

    private static UrlMapping findOrCreateMapping(List<UrlMapping> mappings, UrlPattern pattern) {
        for (UrlMapping mapping : mappings) {
            if (mapping.getPattern().getPattern().equals(pattern.getPattern())) {
                return mapping;
            }
        }
        UrlMapping newMapping = new UrlMapping(pattern);
        mappings.add(newMapping);
        return newMapping;
    }

    /**
     * Extrait les méthodes annotées avec MapURL d'une classe.
     */
    private static List<Method> extractMappedMethods(Class<?> cls) {
        return Arrays.stream(cls.getDeclaredMethods())
                .filter(m -> isAnnotatedWithSimpleName(m, MAP_URL_ANNOTATION) || m.isAnnotationPresent(MapURL.class))
                .collect(Collectors.toList());
    }

    private static String extractUrl(Method m) {
        try {
            MapURL ann = m.getAnnotation(MapURL.class);
            if (ann != null) {
                return ann.url();
            }
        } catch (Throwable ignored) {
            // Ignorer problèmes réflexion
        }
        // Si annotation chargée via nom simple seulement, tenter introspection générique
        return null;
    }

    /**
     * Trouve toutes les classes annotées avec le nom simple donné.
     */
    private static Set<Class<?>> findAnnotatedClasses() {
        Set<Class<?>> classes = new HashSet<>();
        scanClassPath(classes);

        return classes;
    }

    /**
     * Scanne les entrées du classpath système.
     */
    private static void scanClassPath(Set<Class<?>> classes) {
        String classPath = System.getProperty("java.class.path", "");
        if (classPath.isEmpty()) return;

        Arrays.stream(classPath.split(File.pathSeparator))
                .map(File::new)
                .forEach(file -> processClassPathEntry(file, classes));
    }

    /**
     * Traite une entrée du classpath (répertoire ou JAR).
     */
    private static void processClassPathEntry(File file, Set<Class<?>> classes) {
        try {
            if (file.isDirectory()) {
                findClassesInDirectory(file, "", ControllerScanner.CONTROLLER_ANNOTATION, classes);
            }
        } catch (Throwable ignored) {
            // Ignore les erreurs
        }
    }

    /**
     * Trouve récursivement les classes dans un répertoire.
     */
    private static void findClassesInDirectory(File directory, String packageName,
                                               String simpleName, Set<Class<?>> classes) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                findClassesInDirectory(file, packageName + file.getName() + ".", simpleName, classes);
            } else if (file.getName().endsWith(CLASS_EXTENSION)) {
                String className = packageName + file.getName().replace(CLASS_EXTENSION, "");
                tryLoadAndCheck(className, simpleName, classes);
            }
        }
    }

    /**
     * Tente de charger une classe et vérifie son annotation.
     */
    private static void tryLoadAndCheck(String className, String simpleName, Set<Class<?>> classes) {
        try {
            Class<?> cls = Class.forName(className);
            if (isAnnotatedWithSimpleName(cls, simpleName)) {
                classes.add(cls);
            }
        } catch (ClassNotFoundException | LinkageError | SecurityException ignored) {
            // Ignore les classes qui ne peuvent pas être chargées
        }
    }

    /**
     * Vérifie si un élément est annoté avec une annotation ayant le nom simple donné.
     */
    private static boolean isAnnotatedWithSimpleName(AnnotatedElement element, String simpleName) {
        return Arrays.stream(element.getAnnotations())
                .anyMatch(ann -> {
                    try {
                        return ann.annotationType().getSimpleName().equals(simpleName);
                    } catch (Throwable ignored) {
                        return false;
                    }
                });
    }
}