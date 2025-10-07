package com.appmusicale.controller.member.sidebar;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.dao.*;
import com.appmusicale.model.Comment;
import com.appmusicale.model.Track;
import com.appmusicale.util.MemberContextUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.stream.Collectors;

//SEZIONE DI TUTTI I COMMENTI FATTI DALL'UTENTE

public class CommentedController {
    @FXML private GridPane tracksGrid;
    @FXML private VBox mainContent;

    private UserController userController;
    private int currentMemberId;

    CommentDao commentDao= new CommentDaoImpl();
    GenreDao genreDao= new GenreDaoImpl();
    TrackDao trackDao= new TrackDaoImpl();

    @FXML
    private void initialize() {
        System.out.println("CommentedController inizializzato");
        currentMemberId = MemberContextUtils.getLoggedInMember().getId();
        loadCommentedTracks();
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    //CARICA COMMENTI
    private void loadCommentedTracks() {
        mainContent.getChildren().removeIf(node -> node instanceof Label &&
                !GridPane.getRowIndex(node).equals(0));

        //recupera commenti dell utente
        List<Comment> userComments = commentDao.getCommentsByMember(currentMemberId);
        if (userComments == null || userComments.isEmpty()) {
            showNoResultsMessage();
            return;
        }

        //recupera gli id dei brani commentati
        List<Integer> tracksIds = userComments.stream()
                .map(comment -> comment.getTrack().getId())
                .distinct()
                .collect(Collectors.toList());

        //pulizia griglia
        tracksGrid.getChildren().removeIf(node -> GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0);

        //popola griglia
        int row = 1;
        for (Integer trackId : tracksIds) {
            Track track = trackDao.getTrackById(trackId);
            if (track != null) {
                addTrackToGrid(track, row);
                row++;
            }
        }

        //se non ha commentato
        if (row == 1) {
            Label noResults = new Label("Non hai ancora commentato alcun brano");
            noResults.getStyleClass().add("No-results-message");
            mainContent.getChildren().add(noResults);
        }
    }

    //BRANI COMMENTATI
    private void addTrackToGrid(Track track, int row) {
        Label titleLabel = new Label(track.getTitle());
        Label artistLabel = new Label(track.getAuthor().getName());
        Label yearLabel = new Label(track.getCompositionYear().toString());
        Label genreLabel = new Label(genreDao.getGenreById(track.getGenreId()).getDisplayName());/////

        Button viewButton = new Button("Vai");
        viewButton.getStyleClass().add("secondary-button");
        viewButton.setOnAction(event -> {
            if (userController != null) {
                //stato stack
                userController.loadTrackViewFromCommented(track.getId());
            }
        });

        titleLabel.getStyleClass().add("track-info");
        artistLabel.getStyleClass().add("track-info");
        yearLabel.getStyleClass().add("track-info");
        genreLabel.getStyleClass().add("track-info");

        tracksGrid.add(titleLabel, 0, row);
        tracksGrid.add(artistLabel, 1, row);
        tracksGrid.add(yearLabel, 2, row);
        tracksGrid.add(genreLabel, 3, row);
        tracksGrid.add(viewButton, 4, row);
    }

    //messaggio no commenti
    private void showNoResultsMessage() {
        Label noResults = new Label("Non hai ancora commentato alcun brano");
        noResults.getStyleClass().add("no-results-message");
        mainContent.getChildren().add(noResults);
    }
}