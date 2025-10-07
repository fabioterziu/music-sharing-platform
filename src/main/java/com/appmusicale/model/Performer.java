package com.appmusicale.model;

public class Performer {
    private Integer id;
    private String name;
    private String photoPath;

    public Performer() {}

    //ID
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    //NOME
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    //FOTO PATH
    public String getPhotoPath() {
        return photoPath;
    }
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
}
