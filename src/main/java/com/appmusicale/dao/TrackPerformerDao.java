package com.appmusicale.dao;

import com.appmusicale.model.Performer;
import com.appmusicale.model.Track;

import java.util.List;

public interface TrackPerformerDao {
    List<Track> getTracksByPerformer(int performerId);
    boolean associatePerformerToTrack(int trackId, int performerId);
    List<Performer> getPerformersByTrack(int trackId);

}
