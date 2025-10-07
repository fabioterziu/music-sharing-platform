package com.appmusicale.dao;

import com.appmusicale.model.Track;

import java.util.List;

public interface TrackDao {
    void insertTrack(Track track);
    List<Track> getAllTracks();
    List<Track> getRandomTracks(int count);
    List<Track> getTracksByTitle(String title);
    Track getTrackById(int id);
    List<Track> getTracksByAuthor(int authorId);
    List<Track> getTracksByGenre(int genreId);
    List<Track> searchTracks(String query);
    boolean deleteTrack(Integer trackId);

}
