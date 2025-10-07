package com.appmusicale.dao;

import com.appmusicale.model.Concert;
import com.appmusicale.model.Member;
import com.appmusicale.util.DatabaseConnectionUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConcertDaoImpl implements ConcertDao {

    private static final String INSERT = "INSERT INTO CONCERT(TITLE, YOUTUBE_URL, YOUTUBE_ID, THUMBNAIL_URL, MEMBER_ID) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_ALL = "SELECT * FROM CONCERT";
    private static final String SELECT_BY_ID = "SELECT * FROM CONCERT WHERE ID = ?";
    private static final String SELECT_BY_YOUTUBE_ID = "SELECT * FROM CONCERT WHERE YOUTUBE_ID = ?";
    private static final String DELETE_BY_ID = "DELETE FROM CONCERT WHERE ID = ?";

    MemberDao memberDao = new MemberDaoImpl();

    public void insertConcert(Concert concert) {
        try(Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(INSERT, PreparedStatement.RETURN_GENERATED_KEYS)) {

            setConcertParameters(pstmt, concert);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    concert.setId(rs.getInt(1));
                }
            }
        } catch (Exception e) {
            System.err.println("Errore nell'inserimento del concerto: " + e.getMessage());
        }
    }

    public List<Concert> getAllConcerts() {
        List<Concert> concerts = new ArrayList<>();

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL)) {
            while (rs.next()) {
                concerts.add(createConcertFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero concerti: "+ e.getMessage());
        }
        return concerts;
    }

    public Concert getConcertById(int id) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createConcertFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero concerto per ID: " + e.getMessage());
        }
        return null;
    }

    public Concert getConcertByYoutubeId(String youtubeId) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_YOUTUBE_ID)) {

            pstmt.setString(1, youtubeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createConcertFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero concerto per YouTube ID: " + e.getMessage());
        }
        return null;
    }

    public boolean deleteConcert(int id) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_BY_ID)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Errore nell'eliminazione concerto: " + e.getMessage());
            return false;
        }
    }

    private Concert createConcertFromResultSet(ResultSet rs) throws SQLException {
        Concert concert = new Concert();
        concert.setId(rs.getInt("ID"));
        concert.setTitle(rs.getString("TITLE"));
        concert.setYoutubeUrl(rs.getString("YOUTUBE_URL"));
        concert.setYoutubeId(rs.getString("YOUTUBE_ID"));
        concert.setThumbnailUrl(rs.getString("THUMBNAIL_URL"));
        // Recupera member
        int memberId = rs.getInt("MEMBER_ID");
        if (!rs.wasNull()) {
            Member member = memberDao.getMemberById(memberId);
            concert.setMember(member);
        }
        return concert;
    }

    private static void setConcertParameters(PreparedStatement pstmt, Concert concert) throws SQLException {
        pstmt.setString(1, concert.getTitle());
        pstmt.setString(2, concert.getYoutubeUrl());
        pstmt.setString(3, concert.getYoutubeId());
        pstmt.setString(4, concert.getThumbnailUrl());
        pstmt.setInt(5, concert.getMember().getId());
    }
}
