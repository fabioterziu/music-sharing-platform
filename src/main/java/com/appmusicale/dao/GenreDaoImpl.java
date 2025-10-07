package com.appmusicale.dao;

import com.appmusicale.model.GenreType;
import com.appmusicale.util.DatabaseConnectionUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GenreDaoImpl implements GenreDao {

    private static final String INITIALIZE_GENRES = "INSERT INTO GENRE(NAME, DISPLAY_NAME) VALUES (?, ?)";
    private static final String SELECT_ALL = "SELECT * FROM GENRE ORDER BY UPPER(DISPLAY_NAME) ASC";
    private static final String SELECT_BY_NAME = "SELECT * FROM GENRE WHERE UPPER(NAME) = UPPER(?)";
    private static final String SELECT_BY_ID = "SELECT * FROM GENRE WHERE ID = ?";
    private static final String SEARCH_BY_GENRE = "SELECT * FROM GENRE WHERE UPPER(NAME) LIKE UPPER (?) OR UPPER(DISPLAY_NAME) LIKE UPPER(?)";

    // Inizializza il database con i generi predefiniti
    public void initializePredefinedGenres() {
        for (GenreType genre : GenreType.values()) {
            // Verifica se il genere esiste già
            if (getGenreByName(genre.name()) == null) {
                // Inserisce il genere predefinito
                try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(INITIALIZE_GENRES)) {

                    pstmt.setString(1, genre.name());
                    pstmt.setString(2, genre.getDisplayName());
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Errore nell'inserimento del genere predefinito " + genre.name() + ": " + e.getMessage());
                }
            }
        }
    }

    public List<GenreType> getAllGenres() {
        List<GenreType> genres = new ArrayList<>();

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL)) {

            while (rs.next()) {
                String name = rs.getString("NAME");
                try {
                    GenreType genre = GenreType.valueOf(name);
                    genres.add(genre);
                } catch (IllegalArgumentException e) {
                    System.err.println("Genere non riconosciuto nel database: " + name);
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nel caricamento dei generi: " + e.getMessage());
        }
        return genres;
    }

    // Ottiene un genere per nome solo se è predefinito
    public GenreType getGenreByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_NAME)) {

            pstmt.setString(1, name.trim());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return GenreType.valueOf(rs.getString("NAME"));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero genere per nome: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Genere non valido: " + name);
        }
        return null;
    }

    public Integer getGenreId(GenreType genre) {
        if (genre == null) {
            return null;
        }

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_NAME)) {

            pstmt.setString(1, genre.name());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("ID");
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero ID genere: " + e.getMessage());
        }
        return null;
    }

    public GenreType getGenreById(int id) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return GenreType.valueOf(rs.getString("NAME"));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero genere per ID: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("Genere non valido per ID: " + id);
        }
        return null;
    }

    public List<GenreType> searchGenres(String query) {
        List<GenreType> genres = new ArrayList<>();

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SEARCH_BY_GENRE)) {

            pstmt.setString(1, query);
            pstmt.setString(2, query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                try {
                    GenreType genre = GenreType.valueOf(rs.getString("NAME"));
                    genres.add(genre);
                } catch (IllegalArgumentException e) {
                    System.err.println("Genere non riconosciuto: " + rs.getString("NAME"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca generi: " + e.getMessage());
        }
        return genres;
    }
    // Ottiene solo i generi predefiniti
    public List<GenreType> getPredefinedGenres() {
        List<GenreType> genres = new ArrayList<>();

        for (GenreType genre : GenreType.values()) {
            // Verifica che il genere esista nel database
            if (getGenreByName(genre.name()) != null) {
                genres.add(genre);
            }
        }

        return genres;
    }
}