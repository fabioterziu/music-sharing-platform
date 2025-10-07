package com.appmusicale.model;

public enum GenreType {
    RAP("Rap", "/com/appmusicale/images/genre/rap.jpg"),
    POP("Pop", "/com/appmusicale/images/genre/pop.jpg"),
    ROCK("Rock", "/com/appmusicale/images/genre/rock.jpg"),
    JAZZ("Jazz", "/com/appmusicale/images/genre/jazz.jpg"),
    CLASSICA("Classica", "/com/appmusicale/images/genre/classica.jpg"),
    ELETTRONICA("Elettronica", "/com/appmusicale/images/genre/elettronica.jpg"),
    BLUES("Blues", "/com/appmusicale/images/genre/blues.jpg"),
    COUNTRY("Country", "/com/appmusicale/images/genre/country.jpg"),
    REGGAE("Reggae", "/com/appmusicale/images/genre/reggae.jpg"),
    METAL("Metal", "/com/appmusicale/images/genre/metal.jpg"),
    FOLK("Folk", "/com/appmusicale/images/genre/folk.jpg"),
    R_B("R&B", "/com/appmusicale/images/genre/reb.jpg"),
    FUNK("Funk", "/com/appmusicale/images/genre/funk.jpg"),
    DISCO("Disco", "/com/appmusicale/images/genre/disco.jpg"),
    PUNK("Punk", "/com/appmusicale/images/genre/punk.jpg"),
    ALTERNATIVA("Alternativa", "/com/appmusicale/images/genre/alternativa.jpg"),
    INDIE("Indie", "/com/appmusicale/images/genre/indie.jpg");

    private final String displayName;
    private final String imagePath;

    //costruttore
    GenreType(String name, String imagePath) {
        this.displayName = name;
        this.imagePath = imagePath;
    }
    public String getDisplayName() {
        return displayName;
    }
    public String getImagePath() {
        return imagePath;
    }
}

