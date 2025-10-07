module com.appmusicale {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.base;
    requires java.desktop;
    requires javafx.graphics;
    requires jdk.jfr;
    requires javafx.media;
    requires javafx.web;
    requires java.net.http;
    requires org.kordamp.ikonli.javafx;
    requires org.apache.pdfbox;
    requires javafx.swing;

    //opens. package di controllers con @FXML
    opens com.appmusicale.controller.access to javafx.fxml;
    opens com.appmusicale.controller.member to javafx.fxml;
    opens com.appmusicale.controller.member.homebar to javafx.fxml;
    opens com.appmusicale.controller.member.homebar.centre to javafx.fxml;
    opens com.appmusicale.controller.member.sidebar to javafx.fxml;
    opens com.appmusicale.controller.member.topbar to javafx.fxml;

    //export. packages con logica
    exports com.appmusicale;
    exports com.appmusicale.model;
    exports com.appmusicale.util;
    exports com.appmusicale.service;
    exports com.appmusicale.dao;

    //export. per test
    exports com.appmusicale.controller.member.homebar.centre;
    exports com.appmusicale.controller.member.sidebar;
}