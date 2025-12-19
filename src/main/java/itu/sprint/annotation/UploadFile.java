package itu.sprint.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Annotation pour injecter un fichier uploadé dans une méthode de contrôleur.
 * Le paramètre annoté doit être de type Part (jakarta.servlet.http.Part)
 * ou FileUpload (classe wrapper personnalisée).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface UploadFile {
    /**
     * Nom du champ de formulaire contenant le fichier a telecharger
     */
    String value();
}
