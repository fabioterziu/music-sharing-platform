package com.appmusicale.dao;

import com.appmusicale.model.*;
import com.appmusicale.util.DatabaseConnectionUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MediaDaoImpl implements MediaDao {
    private static final String INSERT = "INSERT INTO MEDIA(TITLE, PATH, TYPE, TRACK_ID, MEMBER_ID) VALUES(?,?,?,?,?)";
    private static final String DELETE =  "DELETE FROM MEDIA WHERE ID = ?";
    private static final String SELECT_BY_TRACK_ID = "SELECT * FROM MEDIA WHERE TRACK_ID = ?";

    //private final MemberDao memberDao;
    TrackDao trackDao = new TrackDaoImpl();
    MemberDao memberDao = new MemberDaoImpl();

    public void insertMedia(Media media) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            setMediaParameters(pstmt, media);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    media.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nell'inserimento del media: " + e.getMessage());
        }
    }

    public List<Media> getMediaByTrackId(int trackId) {
        List<Media> mediaList = new ArrayList<>();
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_TRACK_ID)) {
            pstmt.setInt(1, trackId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                mediaList.add(createMediaFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero media per TRACK_ID: " + e.getMessage());
        }
        return mediaList;
    }

    public boolean deleteMedia(int mediaId) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE)) {
            pstmt.setInt(1, mediaId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore nella cancellazione del media: " + e.getMessage());
            return false;
        }
    }

    private Media createMediaFromResultSet(ResultSet rs) throws SQLException {
        Media media = new Media();
        media.setId(rs.getInt("ID"));
        media.setTitle(rs.getString("TITLE"));
        media.setPath(rs.getString("PATH"));
        Track track = trackDao.getTrackById(rs.getInt("TRACK_ID"));
        media.setTrack(track);
        Member member = memberDao.getMemberById(rs.getInt("MEMBER_ID"));
        media.setMember(member);
        media.setType(rs.getString("TYPE"));
        return media;
    }

    private static void setMediaParameters(PreparedStatement pstmt, Media media) throws SQLException {
        pstmt.setString(1, media.getTitle());
        pstmt.setString(2, media.getPath());
        pstmt.setString(3, media.getType());
        pstmt.setInt(4, media.getTrack().getId());
        pstmt.setInt(5, media.getMember().getId());
    }
}