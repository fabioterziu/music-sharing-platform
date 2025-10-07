package com.appmusicale.controller.member.homebar;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.dao.*;
import com.appmusicale.model.GenreType;
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

public class GenreListController {
    @FXML private Button backButton;
    @FXML private Label genreTitleLabel;
    @FXML private GridPane tracksGrid;

    private UserController userController;
    private GenreType selectedGenre;

    GenreDao genreDao= new GenreDaoImpl();
    TrackDao trackDao= new TrackDaoImpl();
    TrackPerformerDao trackPerformerDao= new TrackPerformerDaoImpl();

    @FXML
    private void initialize() {
        System.out.println("GenreListController inizializzato");
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    public void setGenre(GenreType genre) {
        this.selectedGenre = genre;
        updateUI();
    }

    @FXML
    private void handleBackButton() {
        //torna al genere
        try {
            VBox parentContainer = (VBox) backButton.getScene().lookup("#contentContainer");
            if (parentContainer != null) {
                Object controller = SceneManagerUtils.loadViewInVBox(
                        parentContainer,
                        "/com/appmusicale/fxml/member/homebar/genres-view.fxml"
                );
                if (controller instanceof GenresController) {
                    ((GenresController) controller).setUserController(userController);
                }
            } else {
                //home genres
                if (userController != null) {
                    userController.loadHomeWithGenres();
                }
            }
        } catch (Exception e) {
            //home genres
            System.out.println("Errore nel back button: " + e.getMessage());
            if (userController != null) {
                userController.loadHomeWithGenres();
            }
        }
    }

    private void updateUI() {
        if (selectedGenre != null) {
            genreTitleLabel.setText("Tracce del genere: " + selectedGenre.getDisplayName());
            loadTracks();
        }
    }

    private void loadTracks() {
        tracksGrid.getChildren().clear();
        Integer genreId = genreDao.getGenreId(selectedGenre);
        List<Track> tracks = trackDao.getTracksByGenre(genreId);

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

        String artistName = (track.getAuthor() != null) ? track.getAuthor().getName() : "Autore sconosciuto";
        Label artistLabel = new Label(artistName);
        artistLabel.getStyleClass().add("track-artist");
        artistLabel.setWrapText(true);
        artistLabel.setMaxWidth(120);

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
                userController.loadTrackViewFromGenreList(track.getId(), selectedGenre);
            }
        });

        card.getChildren().addAll(imageView, titleLabel, artistLabel, performerLabel);
        return card;
    }
}