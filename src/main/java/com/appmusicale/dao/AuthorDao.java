package com.appmusicale.dao;

import com.appmusicale.model.Author;

import java.sql.ResultSet;
import java.util.List;

public interface AuthorDao {
    boolean insertAuthor(Author author);
    List<Author> getAllAuthors();
    Author getAuthorByName(String name);
    Author getOrCreateAuthor(String authorName);
    Author getAuthorById(int id);
    List<Author> searchAuthors(String query);
    void deleteOrphanedAuthors();
}
