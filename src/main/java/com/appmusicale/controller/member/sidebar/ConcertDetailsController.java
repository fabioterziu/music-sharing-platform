package com.appmusicale.controller.member.sidebar;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.dao.*;
import com.appmusicale.model.Concert;
import com.appmusicale.model.ConcertData;
import com.appmusicale.util.MemberContextUtils;
import com.appmusicale.util.SceneManagerUtils;
import com.appmusicale.util.YouTubeAPIUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import java.util.List;

//SEZIONE DENTRO CONCERTO

public class ConcertDetailsController {

    @FXML private WebView youtubePlayer;
    @FXML private Label concertTitleLabel;
    @FXML private TextField trackTitleField;
    @FXML private TextField authorField;
    @FXML private TextField startTimeField;
    @FXML private TextField endTimeField;
    @FXML private GridPane tracksGrid;
    @FXML private StackPane videoContainer;
    @FXML private Pane videoOverlay;
    @FXML private Hyperlink youtubeLink;
    @FXML private Label formMessageLabel;
    @FXML private Button deleteConcertButton;
    @FXML private ScrollPane tracksSection;

    private UserController userController;
    private Node originalContent;
    private Concert concert;
    private List<ConcertData> concertDataList;

    ConcertDao concertDao= new ConcertDaoImpl();
    ConcertDataDao concertDataDao= new ConcertDataDaoImpl();


