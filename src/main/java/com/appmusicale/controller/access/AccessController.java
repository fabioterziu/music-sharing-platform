package com.appmusicale.controller.access;

import com.appmusicale.service.AccessService;
import com.appmusicale.model.LogSignResult;
import com.appmusicale.service.TrackService;
import com.appmusicale.util.MemberContextUtils;
import com.appmusicale.util.SceneManagerUtils;
import com.appmusicale.util.UIVisibilityUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

//ACCEDI - REGISTRATI

public class AccessController {
    @FXML private ImageView logoImage;
    @FXML private VBox loginPane, signinPane;
    @FXML private TextField txtLoginUsername, txtSigninUsername, txtSigninEmail;
    @FXML private PasswordField txtLoginPassword, txtSigninPassword, txtSigninRepPassword;
    @FXML private Label errorLabelLogin, errorLabelSignin;

    AccessService accessService = new AccessService();



    @FXML
    public void initialize() {
        logoImage.setImage(
                new Image(getClass().getResourceAsStream("/com/appmusicale/images/logo.png")));
        mostraLogin();
        Platform.runLater(() -> loginPane.requestFocus()); //rimuovo focus

        setupEnterKeyHandlers();
    }


    //PREMI BOTTONE 'ACCEDI'
    @FXML
    private void ACCEDI() {
        String lUsername = txtLoginUsername.getText();
        String lPassword = txtLoginPassword.getText();

        LogSignResult result = this.accessService.attemptLogin(lUsername,lPassword);

        if(result.success()){
            MemberContextUtils.setLoggedInMember(result.member());
            SceneManagerUtils.setScene("/com/appmusicale/fxml/member/user-view.fxml");
        }
        else{
            UIVisibilityUtils.showError(errorLabelLogin, result.message());
        }

    }

    // PREMI BOTTONE 'REGISTRATI'
    @FXML
    private void REGISTRATI() {
        String sUsername = txtSigninUsername.getText();
        String sEmail = txtSigninEmail.getText();
        String sPassword = txtSigninPassword.getText();
        String repPassword = txtSigninRepPassword.getText();

        LogSignResult result = this.accessService.attemptSignin(sUsername, sEmail,sPassword, repPassword);


        if(!result.success()){
            UIVisibilityUtils.showError(errorLabelSignin, result.message());
        }

        else{ //se è tutto corretto
            clearLoginFields();
            UIVisibilityUtils.hideError(errorLabelLogin);

            mostraLogin();

            clearSigninFields();
            UIVisibilityUtils.hideError(errorLabelSignin);
        }
    }

    //INVIO CON TASTIERA ENTER
    private void setupEnterKeyHandlers() {
        //per login
        txtLoginUsername.setOnKeyPressed(this::handleLoginKeyPress);
        txtLoginPassword.setOnKeyPressed(this::handleLoginKeyPress);

        //per signin
        txtSigninUsername.setOnKeyPressed(this::handleSigninKeyPress);
        txtSigninEmail.setOnKeyPressed(this::handleSigninKeyPress);
        txtSigninPassword.setOnKeyPressed(this::handleSigninKeyPress);
        txtSigninRepPassword.setOnKeyPressed(this::handleSigninKeyPress);
    }

    private void handleLoginKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            ACCEDI();
        }
    }

    private void handleSigninKeyPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            REGISTRATI();
        }
    }

    //hyperlink 'login' da vista signin a vista login
    @FXML
    private void linkLogin(ActionEvent event) {
        mostraLogin();
        clearSigninFields();
        UIVisibilityUtils.hideError(errorLabelSignin);
    }

    //hyperlink 'login' da vista login a vista signin
    @FXML
    private void linkSignin(ActionEvent event) {
        mostraSignin();
    }


    //MOSTRA VISTA LOGIN
    private void mostraLogin() {
        Platform.runLater(() -> loginPane.requestFocus()); //rimuovo focus
        UIVisibilityUtils.showPane(loginPane);
        UIVisibilityUtils.hidePane(signinPane);
    }
    //MOSTRA VISTA SIGNIN
    private void mostraSignin() {
        Platform.runLater(() -> signinPane.requestFocus()); //rimuovo focus
        UIVisibilityUtils.hidePane(loginPane);
        UIVisibilityUtils.showPane(signinPane);
    }

    //PULISCI CAMPI SIGNIN
    private void clearSigninFields() {
        txtSigninUsername.clear();
        txtSigninEmail.clear();
        txtSigninPassword.clear();
        txtSigninRepPassword.clear();
    }

    //PULISCI CAMPI LOGIN
    private void clearLoginFields() {
        txtLoginUsername.clear();
        txtLoginPassword.clear();
    }

}