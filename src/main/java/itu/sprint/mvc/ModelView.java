package itu.sprint.mvc;

import java.util.HashMap;

public class ModelView {
    private String view;
    private HashMap<String,Object> attributes;

    public ModelView() {
        attributes = new HashMap<>();
    }

    public ModelView(String view) {
        attributes = new HashMap<>();

        this.view = view;
    }

    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public HashMap<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, Object> attributes) {
        this.attributes = attributes;
    }
}
