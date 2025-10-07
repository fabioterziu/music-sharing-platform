package com.appmusicale.controller.member.homebar;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.dao.AuthorDao;
import com.appmusicale.dao.PerformerDao;
import com.appmusicale.dao.PerformerDaoImpl;
import com.appmusicale.model.Performer;
import com.appmusicale.util.SceneManagerUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.util.List;

public class PerformersController {
    @FXML private GridPane performersGrid;
    @FXML private VBox contentContainer;
    private UserController userController;

    PerformerDao performerDao= new PerformerDaoImpl();

    @FXML
    private void initialize() {
        System.out.println("PerformersController inizializzato");
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
        loadPerformers();
    }

    private void loadPerformers() {
        performersGrid.getChildren().clear();
        List<Performer> performers = performerDao.getAllPerformers();

        int col = 0;
        int row = 0;

        for (Performer performer : performers) {
            VBox performerCard = createPerformerCard(performer);
            performersGrid.add(performerCard, col, row);

            col++;
            if (col == 5) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createPerformerCard(Performer performer) {
        VBox card = new VBox(5);
        card.getStyleClass().add("performer-card");
        card.setOnMouseClicked(event -> loadPerformerListView(performer));

        ImageView imageView = new ImageView();
        imageView.setFitWidth(120);
        imageView.setFitHeight(120);
        imageView.getStyleClass().add("performer-image");

        //copertina di default
        String defaultImagePath = "/com/appmusicale/images/performer.jpg";
        imageView.setImage(new Image(getClass().getResourceAsStream(defaultImagePath)));

        Label nameLabel = new Label(performer.getName());
        nameLabel.getStyleClass().add("performer-name");
        nameLabel.setMaxWidth(120);
        nameLabel.setWrapText(true);

        card.getChildren().addAll(imageView, nameLabel);
        return card;
    }

    private void loadPerformerListView(Performer performer) {
        Object controller = SceneManagerUtils.loadViewInVBox(
                contentContainer,
                "/com/appmusicale/fxml/member/homebar/performer-list-view.fxml"
        );

        if (controller instanceof PerformerListController) {
            ((PerformerListController) controller).setUserController(userController);
            ((PerformerListController) controller).setPerformer(performer);
        }
    }
}