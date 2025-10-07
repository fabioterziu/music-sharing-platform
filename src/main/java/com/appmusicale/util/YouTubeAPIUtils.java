// YouTubeAPIUtils.java
package com.appmusicale.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class YouTubeAPIUtils {


    private static final String YOUTUBE_API_KEY = System.getenv("YOUTUBE_API_KEY");
    //da inserire chiave da terminale con "export YOUTUBE_API_KEY="KEY"

    public static VideoStatus checkVideoStatus(String videoId) {
        try {
            String apiUrl = "https://www.googleapis.com/youtube/v3/videos?id=" + videoId +
                    "&part=status,player&key=" + YOUTUBE_API_KEY;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseVideoStatus(response.body());
            } else {
                return new VideoStatus(false, "Errore API YouTube: " + response.statusCode(), false);
            }

        } catch (Exception e) {
            return new VideoStatus(false, "Errore di connessione: " + e.getMessage(), false);
        }
    }

    private static VideoStatus parseVideoStatus(String jsonResponse) {
        try {
            // Verifica se il video esiste
            Pattern existsPattern = Pattern.compile("\"totalResults\"\\s*:\\s*(\\d+)");
            Matcher existsMatcher = existsPattern.matcher(jsonResponse);

            if (existsMatcher.find() && Integer.parseInt(existsMatcher.group(1)) == 0) {
                return new VideoStatus(false, "Video non trovato", false);
            }

            // Verifica se il video è embeddable
            Pattern embedPattern = Pattern.compile("\"embeddable\"\\s*:\\s*(true|false)");
            Matcher embedMatcher = embedPattern.matcher(jsonResponse);
            boolean isEmbeddable = embedMatcher.find() && Boolean.parseBoolean(embedMatcher.group(1));

            // Verifica lo stato del video
            Pattern statusPattern = Pattern.compile("\"privacyStatus\"\\s*:\\s*\"([^\"]+)\"");
            Matcher statusMatcher = statusPattern.matcher(jsonResponse);
            String privacyStatus = statusMatcher.find() ? statusMatcher.group(1) : "unknown";

            if (!"public".equals(privacyStatus)) {
                return new VideoStatus(false, "Video non pubblico: " + privacyStatus, isEmbeddable);
            }

            return new VideoStatus(true, "Video disponibile", isEmbeddable);

        } catch (Exception e) {
            return new VideoStatus(false, "Errore nell'analisi della risposta", false);
        }
    }

    public static class VideoStatus {
        public final boolean isAvailable;
        public final String message;
        public final boolean isEmbeddable;

        public VideoStatus(boolean isAvailable, String message, boolean isEmbeddable) {
            this.isAvailable = isAvailable;
            this.message = message;
            this.isEmbeddable = isEmbeddable;
        }
    }
}