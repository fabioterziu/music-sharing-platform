package com.appmusicale.controller.member;

import com.appmusicale.controller.member.homebar.*;
import com.appmusicale.controller.member.sidebar.*;
import com.appmusicale.controller.member.topbar.TopBarController;
import com.appmusicale.controller.member.homebar.centre.TrackController;
import com.appmusicale.controller.member.topbar.UploadConcertController;
import com.appmusicale.controller.member.topbar.UploadTrackController;
import com.appmusicale.model.Author;
import com.appmusicale.model.Concert;
import com.appmusicale.model.GenreType;
import com.appmusicale.model.Performer;
import com.appmusicale.util.SceneManagerUtils;
import com.appmusicale.util.NavigationManagerUtils;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

import java.util.HashMap;
import java.util.Map;

//CONTROLLER USER (gestione)

public class UserController {
    @FXML private BorderPane mainContainer;
    @FXML private TopBarController topBarController;
    @FXML private SideBarController sideBarController;
    @FXML private HomeController currentViewController;

    @FXML
    private void initialize() {
        sideBarController.setUserController(this);
        topBarController.setUserController(this);
        if (currentViewController != null) {
            currentViewController.setUserController(this);
        }
        mainContainer.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }

    //get per maincontainer
    public BorderPane getMainContainer() {
        return mainContainer;
    }

    //carica vista in base al controlelr
    public void loadView(String fxmlPath) {
        Object controller = SceneManagerUtils.loadViewInBorderPane(mainContainer, fxmlPath);
        if (controller instanceof UploadTrackController) {
            ((UploadTrackController) controller).setUserController(this);
        } else if (controller instanceof HomeController) {
            ((HomeController) controller).setUserController(this);
        } else if (controller instanceof UploadConcertController) {
            ((UploadConcertController) controller).setUserController(this);
        } else if (controller instanceof CommentedController) {
            ((CommentedController) controller).setUserController(this);
        } else if (controller instanceof ConcertsController) {
            ((ConcertsController) controller).setUserController(this);
        } else if (controller instanceof ExploreController) {
            ((ExploreController) controller).setUserController(this);
        }
    }

    //CARICA HOME VIEW IN CATEGORIA SPECIFICA
    public void loadHomeWithExplore() {
        Object controller = SceneManagerUtils.loadViewInBorderPane(mainContainer,
                "/com/appmusicale/fxml/member/sidebar/home-view.fxml");
        if (controller instanceof HomeController) {
            ((HomeController) controller).setInitialCategoryExplore();
            ((HomeController) controller).setUserController(this);
        }
    }

    public void loadHomeWithGenres() {
        Object controller = SceneManagerUtils.loadViewInBorderPane(mainContainer,
                "/com/appmusicale/fxml/member/sidebar/home-view.fxml");
        if (controller instanceof HomeController) {
            ((HomeController) controller).setInitialCategoryGenres();
            ((HomeController) controller).setUserController(this);
        }
    }

    public void loadHomeWithAuthors() {
        Object controller = SceneManagerUtils.loadViewInBorderPane(mainContainer,
                "/com/appmusicale/fxml/member/sidebar/home-view.fxml");
        if (controller instanceof HomeController) {
            ((HomeController) controller).setInitialCategoryAuthors();
            ((HomeController) controller).setUserController(this);
        }
    }

    public void loadHomeWithPerformers() {
        Object controller = SceneManagerUtils.loadViewInBorderPane(mainContainer,
                "/com/appmusicale/fxml/member/sidebar/home-view.fxml");
        if (controller instanceof HomeController) {
            ((HomeController) controller).setInitialCategoryPerformers();
            ((HomeController) controller).setUserController(this);
        }
    }

    public void loadTrackView(int trackId) {
        Object controller = SceneManagerUtils.loadViewInBorderPane(mainContainer,
                "/com/appmusicale/fxml/member/homebar/centre/track-view.fxml");
        if (controller instanceof TrackController) {
            ((TrackController) controller).setTrackId(trackId);
            ((TrackController) controller).setUserController(this);
        }
    }

