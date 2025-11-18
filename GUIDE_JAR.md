# Guide : Créer et utiliser le JAR du Framework

## 1. Créer le JAR

### Méthode 1 : Avec Maven (recommandé)

Ouvrez un terminal dans le dossier du projet et exécutez :

```bash
mvn clean package
```

Le JAR sera créé dans le dossier `target/` avec le nom : `sprint_framework-1.0-SNAPSHOT.jar`

### Méthode 2 : Avec Maven (sans les tests)

```bash
mvn clean package -DskipTests
```

## 2. Utiliser le JAR dans un autre projet

### Option A : Ajouter le JAR manuellement dans un projet Maven

1. **Copiez le JAR** dans un dossier `lib/` de votre nouveau projet (par exemple : `mon_projet/lib/sprint_framework-1.0-SNAPSHOT.jar`)

2. **Ajoutez la dépendance dans le `pom.xml`** de votre nouveau projet :

```xml
<dependencies>
    <!-- Votre framework -->
    <dependency>
        <groupId>itu.sprint</groupId>
        <artifactId>sprint_framework</artifactId>
        <version>1.0-SNAPSHOT</version>
        <scope>system</scope>
        <systemPath>${project.basedir}/lib/sprint_framework-1.0-SNAPSHOT.jar</systemPath>
    </dependency>
    
    <!-- Autres dépendances... -->
</dependencies>
```

3. **Utilisez le Scanner dans votre code** :

```java
package mon.projet;

import itu.sprint.annotation.WebRoute;
import itu.sprint.util.Scanner;

public class MonController {
    
    @WebRoute(url = "/hello")
    public void hello() {
        System.out.println("Hello World!");
    }
    
    public static void main(String[] args) {
        // Scanner toutes les routes dans votre package
        Scanner.printWebRouteAnnotations("mon.projet");
        
        // Exécuter une route spécifique
        Scanner.executeWebRoute("mon.projet", "/hello");
    }
}
```

### Option B : Installer le JAR dans le repository Maven local

1. **Installez le JAR dans votre repository Maven local** :

```bash
mvn install:install-file -Dfile=target/sprint_framework-1.0-SNAPSHOT.jar \
                         -DgroupId=itu.sprint \
                         -DartifactId=sprint_framework \
                         -Dversion=1.0-SNAPSHOT \
                         -Dpackaging=jar
```

2. **Dans votre nouveau projet, ajoutez simplement la dépendance** :

```xml
<dependencies>
    <dependency>
        <groupId>itu.sprint</groupId>
        <artifactId>sprint_framework</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

### Option C : Pour un projet non-Maven (Java standard)

1. **Copiez le JAR** dans un dossier `lib/` de votre projet

2. **Ajoutez le JAR au classpath** lors de la compilation et l'exécution :

**Compilation :**
```bash
javac -cp "lib/sprint_framework-1.0-SNAPSHOT.jar:." src/**/*.java
```

**Exécution :**
```bash
java -cp "lib/sprint_framework-1.0-SNAPSHOT.jar:.:target/classes" mon.projet.Main
```

**Ou avec un IDE :**
- Dans IntelliJ IDEA : File → Project Structure → Libraries → + → Java → Sélectionnez le JAR
- Dans Eclipse : Right-click project → Properties → Java Build Path → Libraries → Add External JARs

## 3. Exemple complet d'utilisation

```java
package com.example.app;

import itu.sprint.annotation.WebRoute;
import itu.sprint.util.Scanner;

public class AppController {
    
    @WebRoute(url = "/home")
    public void home() {
        System.out.println("Page d'accueil");
    }
    
    @WebRoute(url = "/about")
    public void about() {
        System.out.println("À propos");
    }
    
    public static void main(String[] args) {
        String packageName = "com.example.app";
        
        // Lister toutes les routes disponibles
        System.out.println("=== Routes disponibles ===");
        Scanner.printWebRouteAnnotations(packageName);
        
        // Exécuter une route
        System.out.println("\n=== Exécution d'une route ===");
        Scanner.executeWebRoute(packageName, "/home");
    }
}
```

## 4. Notes importantes

- Le Scanner utilise la réflexion Java pour trouver les classes dans un package
- Assurez-vous que le package que vous scannez correspond au package de vos classes
- Les méthodes annotées avec `@WebRoute` peuvent être statiques ou d'instance
- Le JAR contient uniquement les classes du framework (pas les dépendances comme Jakarta Servlet qui sont en scope `provided`)