    @FXML
    private void initialize() {
        System.out.println("ConcertDetailsController inizializzato");
        youtubePlayer.getEngine().setJavaScriptEnabled(true);
        youtubePlayer.setMinSize(400, 225);
        videoContainer.widthProperty().addListener((obs, oldVal, newVal) -> {
            double width = newVal.doubleValue();
            double height = width * 9.0 / 16.0;
            youtubePlayer.setPrefSize(width, height);
            videoContainer.setPrefHeight(height);
            resizeYouTubePlayer();
        });
        videoOverlay.setVisible(false);
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    private void resizeYouTubePlayer() {
        Platform.runLater(() -> {
            if (youtubePlayer.getEngine().getDocument() != null) {
                try { //iframe per gestire spazio
                    String script = "var iframe = document.getElementsByTagName('iframe')[0];" +
                            "if (iframe) {iframe.style.width = '100%';iframe.style.height = '100%';}"
                            ;
                    youtubePlayer.getEngine().executeScript(script);
                } catch (Exception e) {
                    //
                }
            }
        });
    }

    public void setConcert(Concert concert) {
        this.concert = concert;
        initializeConcertData();
    }

    private void initializeConcertData() {
        concertTitleLabel.setText(concert.getTitle());
        boolean canDelete = MemberContextUtils.isCurrentUserOwnerOrAdmin(concert.getMember());
        deleteConcertButton.setVisible(canDelete);
        deleteConcertButton.setManaged(canDelete);

        if (concert.getYoutubeId() != null && !concert.getYoutubeId().isEmpty()) {
            youtubeLink.setText("Apri su YouTube: " + concert.getYoutubeId());
            loadYouTubeVideo();
        } else {
            youtubeLink.setVisible(false);
        }
        loadExistingTracks();
    }

    //HTML (width, height, frameborder, allowfullscreen
    private void loadYouTubeVideo() {
        try {
            String videoId = concert.getYoutubeId();
            String videoUrl = "https://www.youtube.com/embed/" + videoId + "?rel=0&modestbranding=1";
            String videoHtml = "<html><body style='margin:0;padding:0;'>" +
                    "<iframe width='100%' height='100%' src='" + videoUrl + "' " +
                    "frameborder='0' allowfullscreen></iframe>" +
                    "</body></html>";
            youtubePlayer.getEngine().loadContent(videoHtml, "text/html");
            youtubePlayer.setVisible(true);
            videoOverlay.setVisible(false);
        } catch (Exception e) {
            System.err.println("Errore nel caricamento del video: " + e.getMessage());
        }
    }

    @FXML
    private void openYouTubeInBrowser() {
        if (concert != null && concert.getYoutubeId() != null && !concert.getYoutubeId().isEmpty()) {
            try {
                java.awt.Desktop.getDesktop().browse(
                        new java.net.URI("https://www.youtube.com/watch?v=" + concert.getYoutubeId()));
            } catch (Exception ex) {
                showFormMessage("Impossibile aprire il link YouTube", "error");
            }
        } else {
            showFormMessage("Nessun video YouTube disponibile", "error");
        }
    }

    //AGGIUNGI BRANO
    @FXML
    private void handleAddTrack() {
        String title = trackTitleField.getText().trim();
        String artist = authorField.getText().trim();
        String startTimeStr = startTimeField.getText().trim();
        String endTimeStr = endTimeField.getText().trim();

        if (title.isEmpty() || artist.isEmpty() || startTimeStr.isEmpty() || endTimeStr.isEmpty()) {
            showFormMessage("Informazioni brano non complete", "error");
            return;
        }

        //crea brano vuotos
        try {
            int startTime = parseTimeToSeconds(startTimeStr);
            int endTime = parseTimeToSeconds(endTimeStr);

            //controllo endtime > starttime
            if (endTime <= startTime) {
                showFormMessage("Il tempo di fine deve essere maggiore del tempo di inizio", "error");
                return;
            }

            //controllo sovrapposizioni
            if (hasTimeOverlap(startTime, endTime)) {
                showFormMessage("La traccia si sovrappone ad una traccia esistente", "error");
                return;
            }

            //crea nuovo ConcertData
            ConcertData concertData = new ConcertData();
            concertData.setConcert(concert);
            concertData.setTrackTitle(title);
            concertData.setTrackAuthor(artist);
            concertData.setStartTime(startTime);
            concertData.setEndTime(endTime);
            concertData.setMember(MemberContextUtils.getLoggedInMember());

            //salva nel db
            concertDataDao.insertConcertData(concertData);

            //ricarica lista
            loadExistingTracks();
            clearTrackFields();

            showFormMessage("Brano aggiunto con successo!", "success");

        } catch (NumberFormatException e) {
            showFormMessage("Formato tempo non valido. Usa il formato hh:mm:ss", "error");
        } catch (Exception e) {
            showFormMessage("Errore nell'inserimento del brano: " + e.getMessage(), "error");
        }
    }

    //ELIMINA CONCERTO
    @FXML
    private void handleDeleteConcert() {
        boolean deleted = concertDao.deleteConcert(concert.getId());
        if (deleted && userController != null) {
            userController.loadView("/com/appmusicale/fxml/member/sidebar/concerts-view.fxml");
        }
    }


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

    //VERIFICA sovrapposizioni temporali tra i brani
    private boolean hasTimeOverlap(int newStartTime, int newEndTime) {
        for (ConcertData existingTrack : concertDataList) {
            int existingStart = existingTrack.getStartTime();
            int existingEnd = existingTrack.getEndTime();

            if ((newStartTime >= existingStart && newStartTime < existingEnd) ||
                    (newEndTime > existingStart && newEndTime <= existingEnd) ||
                    (newStartTime <= existingStart && newEndTime >= existingEnd)) {
                return true;
            }
        }
        return false;
    }

    //conversione il formato "hh:mm:ss" in secondi
    private int parseTimeToSeconds(String timeStr) throws NumberFormatException {
        String[] parts = timeStr.split(":");
        int seconds = 0;

        if (parts.length == 3) {
            //hh:mm:ss
            seconds = Integer.parseInt(parts[0]) * 3600 +
                    Integer.parseInt(parts[1]) * 60 +
                    Integer.parseInt(parts[2]);
        } else if (parts.length == 2) {
            //mm:ss
            seconds = Integer.parseInt(parts[0]) * 60 +
                    Integer.parseInt(parts[1]);
        } else if (parts.length == 1) {
            //ss
            seconds = Integer.parseInt(parts[0]);
        } else  {
            throw new NumberFormatException("Formato tempo non supportato");
        }

        return seconds;
    }

    //conversione secondi in formato hh:mm:ss
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

    private void loadExistingTracks() {
        concertDataList = concertDataDao.getConcertDataByConcert(concert.getId());
        refreshTracks();
    }

    //AGGIORNA GRIGLIA TRACCE
    private void refreshTracks() {
        tracksGrid.getChildren().clear();

        int row = 0;
        for (ConcertData concertData : concertDataList) {
            String startTimeStr = formatSecondsToTime(concertData.getStartTime());
            String endTimeStr = formatSecondsToTime(concertData.getEndTime());

            Label titleLabel = new Label(concertData.getTrackTitle());
            Label artistLabel = new Label(concertData.getTrackAuthor());

            Button detailsButton = new Button("Dettagli");
            detailsButton.getStyleClass().add("secondary-button");
            detailsButton.setOnAction(e -> openTrackDetails(concertData));

            Button deleteButton = new Button("Elimina");
            deleteButton.getStyleClass().add("red-secondary-button");
            deleteButton.setOnAction(e -> {
                try {
                    concertDataDao.deleteConcertData(concertData.getId());
                    loadExistingTracks();
                } catch (Exception ex) {
                    showFormMessage("Impossibile eliminare la traccia: " + ex.getMessage(), "error");
                }
            });

            boolean canDelete = MemberContextUtils.isCurrentUserOwnerOrAdmin(concertData.getMember()) || MemberContextUtils.isCreator(concert.getMember());
            deleteButton.setVisible(canDelete);
            deleteButton.setDisable(!canDelete);

            //aggiungo componenti alla griglia
            tracksGrid.add(titleLabel, 0, row);
            tracksGrid.add(artistLabel, 1, row);
            tracksGrid.add(new Label(startTimeStr), 2, row);
            tracksGrid.add(new Label(endTimeStr), 3, row);

            //hbox bottoni
            HBox actionsBox = new HBox(5);
            actionsBox.setAlignment(Pos.CENTER);
            actionsBox.getChildren().addAll(detailsButton, deleteButton);
            tracksGrid.add(actionsBox, 4, row);

            row++;
        }
    }

    //apre i dettagli della traccia
    private void openTrackDetails(ConcertData concertData) {
        try {
            originalContent = tracksSection.getContent();
            ConcertTrackController controller = (ConcertTrackController) SceneManagerUtils.loadViewInScrollPane(
                    tracksSection,
                    "/com/appmusicale/fxml/member/sidebar/concert-track-view.fxml"
            );

            if (controller != null) {
                controller.setConcertData(concertData, concert.getMember());
                controller.setConcertDetailsController(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showFormMessage("Impossibile caricare i dettagli della traccia", "error");
        }
    }

    //ripristina la vista originale dello ScrollPane
    public void restoreTracksSection() {
        //ripristina i children originali
        if (originalContent != null) {
            tracksSection.setContent(originalContent);
            loadExistingTracks(); //ricarica dati
        }
    }

    private void clearTrackFields() {
        trackTitleField.clear();
        authorField.clear();
        startTimeField.clear();
        endTimeField.clear();
    }
}
