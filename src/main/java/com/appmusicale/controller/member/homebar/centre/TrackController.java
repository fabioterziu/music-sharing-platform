package com.appmusicale.controller.member.homebar.centre;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.controller.member.homebar.*;
import com.appmusicale.controller.member.sidebar.CommentedController;
import com.appmusicale.dao.*;
import com.appmusicale.model.*;
import com.appmusicale.model.CommentData;
import com.appmusicale.service.AccessService;
import com.appmusicale.service.TrackService;
import com.appmusicale.util.MemberContextUtils;
import com.appmusicale.util.SceneManagerUtils;
import com.appmusicale.util.AppUtils;
import com.appmusicale.util.NavigationManagerUtils;
import com.appmusicale.util.ViewStateUtils;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.*;

public class TrackController {
    @FXML private ImageView coverImage;
    @FXML private Label titleLabel, artistLabel, yearLabel, genreLabel, performersLabel, instrumentsLabel, replyHintLabel;
    @FXML private Hyperlink youtubeLink;
    @FXML private VBox documentsList, notesList, multimediaList, commentsList;
    @FXML private Button deleteTrackButton, cancelReplyButton, backButton; // Aggiunto backButton
    @FXML private TextField newNoteField, startTimeField, endTimeField, newCommentField;

    private Comment selectedComment;
    private UserController userController;
    private int trackId;
    private Member trackOwner;

    TrackService trackService = new TrackService();



    @FXML
    public void initialize() {
        System.out.println("TrackController inizializzato");

        //inizializzo bottone back
        if (backButton != null) {
            backButton.setOnAction(e -> handleBackButton());
        }
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
        loadTrackData();
    }

    //BACK BUTTON
    @FXML
    private void handleBackButton() {
        //ottengo istanza dal singleton
        NavigationManagerUtils navManager = NavigationManagerUtils.getInstance();
        //recupera l'ultimo stato salvato nello stack
        ViewStateUtils lastState = navManager.popViewState();

        if (lastState != null && userController != null) {
            navigateBack(lastState);
        } else {
            //se non c'è nessuno stato precedente
            userController.loadView("/com/appmusicale/fxml/member/sidebar/home-view.fxml");
        }
    }

    //naviga alla vista precedente ripristinando lo stato salvato
    private void navigateBack(ViewStateUtils state) {
        //recupera la mappa di attributi salvati nello stato della vista
        Map<String, Object> attributes = state.attributes();

        switch (state.controllerType()) {
            case "ExploreController":
                //torna alla home di explore
                if (userController != null) {
                    userController.loadHomeWithExplore();
                }
                break;

            case "GenreListController":
                Object genreController = SceneManagerUtils.loadViewInBorderPane(
                        userController.getMainContainer(),
                        state.viewPath()
                );
                if (genreController instanceof GenreListController) {
                    ((GenreListController) genreController).setUserController(userController);
                    if (attributes.containsKey("genre")) {
                        ((GenreListController) genreController).setGenre((GenreType) attributes.get("genre"));
                    }
                }
                break;

            case "AuthorListController":
                Object authorController = SceneManagerUtils.loadViewInBorderPane(
                        userController.getMainContainer(),
                        state.viewPath()
                );
                if (authorController instanceof AuthorListController) {
                    ((AuthorListController) authorController).setUserController(userController);
                    if (attributes.containsKey("author")) {
                        ((AuthorListController) authorController).setAuthor((Author) attributes.get("author"));
                    }
                }
                break;

            case "PerformerListController":
                Object performerController = SceneManagerUtils.loadViewInBorderPane(
                        userController.getMainContainer(),
                        state.viewPath()
                );
                if (performerController instanceof PerformerListController) {
                    ((PerformerListController) performerController).setUserController(userController);
                    if (attributes.containsKey("performer")) {
                        ((PerformerListController) performerController).setPerformer((Performer) attributes.get("performer"));
                    }
                }
                break;

            case "CommentedController":
                Object commentedController = SceneManagerUtils.loadViewInBorderPane(
                        userController.getMainContainer(),
                        state.viewPath()
                );
                if (commentedController instanceof CommentedController) {
                    ((CommentedController) commentedController).setUserController(userController);
                }
                break;

            default:
                //se non è nessun caso specifico
                userController.loadView(state.viewPath());
                break;
        }
    }

