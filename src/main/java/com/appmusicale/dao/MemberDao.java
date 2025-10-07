package com.appmusicale.dao;

import com.appmusicale.model.Member;
import com.appmusicale.model.Status;

import java.util.List;

public interface MemberDao {
    void insertMember(Member member);
    Member getMemberById(int id);
    Member getMemberByEmail(String email);
    Member getMemberByUsername(String username);
    List<Member> getAllMembersByStatus(Status status);
    List<Member> getAllActiveUsers(Status status);
    void updateMember(Member member);
}
