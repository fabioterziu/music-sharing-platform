package com.appmusicale.dao;

import com.appmusicale.model.ConcertData;

import java.util.List;

public interface ConcertDataDao {
    void insertConcertData(ConcertData concertData);
    ConcertData getConcertDataById(int id);
    List<ConcertData> getConcertDataByConcert(int concertId);
    boolean updateConcertData(ConcertData concertData);
    boolean deleteConcertData(int id);

}