    //RECUPERO LA TRACCIA
    private void loadTrackData() {
        Track track = trackService.getTrackDetails(trackId);
        if (track != null) {
            trackOwner = track.getMember();
            titleLabel.setText(track.getTitle());
            artistLabel.setText(track.getAuthor().getName());
            yearLabel.setText(track.getCompositionYear().toString());
            genreLabel.setText(trackService.getGenreDisplayName(track));
            performersLabel.setText(trackService.getPerformersString(trackId));
            instrumentsLabel.setText(trackService.getInstrumentsString(track));
            coverImage.setImage(trackService.getCover(track, getClass()));

            TrackService.TrackLink youtubeInfo = trackService.getYoutubeLink(track);
            youtubeLink.setText(youtubeInfo.getUrl());
            if (youtubeInfo.isAvailable()) {
                youtubeLink.setOnAction(event -> AppUtils.openYoutubeLink(youtubeInfo.getUrl()));
                youtubeLink.setDisable(false);
                youtubeLink.setStyle(null);
            } else {
                youtubeLink.setDisable(true);
                youtubeLink.setStyle("-fx-text-fill: #7f8c8d;");
            }
        } else {
            titleLabel.setText("Brano non trovato");
        }

        //Permessi
        boolean canDelete = MemberContextUtils.isCurrentUserOwnerOrAdmin(track.getMember());
        deleteTrackButton.setVisible(canDelete);
        deleteTrackButton.setManaged(canDelete);

        //carica documenti, note, file multimediali e commenti
        loadDocuments();
        loadNotes();
        loadMultimedia();
        loadComments();
    }

    //ELIMINA TRACK
    @FXML
    private void handleDeleteTrack() {
        boolean deleted = trackService.deleteTrack(trackId);
        if (deleted) {
            if (userController != null) {
                userController.loadView("/com/appmusicale/fxml/member/sidebar/home-view.fxml");
            }
        }
    }

    //AGGIUNGI MULTIMEDIA
    @FXML
    private void handleUploadMultimedia() {
        chooseMultimedia();
        loadMultimedia();
    }

    //AGGIUNGI DOCUMENTO
    @FXML
    private void handleUploadDocument() {
        chooseDocument();
        loadDocuments();
    }

    //AGGIUNGI NOTA
    @FXML
    private void handleAddNote() {
        String noteText = newNoteField.getText().trim();
        if (!noteText.isEmpty()) {
            Integer startSeconds = AppUtils.parseTimeToSeconds(startTimeField.getText().trim());
            Integer endSeconds = AppUtils.parseTimeToSeconds(endTimeField.getText().trim());

            boolean success = trackService.addNote(noteText, startSeconds, endSeconds, trackId, MemberContextUtils.getLoggedInMember());
            if (success) {
                newNoteField.clear();
                startTimeField.clear();
                endTimeField.clear();
                loadNotes();
            } else {
                System.out.println("Errore nel salvataggio della nota");
            }
        }
    }

    //AGGIUNGI COMMENTO
    @FXML
    private void handleAddComment() {
        String text = newCommentField.getText().trim();
        if (!text.isEmpty()) {
            trackService.addComment(text, trackId, selectedComment, MemberContextUtils.getLoggedInMember());
            selectedComment = null;
            newCommentField.setPromptText("Lascia un commento");
            newCommentField.clear();
            updateReplyUI();
            loadComments();
        }
    }

    //ANNULLA RISPOSTA AL COMMENTO
    @FXML
    private void handleCancelReply() {
        selectedComment = null;
        newCommentField.setPromptText("Lascia un commento");
        newCommentField.clear();
        updateReplyUI();
    }

    private void updateReplyUI() {
        if (selectedComment != null) {
            replyHintLabel.setText("Rispondi a: " + selectedComment.getMember().getUsername());
            cancelReplyButton.setVisible(true);
        } else {
            replyHintLabel.setText("Doppio click per rispondere");
            cancelReplyButton.setVisible(false);
        }
    }

