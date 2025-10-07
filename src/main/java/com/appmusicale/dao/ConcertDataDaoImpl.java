package com.appmusicale.dao;

import com.appmusicale.model.Concert;
import com.appmusicale.model.ConcertData;
import com.appmusicale.model.Member;
import com.appmusicale.util.DatabaseConnectionUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConcertDataDaoImpl implements ConcertDataDao {

    private static final String INSERT = "INSERT INTO CONCERT_DATA(CONCERT_ID, TRACK_TITLE, TRACK_ARTIST, START_TIME, END_TIME, PERFORMERS, INSTRUMENTS, DATE, PLACE, MEMBER_ID) VALUES(?,?,?,?,?,?,?,?,?,?)";
    private static final String SELECT_BY_ID = "SELECT * FROM CONCERT_DATA WHERE ID = ?";
    private static final String SELECT_BY_CONCERT = "SELECT * FROM CONCERT_DATA WHERE CONCERT_ID = ? ORDER BY START_TIME ASC";
    private static final String UPDATE = "UPDATE CONCERT_DATA SET PERFORMERS = ?, INSTRUMENTS = ?, DATE = ?, PLACE = ? WHERE ID = ?";
    private static final String DELETE_BY_ID = "DELETE FROM CONCERT_DATA WHERE ID = ?";

    ConcertDao concertDao = new ConcertDaoImpl();
    MemberDao memberDao = new MemberDaoImpl();

    public void insertConcertData(ConcertData concertData) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            setConcertDataParameters(pstmt, concertData);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    concertData.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nell'inserimento dati concerto: " + e.getMessage());
        }
    }

    public ConcertData getConcertDataById(int id) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createConcertDataFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero dati concerto per ID: " + e.getMessage());
        }
        return null;
    }

    public List<ConcertData> getConcertDataByConcert(int concertId) {
        List<ConcertData> concertDataList = new ArrayList<>();

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_CONCERT)) {

            pstmt.setInt(1, concertId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                concertDataList.add(createConcertDataFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero dati concerto per concerto: " + e.getMessage());
        }
        return concertDataList;
    }

    public boolean updateConcertData(ConcertData concertData) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE)) {

            pstmt.setString(1, concertData.getPerformer());
            pstmt.setString(2, concertData.getInstrument());
            pstmt.setString(3, concertData.getDate());
            pstmt.setString(4, concertData.getPlace());
            pstmt.setInt(5, concertData.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Errore nell'aggiornamento dati concerto: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteConcertData(int id) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE_BY_ID)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Errore nell'eliminazione dati concerto: " + e.getMessage());
            return false;
        }
    }

    private ConcertData createConcertDataFromResultSet(ResultSet rs) throws SQLException {
        ConcertData concertData = new ConcertData();
        concertData.setId(rs.getInt("ID"));

        // Recupera concert
        int concertId = rs.getInt("CONCERT_ID");
        if (!rs.wasNull()) {
            Concert concert = concertDao.getConcertById(concertId);
            concertData.setConcert(concert);
        }

        concertData.setTrackTitle(rs.getString("TRACK_TITLE"));
        concertData.setTrackAuthor(rs.getString("TRACK_ARTIST"));
        concertData.setStartTime(rs.getInt("START_TIME"));
        concertData.setEndTime(rs.getInt("END_TIME"));
        concertData.setPerformer(rs.getString("PERFORMERS"));
        concertData.setInstrument(rs.getString("INSTRUMENTS"));
        concertData.setDate(rs.getString("DATE"));
        concertData.setPlace(rs.getString("PLACE"));

        // Recupera member
        int memberId = rs.getInt("MEMBER_ID");
        if (!rs.wasNull()) {
            Member member = memberDao.getMemberById(memberId);
            concertData.setMember(member);
        }

        return concertData;
    }

    private static void setConcertDataParameters(PreparedStatement pstmt, ConcertData concertData) throws SQLException {
        pstmt.setInt(1, concertData.getConcert().getId());
        pstmt.setString(2, concertData.getTrackTitle());
        pstmt.setString(3, concertData.getTrackAuthor());
        pstmt.setInt(4, concertData.getStartTime());
        pstmt.setInt(5, concertData.getEndTime());
        pstmt.setString(6, concertData.getPerformer());
        pstmt.setString(7, concertData.getInstrument());
        pstmt.setString(8, concertData.getDate());
        pstmt.setString(9, concertData.getPlace());
        pstmt.setInt(10, concertData.getMember().getId());
    }
}