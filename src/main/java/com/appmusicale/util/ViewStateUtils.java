package com.appmusicale.util;

import java.util.HashMap;
import java.util.Map;

//RECORD rappresenta lo stato di una vista

public record ViewStateUtils(String viewPath, String controllerType, Map<String, Object> attributes) {
    //costruttore
    public ViewStateUtils(String viewPath, String controllerType, Map<String, Object> attributes) {
        this.viewPath = viewPath;
        this.controllerType = controllerType;
        this.attributes = new HashMap<>(attributes);
    }

    @Override
    public Map<String, Object> attributes() {
        return new HashMap<>(attributes); //restituisce dati salvati
    }
}