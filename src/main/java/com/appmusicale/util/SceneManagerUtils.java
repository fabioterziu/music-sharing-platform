package com.appmusicale.util;

import com.appmusicale.controller.access.AccessController;
import com.appmusicale.controller.member.homebar.centre.MultimediaController;
import com.appmusicale.controller.member.sidebar.ConcertDetailsController;
import com.appmusicale.dao.MemberDao;
import com.appmusicale.dao.MemberDaoImpl;
import com.appmusicale.service.AccessService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

import javafx.scene.paint.Color;
import javafx.util.Callback;

public class SceneManagerUtils {
    private static Stage stage;
    private static double windowWidth = 1000; //larghezza
    private static double windowHeight = 700;

    public static void setStage(Stage s) {
        stage = s;
    }

    //NUOVA SCENA (AccessController, AdminController)
    public static void setScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManagerUtils.class.getResource(fxml));

            Parent root = loader.load();

            if (stage.getScene() == null) {
                //prima volta creo scena
                Scene scene = new Scene(root, windowWidth, windowHeight);
                scene.setFill(Color.web("#001220"));
                stage.setScene(scene);
            } else {
                //cambio solo root senza ricreare scena
                Scene scene = stage.getScene();
                scene.setRoot(root);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //VISTA BORDERPANE
    public static Object loadViewInBorderPane(BorderPane container, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManagerUtils.class.getResource(fxmlPath));
            Node view = loader.load();
            BorderPane.setMargin(view, javafx.geometry.Insets.EMPTY);
            container.setCenter(view);
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore nel caricamento della vista: " + fxmlPath);
            return null;
        }
    }

    //VISTA VBOX
    public static Object loadViewInVBox(VBox container, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManagerUtils.class.getResource(fxmlPath));
            Node view = loader.load();
            container.getChildren().setAll(view);
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore nel caricamento della vista: " + fxmlPath);
            return null;
        }
    }

    //SCROLLPANE
    public static Object loadViewInScrollPane(ScrollPane container, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManagerUtils.class.getResource(fxmlPath));
            Node view = loader.load();
            container.setContent(view);
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore nel caricamento della vista: " + fxmlPath);
            return null;
        }
    }


    //PER APERTURA MULTIMEDIA
    private static boolean multimediaStageOpen = false; //variabile per blocco
    public static Stage openNewStageForMultimedia(String fxmlPath, String title, Consumer<Object> controllerInitializer) {
        if (multimediaStageOpen) {
            return null;// blocca se c'è già una finestra aperta
        }
        try {
            FXMLLoader loader = new FXMLLoader(SceneManagerUtils.class.getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();

            Stage newStage = new Stage();
            newStage.setTitle(title);
            newStage.setScene(new Scene(root));

            //blocco true
            multimediaStageOpen = true;

            //adatta finestra al contenuto
            newStage.sizeToScene();

            //blocca ridimensionamento
            newStage.setResizable(false);

            //posiziona al centro
            newStage.centerOnScreen();

            newStage.setFullScreen(false);
            newStage.setMaximized(false);

            //finestra figlia
            if (stage != null) {
                newStage.initOwner(stage);
            }

            //quando chiudo stop payer
            newStage.setOnCloseRequest(event -> {
                if (controller instanceof MultimediaController mc) {
                    mc.stopMedia();
                }
                multimediaStageOpen = false;
            });

            //mostra la finestra
            newStage.show();

            if (controller != null && controllerInitializer != null) {
                controllerInitializer.accept(controller);
            }

            return newStage;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Errore apertura finestra: " + fxmlPath);
            multimediaStageOpen = false;
            return null;
        }
    }

    //PER DOCUMENTI
    private static boolean documentStageOpen = false;
    public static Stage openNewStageForDocument(String fxmlPath, String title, Consumer<Object> controllerInitializer) {
        if (documentStageOpen) return null; //blocca se già aperto
        try {
            FXMLLoader loader = new FXMLLoader(SceneManagerUtils.class.getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();

            Stage newStage = new Stage();
            newStage.setTitle(title);
            newStage.setScene(new Scene(root));

            documentStageOpen = true; //blocco

            if (stage != null) newStage.initOwner(stage);

            if (controller != null && controllerInitializer != null) {
                controllerInitializer.accept(controller);
            }

            newStage.show();
            Platform.runLater(() -> {
                newStage.sizeToScene();
                newStage.centerOnScreen();
                newStage.setResizable(false);
            });

            newStage.setOnCloseRequest(event -> documentStageOpen = false);

            return newStage;

        } catch (IOException e) {
            e.printStackTrace();
            documentStageOpen = false;
            return null;
        }
    }



    //SE MEDIA NON PRESENTE STAGE FIGLIO DEFAULT
    public static void openImageStage(String resourcePath) {
        try {
            Image image = new Image(SceneManagerUtils.class.getResourceAsStream(resourcePath));

            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            imageView.setFitWidth(300);
            imageView.setFitHeight(300);

            StackPane root = new StackPane(imageView);
            Scene scene = new Scene(root);

            Stage stage = new Stage();
            stage.setScene(scene);

            //non ridimensionabile
            stage.sizeToScene();
            stage.setResizable(false);
            stage.centerOnScreen();

            if (SceneManagerUtils.stage != null) {
                stage.initOwner(SceneManagerUtils.stage);
                stage.initModality(Modality.APPLICATION_MODAL);
            }

            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}