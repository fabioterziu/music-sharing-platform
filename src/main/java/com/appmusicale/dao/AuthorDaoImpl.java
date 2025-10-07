package com.appmusicale.dao;

import com.appmusicale.model.Author;
import com.appmusicale.util.DatabaseConnectionUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthorDaoImpl implements AuthorDao {
    private static final String SELECT_ALL = "SELECT * FROM AUTHOR ORDER BY UPPER(NAME) ASC";
    private static final String INSERT = "INSERT INTO AUTHOR(NAME) VALUES(?)";
    private static final String SELECT_BY_NAME = "SELECT * FROM AUTHOR WHERE UPPER(NAME) = UPPER(?)";
    private static final String SELECT_BY_ID = "SELECT * FROM AUTHOR WHERE ID = ?";
    private static final String SEARCH_BY_NAME = "SELECT * FROM AUTHOR WHERE UPPER(NAME) LIKE UPPER(?)";
    private static final String DELETE = "DELETE FROM AUTHOR WHERE ID NOT IN (SELECT DISTINCT AUTHOR_ID FROM TRACK WHERE AUTHOR_ID IS NOT NULL)";

    public List<Author> getAllAuthors() {
        List<Author> authors = new ArrayList<>();
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL)) {
            while (rs.next()) {
                authors.add(createAuthorFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero autori: " + e.getMessage());
        }
        return authors;
    }

    public boolean insertAuthor(Author author) {
        if (author.getName() == null || author.getName().trim().isEmpty()) {
            System.err.println("Impossibile inserire un autore senza nome.");
            return false;
        }
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, author.getName().trim());
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        author.setId(rs.getInt(1));
                        System.out.println("Autore inserito con ID: " + author.getId());
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nell'inserimento dell'autore: " + e.getMessage());
        }
        return false;
    }

    public Author getAuthorByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_NAME)) {
            pstmt.setString(1, name.trim());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createAuthorFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero autore per nome: " + e.getMessage());
        }
        return null;
    }

    public Author getOrCreateAuthor(String authorName) {
        if (authorName == null || authorName.trim().isEmpty()) {
            return null;
        }
        authorName = authorName.trim();
        Author existingAuthor = getAuthorByName(authorName);
        if (existingAuthor != null) {
            return existingAuthor;
        }
        Author newAuthor = new Author();
        newAuthor.setName(authorName);
        if (insertAuthor(newAuthor)) {
            return newAuthor;
        } else {
            return getAuthorByName(authorName);
        }
    }

    public Author getAuthorById(int id) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createAuthorFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero autore per ID: " + e.getMessage());
        }
        return null;
    }

    public List<Author> searchAuthors(String query) {
        List<Author> authors = new ArrayList<>();
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SEARCH_BY_NAME)) {
            pstmt.setString(1, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                authors.add(createAuthorFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca autori: " + e.getMessage());
        }
        return authors;
    }

    public void deleteOrphanedAuthors() {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE)) {

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore nell'eliminazione autori orfani: " + e.getMessage());
        }
    }

    private static Author createAuthorFromResultSet(ResultSet rs) throws SQLException {
        Author author = new Author();
        author.setId(rs.getInt("ID"));
        author.setName(rs.getString("NAME"));
        return author;
    }
}