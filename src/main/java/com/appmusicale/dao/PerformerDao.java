package com.appmusicale.dao;

import com.appmusicale.model.Performer;

import java.util.List;

public interface PerformerDao {
    void insertPerformer(Performer performer);
    List<Performer> getAllPerformers();
    Performer getOrCreatePerformer(String performerName);
    Performer getPerformerByName(String name);
    List<Performer> searchPerformers(String query);
    void deleteOrphanedPerformers();

}
