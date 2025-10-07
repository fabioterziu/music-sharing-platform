package com.appmusicale.controller.member.homebar;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.dao.TrackDao;
import com.appmusicale.dao.TrackDaoImpl;
import com.appmusicale.dao.TrackPerformerDao;
import com.appmusicale.dao.TrackPerformerDaoImpl;
import com.appmusicale.model.Performer;
import com.appmusicale.model.Track;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.List;

public class ExploreController {
    @FXML private GridPane exploreGrid;
    private UserController userController;

    TrackDao trackDao= new TrackDaoImpl();
    TrackPerformerDao trackPerformerDao= new TrackPerformerDaoImpl();

    @FXML
    private void initialize() {
        System.out.println("ExploreController inizializzato");
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
        loadRandomTracks();
    }

    private void loadRandomTracks() {
        exploreGrid.getChildren().clear();
        List<Track> randomTracks = trackDao.getRandomTracks(20);

        int col = 0;
        int row = 0;

        for (Track track : randomTracks) {
            VBox trackCard = createTrackCard(track);
            exploreGrid.add(trackCard, col, row);
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

        ImageView trackImageView = new ImageView();
        trackImageView.setFitWidth(120);
        trackImageView.setFitHeight(120);
        trackImageView.getStyleClass().add("track-image");

        String coverPath = track.getCoverPath();
        if (coverPath != null) {
            if (coverPath.startsWith("/com/appmusicale")) {
                trackImageView.setImage(new Image(
                        getClass().getResource(coverPath).toExternalForm()));
            } else {
                File file = new File(coverPath);
                if (file.exists()) {
                    trackImageView.setImage(new Image(file.toURI().toString()));
                } else {
                    trackImageView.setImage(new Image(
                            getClass().getResource("/com/appmusicale/images/default.jpeg").toExternalForm()));
                }
            }
        } else {
            trackImageView.setImage(new Image(
                    getClass().getResource("/com/appmusicale/images/default.jpeg").toExternalForm()));
        }

        Label titleLabel = new Label(track.getTitle());
        titleLabel.getStyleClass().add("track-title");
        titleLabel.setMaxWidth(120);
        titleLabel.setWrapText(true);

        String artistName = (track.getAuthor() != null) ? track.getAuthor().getName() : "Autore sconosciuto";
        Label artistLabel = new Label(artistName);
        artistLabel.getStyleClass().add("track-artist");
        artistLabel.setMaxWidth(120);
        artistLabel.setWrapText(true);

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

        trackImageView.setPickOnBounds(false);
        titleLabel.setPickOnBounds(false);
        artistLabel.setPickOnBounds(false);

        //salvo stato nello stack
        card.setOnMouseClicked(event -> {
            int trackId = track.getId();
            if (userController != null) {
                userController.loadTrackViewFromExplore(trackId);
            }
        });

        card.getChildren().addAll(trackImageView, titleLabel, artistLabel, performerLabel);
        return card;
    }
}