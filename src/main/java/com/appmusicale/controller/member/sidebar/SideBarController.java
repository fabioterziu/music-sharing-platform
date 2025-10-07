package com.appmusicale.controller.member.sidebar;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.util.MemberContextUtils;
import com.appmusicale.util.NavigationManagerUtils;
import com.appmusicale.util.SceneManagerUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

//GESTIONE DELLA BARRA LATERALE

public class SideBarController {

    @FXML
    private Button Admin;

    private UserController userController;

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    @FXML
    public void initialize() {
        //se sono admin, visualizzo sezione admin
        boolean isAdmin = MemberContextUtils.isCurrentAdmin();
        Admin.setVisible(isAdmin); //se è admin= true, quindi setvisible true
    }

    @FXML
    private void loadHome() {
        if (userController != null) {
            userController.loadView("/com/appmusicale/fxml/member/sidebar/home-view.fxml");
        }
    }

    @FXML
    private void loadConcerts() {
        if (userController != null) {
            userController.loadView("/com/appmusicale/fxml/member/sidebar/concerts-view.fxml");
        }
    }

    @FXML
    private void loadCommented() {
        if (userController != null) {
            userController.loadView("/com/appmusicale/fxml/member/sidebar/commented-view.fxml");
        }
    }

    @FXML
    private void loadAdmin() {
        if (userController != null) {
            userController.loadView("/com/appmusicale/fxml/member/sidebar/admin-view.fxml");
        }
    }


    @FXML
    private void logout() {
        MemberContextUtils.clear();
        NavigationManagerUtils.getInstance().clearStack();
        SceneManagerUtils.setScene("/com/appmusicale/fxml/access/access-view.fxml");
    }

}
