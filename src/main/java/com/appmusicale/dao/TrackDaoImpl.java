package com.appmusicale.dao;

import com.appmusicale.model.Author;
import com.appmusicale.model.Track;
import com.appmusicale.model.Member;
import com.appmusicale.util.DatabaseConnectionUtils;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class    TrackDaoImpl implements TrackDao {

    private static final String INSERT = "INSERT INTO TRACK(TITLE, COMPOSITION_YEAR, YOUTUBE_LINK, MEMBER_ID, GENRE_ID, AUTHOR_ID, COVER_PATH, INSTRUMENTS) VALUES(?,?,?,?,?,?,?,?)";
    private static final String SELECT_ALL = "SELECT * FROM TRACK";
    private static final String SELECT_RANDOM = "SELECT * FROM TRACK ORDER BY RANDOM() LIMIT ?";
    private static final String SELECT_BY_TITLE = "SELECT * FROM TRACK WHERE TITLE = ?";
    private static final String SELECT_BY_ID = "SELECT * FROM TRACK WHERE ID = ?";
    private static final String SELECT_BY_AUTHOR = "SELECT * FROM TRACK WHERE AUTHOR_ID = ? ORDER BY UPPER(TITLE) ASC";
    private static final String SELECT_BY_GENRE = "SELECT * FROM TRACK WHERE GENRE_ID = ? ORDER BY UPPER(TITLE) ASC";
    private static final String SEARCH_BY_TITLE = "SELECT * FROM TRACK WHERE UPPER(TITLE) LIKE UPPER(?)";
    private static final String DELETE = "DELETE FROM TRACK WHERE ID = ?";


    AuthorDao authorDao = new AuthorDaoImpl();
    MemberDao memberDao = new MemberDaoImpl();
    PerformerDao performerDao = new PerformerDaoImpl();

    public void insertTrack(Track track) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            setTrackParameters(pstmt, track);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    track.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nell'inserimento traccia: " + e.getMessage());
        }
    }

    public List<Track> getAllTracks() {
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL)) {

            while (rs.next()) {
                tracks.add(createTrackFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero tracce: " + e.getMessage());
        }
        return tracks;
    }

    public List<Track> getRandomTracks(int count) {
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_RANDOM)) {

            pstmt.setInt(1, count);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tracks.add(createTrackFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero tracce casuali: " + e.getMessage());
            // Se la funzione RANDOM() della query non funziona, prendo le tracce e le mescolo
            tracks = getAllTracks();
            java.util.Collections.shuffle(tracks);
            if (tracks.size() > count) {
                tracks = tracks.subList(0, count);
            }
        }
        return tracks;
    }

    public List<Track> getTracksByTitle(String title) {
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_TITLE)) {

            pstmt.setString(1, title);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tracks.add(createTrackFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero tracce per titolo: " + e.getMessage());
        }
        return tracks;
    }

    public Track getTrackById(int id) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return createTrackFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero traccia per ID: " + e.getMessage());
        }
        return null;
    }

    public List<Track> getTracksByAuthor(int authorId) {
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_AUTHOR)) {

            pstmt.setInt(1, authorId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tracks.add(createTrackFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero tracce per autore: " + e.getMessage());
        }
        return tracks;
    }

    public List<Track> getTracksByGenre(int genreId) {
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_GENRE)) {

            pstmt.setInt(1, genreId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tracks.add(createTrackFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero tracce per genere: " + e.getMessage());
        }
        return tracks;
    }


    public List<Track> searchTracks(String query) {
        List<Track> tracks = new ArrayList<>();

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SEARCH_BY_TITLE)) {

            pstmt.setString(1, query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tracks.add(createTrackFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nella ricerca brano tramite titolo: " + e.getMessage());
        }
        return tracks;
    }

    public boolean deleteTrack(Integer trackId) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            // Elimina le associazioni con i performer
            try (PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM TRACK_PERFORMER WHERE TRACK_ID = ?")) {
                pstmt.setInt(1, trackId);
                pstmt.executeUpdate();
            }

            // Elimina la traccia
            try (PreparedStatement pstmt = conn.prepareStatement(DELETE)) {
                pstmt.setInt(1, trackId);
                pstmt.executeUpdate();
            }

            conn.commit();

            authorDao.deleteOrphanedAuthors();
            performerDao.deleteOrphanedPerformers();

            return true;
        } catch (SQLException e) {
            System.err.println("Errore nell'eliminazione brano: " + e.getMessage());
            return false;
        }
    }

    private Track createTrackFromResultSet(ResultSet rs) throws SQLException {
        Track track = new Track();
        track.setId(rs.getInt("ID"));
        track.setTitle(rs.getString("TITLE"));
        track.setCompositionYear(rs.getInt("COMPOSITION_YEAR"));
        track.setYoutubeLink(rs.getString("YOUTUBE_LINK"));
        track.setCoverPath(rs.getString("COVER_PATH"));
        track.setInstruments(rs.getString("INSTRUMENTS"));

        // Recupera member
        int memberId = rs.getInt("MEMBER_ID");
        if (!rs.wasNull()) {
            Member member = memberDao.getMemberById(memberId);
            track.setMember(member);
        }

        // Recupera genre ID
        int genreId = rs.getInt("GENRE_ID");
        if (!rs.wasNull()) {
            track.setGenreId(genreId);
        }

        // Recupera autore (se presente)
        int authorId = rs.getInt("AUTHOR_ID");
        if (!rs.wasNull()) {
            Author author = authorDao.getAuthorById(authorId);
            track.setAuthor(author);
        }

        return track;
    }

    private static void setTrackParameters(PreparedStatement pstmt, Track track) throws SQLException {
        pstmt.setString(1, track.getTitle());
        pstmt.setInt(2, track.getCompositionYear());
        pstmt.setString(3, track.getYoutubeLink());
        pstmt.setInt(4, track.getMember().getId());

        //genre
        if (track.getGenreId() != null) {
            pstmt.setInt(5, track.getGenreId());
        } else {
            pstmt.setNull(5, Types.INTEGER);
        }

        //autore
        if (track.getAuthor() != null && track.getAuthor().getId() != null) {
            pstmt.setInt(6, track.getAuthor().getId());
        } else {
            pstmt.setNull(6, Types.INTEGER);
        }

        pstmt.setString(7, track.getCoverPath());
        pstmt.setString(8, track.getInstruments());
    }
}