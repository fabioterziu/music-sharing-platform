package com.appmusicale.controller.member.homebar;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.dao.AuthorDao;
import com.appmusicale.dao.AuthorDaoImpl;
import com.appmusicale.model.Author;
import com.appmusicale.util.SceneManagerUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.util.List;

public class AuthorsController {
    @FXML private GridPane authorsGrid;
    @FXML private VBox contentContainer;
    private UserController userController;

    AuthorDao authorDao = new AuthorDaoImpl();


    @FXML
    private void initialize() {
        System.out.println("AuthorsController inizializzato");
    }


    public void setUserController(UserController userController) {
        this.userController = userController;
        loadAuthors();
    }

    private void loadAuthors() {
        authorsGrid.getChildren().clear();
        List<Author> authors = authorDao.getAllAuthors();

        int col = 0;
        int row = 0;

        for (Author author : authors) {
            VBox authorCard = createAuthorCard(author);
            authorsGrid.add(authorCard, col, row);

            col++;
            if (col == 5) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createAuthorCard(Author author) {
        VBox card = new VBox(5);
        card.getStyleClass().add("author-card");
        card.setOnMouseClicked(event -> loadAuthorListView(author));

        ImageView imageView = new ImageView();
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.getStyleClass().add("author-image");

        //copertina di default
        String defaultImagePath = "/com/appmusicale/images/author.jpg";
        imageView.setImage(new Image(getClass().getResourceAsStream(defaultImagePath)));

        Label nameLabel = new Label(author.getName());
        nameLabel.getStyleClass().add("author-name");
        nameLabel.setMaxWidth(120);
        nameLabel.setWrapText(true);

        card.getChildren().addAll(imageView, nameLabel);
        return card;
    }

    private void loadAuthorListView(Author author) {
        Object controller = SceneManagerUtils.loadViewInVBox(
                contentContainer,
                "/com/appmusicale/fxml/member/homebar/author-list-view.fxml"
        );

        if (controller instanceof AuthorListController) {
            ((AuthorListController) controller).setUserController(userController);
            ((AuthorListController) controller).setAuthor(author);
        }
    }
}