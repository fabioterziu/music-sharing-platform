package com.appmusicale.dao;

import com.appmusicale.model.Comment;
import com.appmusicale.model.Member;
import com.appmusicale.model.Track;
import com.appmusicale.util.DatabaseConnectionUtils;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentDaoImpl implements CommentDao {

    private static final String INSERT = "INSERT INTO COMMENT (MEMBER_ID, PARENT_COMMENT_ID, CONTENT, CREATED_AT, TRACK_ID) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_TRACK = "SELECT c.*, m.USERNAME " +
            "FROM COMMENT c JOIN MEMBER m ON c.MEMBER_ID = m.ID " +
            "WHERE c.TRACK_ID = ? " +
            "ORDER BY c.CREATED_AT ASC";
    private static final String DELETE = "DELETE FROM COMMENT WHERE PARENT_COMMENT_ID = ? OR ID = ?";
    private static final String SELECT_BY_MEMBER = "SELECT c.*, m.USERNAME, t.TITLE as TRACK_TITLE " +
            "FROM COMMENT c " +
            "JOIN MEMBER m ON c.MEMBER_ID = m.ID " +
            "JOIN TRACK t ON c.TRACK_ID = t.ID " +
            "WHERE c.MEMBER_ID = ? " +
            "ORDER BY c.CREATED_AT DESC";

    public void insertComment(Comment comment) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT, PreparedStatement.RETURN_GENERATED_KEYS)) {

            setCommentParameters(pstmt, comment);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    comment.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nell'inserimento del commento" + e.getMessage());
        }
    }

    public List<Comment> getCommentsByTrackId(int trackId) {
        List<Comment> allComments = new ArrayList<>();

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_TRACK)) {

            pstmt.setInt(1, trackId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Comment comment = createCommentFromResultSet(rs);
                allComments.add(comment);
            }

        } catch (SQLException e) {
            System.err.println("Errore nel recupero commenti: " + e.getMessage());
        }
        return allComments;
    }

    public boolean deleteCommentAndChildren(int commentId) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(DELETE)) {

            pstmt.setInt(1, commentId);
            pstmt.setInt(2, commentId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Errore nella cancellazione del commento: " + e.getMessage());
            return false;
        }
    }

    public List<Comment> getCommentsByMember(int memberId) {
        List<Comment> comments = new ArrayList<>();

        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_MEMBER)) {

            pstmt.setInt(1, memberId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                comments.add(createCommentFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero commenti per membro: " + e.getMessage());
        }
        return comments;
    }

    private static Comment createCommentFromResultSet(ResultSet rs) throws SQLException {
        Comment comment = new Comment();
        comment.setId(rs.getInt("ID"));

        String content = rs.getString("CONTENT");
        comment.setContent(content != null ? content : "");

        Integer parentCommentId = (Integer) rs.getObject("PARENT_COMMENT_ID");
        comment.setParentCommentId(parentCommentId);

        Timestamp createdAt = rs.getTimestamp("CREATED_AT");
        if (createdAt != null) {
            comment.setCreatedAt(createdAt.toLocalDateTime());
        } else {
            comment.setCreatedAt(LocalDateTime.now());
        }

        Member member = new Member();
        int memberId = rs.getInt("MEMBER_ID");
        if (!rs.wasNull()) {
            member.setId(memberId);
        }

        String username = rs.getString("USERNAME");
        if (username != null) {
            member.setUsername(username);
        } else {
            member.setUsername("Unknown");
        }
        comment.setMember(member);

        int trackId = rs.getInt("TRACK_ID");
        if (!rs.wasNull()) {
            Track track = new Track();
            track.setId(trackId);
            comment.setTrack(track);
        }

        return comment;
    }

    private static void setCommentParameters (PreparedStatement pstmt, Comment comment) throws SQLException {
        pstmt.setInt(1, comment.getMember().getId());

        if (comment.getParentComment() != null) {
            pstmt.setInt(2, comment.getParentComment().getId());
        } else {
            pstmt.setNull(2, Types.INTEGER);
        }

        pstmt.setString(3, comment.getContent());
        pstmt.setTimestamp(4, Timestamp.valueOf(comment.getCreatedAt()));

        pstmt.setInt(5, comment.getTrack().getId());
    }
}