package com.appmusicale.controller.member.topbar;

import com.appmusicale.controller.member.UserController;
import com.appmusicale.dao.*;
import com.appmusicale.model.*;
import com.appmusicale.util.MemberContextUtils;
import com.appmusicale.util.UIVisibilityUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

//SEZIONE CARICA BRANO

public class UploadTrackController {

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private TextField yearField;
    @FXML private ComboBox<GenreType> genreComboBox;
    @FXML private TextField performersField;
    @FXML private TextField instrumentsField;
    @FXML private TextField youtubeField;

    @FXML private ImageView coverPreview;
    @FXML private Label noCoverLabel;
    @FXML private Label previewTitle;
    @FXML private Label previewAuthor;

    @FXML private Button attachFileButton;
    @FXML private Button uploadButton;

    @FXML private Label titleErrorLabel;
    @FXML private Label authorErrorLabel;
    @FXML private Label yearErrorLabel;
    @FXML private Label genreErrorLabel;
    @FXML private Label instrumentErrorLabel;
    @FXML private Label generalErrorLabel;
    @FXML private Label youtubeErrorLabel;
    @FXML private Label successLabel;
    @FXML private VBox errorContainer;
    @FXML private VBox successContainer;

    private File selectedCoverFile;

    private UserController userController;


    private static final Pattern YOUTUBE_PATTERN = Pattern.compile(
            "^(https?://)?(www\\.)?(youtube\\.com|youtu\\.?be)/.+$",
            Pattern.CASE_INSENSITIVE);

    private final AuthorDao authorDao=new AuthorDaoImpl();
    private final GenreDao genreDao=new GenreDaoImpl();
    private final PerformerDao performerDao=new PerformerDaoImpl();
    private final TrackDao trackDao=new TrackDaoImpl();
    private final TrackPerformerDao trackPerformerDao=new TrackPerformerDaoImpl();

    @FXML
    public void initialize() {
        setupGenreComboBox();
        updatePreview();
        hideAllErrorMessages();
        hideSuccessMessage();
    }

    public void setUserController(UserController userController) {
        this.userController = userController;
    }

    //LISTA GENERI DA SCEGLIERE
    private void setupGenreComboBox() {
        try {
            List<GenreType> genres = genreDao.getPredefinedGenres();

            //ordina generi
            genres.sort(Comparator.comparing(GenreType::getDisplayName, String.CASE_INSENSITIVE_ORDER));

            genreComboBox.getItems().addAll(genres);

            genreComboBox.setCellFactory(lv -> new ListCell<GenreType>() {
                @Override
                protected void updateItem(GenreType genreType, boolean empty) {
                    super.updateItem(genreType, empty);
                    setText(empty || genreType == null ? "" : genreType.getDisplayName());
                }
            });

            genreComboBox.setButtonCell(new ListCell<GenreType>() {
                @Override
                protected void updateItem(GenreType genreType, boolean empty) {
                    super.updateItem(genreType, empty);
                    setText(empty || genreType == null ? "Seleziona genere" : genreType.getDisplayName());
                }
            });

        } catch (Exception e) {
            showGeneralError("Errore nel caricamento dei generi: " + e.getMessage());
        }
    }

    @FXML
    private void onTitleChanged() {
        updatePreview();
        hideError(titleErrorLabel);
    }

    @FXML
    private void onArtistChanged() {
        updatePreview();
        hideError(authorErrorLabel);
    }

    @FXML
    private void onYearChanged() {
        hideError(yearErrorLabel);
    }
    @FXML
    private void onYoutubeChanged() {
        hideError(youtubeErrorLabel);
    }

    @FXML
    private void onGenreSelected() {
        hideError(genreErrorLabel);
    }

    private void updatePreview() {
        String title = titleField.getText();
        String artist = authorField.getText();

        previewTitle.setText(title.isEmpty() ? "Titolo del brano" : title);
        previewAuthor.setText(artist.isEmpty() ? "Artista" : artist);
    }

