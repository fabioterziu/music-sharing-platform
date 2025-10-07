package com.appmusicale.dao;

import com.appmusicale.model.GenreType;

import java.util.List;

public interface GenreDao {
    void initializePredefinedGenres();
    List<GenreType> getAllGenres();
    GenreType getGenreByName(String name);
    Integer getGenreId(GenreType genre);
    GenreType getGenreById(int id);
    List<GenreType> searchGenres(String query);
    List<GenreType> getPredefinedGenres();
}
