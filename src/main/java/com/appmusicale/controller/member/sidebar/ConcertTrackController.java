package com.appmusicale.controller.member.sidebar;

import com.appmusicale.dao.ConcertDataDao;
import com.appmusicale.dao.ConcertDataDaoImpl;
import com.appmusicale.dao.NoteDao;
import com.appmusicale.dao.NoteDaoImpl;
import com.appmusicale.model.ConcertData;
import com.appmusicale.model.Member;
import com.appmusicale.model.Note;
import com.appmusicale.util.MemberContextUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.List;

//SEZIONE BRANI DEI CONCERTI

public class ConcertTrackController {

    @FXML private Label trackTitleLabel;
    @FXML private Label artistLabel;
    @FXML private Label startTimeLabel;
    @FXML private Label endTimeLabel;
    @FXML private Label performersLabel;
    @FXML private Label instrumentsLabel;
    @FXML private Label dateLabel;
    @FXML private Label placeLabel;
    @FXML private Label formMessageLabel;

    @FXML private Button editPerformersBtn;
    @FXML private Button editInstrumentsBtn;
    @FXML private Button editDateBtn;
    @FXML private Button editPlaceBtn;

    @FXML private VBox editForm;
    @FXML private Label editFormTitle;
    @FXML private TextField editField;

    @FXML private TextField newNoteField;
    @FXML private VBox notesList;

    private ConcertData concertData;
    private ConcertDetailsController concertDetailsController;
    private String currentEditField;
    private Member concertOwner;

    ConcertDataDao concertDataDao= new ConcertDataDaoImpl();
    NoteDao noteDao= new NoteDaoImpl();

    @FXML
    private void initialize() {
        System.out.println("ConcertTrackController inizializzato");
    }

    public void setConcertData(ConcertData concertData, Member concertOwner) {
        this.concertData = concertData;
        this.concertOwner = concertOwner;
        loadTrackData();
    }

    public void setConcertDetailsController(ConcertDetailsController concertDetailsController) {
        this.concertDetailsController = concertDetailsController;
    }

    //CARICA DATI TRACCIA CONCERTO
    private void loadTrackData() {
        trackTitleLabel.setText(concertData.getTrackTitle());
        artistLabel.setText(concertData.getTrackAuthor());
        startTimeLabel.setText(formatSecondsToTime(concertData.getStartTime()));
        endTimeLabel.setText(formatSecondsToTime(concertData.getEndTime()));

        //esecutori
        if (concertData.getPerformer() != null && !concertData.getPerformer().isEmpty()) {
            performersLabel.setText(concertData.getPerformer());
        } else {
            performersLabel.setText("Non specificato");
            performersLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
        }

        //strumenti
        if (concertData.getInstrument() != null && !concertData.getInstrument().isEmpty()) {
            instrumentsLabel.setText(concertData.getInstrument());
        } else {
            instrumentsLabel.setText("Non specificato");
            instrumentsLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
        }

        //data
        if (concertData.getDate() != null && !concertData.getDate().isEmpty()) {
            dateLabel.setText(concertData.getDate());
        } else {
            dateLabel.setText("Non specificato");
            dateLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
        }

        //luogo
        if (concertData.getPlace() != null && !concertData.getPlace().isEmpty()) {
            placeLabel.setText(concertData.getPlace());
        } else {
            placeLabel.setText("Non specificato");
            placeLabel.setStyle("-fx-font-style: italic; -fx-text-fill: #666;");
        }

        loadNotes(); //carica note relative
    }

    private String formatSecondsToTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    //CARICA NOTE
    private void loadNotes() {
        notesList.getChildren().clear();

        List<Note> notes = noteDao.getNotesByConcertData(concertData.getId());
        for (Note note : notes) {
            VBox noteBox = createNoteBox(note);
            notesList.getChildren().add(noteBox);
        }
    }

