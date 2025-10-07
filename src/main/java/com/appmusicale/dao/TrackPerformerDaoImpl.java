package com.appmusicale.dao;

import com.appmusicale.model.Author;
import com.appmusicale.model.Performer;
import com.appmusicale.model.Track;
import com.appmusicale.util.DatabaseConnectionUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrackPerformerDaoImpl implements TrackPerformerDao {
    private static final String SELECT_PERFORMERS_BY_TRACK = "SELECT p.* FROM PERFORMER p " +
            "JOIN TRACK_PERFORMER tp ON p.ID = tp.PERFORMER_ID " +
            "WHERE tp.TRACK_ID = ? ";
    private static final String INSERT_TRACK_PERFORMER = "INSERT INTO TRACK_PERFORMER(TRACK_ID, PERFORMER_ID) VALUES(?, ?)";
    private static final String SELECT_TRACKS_BY_PERFORMER = "SELECT t.* FROM TRACK t " +
            "JOIN TRACK_PERFORMER tp ON t.ID = tp.TRACK_ID " +
            "WHERE tp.PERFORMER_ID = ? ORDER BY t.TITLE";

    AuthorDao authorDao = new AuthorDaoImpl();


    public List<Track> getTracksByPerformer(int performerId) {
        List<Track> tracks = new ArrayList<>();
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_TRACKS_BY_PERFORMER)) {
            pstmt.setInt(1, performerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tracks.add(createTrackFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero delle tracce per esecutore: " + e.getMessage());
        }
        return tracks;
    }

    public boolean associatePerformerToTrack(int trackId, int performerId) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT_TRACK_PERFORMER)) {
            pstmt.setInt(1, trackId);
            pstmt.setInt(2, performerId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Errore nell'associazione esecutore-traccia: " + e.getMessage());
        }
        return false;
    }

    public List<Performer> getPerformersByTrack(int trackId) {
        List<Performer> performers = new ArrayList<>();
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_PERFORMERS_BY_TRACK)) {
            pstmt.setInt(1, trackId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    performers.add(createPerformerFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero degli esecutori per traccia: " + e.getMessage());
        }
        return performers;
    }

    private static Performer createPerformerFromResultSet(ResultSet rs) throws SQLException {
        Performer performer = new Performer();
        performer.setId(rs.getInt("ID"));
        performer.setName(rs.getString("NAME"));
        performer.setPhotoPath(rs.getString("IMAGE_PATH"));
        return performer;
    }

    private  Track createTrackFromResultSet(ResultSet rs) throws SQLException {
        Track track = new Track();
        track.setId(rs.getInt("ID"));
        track.setTitle(rs.getString("TITLE"));
        track.setCompositionYear(rs.getInt("COMPOSITION_YEAR"));
        track.setYoutubeLink(rs.getString("YOUTUBE_LINK"));
        track.setCoverPath(rs.getString("COVER_PATH"));
        int authorId = rs.getInt("AUTHOR_ID");
        if (!rs.wasNull()) {
            Author author = authorDao.getAuthorById(authorId);
            track.setAuthor(author);
        }
        return track;
    }
}