package itu.sprint.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

public class JSONConverter {
    
    /**
     * Convertit un objet ou un tableau d'objets en JSON
     */
    public static String toJSON(Object obj) throws Exception {
        if (obj == null) {
            return "null";
        }
        
        // Si c'est un tableau
        if (obj.getClass().isArray()) {
            return arrayToJSON(obj);
        }
        
        // Si c'est une Collection (List, Set, etc.)
        if (obj instanceof Collection) {
            return collectionToJSON((Collection<?>) obj);
        }
        
        // Si c'est un objet simple
        return objectToJSON(obj);
    }
    
    /**
     * Convertit un tableau en JSON
     */
    private static String arrayToJSON(Object array) throws Exception {
        StringBuilder json = new StringBuilder("[");
        int length = Array.getLength(array);
        
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                json.append(",");
            }
            Object element = Array.get(array, i);
            json.append(toJSON(element));
        }
        
        json.append("]");
        return json.toString();
    }
    
    /**
     * Convertit une Collection en JSON
     */
    private static String collectionToJSON(Collection<?> collection) throws Exception {
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        
        for (Object element : collection) {
            if (!first) {
                json.append(",");
            }
            json.append(toJSON(element));
            first = false;
        }
        
        json.append("]");
        return json.toString();
    }
    
    /**
     * Convertit un objet en JSON
     */
    private static String objectToJSON(Object obj) throws Exception {
        if (obj == null) {
            return "null";
        }
        
        // Types primitifs et String
        if (obj instanceof String) {
            return "\"" + escapeJSON(obj.toString()) + "\"";
        }
        if (obj instanceof Number) {
            return obj.toString();
        }
        if (obj instanceof Boolean) {
            return obj.toString();
        }
        if (obj instanceof Character) {
            return "\"" + escapeJSON(obj.toString()) + "\"";
        }
        if (obj instanceof Date) {
            return "\"" + obj.toString() + "\"";
        }
        
        // Si c'est une Map
        if (obj instanceof Map) {
            return mapToJSON((Map<?, ?>) obj);
        }
        
        // Objet complexe avec des champs
        StringBuilder json = new StringBuilder("{");
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        boolean first = true;
        
        for (Field field : fields) {
            field.setAccessible(true);
            Object value = field.get(obj);
            
            if (!first) {
                json.append(",");
            }
            
            json.append("\"").append(field.getName()).append("\":");
            json.append(toJSON(value));
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Convertit une Map en JSON
     */
    private static String mapToJSON(Map<?, ?> map) throws Exception {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey().toString()).append("\":");
            json.append(toJSON(entry.getValue()));
            first = false;
        }
        
        json.append("}");
        return json.toString();
    }
    
    /**
     * Échappe les caractères spéciaux pour JSON
     */
    private static String escapeJSON(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
