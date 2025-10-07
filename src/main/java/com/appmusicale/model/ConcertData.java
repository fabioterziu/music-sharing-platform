package com.appmusicale.model;

public class ConcertData {
    private Integer id;
    private String trackTitle;
    private String trackAuthor;
    private Integer startTime;
    private Integer endTime;
    private String performer;
    private String instrument;
    private String date;
    private String place;

    // Relazioni
    private Concert concert;
    private Member member;

    public ConcertData() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTrackTitle() { return trackTitle; }
    public void setTrackTitle(String trackTitle) { this.trackTitle = trackTitle; }

    public String getTrackAuthor() { return trackAuthor; }
    public void setTrackAuthor(String trackAuthor) { this.trackAuthor = trackAuthor; }

    public Integer getStartTime() { return startTime; }
    public void setStartTime(Integer startTime) { this.startTime = startTime; }

    public Integer getEndTime() { return endTime; }
    public void setEndTime(Integer end_time) { this.endTime = end_time; }

    public String getPerformer() { return performer; }
    public void setPerformer(String performer) { this.performer = performer; }

    public String getInstrument() { return instrument; }
    public void setInstrument(String instrument) { this.instrument = instrument; }

    public Concert getConcert() { return concert; }
    public void setConcert(Concert concert) { this.concert = concert; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getPlace() { return place; }
    public void setPlace(String place) { this.place = place; }
}
