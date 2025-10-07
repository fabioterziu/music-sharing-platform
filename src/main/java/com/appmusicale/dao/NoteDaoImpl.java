package com.appmusicale.dao;

import com.appmusicale.model.Note;
import com.appmusicale.util.DatabaseConnectionUtils;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NoteDaoImpl implements NoteDao {
    private static final String INSERT = "INSERT INTO NOTE(CONTENT, CREATED_AT, START_TIME, END_TIME, MEMBER_ID, TRACK_ID, CONCERT_ID, CONCERT_DATA_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_TRACK = "SELECT * FROM NOTE WHERE TRACK_ID = ? ORDER BY CREATED_AT DESC";
    private static final String SELECT_BY_CONCERT_DATA = "SELECT * FROM NOTE WHERE CONCERT_DATA_ID = ? ORDER BY CREATED_AT DESC";
    private static final String DELETE = "DELETE FROM NOTE WHERE ID = ?";

    ConcertDao concertDao = new ConcertDaoImpl();
    ConcertDataDao concertDataDao= new ConcertDataDaoImpl();
    MemberDao memberDao = new MemberDaoImpl();
    TrackDao trackDao= new TrackDaoImpl();

    public boolean insertNote(Note note) {
        if (note.getContent() == null || note.getContent().trim().isEmpty()) {
            System.err.println("Impossibile inserire una nota senza contenuto.");
            return false;
        }

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            setNoteParameters(pstmt, note);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    note.setId(rs.getInt(1));
                    note.setCreatedAt(LocalDateTime.now());
                    return true;
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nell'inserimento della nota: " + e.getMessage());
        }
        return false;
    }

    public List<Note> getNotesByTrack(int trackId) {
        List<Note> notes = new ArrayList<>();
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_TRACK)) {
            pstmt.setInt(1, trackId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                notes.add(createNoteFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero delle note per brano: " + e.getMessage());
        }
        return notes;
    }

    public List<Note> getNotesByConcertData(int concertDataId) {
        List<Note> notes = new ArrayList<>();
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_CONCERT_DATA)) {
            pstmt.setInt(1, concertDataId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                notes.add(createNoteFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero delle note per dati concerto: " + e.getMessage());
        }
        return notes;
    }

    public boolean deleteNote(int id) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Errore nell'eliminazione della nota: " + e.getMessage());
        }
        return false;
    }

    private Note createNoteFromResultSet(ResultSet rs) throws SQLException {
        Note note = new Note();
        note.setId(rs.getInt("ID"));
        note.setContent(rs.getString("CONTENT"));
        note.setCreatedAt(rs.getTimestamp("CREATED_AT").toLocalDateTime());
        note.setStartTime(rs.getInt("START_TIME"));
        if (rs.wasNull()) note.setStartTime(null);
        note.setEndTime(rs.getInt("END_TIME"));
        if (rs.wasNull()) note.setEndTime(null);

        int memberId = rs.getInt("MEMBER_ID");
        if (!rs.wasNull()) {
            note.setMember(memberDao.getMemberById(memberId));
        }

        int trackId = rs.getInt("TRACK_ID");
        if (!rs.wasNull()) {
            note.setTrack(trackDao.getTrackById(trackId));
        }

        int concertId = rs.getInt("CONCERT_ID");
        if (!rs.wasNull()) {
            note.setConcert(concertDao.getConcertById(concertId));
        }

        int concertDataId = rs.getInt("CONCERT_DATA_ID");
        if (!rs.wasNull()) {
            note.setConcertData(concertDataDao.getConcertDataById(concertDataId));
        }

        return note;
    }

    private static void setNoteParameters(PreparedStatement pstmt, Note note) throws SQLException {
        pstmt.setString(1, note.getContent().trim());
        pstmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        pstmt.setObject(3, note.getStartTime(), Types.INTEGER);
        pstmt.setObject(4, note.getEndTime(), Types.INTEGER);
        pstmt.setObject(5, note.getMember() != null ? note.getMember().getId() : null, Types.INTEGER);
        pstmt.setObject(6, note.getTrack() != null ? note.getTrack().getId() : null, Types.INTEGER);
        pstmt.setObject(7, note.getConcert() != null ? note.getConcert().getId() : null, Types.INTEGER);
        pstmt.setObject(8, note.getConcertData() != null ? note.getConcertData().getId() : null, Types.INTEGER);
    }
}