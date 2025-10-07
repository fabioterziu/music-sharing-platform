package com.appmusicale.dao;

import com.appmusicale.model.Note;

import java.util.List;

public interface NoteDao {
    boolean insertNote(Note note);
    List<Note> getNotesByTrack(int trackId);
    List<Note> getNotesByConcertData(int concertDataId);
    boolean deleteNote(int id);

}
