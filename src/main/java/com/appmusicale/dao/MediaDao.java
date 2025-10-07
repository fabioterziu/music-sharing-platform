package com.appmusicale.dao;

import com.appmusicale.model.Media;

import java.util.List;

public interface MediaDao {
    void insertMedia(Media media);
    List<Media> getMediaByTrackId(int trackId);
    boolean deleteMedia(int mediaId);

}
