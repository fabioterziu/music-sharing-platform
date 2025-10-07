package com.appmusicale.model;

import java.time.LocalDateTime;

public class Comment {
    private Integer id;
    private String content;
    private LocalDateTime createdAt;

    // Relazioni
    private Member member;
    private Comment parentComment;
    private Integer parentCommentId;
    private transient Member parentCommentMember;
    private Track track;
    private boolean isAuthorOrPerformer;

    public Comment() {}

    public Comment(Integer id, String content, LocalDateTime createdAt,
                   Member member, Comment parentComment, Track track, boolean isAuthorOrPerformer) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.member = member;
        this.parentComment = parentComment;
        this.track = track;
        this.isAuthorOrPerformer = isAuthorOrPerformer;

        if (parentComment != null) {
            this.parentCommentId = parentComment.getId();
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public Comment getParentComment() { return parentComment; }
    public void setParentComment(Comment parentComment) { this.parentComment = parentComment; }

    public Integer getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(Integer parentCommentId) { this.parentCommentId = parentCommentId; }

    public Member getParentCommentMember() { return parentCommentMember; }
    public void setParentCommentMember(Member parentCommentMember) { this.parentCommentMember = parentCommentMember; }

    public Track getTrack() { return track; }
    public void setTrack(Track track) { this.track = track; }

    public boolean isAuthorOrPerformer() { return isAuthorOrPerformer; }
    public void setAuthorOrPerformer(boolean authorOrPerformer) { this.isAuthorOrPerformer = authorOrPerformer; }
}

