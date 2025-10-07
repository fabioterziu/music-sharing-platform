package com.appmusicale.controller.member.homebar;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.dao.TrackDao;
import com.appmusicale.dao.TrackDaoImpl;
import com.appmusicale.dao.TrackPerformerDao;
import com.appmusicale.dao.TrackPerformerDaoImpl;
import com.appmusicale.model.Author;
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

public class AuthorListController {
    @FXML private Button backButton;
    @FXML private Label authorTitleLabel;
    @FXML private GridPane tracksGrid;

    private UserController userController;
    private Author selectedAuthor;

    TrackDao trackDao= new TrackDaoImpl();
    TrackPerformerDao trackPerformerDao= new TrackPerformerDaoImpl();

    @FXML
    private void initialize() {
        System.out.println("AuthorListController inizializzato");
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    public void setAuthor(Author author) {
        this.selectedAuthor = author;
        updateUI();
    }

    @FXML
    private void handleBackButton() {
        //torna all autore
        try {
            VBox parentContainer = (VBox) backButton.getScene().lookup("#contentContainer");
            if (parentContainer != null) {
                Object controller = SceneManagerUtils.loadViewInVBox(
                        parentContainer,
                        "/com/appmusicale/fxml/member/homebar/authors-view.fxml"
                );
                if (controller instanceof AuthorsController) {
                    ((AuthorsController) controller).setUserController(userController);
                }
            } else {
                //home autore
                if (userController != null) {
                    userController.loadHomeWithAuthors();
                }
            }
        } catch (Exception e) {
            //home autore
            System.out.println("Errore back button: " + e.getMessage());
            if (userController != null) {
                userController.loadHomeWithAuthors();
            }
        }
    }

    private void updateUI() {
        if (selectedAuthor != null) {
            authorTitleLabel.setText("Tracce dell'autore: " + selectedAuthor.getName());
            loadTracks();
        }
    }

    private void loadTracks() {
        tracksGrid.getChildren().clear();
        List<Track> tracks = trackDao.getTracksByAuthor(selectedAuthor.getId());

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

        List<Performer> performers = trackPerformerDao.getPerformersByTrack(track.getId());
        String performerNames = "";
        if (performers != null && !performers.isEmpty()) {
            for (int i = 0; i < performers.size(); i++) {
                if (i > 0) performerNames += ", ";
                performerNames += performers.get(i).getName();
            }
        }

        Label performerLabel = new Label(performerNames);
        performerLabel.getStyleClass().add("track-performer");
        performerLabel.setMaxWidth(120);
        performerLabel.setWrapText(true);

        //salva lo stato
        card.setOnMouseClicked(event -> {
            if (userController != null) {
                userController.loadTrackViewFromAuthorList(track.getId(), selectedAuthor);
            }
        });

        card.getChildren().addAll(imageView, titleLabel, performerLabel);
        return card;
    }
}