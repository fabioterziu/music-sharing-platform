package com.appmusicale.controller.member.homebar;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.dao.AuthorDao;
import com.appmusicale.dao.TrackPerformerDao;
import com.appmusicale.dao.TrackPerformerDaoImpl;
import com.appmusicale.model.Performer;
import com.appmusicale.model.Track;
import com.appmusicale.util.SceneManagerUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;

public class PerformerListController {
    @FXML private Button backButton;
    @FXML private Label performerTitleLabel;
    @FXML private GridPane tracksGrid;

    private UserController userController;
    private Performer selectedPerformer;

    TrackPerformerDao trackPerformerDao= new TrackPerformerDaoImpl();

    @FXML
    private void initialize() {
        System.out.println("PerformerListController inizializzato");
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    public void setPerformer(Performer performer) {
        this.selectedPerformer = performer;
        updateUI();
    }

    @FXML
    private void handleBackButton() {
        //torna al performer
        try {
            VBox parentContainer = (VBox) backButton.getScene().lookup("#contentContainer");
            if (parentContainer != null) {
                Object controller = SceneManagerUtils.loadViewInVBox(
                        parentContainer,
                        "/com/appmusicale/fxml/member/homebar/performers-view.fxml"
                );
                if (controller instanceof PerformersController) {
                    ((PerformersController) controller).setUserController(userController);
                }
            } else {
                //home performers
                if (userController != null) {
                    userController.loadHomeWithPerformers();
                }
            }
        } catch (Exception e) {
            //home performers
            System.out.println("Errore nel back button: " + e.getMessage());
            if (userController != null) {
                userController.loadHomeWithPerformers();
            }
        }
    }

    private void updateUI() {
        if (selectedPerformer != null) {
            performerTitleLabel.setText("Tracce dell'esecutore: " + selectedPerformer.getName());
            loadTracks();
        }
    }

    private void loadTracks() {
        tracksGrid.getChildren().clear();
        List<Track> tracks = trackPerformerDao.getTracksByPerformer(selectedPerformer.getId());

        int col = 0;
        int row = 0;

        for (Track track : tracks) {
            VBox trackCard = createTrackCard(track);
            tracksGrid.add(trackCard, col, row);
            col++;
            if (col == 5) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createTrackCard(Track track) {
        VBox card = new VBox(5);
        card.getStyleClass().add("track-card");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(120);
        imageView.setFitWidth(120);
        imageView.getStyleClass().add("track-image");

        String coverPath = track.getCoverPath();
        if (coverPath != null) {
            if (coverPath.startsWith("/com/appmusicale")) {
                imageView.setImage(new Image(
                        getClass().getResource(coverPath).toExternalForm()));
            } else {
                File file = new File(coverPath);
                if (file.exists()) {
                    imageView.setImage(new Image(file.toURI().toString()));
                } else {
                    imageView.setImage(new Image(
                            getClass().getResource("/com/appmusicale/images/default.jpeg").toExternalForm()));
                }
            }
        } else {
            imageView.setImage(new Image(
                    getClass().getResource("/com/appmusicale/images/default.jpeg").toExternalForm()));
        }

        Label titleLabel = new Label(track.getTitle());
        titleLabel.getStyleClass().add("track-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(120);

        //salva stato
        card.setOnMouseClicked(event -> {
            if (userController != null) {
                userController.loadTrackViewFromPerformerList(track.getId(), selectedPerformer);
            }
        });

        card.getChildren().addAll(imageView, titleLabel);
        return card;
    }
}