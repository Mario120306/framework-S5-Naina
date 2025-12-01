package itu.sprint.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class UrlMapping {
    private UrlPattern pattern;
    private Map<Class<?>, Method> classMethodMap;

    public UrlMapping(UrlPattern pattern) {
        this.pattern = pattern;
        this.classMethodMap = new HashMap<>();
    }

    public UrlPattern getPattern() {
        return pattern;
    }

    public Map<Class<?>, Method> getClassMethodMap() {
        return classMethodMap;
    }

    public void addMethod(Class<?> cls, Method method) {
        classMethodMap.put(cls, method);
    }
}