package com.appmusicale.model;

public class Concert {
    private Integer id;
    private String title;
    private String youtubeUrl;
    private String youtubeId;
    private String thumbnailUrl;

    // Relazioni
    private Member member;
    private ConcertData concertData;

    public Concert() {}

    public Concert(Integer id, String title, String youtubeUrl, String youtubeId, String thumbnailUrl, Member member) {
        this.id = id;
        this.title = title;
        this.youtubeUrl = youtubeUrl;
        this.youtubeId = youtubeId;
        this.thumbnailUrl = thumbnailUrl;//
        this.member = member;
        this.concertData = new ConcertData();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getYoutubeUrl() { return youtubeUrl; }
    public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }

    public String getYoutubeId() { return youtubeId; }
    public void setYoutubeId(String youtubeId) { this.youtubeId = youtubeId; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public ConcertData getConcertData() { return concertData; }
    public void setConcertData(ConcertData concertData) { this.concertData = concertData; }
}

