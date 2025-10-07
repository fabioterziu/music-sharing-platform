package com.appmusicale.util;

import com.appmusicale.dao.AuthorDao;
import com.appmusicale.dao.GenreDao;
import com.appmusicale.dao.GenreDaoImpl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializerUtils {

    GenreDao genreDao = new GenreDaoImpl();

    public void initialize() {
        //GenreDao genreDao = new GenreDaoImpl();
        try (Connection conn = DatabaseConnectionUtils.getInstance().getConnection();
             //invia istruzioni al db
             Statement stmt = conn.createStatement()) {

            //x supporto chiavi esterne
            stmt.execute("PRAGMA foreign_keys = ON");

            String[] createTablesSQL = {
                    // MEMBER
                    "CREATE TABLE IF NOT EXISTS MEMBER (" +
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "USERNAME TEXT NOT NULL, " +
                            "EMAIL TEXT UNIQUE NOT NULL, " +
                            "PASSWORD TEXT NOT NULL, " +
                            "ROLE TEXT NOT NULL CHECK(ROLE IN ('USER','ADMIN'))," +
                            "STATUS TEXT NOT NULL CHECK(STATUS IN ('ACTIVE','PENDING','BANNED')))",

                    // GENRE
                    "CREATE TABLE IF NOT EXISTS GENRE (" +
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "NAME TEXT UNIQUE NOT NULL," +
                            "DISPLAY_NAME TEXT NOT NULL)",

                    // AUTHOR
                    "CREATE TABLE IF NOT EXISTS AUTHOR (" +
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "NAME TEXT UNIQUE NOT NULL," +
                            "IMAGE_PATH TEXT)",

                    // PERFORMER
                    "CREATE TABLE IF NOT EXISTS PERFORMER (" +
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "NAME TEXT UNIQUE NOT NULL," +
                            "IMAGE_PATH TEXT)",

                    //JOIN TRACK-PERFORMER
                    "CREATE TABLE IF NOT EXISTS TRACK_PERFORMER (" +
                            "TRACK_ID INTEGER," +
                            "PERFORMER_ID INTEGER," +
                            "PRIMARY KEY (TRACK_ID, PERFORMER_ID)," +
                            "FOREIGN KEY (TRACK_ID) REFERENCES TRACK(ID) ON DELETE CASCADE," +
                            "FOREIGN KEY (PERFORMER_ID) REFERENCES PERFORMER(ID) ON DELETE CASCADE)",

                    // TRACK
                    "CREATE TABLE IF NOT EXISTS TRACK (" +
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "TITLE TEXT NOT NULL," +
                            "COMPOSITION_YEAR INTEGER," +
                            "YOUTUBE_LINK TEXT," +
                            "MEMBER_ID INTEGER REFERENCES MEMBER(ID) ON DELETE CASCADE," +
                            "GENRE_ID INTEGER REFERENCES GENRE(ID)," +
                            "AUTHOR_ID INTEGER REFERENCES AUTHOR(ID) ON DELETE CASCADE," +
                            "COVER_PATH TEXT," +
                            "INSTRUMENTS TEXT)",

                    // MEDIA
                    "CREATE TABLE IF NOT EXISTS MEDIA (" +
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "TITLE TEXT NOT NULL," +
                            "PATH TEXT NOT NULL," +
                            "TYPE TEXT NOT NULL," +
                            "TRACK_ID INTEGER  REFERENCES TRACK(ID) ON DELETE CASCADE," +
                            "MEMBER_ID INTEGER REFERENCES MEMBER(ID) ON DELETE SET NULL)",

                    // CONCERT
                    "CREATE TABLE IF NOT EXISTS CONCERT (" +
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "TITLE TEXT NOT NULL," +
                            "YOUTUBE_URL TEXT UNIQUE NOT NULL," +
                            "YOUTUBE_ID TEXT UNIQUE NOT NULL," +
                            "THUMBNAIL_URL TEXT NOT NULL," +
                            "MEMBER_ID INTEGER REFERENCES MEMBER(ID) ON DELETE SET NULL)",

                    // CONCERT_DATA
                    "CREATE TABLE IF NOT EXISTS CONCERT_DATA (" +
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "CONCERT_ID INTEGER NOT NULL REFERENCES CONCERT(ID) ON DELETE CASCADE," +
                            "TRACK_TITLE TEXT," +
                            "TRACK_ARTIST TEXT," +
                            "START_TIME INTEGER," +
                            "END_TIME INTEGER," +
                            "PERFORMERS TEXT," +
                            "INSTRUMENTS TEXT," +
                            "DATE TEXT," +
                            "PLACE TEXT," +
                            "MEMBER_ID INTEGER REFERENCES MEMBER(ID) ON DELETE SET NULL)",

                    // COMMENT
                    "CREATE TABLE IF NOT EXISTS COMMENT (" +
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "MEMBER_ID INTEGER REFERENCES MEMBER(ID) ON DELETE SET NULL," +
                            "PARENT_COMMENT_ID INTEGER REFERENCES COMMENT(ID) ON DELETE CASCADE," +
                            "CONTENT TEXT," +
                            "CREATED_AT TIMESTAMP," +
                            "TRACK_ID INTEGER REFERENCES TRACK(ID) ON DELETE CASCADE)",

                    // NOTE
                    "CREATE TABLE IF NOT EXISTS NOTE (" +
                            "ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "MEMBER_ID INTEGER REFERENCES MEMBER(ID) ON DELETE SET NULL," +
                            "CONTENT TEXT," +
                            "CREATED_AT TIMESTAMP," +
                            "START_TIME INTEGER," +
                            "END_TIME INTEGER," +
                            "TRACK_ID INTEGER REFERENCES TRACK(ID) ON DELETE CASCADE," +
                            "CONCERT_ID INTEGER REFERENCES CONCERT(ID) ON DELETE CASCADE," +
                            "CONCERT_DATA_ID INTEGER REFERENCES CONCERT_DATA(ID) ON DELETE CASCADE)"
            };

            for (String sql : createTablesSQL) {
                stmt.execute(sql);
            }

            System.out.println("Tabelle create/verificate con successo!");

        } catch (SQLException e) {
            System.err.println("Errore creazione tabelle: " + e.getMessage());
        }

        // Inizializza i generi redefiniti
        genreDao.initializePredefinedGenres();
    }
}