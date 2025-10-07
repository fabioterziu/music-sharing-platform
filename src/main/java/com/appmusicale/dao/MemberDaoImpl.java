package com.appmusicale.dao;

import com.appmusicale.model.Member;
import com.appmusicale.model.Role;
import com.appmusicale.model.Status;
import com.appmusicale.util.DatabaseConnectionUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//DAO DEL MEMBRO (USER-ADMIN)
public class MemberDaoImpl implements MemberDao {
    private static final String INSERT = "INSERT INTO MEMBER(USERNAME, EMAIL, PASSWORD, ROLE, STATUS) VALUES(?,?,?,?,?)";
    private static final String SELECT_BY_ID = "SELECT * FROM MEMBER WHERE id = ?";
    private static final String SELECT_BY_EMAIL = "SELECT * FROM MEMBER WHERE email = ?";
    private static final String SELECT_BY_USERNAME = "SELECT * FROM MEMBER WHERE username = ?";
    private static final String SELECT_BY_STATUS = "SELECT * FROM MEMBER WHERE status = ?";
    private static final String SELECT_ACTIVE_USERS = "SELECT * FROM MEMBER WHERE role= 'USER' AND status = ?";
    private static final String UPDATE_MEMBER = "UPDATE member SET status = ? WHERE email = ?";

    public void insertMember(Member member) {
        //trywithresource è un autocloseable, libera risorse. a fine try chiude connessione
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            setMemberParameters(pstmt, member);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    member.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore nell'inserimento membro: " + e.getMessage());
        }
    }

    public Member getMemberById(int id) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_ID)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createMemberFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero membro per ID: " + e.getMessage());
        }
        return null;
    }

    public Member getMemberByEmail(String email) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_EMAIL)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createMemberFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero membro per email: " + e.getMessage());
        }
        return null;
    }

    public Member getMemberByUsername(String username) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_USERNAME)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return createMemberFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Errore nel recupero membro per username: " + e.getMessage());
        }
        return null;
    }

    public List<Member> getAllMembersByStatus(Status status) {
        List<Member> members = new ArrayList<>();
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_BY_STATUS)) {
            pstmt.setString(1, String.valueOf(status));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                members.add(createMemberFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return members;
    }

    public List<Member> getAllActiveUsers(Status status) {
        List<Member> members = new ArrayList<>();
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SELECT_ACTIVE_USERS)) {
            pstmt.setString(1, String.valueOf(status));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                members.add(createMemberFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return members;
    }

    public void updateMember(Member member) {
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(UPDATE_MEMBER)) {
            pstmt.setString(1, member.getStatus().toString());
            pstmt.setString(2, member.getEmail());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static Member createMemberFromResultSet(ResultSet rs) throws SQLException {
        Member member = new Member();
        member.setId(rs.getInt("ID"));
        member.setUsername(rs.getString("USERNAME"));
        member.setEmail(rs.getString("EMAIL"));
        member.setPassword(rs.getString("PASSWORD"));
        member.setRole(Role.valueOf(rs.getString("ROLE")));
        member.setStatus(Status.valueOf(rs.getString("STATUS")));
        return member;
    }

    private static void setMemberParameters(PreparedStatement pstmt, Member member) throws SQLException {
        pstmt.setString(1, member.getUsername());
        pstmt.setString(2, member.getEmail());
        pstmt.setString(3, member.getPassword());
        pstmt.setString(4, member.getRole().name());
        pstmt.setString(5, member.getStatus().name());
    }
}