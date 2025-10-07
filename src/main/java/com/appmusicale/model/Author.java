package com.appmusicale.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Author {
    private Integer id;
    private String name;
    //private String photoPath;

    public Author () {}

    public Author(Integer id, String name, String photoPath) {
        this.id = id;
        this.name = name;
        //this.photoPath = photoPath;
    }

    public void setId(Integer id) { this.id = id; }
    public Integer getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    //public String getPhotoPath() { return photoPath; }
    //public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
}