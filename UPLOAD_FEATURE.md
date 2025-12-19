# 📤 Fonctionnalité d'Upload de Fichiers

## Vue d'ensemble

Cette fonctionnalité permet d'uploader des fichiers dans votre framework Sprint via une annotation simple `@UploadFile`.

## Composants ajoutés

### 1. Annotation `@UploadFile`
- **Package**: `itu.sprint.annotation`
- **Utilisation**: Annoter un paramètre de méthode pour injecter un fichier uploadé
- **Type supporté**: `FileUpload` ou `Part`

### 2. Classe `FileUpload`
- **Package**: `itu.sprint.util`
- **Fonctionnalités**:
  - `getFileName()`: Récupère le nom du fichier
  - `getContentType()`: Récupère le type MIME
  - `getSize()`: Récupère la taille en octets
  - `getInputStream()`: Obtient un stream pour lire le fichier
  - `save(directory)`: Sauvegarde le fichier
  - `save(directory, customFileName)`: Sauvegarde avec un nom personnalisé

### 3. Configuration du FrontServlet
- Ajout de `@MultipartConfig` pour supporter les uploads
- Limites configurées:
  - Taille maximale fichier: 10MB
  - Taille maximale requête: 50MB
  - Seuil mémoire: 2MB

## Utilisation

### Dans un contrôleur

```java
@AnnotationController
public class MyController {
    
    // Upload simple avec formulaire HTML
    @MapURL(url = "/upload", method = "POST")
    public ModelView handleUpload(
            @UploadFile("file") FileUpload file,
            @RequestParam("description") String description) {
        
        ModelView mv = new ModelView();
        try {
            if (file != null && file.getSize() > 0) {
                // Sauvegarder le fichier
                String path = file.save("C:/uploads");
                
                mv.addAttribute("success", true);
                mv.addAttribute("fileName", file.getFileName());
                mv.addAttribute("savedPath", path);
            }
        } catch (IOException e) {
            mv.addAttribute("success", false);
            mv.addAttribute("message", e.getMessage());
        }
        
        mv.setView("/result.jsp");
        return mv;
    }
    
    // Upload via API REST (JSON)
    @MapURL(url = "/api/upload", method = "POST")
    @RestAPI
    public ApiResponse<Map<String, Object>> apiUpload(
            @UploadFile("file") FileUpload file) {
        
        try {
            String path = file.save("C:/uploads");
            
            Map<String, Object> data = new HashMap<>();
            data.put("fileName", file.getFileName());
            data.put("savedPath", path);
            
            return ApiResponse.success("Upload réussi", data);
        } catch (IOException e) {
            return ApiResponse.error("Erreur: " + e.getMessage());
        }
    }
}
```

### Formulaire HTML

```html
<form action="upload" method="post" enctype="multipart/form-data">
    <input type="file" name="file" required>
    <input type="text" name="description">
    <button type="submit">Uploader</button>
</form>
```

### Upload via JavaScript (AJAX)

```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);
formData.append('description', 'Ma description');

fetch('api/upload', {
    method: 'POST',
    body: formData
})
.then(response => response.json())
.then(data => console.log(data));
```

## Exemples fournis

### Pages de test disponibles:
1. **`/upload`**: Formulaire d'upload classique (HTML form)
2. **`/api-upload-test.html`**: Test de l'API REST avec JavaScript

### Contrôleur d'exemple:
- `FileUploadController.java`: Contient des exemples d'implémentation

## Configuration

### Modifier les limites d'upload

Dans `FrontServlet.java`, vous pouvez ajuster les paramètres:

```java
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,  // Seuil mémoire: 2MB
    maxFileSize = 1024 * 1024 * 10,       // Max fichier: 10MB
    maxRequestSize = 1024 * 1024 * 50     // Max requête: 50MB
)
```

### Définir le répertoire d'upload

Créez le répertoire sur votre système et ajustez le chemin dans vos contrôleurs:

```java
String uploadDir = "C:/uploads"; // Windows
// ou
String uploadDir = "/var/uploads"; // Linux/Mac
```

## Fonctionnalités avancées

### Upload multiple

```java
@MapURL(url = "/upload-multiple", method = "POST")
public ModelView handleMultipleUploads(
        @UploadFile("files[]") FileUpload[] files) {
    
    for (FileUpload file : files) {
        if (file != null) {
            file.save("C:/uploads");
        }
    }
    // ...
}
```

### Validation du type de fichier

```java
@MapURL(url = "/upload-image", method = "POST")
public ModelView uploadImage(@UploadFile("image") FileUpload file) {
    
    if (!file.getContentType().startsWith("image/")) {
        return error("Seules les images sont acceptées");
    }
    
    file.save("C:/uploads/images");
    // ...
}
```

### Renommer le fichier

```java
@MapURL(url = "/upload-rename", method = "POST")
public ModelView uploadWithRename(@UploadFile("file") FileUpload file) {
    
    String customName = UUID.randomUUID().toString() + ".jpg";
    String path = file.save("C:/uploads", customName);
    // ...
}
```

## Tests

Pour tester la fonctionnalité:

1. Compiler le framework:
   ```bash
   cd sprint_framework
   mvn clean install
   ```

2. Démarrer l'application de test:
   ```bash
   cd sprint_test
   mvn tomcat7:run
   ```

3. Accéder aux pages:
   - Formulaire classique: `http://localhost:8080/sprint_test/upload`
   - Test API REST: `http://localhost:8080/sprint_test/api-upload-test.html`

## Notes importantes

- ⚠️ Assurez-vous que le répertoire d'upload existe et a les permissions d'écriture
- ⚠️ Les fichiers sont sauvegardés sur le serveur, pensez à la sécurité
- ⚠️ Validez toujours le type et la taille des fichiers côté serveur
- 💡 Pour la production, considérez stocker les fichiers dans un service cloud (AWS S3, etc.)

## Sécurité

### Bonnes pratiques:
1. Valider l'extension et le type MIME
2. Limiter la taille des fichiers
3. Scanner les fichiers pour les virus
4. Ne pas exposer le chemin complet du serveur
5. Utiliser des noms de fichiers uniques (UUID)
6. Stocker les fichiers hors du répertoire web si possible
