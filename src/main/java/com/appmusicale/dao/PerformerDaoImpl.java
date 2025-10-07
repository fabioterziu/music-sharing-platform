package com.appmusicale.dao;

import com.appmusicale.model.Performer;
import com.appmusicale.util.DatabaseConnectionUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PerformerDaoImpl implements PerformerDao {
    private static final String INSERT = "INSERT INTO PERFORMER(NAME, IMAGE_PATH) VALUES(?, ?)";
    private static final String SELECT_ALL = "SELECT * FROM PERFORMER ORDER BY NAME";
    private static final String SELECT_BY_NAME = "SELECT * FROM PERFORMER WHERE UPPER(NAME) = UPPER(?)";
    private static final String SEARCH_BY_NAME = "SELECT * FROM PERFORMER WHERE UPPER(NAME) LIKE UPPER(?) ORDER BY NAME";
    private static final String DELETE = "DELETE FROM PERFORMER WHERE ID NOT IN (SELECT DISTINCT PERFORMER_ID FROM TRACK_PERFORMER)";

    public void insertPerformer(Performer performer) {
        if (performer.getName() == null || performer.getName().trim().isEmpty()) {
            System.err.println("Non è possibile inserire un esecutore senza nome");
            return;
        }

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            setPerformerParameters(pstmt, performer);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    performer.setId(rs.getInt(1));
                    System.out.println("Esecutore inserito con ID: " + performer.getId());
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nell'inserimento dell'esecutore: " + e.getMessage());
        }
    }

    public List<Performer> getAllPerformers() {
        List<Performer> performers = new ArrayList<>();
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL)) {
            while (rs.next()) {
                performers.add(createPerformerFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero esecutori: " + e.getMessage());
        }
        return performers;
    }

    public Performer getOrCreatePerformer(String performerName) {
        if (performerName == null || performerName.trim().isEmpty()) {
            return null;
        }

        performerName = performerName.trim();
        Performer existingPerformer = getPerformerByName(performerName);

        if (existingPerformer != null) {
            return existingPerformer;
        }

        Performer newPerformer = new Performer();
        newPerformer.setName(performerName);
        insertPerformer(newPerformer);
        return getPerformerByName(performerName);
    }

    public Performer getPerformerByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_NAME)) {
            pstmt.setString(1, name.trim());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createPerformerFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero esecutore per nome: " + e.getMessage());
        }
        return null;
    }

    public List<Performer> searchPerformers(String query) {
        List<Performer> performers = new ArrayList<>();
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SEARCH_BY_NAME)) {
            pstmt.setString(1, "%" + query + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                performers.add(createPerformerFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca esecutori: " + e.getMessage());
        }
        return performers;
    }

    public void deleteOrphanedPerformers() {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore nell'eliminazione performer orfani: " + e.getMessage());
        }
    }

    private static Performer createPerformerFromResultSet(ResultSet rs) throws SQLException {
        Performer performer = new Performer();
        performer.setId(rs.getInt("ID"));
        performer.setName(rs.getString("NAME"));
        performer.setPhotoPath(rs.getString("IMAGE_PATH"));
        return performer;
    }

    private static void setPerformerParameters(PreparedStatement pstmt, Performer performer) throws SQLException {
        pstmt.setString(1, performer.getName().trim());
        pstmt.setString(2, performer.getPhotoPath());
    }
}