    //COVER
    @FXML
    private void handleAttachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona Copertina");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Immagini", "*.png", "*.jpg", "*.jpeg", "*.gif"),
                new FileChooser.ExtensionFilter("Tutti i file", "*.*")
        );

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedCoverFile = file;
            try {
                Image image = new Image(file.toURI().toString());
                coverPreview.setImage(image);
                noCoverLabel.setVisible(false);
                coverPreview.setVisible(true);
                System.out.println("PATH selectedCoverFile" + selectedCoverFile.getAbsolutePath());
            } catch (Exception e) {
                showGeneralError("Impossibile caricare l'immagine selezionata.");
            }
        }
    }

    @FXML
    private void handleUpload() {
        hideAllErrorMessages();
        hideSuccessMessage();

        if (!validateForm()) {
            return;
        }

        try {
            //creo track
            Track track = createTrackFromForm();
            List<String> performerNames = getPerformerNamesFromTextArea();
            List<Performer> performers = new ArrayList<Performer>();

            //creo/recupero performer
            for (int i = 0; i < performerNames.size(); i++) {
                String name = performerNames.get(i);
                Performer p = performerDao.getOrCreatePerformer(name);
                if (p != null) {
                    performers.add(p);
                }
            }

            if (track != null) {
                String title = track.getTitle();
                Integer genreId = track.getGenreId();
                int year = track.getCompositionYear();

                //recupero tracce con lo stesso titolo x verifica
                List<Track> tracksWithSameTitle = trackDao.getTracksByTitle(title);
                boolean canCreate = true;

                for (int i = 0; i < tracksWithSameTitle.size(); i++) {
                    Track t = tracksWithSameTitle.get(i);

                    //controlla titolo, autore, genere e anno
                    boolean sameTitle = t.getTitle().equalsIgnoreCase(title);
                    boolean sameAuthor = t.getAuthor() != null && track.getAuthor() != null &&
                            t.getAuthor().getName().equalsIgnoreCase(track.getAuthor().getName());
                    boolean sameGenre = t.getGenreId() != null && t.getGenreId().equals(genreId);
                    boolean sameYear = t.getCompositionYear() == year;

                    if (sameTitle && sameAuthor && sameGenre && sameYear) {
                        //recupero performer della brano esistente
                        List<Performer> existingPerformers = trackPerformerDao.getPerformersByTrack(t.getId());
                        //verifica duplicato
                        if (existingPerformers.size() == performers.size()) {
                            boolean allMatch = true;
                            for (int j = 0; j < performers.size(); j++) {
                                boolean matchFound = false;
                                for (int k = 0; k < existingPerformers.size(); k++) {
                                    if (performers.get(j).getName().equalsIgnoreCase(existingPerformers.get(k).getName())) {
                                        matchFound = true;
                                        break;
                                    }
                                }
                                if (!matchFound) {
                                    allMatch = false;
                                    break;
                                }
                            }
                            if (allMatch) {
                                canCreate = false;
                                break; //duplicato
                            }
                        }
                    }
                }

                if (!canCreate) {
                    showGeneralError("Questo brano esiste già.");
                    return;
                }

                trackDao.insertTrack(track);

                //associo performer alla traccia
                for (int i = 0; i < performers.size(); i++) {
                    trackPerformerDao.associatePerformerToTrack(track.getId(), performers.get(i).getId());
                }

                showSuccess("Brano caricato con successo!");
                clearForm();

                if (userController != null) {
                    userController.loadTrackView(track.getId());
                }
            }

        } catch (Exception e) {
            showGeneralError("Errore durante il salvataggio: " + e.getMessage());
        }
    }


    //recupera i nomi dei performer da input
    private List<String> getPerformerNamesFromTextArea() {
        String input = performersField.getText();
        List<String> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        if (input != null && !input.isEmpty()) {
            String[] parts = input.split(",");
            for (String part : parts) {
                String original = part.trim();
                if (!original.isEmpty()) {
                    String normalized = original.toLowerCase();//normalizzo
                    if (!seen.contains(normalized)) {
                        result.add(correctCharacters(original));
                        seen.add(normalized);
                    }
                }
            }
        }
        return result;
    }


    //CREO TRACCIA
    private Track createTrackFromForm() {
        String rawtitle = titleField.getText().trim();
        String title = correctCharacters(rawtitle);

        String rawAuthName = authorField.getText().trim();
        String authorName = correctCharacters(rawAuthName);

        int year = Integer.parseInt(yearField.getText());
        GenreType selectedGenre = genreComboBox.getValue();
        String instruments = instrumentsField.getText();

        Integer genreId = genreDao.getGenreId(selectedGenre);
        if (genreId == null) {
            showGeneralError("Genere selezionato non valido.");
            return null;
        }

        String cover;
        if (selectedCoverFile != null) {
            cover = selectedCoverFile.getAbsolutePath();
        } else {
            cover = ("/com/appmusicale/images/default.jpeg");
        }

        Author author = authorDao.getOrCreateAuthor(authorName.trim());
        if (author == null || author.getId() == null) {
            showGeneralError("Impossibile salvare l'autore nel database.");
            return null;
        }

        Track track = new Track();
        track.setTitle(title);
        track.setCompositionYear(year);
        track.setGenreId(genreId);
        track.setInstruments(instruments);
        track.setAuthor(author);
        track.setCoverPath(cover);
        track.setYoutubeLink(youtubeField.getText().trim());
        track.setMember(MemberContextUtils.getLoggedInMember());

        return track;
    }

    private boolean validateForm() {
        boolean isValid = true;
        if (titleField.getText().isEmpty() || authorField.getText().isEmpty() ||
                yearField.getText().isEmpty() || genreComboBox.getValue() == null) {
            showGeneralError("Completa i campi obbligatori *");
            isValid = false;
        }
        //se l'utente è tra gli esecutori, gli strumenti sono obbligatori
        Member loggedInMember = MemberContextUtils.getLoggedInMember();
        if (loggedInMember != null && !performersField.getText().isEmpty()) {
            List<String> performerNames = getPerformerNamesFromTextArea();
            String correctedMemberName = correctCharacters(loggedInMember.getUsername());

            if (performerNames.contains(correctedMemberName) &&
                    instrumentsField.getText().trim().isEmpty()) {
                showError(instrumentErrorLabel, "Devi specificare almeno uno strumento se sei un esecutore");
                isValid = false;
            }
        }
        if (!yearField.getText().isEmpty()) {
            try {
                int year = Integer.parseInt(yearField.getText());
                if (year < 1900 || year > 2025) {
                    showError(yearErrorLabel, "Inserisci un anno valido");
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                showError(yearErrorLabel, "Inserisci un anno valido.");
                isValid = false;
            }
        }
        String youtubeLink = youtubeField.getText().trim();
        if (!youtubeLink.isEmpty() && !isValidYoutubeLink(youtubeLink)) {
            showError(youtubeErrorLabel, "Il link deve essere un URL valido di YouTube.");
            isValid = false;
        }
        return isValid;
    }

    private boolean isValidYoutubeLink(String link) {
        return YOUTUBE_PATTERN.matcher(link).matches();
    }

    private void clearForm() {
        titleField.clear();
        authorField.clear();
        yearField.clear();
        genreComboBox.getSelectionModel().clearSelection();
        instrumentsField.clear();
        performersField.clear();
        youtubeField.clear();
        coverPreview.setImage(null);
        coverPreview.setVisible(false);
        noCoverLabel.setVisible(true);
        selectedCoverFile = null;
        updatePreview();
        hideAllErrorMessages();
    }

    private void showError(Label label, String message) {
        UIVisibilityUtils.showError(label, message);
        UIVisibilityUtils.showPane(errorContainer);
    }

    private void hideError(Label label) {
        UIVisibilityUtils.hideError(label);
    }

    private void showGeneralError(String message) {
        UIVisibilityUtils.showError(generalErrorLabel, message);
        UIVisibilityUtils.showPane(errorContainer);
    }

    private void showSuccess(String message) {
        successLabel.setText(message);
        UIVisibilityUtils.showPane(successContainer);
    }

    private void hideSuccessMessage() {
        successLabel.setText("");
        UIVisibilityUtils.hidePane(successContainer);
    }

    private void hideAllErrorMessages() {
        UIVisibilityUtils.hideError(titleErrorLabel);
        UIVisibilityUtils.hideError(authorErrorLabel);
        UIVisibilityUtils.hideError(yearErrorLabel);
        UIVisibilityUtils.hideError(youtubeErrorLabel);
        UIVisibilityUtils.hideError(genreErrorLabel);
        UIVisibilityUtils.hideError(instrumentErrorLabel);
        UIVisibilityUtils.hideError(generalErrorLabel);
        UIVisibilityUtils.hidePane(errorContainer);
    }

    public static String correctCharacters(String input) {
        if (input == null || input.isBlank()) return input;
        String[] words = input.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1));
            }
            sb.append(" ");
        }
        return sb.toString().trim();
    }

}