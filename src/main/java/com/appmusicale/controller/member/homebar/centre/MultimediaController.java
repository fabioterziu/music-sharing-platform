package com.appmusicale.controller.member.homebar.centre;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.control.Button;
import javafx.util.Duration;

import java.io.File;

public class MultimediaController {

    @FXML private MediaView mediaView;
    @FXML private ImageView imageView;
    @FXML private Button ssButton;
    @FXML private Slider progressSlider;
    @FXML private Label currentTime, totalTime;
    @FXML private StackPane mediaContainer;

    private MediaPlayer mediaPlayer;


    @FXML
    public void initialize() {
        //immagine/video si adatta al contenitore
        imageView.fitWidthProperty().bind(mediaContainer.widthProperty());
        imageView.fitHeightProperty().bind(mediaContainer.heightProperty());

        mediaView.fitWidthProperty().bind(mediaContainer.widthProperty());
        mediaView.fitHeightProperty().bind(mediaContainer.heightProperty());
    }


    @FXML
    public void setMediaPath(String path) {
        try {
            File file = new File(path);
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            //SLIDER
            mediaPlayer.setOnReady(() -> {
                progressSlider.setMax(media.getDuration().toSeconds());
                //durata massima
                Duration totalDuration = media.getDuration();
                if (totalDuration.toSeconds() > 0) {
                    totalTime.setText(formatDuration(totalDuration));
                }
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newTime.toSeconds());
                }
                //aggiorno il tempo durante la riproduzione
                currentTime.setText(formatDuration(newTime));
            });

            //quando rilascio slider fa un seek (ricarica al punto dove mi trovo)
            progressSlider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
                if (!isChanging) {
                    mediaPlayer.seek(Duration.seconds(progressSlider.getValue()));
                }
            });

            progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (!progressSlider.isValueChanging()) { //se non trascino manualmente
                    double currentTime = mediaPlayer.getCurrentTime().toSeconds();
                    if (Math.abs(currentTime - newVal.doubleValue()) > 1) { //se differenza tra player e splider>1
                        mediaPlayer.seek(Duration.seconds(newVal.doubleValue())); //seek
                    }
                }
            });


            //CASI MP3/MP4
            if(path.toLowerCase().endsWith(".mp4")) {
                mostraMp4();
                //riproduci video
                mediaView.setMediaPlayer(mediaPlayer);
            }
            else{
                mostraMp3();
                //mostra immagine
                imageView.setImage(
                        new Image(getClass().getResourceAsStream("/com/appmusicale/images/mp3Image.jpg")));
                mediaView.setMediaPlayer(null);
            }
            //mediaPlayer.play(); per startare in automatico all'avvio
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore apertura media: " + path);
        }
    }

    private String formatDuration(Duration duration) {
        long seconds = (long) duration.toSeconds(); //secondi totali della durata
        long minutes = seconds / 60;
        seconds %= 60;
        return String.format("%02d:%02d", minutes, seconds); //formato
    }

    @FXML
    private void ssMedia() { //start e stop con cambio icona al click
        if (mediaPlayer == null) return;

        if ("▶".equals(ssButton.getText())) {
            mediaPlayer.play();
            ssButton.setText("⏸");
        } else {
            mediaPlayer.pause();
            ssButton.setText("▶");
        }

        mediaPlayer.setOnEndOfMedia(() -> { //quando finisce resetto
            progressSlider.setValue(0);
            currentTime.setText("00:00");
            mediaPlayer.pause();
            ssButton.setText("▶");
        });
    }

    private void mostraMp3() {
        imageView.setVisible(true);
        mediaView.setVisible(false);
    }

    private void mostraMp4() {
        imageView.setVisible(false);
        mediaView.setVisible(true);
    }

    //per fermare mediaplayer quando si chiude senza stoppare
    public void stopMedia() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}
