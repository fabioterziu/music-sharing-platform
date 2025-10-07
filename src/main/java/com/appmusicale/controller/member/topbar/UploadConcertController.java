package com.appmusicale.controller.member.topbar;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.dao.AuthorDao;
import com.appmusicale.dao.ConcertDao;
import com.appmusicale.dao.ConcertDaoImpl;
import com.appmusicale.model.Concert;
import com.appmusicale.util.MemberContextUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//SEZIONE CARICA CONCERTO

public class UploadConcertController {

    @FXML private TextField youtubeLinkField;
    @FXML private Button uploadButton;
    @FXML private Label statusLabel;
    @FXML private VBox messageContainer;
    @FXML private ProgressIndicator progressIndicator;

    private static final String YOUTUBE_API_KEY = System.getenv("YOUTUBE_API_KEY");
    //da inserire chiave da terminale con "export YOUTUBE_API_KEY="KEY"

    private UserController userController;

    ConcertDao concertDao=new ConcertDaoImpl();

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    //CARICA
    @FXML
    private void handleUpload() {
        String youtubeLink = youtubeLinkField.getText().trim();

        uploadButton.setDisable(true);
        showProgress(true);

        if (!validateYouTubeLink(youtubeLink)) {
            showMessage("Inserisci un link YouTube valido.", "error");
            uploadButton.setDisable(false);
            showProgress(false);
            return;
        }

        try {
            String videoId = extractVideoId(youtubeLink);
            if (videoId == null) {
                showMessage("Impossibile estrarre l'ID del video YouTube.", "error");
                uploadButton.setDisable(false);
                showProgress(false);
                return;
            }

            //verifica se il concerto esiste già
            Concert existingConcert = concertDao.getConcertByYoutubeId(videoId);
            if (existingConcert != null) {
                showMessage("Questo concerto è già presente.", "warning");
                uploadButton.setDisable(false);
                showProgress(false);
                return;
            }

            showMessage("Recupero informazioni da YouTube...", "info");


            //recupera dati del video dall'API
            VideoInfo videoInfo = getVideoInfoFromYouTubeAPI(videoId);

            String title;
            String thumbnailUrl;

            if (videoInfo != null && videoInfo.title != null && videoInfo.thumbnailUrl != null) {
                title = videoInfo.title;
                thumbnailUrl = videoInfo.thumbnailUrl;
            } else {
                //se l'API non funziona
                title = "Concerto YouTube - " + videoId;
                thumbnailUrl = "https://i.ytimg.com/vi/" + videoId + "/hqdefault.jpg";
            }

            //crea e salva il nuovo concerto
            Concert newConcert = new Concert();
            newConcert.setTitle(title);
            newConcert.setYoutubeUrl(youtubeLink);
            newConcert.setYoutubeId(videoId);
            newConcert.setThumbnailUrl(thumbnailUrl);
            newConcert.setMember(MemberContextUtils.getLoggedInMember());

            concertDao.insertConcert(newConcert);

            if (userController != null) {
                userController.loadConcertDetailsView(newConcert);
            }

            showMessage("Concerto caricato con successo! Titolo: " + title, "success");
            clearForm();
            uploadButton.setDisable(false);
            showProgress(false);

        } catch (Exception ex) {
            showMessage("Impossibile caricare il concerto: " + ex.getMessage(), "error");
            uploadButton.setDisable(false);
            showProgress(false);
        }
    }

    private void showMessage(String message, String type) {
        Platform.runLater(() -> {
            messageContainer.setVisible(true);
            statusLabel.setText(message);

            //colore in base al tipo di messaggio
            switch(type) {
                case "error":
                    statusLabel.setStyle("-fx-text-fill: #d9534f; -fx-font-size: 14px; -fx-wrap-text: true; -fx-max-width: 400; -fx-alignment: center;");
                    break;
                case "success":
                    statusLabel.setStyle("-fx-text-fill: #5cb85c; -fx-font-size: 14px; -fx-wrap-text: true; -fx-max-width: 400; -fx-alignment: center;");
                    break;
                case "warning":
                    statusLabel.setStyle("-fx-text-fill: #f0ad4e; -fx-font-size: 14px; -fx-wrap-text: true; -fx-max-width: 400; -fx-alignment: center;");
                    break;
                default:
                    statusLabel.setStyle("-fx-text-fill: #5bc0de; -fx-font-size: 14px; -fx-wrap-text: true; -fx-max-width: 400; -fx-alignment: center;");
            }
        });
    }

    private void showProgress(boolean show) {
        progressIndicator.setVisible(show);
    }

    private VideoInfo getVideoInfoFromYouTubeAPI(String videoId) {
        try {
            String apiUrl = "https://www.googleapis.com/youtube/v3/videos?id=" + videoId +
                    "&part=snippet&key=" + YOUTUBE_API_KEY;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return extractVideoInfoFromJson(response.body());
            } else {
                System.err.println("API YouTube error: " + response.statusCode());
            }
        } catch (Exception e) {
            System.err.println("Errore API YouTube: " + e.getMessage());
        }
        return null;
    }

    private VideoInfo extractVideoInfoFromJson(String json) {
        try {
            //estrae il titolo
            Pattern titlePattern = Pattern.compile("\"title\"\\s*:\\s*\"([^\"]+)\"");
            Matcher titleMatcher = titlePattern.matcher(json);

            //estrae la thumbnail
            Pattern thumbPattern = Pattern.compile("\"high\"\\s*:\\s*\\{[^}]*\"url\"\\s*:\\s*\"([^\"]+)\"");
            Matcher thumbMatcher = thumbPattern.matcher(json);

            String title = null;
            String thumbnailUrl = null;

            if (titleMatcher.find()) {
                title = titleMatcher.group(1);
            }

            if (thumbMatcher.find()) {
                thumbnailUrl = thumbMatcher.group(1);
            }

            if (title != null && thumbnailUrl != null) {
                return new VideoInfo(title, thumbnailUrl);
            }

        } catch (Exception e) {
            System.err.println("Errore estrazione info JSON: " + e.getMessage());
        }
        return null;
    }

    private String extractVideoId(String youtubeLink) {
        Pattern pattern = Pattern.compile(
                "(?:youtube\\.com\\/(?:[^\\/]+\\/.+\\/|(?:v|e(?:mbed)?)\\/|.*[?&]v=)|youtu\\.be\\/)([^\"&?\\/\\s]{11})"
        );
        Matcher matcher = pattern.matcher(youtubeLink);
        return matcher.find() ? matcher.group(1) : null;
    }

    private boolean validateYouTubeLink(String link) {
        return link != null && !link.isEmpty() &&
                (link.contains("youtube.com/") || link.contains("youtu.be/"));
    }

    private void clearForm() {
        youtubeLinkField.setText("");
        statusLabel.setText("");
    }

    //classe interna per contenere le informazioni del video
    private static class VideoInfo {
        String title;
        String thumbnailUrl;

        VideoInfo(String title, String thumbnailUrl) {
            this.title = title;
            this.thumbnailUrl = thumbnailUrl;
        }
    }
}