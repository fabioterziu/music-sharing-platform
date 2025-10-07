package com.appmusicale.util;

import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;


public class AppUtils {

    //FILE DOWNLOAD
    public static void downloadFile(File file){
        if (file.exists()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salva file come");
            fileChooser.setInitialFileName(file.getName());

            File destination = fileChooser.showSaveDialog(null);
            if (destination != null) {
                try {
                    Files.copy(file.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("File salvato in: " + destination.getAbsolutePath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            SceneManagerUtils.openImageStage("/com/appmusicale/images/notexist.jpeg");
        }
    }


    //APERTURA PAGINA WEB
    public static void openYoutubeLink(String url) {
        try {
            // Assicurati che l'URL abbia il protocollo
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }

            URI uri = new URI(url);
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(uri);
            }
        } catch (Exception e) {
            System.err.println("Errore nell'aprire il link YouTube: " + e.getMessage());
        }
    }

    //TIME
    public static String formatSecondsToTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        java.time.format.DateTimeFormatter formatter =
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    //per convertire stringa mm:ss in secondi
    public static Integer parseTimeToSeconds(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return null;
        }
        try {
            String[] parts = timeString.split(":");
            if (parts.length == 2) {
                int minutes = Integer.parseInt(parts[0]);
                int seconds = Integer.parseInt(parts[1]);
                return minutes * 60 + seconds;
            } else {
                return Integer.parseInt(timeString);
            }
        } catch (NumberFormatException e) {
            System.err.println("Formato tempo non valido: " + timeString);
            return null;
        }
    }



}
