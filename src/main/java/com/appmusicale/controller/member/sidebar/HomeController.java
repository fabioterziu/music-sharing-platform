package com.appmusicale.controller.member.sidebar;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.controller.member.homebar.*;
import com.appmusicale.util.SceneManagerUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

//CONTROLLER SEZIONE HOME PRINCIPALE

public class HomeController {
    @FXML private Label categotyLabel;
    @FXML private Button exploreButton;
    @FXML private Button authorsButton;
    @FXML private Button genresButton;
    @FXML private Button performersButton;
    @FXML private VBox contentContainer;

    private UserController userController;
    private String initialCategory = null; //per gestire la categoria iniziale

    public void setUserController(UserController userController) {
        this.userController = userController;

        //Se non è specificata una categoria iniziale, mostra explore
        if (initialCategory == null) {
            showExplore();
        } else {
            //carica la categoria specificata
            switch (initialCategory) {
                case "explore":
                    showExplore();
                    break;
                case "genres":
                    showGenres();
                    break;
                case "authors":
                    showAuthors();
                    break;
                case "performers":
                    showPerformers();
                    break;
                default:
                    showExplore();
                    break;
            }
            initialCategory = null;
        }
    }

    //SET CATEGORIE
    public void setInitialCategoryExplore() {
        this.initialCategory = "explore";
    }

    public void setInitialCategoryGenres() {
        this.initialCategory = "genres";
    }

    public void setInitialCategoryAuthors() {
        this.initialCategory = "authors";
    }

    public void setInitialCategoryPerformers() {
        this.initialCategory = "performers";
    }

    @FXML
    private void initialize() {
        System.out.println("HomeController inizializzato");
    }

    @FXML
    private void showExplore() {
        categotyLabel.setText("Esplora");
        loadContentView("/com/appmusicale/fxml/member/homebar/explore-view.fxml");
    }

    @FXML
    private void showAuthors() {
        categotyLabel.setText("Autori");
        loadContentView("/com/appmusicale/fxml/member/homebar/authors-view.fxml");
    }

    @FXML
    private void showGenres() {
        categotyLabel.setText("Generi");
        loadContentView("/com/appmusicale/fxml/member/homebar/genres-view.fxml");
    }

    @FXML
    private void showPerformers() {
        categotyLabel.setText("Esecutori");
        loadContentView("/com/appmusicale/fxml/member/homebar/performers-view.fxml");
    }

    //CARICA VISTA DAL CONTROLLER
    private void loadContentView(String fxmlPath) {
        Object controller = SceneManagerUtils.loadViewInVBox(contentContainer, fxmlPath);

        if (controller instanceof ExploreController) {
            ((ExploreController) controller).setUserController(userController);
        } else if (controller instanceof AuthorsController) {
            ((AuthorsController) controller).setUserController(userController);
        } else if (controller instanceof GenresController) {
            ((GenresController) controller).setUserController(userController);
        } else if (controller instanceof PerformersController) {
            ((PerformersController) controller).setUserController(userController);
        } else if (controller instanceof AuthorListController) {
            ((AuthorListController) controller).setUserController(userController);
        } else if (controller instanceof GenreListController) {
            ((GenreListController) controller).setUserController(userController);
        } else if (controller instanceof PerformerListController) {
            ((PerformerListController) controller).setUserController(userController);
        }
    }
}