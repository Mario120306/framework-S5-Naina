package itu.sprint;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Main {
    
    @WebRoute(url = "/home")
    public static void accueil() {
        System.out.println("Méthode exécutée : accueil()");
    }

    @WebRoute(url = "/about")
    public static void aPropos() {
        System.out.println("Méthode exécutée : aPropos()");
    }

    @WebRoute(url = "/contact")
    public static void contact() {
        System.out.println("Méthode exécutée : contact()");
    }

    public static void autre() {
        System.out.println("Méthode non annotée : autre()");
    }

    public static void main(String[] args) throws Exception {
        // 🔸 Simule une URL reçue (comme dans un servlet)
        String requestedUrl = "/contact";

        System.out.println("=== Simulation requête pour URL : " + requestedUrl + " ===");

        boolean found = false;
        for (Method m : Main.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(WebRoute.class)) {
                WebRoute ann = m.getAnnotation(WebRoute.class);
                
                if (ann.url().equals(requestedUrl)) {
                    found = true;
                    System.out.println("Méthode trouvée pour cette URL : " + m.getName());
                    
                    if (Modifier.isStatic(m.getModifiers())) {
                        m.invoke(null);
                    } else {
                        m.invoke(Main.class.getDeclaredConstructor().newInstance());
                    }
                    break;
                }
            }
        }

        if (!found) {
            System.out.println("Aucune méthode trouvée pour l'URL : " + requestedUrl);
        }
    }
}
