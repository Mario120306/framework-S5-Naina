package itu.sprint.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlPattern {
    private String pattern;
    private Pattern regex;
    private String[] paramNames;

    public UrlPattern(String urlPattern) {
        this.pattern = urlPattern;
        parsePattern();
    }

    private void parsePattern() {
        // Replace {var} with named groups
        String regexStr = pattern.replaceAll("\\{([^}]+)\\}", "(?<$1>[^/]+)");
        this.regex = Pattern.compile("^" + regexStr + "$");

        // Extract param names
        java.util.regex.Pattern paramPattern = java.util.regex.Pattern.compile("\\{([^}]+)\\}");
        Matcher matcher = paramPattern.matcher(pattern);
        java.util.List<String> names = new java.util.ArrayList<>();
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        this.paramNames = names.toArray(new String[0]);
    }

    public boolean matches(String url) {
        return regex.matcher(url).matches();
    }

    public Map<String, String> extractParams(String url) {
        Map<String, String> params = new HashMap<>();
        Matcher matcher = regex.matcher(url);
        if (matcher.matches()) {
            for (String param : paramNames) {
                params.put(param, matcher.group(param));
            }
        }
        return params;
    }

    public String getPattern() {
        return pattern;
    }
}