    //SEZIONE NOTE
    private VBox createNoteBox(Note note) {
        VBox noteBox = new VBox(5);
        noteBox.setStyle("-fx-padding: 10; -fx-background-color: #f9f9f9; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-radius: 5;");

        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label authorLabel = new Label(note.getMember().getUsername());
        authorLabel.setStyle("-fx-text-fill: #2C3E50; -fx-font-weight: bold;");

        Label timeLabel = new Label(formatDateTime(note.getCreatedAt()));
        timeLabel.setStyle("-fx-text-fill: #7F8C8D; -fx-font-size: 12px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        //bottoe elimina
        Button deleteButton = new Button("✕");
        deleteButton.setStyle("-fx-text-fill: black; -fx-background-color: #E74C3C; -fx-border-color: transparent; -fx-min-width: 20; -fx-max-width: 20;");
        deleteButton.setPadding(new javafx.geometry.Insets(2, 5, 2, 5));

        //chi elimina
        boolean canDelete = MemberContextUtils.isCurrentUserOwnerOrAdmin(concertData.getMember()) || MemberContextUtils.isCreator(concertOwner);
        deleteButton.setManaged(canDelete);
        deleteButton.setVisible(canDelete);

        //elimina
        deleteButton.setOnAction(event -> {
            boolean deleted = noteDao.deleteNote(note.getId());
            if (deleted) {
                loadNotes(); // Ricarica le note
            }
        });

        headerBox.getChildren().addAll(authorLabel, timeLabel, spacer, deleteButton);

        Label contentLabel = new Label(note.getContent());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: #2C3E50;");

        noteBox.getChildren().addAll(headerBox, contentLabel);

        return noteBox;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    @FXML
    private void handleBack() {
        concertDetailsController.restoreTracksSection();
    }

    //MODIFICA PERFORMER
    @FXML
    private void handleEditPerformers() {
        currentEditField = "performers";
        editFormTitle.setText("Modifica esecutori");
        editField.setText(concertData.getPerformer() != null ? concertData.getPerformer() : "");
        editForm.setVisible(true);
        editForm.setManaged(true);
    }

    //MODIFICA STRUMENTI
    @FXML
    private void handleEditInstruments() {
        currentEditField = "instruments";
        editFormTitle.setText("Modifica strumenti");
        editField.setText(concertData.getInstrument() != null ? concertData.getInstrument() : "");
        editForm.setVisible(true);
        editForm.setManaged(true);
    }

    //MODIFICA DATA
    @FXML
    private void handleEditDate() {
        currentEditField = "date";
        editFormTitle.setText("Modifica data");
        editField.setText(concertData.getDate() != null ? concertData.getDate() : "");
        editForm.setVisible(true);
        editForm.setManaged(true);
    }

    //MODIFICA LUOGO
    @FXML
    private void handleEditPlace() {
        currentEditField = "place";
        editFormTitle.setText("Modifica luogo");
        editField.setText(concertData.getPlace() != null ? concertData.getPlace() : "");
        editForm.setVisible(true);
        editForm.setManaged(true);
    }

    //SALVA
    @FXML
    private void handleSaveEdit() {
        String value = editField.getText().trim();

        if ("performers".equals(currentEditField)) {
            concertData.setPerformer(value);
            performersLabel.setText(value.isEmpty() ? "Non specificato" : value);
            if (value.isEmpty()) {
                performersLabel.setStyle("-fx-text-fill: #666;");
            } else {
                performersLabel.setStyle("");
            }
        } else if ("instruments".equals(currentEditField)) {
            concertData.setInstrument(value);
            instrumentsLabel.setText(value.isEmpty() ? "Non specificato" : value);
            if (value.isEmpty()) {
                instrumentsLabel.setStyle("-fx-text-fill: #666;");
            } else {
                instrumentsLabel.setStyle("");
            }
        } else if ("date".equals(currentEditField)) {
            concertData.setDate(value);
            dateLabel.setText(value.isEmpty() ? "Non specificato" : value);
            if (value.isEmpty()) {
                dateLabel.setStyle("-fx-text-fill: #666;");
            } else  {
                dateLabel.setStyle("");
            }
        } else if ("place".equals(currentEditField)) {
            concertData.setPlace(value);
            placeLabel.setText(value.isEmpty() ? "Non specificato" : value);
            if (value.isEmpty()) {
                placeLabel.setStyle("-fx-text-fill: #666;");
            } else  {
                placeLabel.setStyle("");
            }
        }

        if (concertDataDao.updateConcertData(concertData)) {
            hideEditForm();
        } else {
            showFormMessage("Impossibile aggiornare i dettagli", "error");
        }
    }

    //ANNULLA MODIFICA
    @FXML
    private void handleCancelEdit() {
        hideEditForm();
    }

    private void hideEditForm() {
        editForm.setVisible(false);
        editForm.setManaged(false);
    }

    //AGGIUNGI NOTA
    @FXML
    private void handleAddNote() {
        String noteText = newNoteField.getText().trim();
        if (!noteText.isEmpty()) {
            Note note = new Note();
            note.setContent(noteText);
            note.setConcertData(concertData);
            note.setMember(MemberContextUtils.getLoggedInMember());

            noteDao.insertNote(note);
            newNoteField.clear();
            VBox noteBox = createNoteBox(note);
            notesList.getChildren().add(0, noteBox);
        }
    }

    //mostra messaggi nel form
    private void showFormMessage(String message, String type) {
        Platform.runLater(() -> {
            formMessageLabel.setText(message);
            if ("error".equals(type)) {
                formMessageLabel.setStyle("-fx-text-fill: #d9534f; -fx-font-size: 14px; -fx-wrap-text: true;");
            } else if ("success".equals(type)) {
                formMessageLabel.setStyle("-fx-text-fill: #5cb85c; -fx-font-size: 14px; -fx-wrap-text: true;");
            }
        });
    }
}
