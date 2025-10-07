package com.appmusicale.dao;

import com.appmusicale.model.Concert;

import java.sql.ResultSet;
import java.util.List;

public interface ConcertDao {
    void insertConcert(Concert concert);
    List<Concert> getAllConcerts();
    Concert getConcertById(int id);
    Concert getConcertByYoutubeId(String youtubeId);
    boolean deleteConcert(int id);
}

