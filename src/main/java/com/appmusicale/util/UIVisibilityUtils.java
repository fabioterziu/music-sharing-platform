package com.appmusicale.util;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

//UTILITY PER VISIBILITÀ UI

public class UIVisibilityUtils {


    public static void showPane(VBox vBox) {
        vBox.setVisible(true);
        vBox.setManaged(true);
    }

    public static void hidePane(VBox vBox){
        vBox.setVisible(false);
        vBox.setManaged(false);
    }

    public static void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    public static void hideError(Label label) {
        label.setText("");
        label.setVisible(false);
        label.setManaged(false);
    }

}
