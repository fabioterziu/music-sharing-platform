package com.appmusicale.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionUtils {
    //CREO SUBITO ISTANZA
    private static final DatabaseConnectionUtils instance = new DatabaseConnectionUtils();

    //URL DEL DATABASE
    private static final String DB_URL = "jdbc:sqlite:appmusicale.db";

    //COSTRUTTORE PPRIVATO
    private DatabaseConnectionUtils() {
    }

    //METODO STATICO PER OTTENERE L'ISTANZA
    public static DatabaseConnectionUtils getInstance() {
        return instance;
    }


    // METODO PUBBLICO PER OTTENERE LA CONNESSIONE AL DATABASE
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }


}