    //SCEGLI DOCUMENTI
    private void chooseDocument() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona documento");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documento", "*.pdf"),
                new FileChooser.ExtensionFilter("Tutti i file", "*.*"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            boolean success = trackService.addMedia(file, "DOCUMENT", trackId, MemberContextUtils.getLoggedInMember());
            if (success) {
                loadDocuments();
            }
        }
    }

    //SCEGLI MULTIMEDIA
    private void chooseMultimedia() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona file multimediale");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Multimedia", "*.mp3", "*.mp4"),
                new FileChooser.ExtensionFilter("Tutti i file", "*.*"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            boolean success = trackService.addMedia(file, "MULTIMEDIA", trackId, MemberContextUtils.getLoggedInMember());
            if (success) {
                loadMultimedia();
            }
        } else {
            System.out.println("Non selezionato");
        }
    }

    //CARICA DOCUMENTI
    private void loadDocuments() {
        documentsList.getChildren().clear();
        List<Media> documents = trackService.getDocumentsForTrack(trackId);

        if (documents != null) {
            for (Media media : documents) {
                Label docTitleLabel = new Label(media.getTitle());
                docTitleLabel.setStyle("-fx-underline: true; -fx-text-fill: blue;");

                Label download = new Label("", new FontIcon("fas-download"));
                download.setStyle("-fx-text-fill: #3498db;");
                download.setOnMouseClicked(event -> {
                    File file = new File(media.getPath());
                    if (file.exists()) {
                        AppUtils.downloadFile(file);
                    } else {
                        SceneManagerUtils.openImageStage("/com/appmusicale/images/notexist.jpeg");
                    }
                });

                //bottone elimina
                Button deleteButton = new Button("✕");
                deleteButton.setStyle("-fx-text-fill: black; -fx-background-color: #E74C3C; -fx-border-color: transparent; -fx-min-width: 20; -fx-max-width: 20;");
                deleteButton.setPadding(new javafx.geometry.Insets(2, 5, 2, 5));
                //chi elimina
                boolean canDelete = MemberContextUtils.isCurrentUserOwnerOrAdmin(media.getMember()) || MemberContextUtils.isCreator(trackOwner);
                deleteButton.setManaged(canDelete);
                deleteButton.setVisible(canDelete);

                //elimina
                deleteButton.setOnAction(event -> {
                    boolean deleted = trackService.deleteMedia(media.getId());
                    if (deleted) {
                        loadDocuments();
                    }
                });

                //hbox elimina
                HBox mediaBox = new HBox(20);
                mediaBox.getChildren().addAll(docTitleLabel, download);
                if (canDelete) {
                    mediaBox.getChildren().add(deleteButton);
                }

                documentsList.getChildren().add(mediaBox);
                System.out.println("Titolo documento: " + media.getTitle());

                //clicco e apro
                docTitleLabel.setOnMouseClicked(event -> {
                    File file = new File(media.getPath());
                    if (!file.exists()) {
                        SceneManagerUtils.openImageStage("/com/appmusicale/images/notexist.jpeg");
                    } else {
                        SceneManagerUtils.openNewStageForDocument(
                                "/com/appmusicale/fxml/member/homebar/centre/document-view.fxml",
                                "Anteprima - " + media.getTitle(),
                                controller -> ((DocumentController) controller).loadPdf(media.getPath()));
                    }
                });
            }
        } else {
            System.out.println("Nessun documento trovato per questo brano");
        }
    }
    //CARICA MULTIMEDIA
    private void loadMultimedia() {
        //pulisco elenco
        multimediaList.getChildren().clear();

        List<Media> multimedia = trackService.getMultimediaForTrack(trackId);

        //se la lista non è vuota
        if (multimedia != null) {
            for (Media media : multimedia) {
                Label mediaTitleLabel = new Label(media.getTitle());
                mediaTitleLabel.setStyle("-fx-underline: true; -fx-text-fill: blue;");

                Label download = new Label("", new FontIcon("fas-download"));
                download.setStyle("-fx-text-fill: #3498db;");
                download.setOnMouseClicked(event -> {
                    File file = new File(media.getPath());
                    if (file.exists()) {
                        AppUtils.downloadFile(file);
                    }
                    else{
                        SceneManagerUtils.openImageStage("/com/appmusicale/images/notexist.jpeg");
                    }
                });

                //bottone elimina
                Button deleteButton = new Button("✕");
                deleteButton.setStyle("-fx-text-fill: black; -fx-background-color: #E74C3C; -fx-border-color: transparent; -fx-min-width: 20; -fx-max-width: 20;");
                deleteButton.setPadding(new javafx.geometry.Insets(2, 5, 2, 5));

                boolean canDelete = MemberContextUtils.isCurrentUserOwnerOrAdmin(media.getMember()) || MemberContextUtils.isCreator(trackOwner);
                deleteButton.setManaged(canDelete);
                deleteButton.setVisible(canDelete);

                deleteButton.setOnAction(event -> {
                    boolean deleted = trackService.deleteMedia(media.getId());
                    if (deleted) {
                        loadMultimedia();
                    }
                });

                //hbox
                HBox mediaBox = new HBox(20);
                mediaBox.getChildren().addAll(mediaTitleLabel, download);
                if (canDelete) {
                    mediaBox.getChildren().add(deleteButton);
                }
                multimediaList.getChildren().add(mediaBox); //inserisco dinamicamente in vbox
                //x debug
                System.out.println("Titolo multimediale: " + media.getTitle());

                //clicca sulfile
                mediaTitleLabel.setOnMouseClicked(event -> {
                    File file = new File(media.getPath());/////
                    if (!file.exists()) {
                        SceneManagerUtils.openImageStage("/com/appmusicale/images/notexist.jpeg");
                    }
                    else{
                        SceneManagerUtils.openNewStageForMultimedia(
                                "/com/appmusicale/fxml/member/homebar/centre/multimedia-view.fxml",
                                "Multimedia - " + media.getTitle(),
                                controller -> ((MultimediaController) controller).setMediaPath(media.getPath()));
                    }
                });
            }
        } else {
            System.out.println("Nessun file multimediale trovato per questo brano");
        }
    }
    //CARICA NOTE
    private void loadNotes() {
        notesList.getChildren().clear();

        List<Note> notes = trackService.getNotesForTrack(trackId);
        for (Note note : notes) {
            VBox noteBox = createNoteBox(note);
            notesList.getChildren().add(noteBox);
        }
    }
    //CARICA COMMENTI
    private void loadComments() {
        selectedComment = null;
        updateReplyUI();
        commentsList.getChildren().clear();

        CommentData commentsData = trackService.getCommentsForTrack(trackId);
        List<Comment> topLevelComments = commentsData.topLevelComments();
        Map<Integer, List<Comment>> commentTree = commentsData.commentTree();

        Collections.reverse(topLevelComments);

        //aggiungo commenti di primo livello e risposte
        for (Comment topLevelComment : topLevelComments) {
            VBox commentBox = createCommentBox(topLevelComment, true);
            commentsList.getChildren().add(commentBox);
            addRepliesToCommentBox(commentBox, topLevelComment.getId(), commentTree, 1);
        }
    }
    //RISPOSTE AI COMMENTI
    private void addRepliesToCommentBox(VBox parentBox, int parentId, Map<Integer, List<Comment>> commentTree, int depth) {
        List<Comment> replies = commentTree.get(parentId);
        if (replies != null) {
            for (Comment reply : replies) {
                VBox replyBox = createCommentBox(reply, false);
                //aumenta l'indentazione in base alla profondità
                replyBox.setStyle("-fx-padding: 5 0 5 " + (20 * depth) + ";");
                parentBox.getChildren().add(replyBox);

                //ricorsione risposte a risposta
                addRepliesToCommentBox(replyBox, reply.getId(), commentTree, depth + 1);
            }
        }
    }
    //VBOX NOTE
    private VBox createNoteBox(Note note) {
        VBox noteBox = new VBox(5);
        noteBox.setStyle("-fx-padding: 10; background-color: #F9F9F9; -fx-background-radius: 5; -fx-border-color: #DDD; border-radius: 5;");
        noteBox.setMaxWidth(Double.MAX_VALUE);

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label authorLabel = new Label(note.getMember().getUsername());
        authorLabel.setStyle("-fx-text-fill: #2C3E50; -fx-font-weight: bold;");

        Label timeLabel = new Label(AppUtils.formatDateTime(note.getCreatedAt()));
        timeLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px;");

        //se ci sono agigiunge info inizio fine
        String timeInfo = "";
        if (note.getStartTime() != null) {
            timeInfo += "Inizio: " + AppUtils.formatSecondsToTime(note.getStartTime());
        }
        if (note.getEndTime() != null) {
            if (!timeInfo.isEmpty()) {
                timeInfo += " - ";
            }
            timeInfo += "Fine: " + AppUtils.formatSecondsToTime(note.getEndTime());
        }

        Label timeRangeLabel = new Label(timeInfo);
        timeRangeLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);


        //bottone elimina
        Button deleteButton = new Button("✕");
        deleteButton.setStyle("-fx-text-fill: black; -fx-background-color: #E74C3C; -fx-border-color: transparent; -fx-min-width: 20; -fx-max-width: 20;");
        deleteButton.setPadding(new javafx.geometry.Insets(2, 5, 2, 5));

        //controlla se chi si è loggato può eliminare la nota
        boolean canDelete = MemberContextUtils.isCurrentUserOwnerOrAdmin(note.getMember()) || MemberContextUtils.isCreator(trackOwner);
        deleteButton.setManaged(canDelete);
        deleteButton.setVisible(canDelete);

        //elimina
        deleteButton.setOnAction(event -> {
            boolean deleted = trackService.deleteNote(note.getId());
            if (deleted) {
                loadNotes();
            }
        });

        headerBox.getChildren().addAll(authorLabel, timeLabel, timeRangeLabel, spacer, deleteButton);

        Label contentLabel = new Label(note.getContent());
        contentLabel.setWrapText(true);
        VBox.setVgrow(contentLabel, Priority.ALWAYS);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
        contentLabel.setStyle("-fx-text-fill: #2C3E50;");

        noteBox.getChildren().addAll(headerBox, contentLabel);

        return noteBox;
    }
    //VBOX COMMENTI
    private VBox createCommentBox(Comment comment, boolean isTopLevel) {
        VBox commentBox = new VBox(5);
        commentBox.setMaxWidth(Double.MAX_VALUE);

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label authorLabel = new Label(comment.getMember().getUsername());
        if (comment.isAuthorOrPerformer()) {
            authorLabel.setStyle("-fx-text-fill: #D35400; -fx-font-weight: bold;");
        } else {
            authorLabel.setStyle("-fx-text-fill: #2C3E50; -fx-font-weight: bold;");
        }

        Label timeLabel = new Label(AppUtils.formatDateTime(comment.getCreatedAt()));
        timeLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);


        //bottone elimina
        Button deleteButton = new Button("✕");
        deleteButton.setStyle("-fx-text-fill: black; -fx-background-color: #E74C3C; -fx-border-color: transparent; -fx-min-width: 20; -fx-max-width: 20;");
        deleteButton.setPadding(new javafx.geometry.Insets(2, 5, 2, 5));

        //chi può eliminare
        boolean canDelete = (MemberContextUtils.isCurrentUserOwnerOrAdmin(comment.getMember()) ||
                (comment.getParentCommentMember() != null && MemberContextUtils.isCurrentUserOwnerOrAdmin(comment.getParentCommentMember()))) ||
                MemberContextUtils.isCreator(trackOwner);
        deleteButton.setManaged(canDelete);
        deleteButton.setVisible(canDelete);

        //elimina
        deleteButton.setOnAction(event -> {
            boolean deleted = trackService.deleteComment(comment.getId());
            if (deleted) {
                loadComments();
            }
        });

        headerBox.getChildren().addAll(authorLabel, timeLabel, spacer, deleteButton);

        Label contentLabel = new Label(comment.getContent());
        contentLabel.setWrapText(true);
        VBox.setVgrow(contentLabel, Priority.ALWAYS);
        contentLabel.setMaxWidth(Double.MAX_VALUE);
        contentLabel.setPrefHeight(Region.USE_COMPUTED_SIZE);
        contentLabel.setStyle("-fx-text-fill: #2C3E50;");

        commentBox.getChildren().addAll(headerBox, contentLabel);

        //doppio click per rispondere
        commentBox.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                selectedComment = comment;
                newCommentField.setPromptText("Rispondi a " + comment.getMember().getUsername());
                newCommentField.requestFocus();
                updateReplyUI();
                event.consume();
            }
        });

        return commentBox;
    }
}