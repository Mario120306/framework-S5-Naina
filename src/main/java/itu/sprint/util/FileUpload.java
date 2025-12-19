package itu.sprint.util;

import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Classe wrapper pour gérer les fichiers uploadés de manière simplifiée
 */
public class FileUpload {
    private final Part part;

    public FileUpload(Part part) {
        this.part = part;
    }

    /**
     * Retourne le nom du fichier uploadé
     */
    public String getFileName() {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            for (String token : contentDisposition.split(";")) {
                if (token.trim().startsWith("filename")) {
                    String fileName = token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                    // Extraire uniquement le nom du fichier (sans le chemin)
                    int lastIndex = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));
                    if (lastIndex >= 0) {
                        fileName = fileName.substring(lastIndex + 1);
                    }
                    return fileName;
                }
            }
        }
        return "unknown";
    }

    /**
     * Retourne le type MIME du fichier
     */
    public String getContentType() {
        return part.getContentType();
    }

    /**
     * Retourne la taille du fichier en octets
     */
    public long getSize() {
        return part.getSize();
    }

    /**
     * Retourne un InputStream pour lire le contenu du fichier
     */
    public InputStream getInputStream() throws IOException {
        return part.getInputStream();
    }

    /**
     * Sauvegarde le fichier uploadé dans le répertoire spécifié
     * @param directory Répertoire de destination
     * @return Le chemin complet du fichier sauvegardé
     */
    public String save(String directory) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = getFileName();
        String filePath = directory + File.separator + fileName;
        part.write(filePath);
        return filePath;
    }

    /**
     * Sauvegarde le fichier avec un nom personnalisé
     * @param directory Répertoire de destination
     * @param customFileName Nom personnalisé pour le fichier
     * @return Le chemin complet du fichier sauvegardé
     */
    public String save(String directory, String customFileName) throws IOException {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filePath = directory + File.separator + customFileName;
        part.write(filePath);
        return filePath;
    }

    /**
     * Retourne l'objet Part sous-jacent
     */
    public Part getPart() {
        return part;
    }
}
