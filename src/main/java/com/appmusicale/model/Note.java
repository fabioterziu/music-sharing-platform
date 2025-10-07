package com.appmusicale.model;

import java.time.LocalDateTime;

public class Note {
    private Integer id;
    private String content;
    private LocalDateTime createdAt;
    private Integer startTime;
    private Integer endTime;

    // Relazioni
    private Member member;
    private Track track;
    private Concert concert;
    private ConcertData concertData;

    public Note() {}

    public Note(Integer id, String content,  LocalDateTime createdAt,
                Integer startTime, Integer endTime, Member member,
                Track track, Concert concert, ConcertData concertData) {
        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.startTime = startTime;
        this.endTime = endTime;
        this.member = member;
        this.track = track;
        this.concert = concert;
        this.concertData = concertData;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Integer getStartTime() { return startTime; }
    public void setStartTime(Integer startTime) { this.startTime = startTime; }

    public Integer getEndTime() { return endTime; }
    public void setEndTime(Integer endTime) { this.endTime = endTime; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public Track getTrack() { return track; }
    public void setTrack(Track track) { this.track = track; }

    public Concert getConcert() { return concert; }
    public void setConcert(Concert concert) { this.concert = concert; }

    public ConcertData getConcertData() { return concertData; }
    public void setConcertData(ConcertData concertData) { this.concertData = concertData; }
}