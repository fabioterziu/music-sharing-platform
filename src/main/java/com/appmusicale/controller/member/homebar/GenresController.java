package com.appmusicale.controller.member.homebar;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.dao.AuthorDao;
import com.appmusicale.dao.GenreDao;
import com.appmusicale.dao.GenreDaoImpl;
import com.appmusicale.model.GenreType;
import com.appmusicale.util.SceneManagerUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.util.List;

public class GenresController {
    @FXML private GridPane genresGrid;
    @FXML private VBox contentContainer;
    private UserController userController;

    GenreDao genreDao= new GenreDaoImpl();

    @FXML
    private void initialize() {
        System.out.println("GenresController inizializzato");
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
        loadGenres();
    }

    private void loadGenres() {
        genresGrid.getChildren().clear();
        List<GenreType> genres = genreDao.getAllGenres();

        int col = 0;
        int row = 0;

        for (GenreType genre : genres) {
            VBox genreCard = createGenreCard(genre);
            genresGrid.add(genreCard, col, row);

            col++;
            if (col == 5) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createGenreCard(GenreType genre) {
        VBox card = new VBox(5);
        card.getStyleClass().add("genre-card");
        card.setOnMouseClicked(event -> loadGenreListView(genre));

        ImageView imageView = new ImageView();
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.getStyleClass().add("genre-image");

        //carico immagine copertina
        if (genre.getImagePath() != null) {
            imageView.setImage(new Image(getClass().getResourceAsStream(genre.getImagePath())));
        }

        Label nameLabel = new Label(genre.getDisplayName());
        nameLabel.getStyleClass().add("genre-name");
        nameLabel.setMaxWidth(120);
        nameLabel.setWrapText(true);

        card.getChildren().addAll(imageView, nameLabel);
        return card;
    }

    private void loadGenreListView(GenreType genre) {
        Object controller = SceneManagerUtils.loadViewInVBox(
                contentContainer,
                "/com/appmusicale/fxml/member/homebar/genre-list-view.fxml"
        );

        if (controller instanceof GenreListController) {
            ((GenreListController) controller).setUserController(userController);
            ((GenreListController) controller).setGenre(genre);
        }
    }
}