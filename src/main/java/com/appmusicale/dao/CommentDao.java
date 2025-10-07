package com.appmusicale.dao;

import com.appmusicale.model.Comment;

import java.util.List;

public interface CommentDao {
    void insertComment(Comment comment);
    List<Comment> getCommentsByTrackId(int trackId);
    boolean deleteCommentAndChildren(int commentId);
    List<Comment> getCommentsByMember(int memberId);

}
