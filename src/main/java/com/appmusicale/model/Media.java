package com.appmusicale.model;

public class Media {

    private Integer id; //id media
    private String title;
    private String path; //path media
    private String type;

    // Relazioni
    private Track track;
    private Member member;

    //
    public Media () {}

    //ID
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    //PATH
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    //TRACK
    public Track getTrack() { return track; }
    public void setTrack(Track track) { this.track = track; }

    //MEMBER
    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }
}
