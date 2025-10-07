package com.appmusicale.model;

import com.appmusicale.dao.GenreDao;
import com.appmusicale.dao.GenreDaoImpl;

public class Track {
    private Integer id;
    private String title;
    private Integer compositionYear;
    private String youtubeLink;
    private String coverPath;
    private String instruments;

    private Member member;
    private Integer genreId;
    private Author author;
    private GenreType genreType;

    public Track() {}

    public Track(Integer id, String title, Integer compositionYear, Author author, Integer genreId, String coverPath, String instruments) {
        this.id = id;
        this.title = title;
        this.compositionYear = compositionYear;
        this.author = author;
        this.genreId = genreId;
        this.coverPath = coverPath;
        this.instruments = instruments;
    }

    public Track(Integer id, String title, Integer compositionYear, String youtubeLink, String coverPath,
                 String instruments, Member member, Integer genreId, Author author) {
        this.id = id;
        this.title = title;
        this.compositionYear = compositionYear;
        this.youtubeLink = youtubeLink;
        this.coverPath = coverPath;
        this.instruments = instruments;
        this.member = member;
        this.genreId = genreId;
        this.author = author;
    }


    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getCompositionYear() {
        return compositionYear;
    }
    public void setCompositionYear(Integer compositionYear) {
        this.compositionYear = compositionYear;
    }

    public String getYoutubeLink() {
        return youtubeLink;
    }
    public void setYoutubeLink(String youtubeLink) {
        this.youtubeLink = youtubeLink;
    }

    public String getCoverPath() {
        return coverPath;
    }
    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public String getInstruments() { return instruments; }
    public void setInstruments(String instruments) { this.instruments = instruments; }

    public Member getMember() {
        return member;
    }
    public void setMember(Member member) {
        this.member = member;
    }

    public Integer getGenreId() {
        return genreId;
    }
    public void setGenreId(Integer genreId) {
        this.genreId = genreId;
    }

    public Author getAuthor() {
        return author;
    }
    public void setAuthor(Author author) {
        this.author = author;
    }

}