    //carica vista brano da explore + salva stato precedente
    public void loadTrackViewFromExplore(int trackId) {
        //recupera istanza singleton
        NavigationManagerUtils navManager = NavigationManagerUtils.getInstance();

        //mappa di attributi per salvare lo stato (nel record)
        Map<String, Object> attributes = new HashMap<>();

        navManager.pushViewState(
                "/com/appmusicale/fxml/member/homebar/explore-view.fxml", //fxml
                "ExploreController", //controller
                attributes //attributi
        );

        loadTrackView(trackId);
    }

    //carica vista brano da mieicommenti + salva stato precedente
    public void loadTrackViewFromCommented(int trackId) {
        NavigationManagerUtils navManager = NavigationManagerUtils.getInstance();
        Map<String, Object> attributes = new HashMap<>();
        navManager.pushViewState(
                "/com/appmusicale/fxml/member/sidebar/commented-view.fxml",
                "CommentedController",
                attributes
        );

        loadTrackView(trackId);
    }

    public void loadConcertDetailsView(Concert concert) {
        Object controller = SceneManagerUtils.loadViewInBorderPane(mainContainer,
                "/com/appmusicale/fxml/member/sidebar/concert-details-view.fxml");
        if (controller instanceof ConcertDetailsController) {
            ((ConcertDetailsController) controller).setConcert(concert);
            ((ConcertDetailsController) controller).setUserController(this);
        }
    }

    public void loadAuthorView(Author author) {
        Object controller = SceneManagerUtils.loadViewInBorderPane(mainContainer,
                "/com/appmusicale/fxml/member/homebar/author-list-view.fxml");
        if (controller instanceof AuthorListController) {
            ((AuthorListController) controller).setAuthor(author);
            ((AuthorListController) controller).setUserController(this);
        }
    }

    //carica vista brano da authorlist + salva stato precedente
    public void loadTrackViewFromAuthorList(int trackId, Author author) {
        NavigationManagerUtils navManager = NavigationManagerUtils.getInstance();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("author", author);
        navManager.pushViewState(
                "/com/appmusicale/fxml/member/homebar/author-list-view.fxml",
                "AuthorListController",
                attributes
        );

        loadTrackView(trackId);
    }

    public void loadGenreView(GenreType genre) {
        Object controller = SceneManagerUtils.loadViewInBorderPane(mainContainer,
                "/com/appmusicale/fxml/member/homebar/genre-list-view.fxml");
        if (controller instanceof GenreListController) {
            ((GenreListController) controller).setGenre(genre);
            ((GenreListController) controller).setUserController(this);
        }
    }

    //carica vista brano da genrelist + salva stato precedente
    public void loadTrackViewFromGenreList(int trackId, GenreType genre) {
        NavigationManagerUtils navManager = NavigationManagerUtils.getInstance();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("genre", genre);
        navManager.pushViewState(
                "/com/appmusicale/fxml/member/homebar/genre-list-view.fxml",
                "GenreListController",
                attributes
        );

        loadTrackView(trackId);
    }

    public void loadPerformerView(Performer performer) {
        Object controller = SceneManagerUtils.loadViewInBorderPane(mainContainer,
                "/com/appmusicale/fxml/member/homebar/performer-list-view.fxml");
        if (controller instanceof PerformerListController) {
            ((PerformerListController) controller).setPerformer(performer);
            ((PerformerListController) controller).setUserController(this);
        }
    }

    //carica vista brano da performerlist + salva stato precedente
    public void loadTrackViewFromPerformerList(int trackId, Performer performer) {
        NavigationManagerUtils navManager = NavigationManagerUtils.getInstance();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("performer", performer);
        navManager.pushViewState(
                "/com/appmusicale/fxml/member/homebar/performer-list-view.fxml",
                "PerformerListController",
                attributes
        );

        loadTrackView(trackId);
    }
}