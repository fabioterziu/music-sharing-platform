package com.appmusicale.controller.member.sidebar;

import com.appmusicale.dao.*;
import com.appmusicale.model.Member;
import com.appmusicale.model.Status;
import com.appmusicale.service.AccessService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

//ADMIN

public class AdminController {

    @FXML private ListView<VBox> pendingUsersList;  //lista utenti in attesa
    @FXML private ListView<VBox> registeredUsersList; //lista utenti accettati
    @FXML private Label noPendingRequestsLabel;
    @FXML private Label noUsersLabel;

    AccessService accessService = new AccessService();

    MemberDao memberDao = new MemberDaoImpl();


    @FXML
    private void initialize() {
        System.out.println("AdminController inizializzato");
        loadPendingUsers();
        loadRegisteredUsers();
    }

    //CARICA PAGINA 'UTENTI DA VERIFICARE'
    private void loadPendingUsers() {
        pendingUsersList.getItems().clear();

        List<Member> members =  memberDao.getAllMembersByStatus(Status.PENDING);

        if (members.isEmpty()) {
            noPendingRequestsLabel.setVisible(true);
            pendingUsersList.setVisible(false);
        } else {
            noPendingRequestsLabel.setVisible(false);
            pendingUsersList.setVisible(true);

            for (Member member : members) {
                VBox userCard = createPendingUserCard(member);
                pendingUsersList.getItems().add(userCard);
            }
        }
    }

    //CARICA PAGINA 'UTENTI REGISTRATI'
    private void loadRegisteredUsers() {
        registeredUsersList.getItems().clear();
        List<Member> members =  memberDao.getAllActiveUsers(Status.ACTIVE);

        if (members.isEmpty()) {
            noUsersLabel.setVisible(true);
            registeredUsersList.setVisible(false);
        } else {
            noUsersLabel.setVisible(false);
            registeredUsersList.setVisible(true);

            for (Member user : members) {
                VBox userCard = createRegisteredUserCard(user);
                registeredUsersList.getItems().add(userCard);
            }
        }
    }

    //SEZIONE ATTESA
    private VBox createPendingUserCard(Member member) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");
        card.setMaxWidth(Double.MAX_VALUE);

        HBox contentBox = new HBox();
        contentBox.setAlignment(Pos.CENTER_LEFT);

        Label emailLabel = new Label(member.getEmail());
        emailLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 14px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button acceptButton = new Button("Accetta");
        acceptButton.setStyle("-fx-background-color: #58BFC0; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 15;");
        acceptButton.setOnAction(e -> handleAcceptUser(member.getEmail()));

        Button rejectButton = new Button("Rifiuta");
        rejectButton.setStyle("-fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 15;");
        rejectButton.setOnAction(e -> handleDeactivateUser(member.getEmail()));

        buttonBox.getChildren().addAll(rejectButton, acceptButton);
        contentBox.getChildren().addAll(emailLabel, spacer, buttonBox);

        card.getChildren().add(contentBox);

        return card;
    }

    //SEZIONE REGISTRATI (UTENTI)
    private VBox createRegisteredUserCard(Member member) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 3, 0, 0, 1);");
        card.setMaxWidth(Double.MAX_VALUE);

        VBox infoBox = new VBox(5);

        Label nameLabel = new Label("Username: " + member.getUsername());
        nameLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 14px;");

        Label emailLabel = new Label("Email: " + member.getEmail());
        emailLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 14px;");

        infoBox.getChildren().addAll(nameLabel, emailLabel);

        HBox buttonBox = new HBox();
        buttonBox.setAlignment(Pos.CENTER_LEFT);
        buttonBox.setSpacing(10);


        Button rejectButton = new Button("Disattiva");
        rejectButton.setStyle("-fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 15;");
        rejectButton.setOnAction(e -> handleDeactivateUser(member.getEmail()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttonBox.getChildren().addAll(infoBox, spacer, rejectButton);

        card.getChildren().addAll(buttonBox);

        return card;
    }

    //ACCETTA
    private void handleAcceptUser(String userEmail) {
        System.out.println("Utente accettato: " + userEmail);

        accessService.changeStateToActive(userEmail);
        loadPendingUsers(); //ricarica lista
        loadRegisteredUsers(); //ricarica lista
    }

    //DISATTIVA
    private void handleDeactivateUser(String userEmail) {
        System.out.println("Account rifiutato/disattivato: " + userEmail);

        accessService.changeStateToBanned(userEmail);
        loadPendingUsers();
        loadRegisteredUsers();
    }

}
