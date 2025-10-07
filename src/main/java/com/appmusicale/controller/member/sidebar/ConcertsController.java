package com.appmusicale.controller.member.sidebar;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.dao.ConcertDao;
import com.appmusicale.dao.ConcertDaoImpl;
import com.appmusicale.model.Concert;
import com.appmusicale.util.SceneManagerUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import java.util.Comparator;
import java.util.List;

//SEZIONE CONCERTI

public class ConcertsController {
    @FXML private GridPane concertsGrid;
    @FXML private VBox contentContainer;


    private UserController userController;

    ConcertDao concertDao= new ConcertDaoImpl();

    @FXML
    private void initialize() {
        System.out.println("ConcertsController inizializzato");
        loadConcerts();
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    //CARICA I CONCERTI
    private void loadConcerts() {
        concertsGrid.getChildren().clear();
        List<Concert> concerts = concertDao.getAllConcerts();
        concerts.sort(Comparator.comparing(Concert::getTitle));

        int col = 0;
        int row = 0;

        for (Concert concert : concerts) {
            VBox concertCard = createConcertCard(concert);
            concertsGrid.add(concertCard, col, row);

            col++;
            if (col == 3) {
                col = 0;
                row++;
            }
        }
    }

    //CARD CONCERTO
    private VBox createConcertCard(Concert concert) {
        VBox card = new VBox(5);
        card.getStyleClass().add("concert-card");
        card.setPrefSize(180, 200);
        card.setOnMouseClicked(event -> openConcertDetails(concert));

        ImageView imageView = new ImageView();
        imageView.setFitWidth(160);
        imageView.setFitHeight(120);
        imageView.getStyleClass().add("concert-image");
        imageView.setPreserveRatio(true);

        if (concert.getThumbnailUrl() != null && !concert.getThumbnailUrl().isEmpty()) {
            try {
                Image image = new Image(concert.getThumbnailUrl(), true);
                imageView.setImage(image);
            } catch (Exception e) {
                imageView.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 5;");
            }
        } else {
            imageView.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 5;");
        }

        Label titleLabel = new Label(concert.getTitle());
        titleLabel.getStyleClass().add("concert-title");
        titleLabel.setMaxWidth(160);
        titleLabel.setWrapText(true);

        card.getChildren().addAll(imageView, titleLabel);
        return card;
    }

    //APRI DETTAGLI CONCERTO
    private void openConcertDetails(Concert concert) {
        Object controller = SceneManagerUtils.loadViewInVBox( contentContainer,
                "/com/appmusicale/fxml/member/sidebar/concert-details-view.fxml"
        );

        if (controller instanceof ConcertDetailsController) {
            ((ConcertDetailsController) controller).setConcert(concert);
            ((ConcertDetailsController) controller).setUserController(this.userController);
        }
    }
}