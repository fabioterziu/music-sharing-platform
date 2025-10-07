package com.appmusicale;

import com.appmusicale.util.DatabaseInitializerUtils;
import com.appmusicale.util.SceneManagerUtils;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.image.Image;

//MAIN

public class StartApplication extends Application{

    @Override
    public void start(Stage primaryStage) {
        SceneManagerUtils.setStage(primaryStage); //collego stage a scenemanager

        SceneManagerUtils.setScene("/com/appmusicale/fxml/access/access-view.fxml"); //scena iniziale


        primaryStage.setTitle("ShareSound");//titolo app
        primaryStage.show(); //apri
    }

    public static void main(String[] args) {
        //DatabaseInitializerUtils.initialize(); //inizializza db
        DatabaseInitializerUtils dbaseInit = new DatabaseInitializerUtils();
        dbaseInit.initialize();
        launch();  //avvia
    }
}