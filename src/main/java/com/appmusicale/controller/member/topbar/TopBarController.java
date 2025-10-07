package com.appmusicale.controller.member.topbar;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.dao.*;
import com.appmusicale.model.*;
import com.appmusicale.service.AccessService;
import com.appmusicale.util.MemberContextUtils;
import com.appmusicale.model.Track;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;

import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

//GESTIONE DELLA BARRA IN ALTO

public class TopBarController {

    @FXML private ImageView logoImage;
    @FXML private TextField searchField;
    @FXML private Label userNameLabel;

    private UserController userController;

    AuthorDao authorDao = new AuthorDaoImpl();
    PerformerDao performerDao = new PerformerDaoImpl();
    GenreDao genreDao = new GenreDaoImpl();
    TrackDao trackDao = new TrackDaoImpl();


    public void setUserController(UserController userController) {
        this.userController = userController;
    }


    @FXML
    private void initialize() {
        //logo
        logoImage.setImage(new Image(getClass().getResourceAsStream("/com/appmusicale/images/logoHome.png")));
        //nome user
        userNameLabel.setText(MemberContextUtils.getLoggedInMember().getUsername());
        //listener per ricerca in tempo reale
        searchField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                handleSearch();
            }
        });
    }

    //CARICA TRACCIA
    @FXML
    private void uploadTrack() {
        if (userController != null) {
            userController.loadView("/com/appmusicale/fxml/member/topbar/upload-track-view.fxml");
        }
    }

    //CARICA CONCERTO
    @FXML
    private void uploadConcert() {
        if (userController != null) {
            userController.loadView("/com/appmusicale/fxml/member/topbar/upload-concert-view.fxml");
        }
    }

    //CERCA
    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();

        //chiude il menu se la query è vuota
        if (query.isEmpty()) {
            ContextMenu existingMenu = searchField.getContextMenu();
            if (existingMenu != null && existingMenu.isShowing()) {
                existingMenu.hide();
            }
            return;
        }

        //ricerche in parallelo
        List<Track> tracks = trackDao.searchTracks("%" + query + "%");
        List<Author> authors = authorDao.searchAuthors("%" + query + "%");
        List<GenreType> genres = genreDao.searchGenres("%" + query + "%");
        List<Performer> performers = performerDao.searchPerformers("%" + query + "%");

        //context menu per i risultati
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("search-context-menu");

        //larghezza uguale a quella della search field
        contextMenu.setMaxWidth(searchField.getWidth());
        contextMenu.setPrefWidth(searchField.getWidth());

        //aggiunge risultati tracce
        if (!tracks.isEmpty()) {
            //aggiunge etichetta
            CustomMenuItem categoryItem = new CustomMenuItem(new Label("Brani"));
            categoryItem.getStyleClass().add("search-category-label");
            categoryItem.setDisable(true);
            contextMenu.getItems().add(categoryItem);

            for (Track track : tracks) {
                MenuItem item = new MenuItem("Brano: " + track.getTitle());
                item.getStyleClass().add("search-menu-item");
                item.setOnAction(e -> {
                    userController.loadTrackView(track.getId());
                    contextMenu.hide();
                });
                contextMenu.getItems().add(item);
            }

            //separatore
            contextMenu.getItems().add(new SeparatorMenuItem());
        }

        //risultati autori
        if (!authors.isEmpty()) {
            CustomMenuItem categoryItem = new CustomMenuItem(new Label("Autori"));
            categoryItem.getStyleClass().add("search-category-label");
            categoryItem.setDisable(true);
            contextMenu.getItems().add(categoryItem);

            for (Author author : authors) {
                MenuItem item = new MenuItem("Autore: " + author.getName());
                item.getStyleClass().add("search-menu-item");
                item.setOnAction(e -> {
                    userController.loadAuthorView(author);
                    contextMenu.hide();
                });
                contextMenu.getItems().add(item);
            }

            contextMenu.getItems().add(new SeparatorMenuItem());
        }


        //risultati generi
        if (!genres.isEmpty()) {
            CustomMenuItem categoryItem = new CustomMenuItem(new Label("Generi"));
            categoryItem.getStyleClass().add("search-category-label");
            categoryItem.setDisable(true);
            contextMenu.getItems().add(categoryItem);

            for (GenreType genre : genres) {
                MenuItem item = new MenuItem("Genere: " + genre.getDisplayName());
                item.getStyleClass().add("search-menu-item");
                item.setOnAction(e -> {
                    userController.loadGenreView(genre);
                    contextMenu.hide();
                });
                contextMenu.getItems().add(item);
            }

            contextMenu.getItems().add(new SeparatorMenuItem());
        }

        //risultati performer
        if (!performers.isEmpty()) {
            CustomMenuItem categoryItem = new CustomMenuItem(new Label("Esecutori"));
            categoryItem.getStyleClass().add("search-category-label");
            categoryItem.setDisable(true);
            contextMenu.getItems().add(categoryItem);

            for (Performer performer : performers) {
                MenuItem item = new MenuItem("Esecutore: " + performer.getName());
                item.getStyleClass().add("search-menu-item");
                item.setOnAction(e -> {
                    userController.loadPerformerView(performer);
                    contextMenu.hide();
                });
                contextMenu.getItems().add(item);
            }
        }

        if (contextMenu.getItems().isEmpty()) {
            MenuItem item = new MenuItem("Nessun risultato trovato");
            item.getStyleClass().add("search-menu-item");
            item.setDisable(true);
            contextMenu.getItems().add(item);
        }

        //nasconde il menu precedente se esiste
        ContextMenu existingMenu = searchField.getContextMenu();
        if (existingMenu != null && existingMenu.isShowing()) {
            existingMenu.hide();
        }

        //imposta nuovo menu come context menu della search field
        searchField.setContextMenu(contextMenu);

        //mostra menu sotto la barra di ricerca
        contextMenu.show(searchField, javafx.geometry.Side.BOTTOM, 0, 0);
    }
}