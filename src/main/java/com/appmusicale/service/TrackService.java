package com.appmusicale.service;

import com.appmusicale.dao.*;
import com.appmusicale.model.*;
import javafx.scene.image.Image;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackService {

    MediaDao mediaDao= new MediaDaoImpl();
    CommentDao commentDao= new CommentDaoImpl();
    GenreDao genreDao= new GenreDaoImpl();
    NoteDao noteDao= new NoteDaoImpl();
    TrackDao trackDao= new TrackDaoImpl();
    TrackPerformerDao trackPerformerDao= new TrackPerformerDaoImpl();


    //TRACCIA TRACK
    public Track getTrackDetails(int trackId){
        Track track = trackDao.getTrackById(trackId);
        return track;
    }

    //GENERE STRING
    public String getGenreDisplayName(Track track){
        GenreType genreType = genreDao.getGenreById(track.getGenreId());//////////////dao
        if (genreType != null) {
            return genreType.getDisplayName();
        } else {
            return "Nessun genere specificato";
        }
    }

    //PERFORMER STRING
    public String getPerformersString(int trackId){
        List<Performer> performers = trackPerformerDao.getPerformersByTrack(trackId);
        if (!performers.isEmpty()) {
            StringBuilder performerNames = new StringBuilder();//per concatenare
            for (int i= 0; i< performers.size(); i++) {
                performerNames.append(performers.get(i).getName());
                if (i< performers.size() - 1) {
                    performerNames.append(", ");
                }
            }
            return performerNames.toString();
        } else {
            return"Nessun esecutore inserito.";
        }
    }

    //STRUMENTI STRING
    public String getInstrumentsString(Track track){
        String instrument = track.getInstruments();
        if (instrument != null && !instrument.trim().isEmpty()) {
            return instrument;
        }
        return "Nessuno strumento specificato";
    }

    //COVER IMAGE
    public Image getCover(Track track, Class<?> controllerClass){
        String coverPath = track.getCoverPath();

        if (coverPath != null) { //se c'è
            if (coverPath.startsWith("/com/appmusicale")) { //path interno
                return new Image(controllerClass.getResource(coverPath).toExternalForm());
            }
            else {//se è sul disco (copertine)
                File file = new File(coverPath);
                if (file.exists()) {
                    return new Image(file.toURI().toString());
                }
                else {//se dovesse essere null metti immagine default
                    return new Image(controllerClass.getResource("/com/appmusicale/images/default.jpeg").toExternalForm());
                }
            }
        }
        //se percorso non valido
        return new Image(controllerClass.getResource("/com/appmusicale/images/default.jpeg").toExternalForm());
    }

    //YOUTUBE STRING
    public TrackLink getYoutubeLink(Track track){
        String youtubeUrl = track.getYoutubeLink();
        if (youtubeUrl != null && !youtubeUrl.trim().isEmpty()) {
            return new TrackLink(youtubeUrl, true);
        }
        else {
            return new TrackLink("Nessun link specificato", false);
        }

    }
    //supporto per TrackLink
    public class TrackLink{
        private String url;
        private boolean isAvailable;

        public TrackLink(String url, boolean isAvailable){
            this.url = url;
            this.isAvailable = isAvailable;
        }

        public String getUrl() {
            return url;
        }
        public boolean isAvailable() {
            return isAvailable;
        }
    }

    //DELETE TRACK
    public boolean deleteTrack(int trackId) {
        return trackDao.deleteTrack(trackId);
    }


    //AGGIUNGI NOTA
    public boolean addNote(String noteText, Integer startSeconds, Integer endSeconds, int trackId, Member currentUser) {
        Note note = new Note();
        note.setContent(noteText);
        note.setStartTime(startSeconds);
        note.setEndTime(endSeconds);
        note.setMember(currentUser);

        Track track = new Track();
        track.setId(trackId);
        note.setTrack(track);

        return noteDao.insertNote(note);
    }

    //CARICA NOTA
    public List<Note> getNotesForTrack(int trackId) {
        return noteDao.getNotesByTrack(trackId);
    }

    //ELIMINA NOTA
    public boolean deleteNote(int noteId) {
        return noteDao.deleteNote(noteId);
    }

    //CARICA COMMENTO
    public CommentData getCommentsForTrack(int trackId) {

        List<Comment> allComments = commentDao.getCommentsByTrackId(trackId);
        Track track = trackDao.getTrackById(trackId);

        String authorName = track.getAuthor() != null ? track.getAuthor().getName() : "";
        List<Performer> performers = trackPerformerDao.getPerformersByTrack(trackId);
        List<String> performerNames = new ArrayList<>();
        for  (Performer performer : performers) {
            performerNames.add(performer.getName());
        }

        //map per accedere ai commenti per ID
        Map<Integer, Comment> commentMap = new HashMap<>();
        for (Comment comment : allComments) {
            commentMap.put(comment.getId(), comment);
        }

        //commenti
        Map<Integer, List<Comment>> commentTree = new HashMap<>();
        List<Comment> topLevelComments = new ArrayList<>();

        for (Comment comment : allComments) {
            String commenterUsername = comment.getMember().getUsername();
            String normalizedCommenter = commenterUsername.trim().toLowerCase();
            String normalizedAuthor = authorName.trim().toLowerCase();

            //constrollo se autore/esecutore
            boolean isAuthor = normalizedCommenter.equals(normalizedAuthor);
            boolean isPerformer = false;
            for (String performerName : performerNames) {
                if (normalizedCommenter.equals(performerName.trim().toLowerCase())) {
                    isPerformer = true;
                    break;
                }
            }
            comment.setAuthorOrPerformer(isAuthor || isPerformer);

            //imposta parentCommentMember se il commento ha un padre
            Integer parentId = comment.getParentCommentId();
            if (parentId != null && parentId != 0) {
                Comment parentComment = commentMap.get(parentId);
                if (parentComment != null) {
                    comment.setParentCommentMember(parentComment.getMember());
                }
            }

            if (parentId == null || parentId == 0) {
                topLevelComments.add(comment);
            } else {
                commentTree.computeIfAbsent(parentId, k -> new ArrayList<>()).add(comment);
            }
        }
        return new CommentData(topLevelComments, commentTree);
    }


    //AGGIUNGI COMMENTO
    public void addComment(String text, int trackId, Comment selectedComment, Member currentUser) {
        Comment comment = new Comment();
        comment.setContent(text);
        comment.setMember(currentUser);
        comment.setCreatedAt(LocalDateTime.now());

        Track track = new Track();
        track.setId(trackId);
        comment.setTrack(track);

        if (selectedComment != null) {
            comment.setParentComment(selectedComment);
        }

        commentDao.insertComment(comment);
    }

    //ELIMINA COMMENTI (a cascata)
    public boolean deleteComment(int commentId) {
        return commentDao.deleteCommentAndChildren(commentId);
    }

    //AGGIUNGI MEDIA
    public boolean addMedia(File mediaFile, String mediaType, int trackId, Member currentUser) {
        try {
            String path = mediaFile.getAbsolutePath();

            Media media = new Media();
            media.setPath(path);
            media.setType(mediaType);
            media.setTitle(mediaFile.getName());
            Track track = trackDao.getTrackById(trackId);
            media.setTrack(track);
            media.setMember(currentUser);
            mediaDao.insertMedia(media);

            System.out.println("File caricato con successo");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore nel caricamento del file.");
            return false;
        }
    }


    //CARICA MULTIMEDIA
    public List<Media> getMultimediaForTrack(int trackId) {
        List<Media> allMedia = mediaDao.getMediaByTrackId(trackId);
        List<Media> multimedia = new ArrayList<>();
        if (allMedia != null) {
            for (Media media : allMedia) {
                if ("MULTIMEDIA".equals(media.getType())) {
                    multimedia.add(media);
                }
            }
        }
        return multimedia;
    }

    //CARICA DOCUMENTO
    public List<Media> getDocumentsForTrack(int trackId) {
        List<Media> allMedia = mediaDao.getMediaByTrackId(trackId);
        List<Media> documents = new ArrayList<>();
        if (allMedia != null) {
            for (Media media : allMedia) {
                if ("DOCUMENT".equals(media.getType())) {
                    documents.add(media);
                }
            }
        }
        return documents;
    }

    //ELIMINA MEDIA
    public boolean deleteMedia(int mediaId) {
        return mediaDao.deleteMedia(mediaId);
    }
}