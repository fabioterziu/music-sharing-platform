package com.appmusicale.util;

import java.util.*;


// SINGLETON PER GESTIRE STACK DI NAVIGAZIONE (vista precedente salvando lo stato)

public class NavigationManagerUtils {

    //UNICA ISTANZA
    private static NavigationManagerUtils instance= null; //private static

    //STACK CHE TIENE TRACCIA DEGLI STATI
    private final Stack<ViewStateUtils> navigationStack = new Stack<>();

    //COSTRUTTORE PRIVATO
    private NavigationManagerUtils() {}

    //METODO STATICO PER OTTENERE L'ISTANZA
    public static NavigationManagerUtils getInstance() {
        //se istanza non esiste creala
        if (instance == null) {
            instance = new NavigationManagerUtils();
        }
        return instance;
    }

    // Aggiunge uno stato di vista nello stack PUSH
    public void pushViewState(String viewPath, String controllerType, Map<String, Object> attributes) {
        ViewStateUtils state = new ViewStateUtils(viewPath, controllerType, attributes);
        navigationStack.push(state);
    }

    // Rimuove e restituisce l'ultimo stato di vista dallo stack POP
    public ViewStateUtils popViewState() {
        return navigationStack.isEmpty() ? null : navigationStack.pop();
    }

    // Svuota lo stack di navigazione
    public void clearStack() {
        navigationStack.clear();
    }

}