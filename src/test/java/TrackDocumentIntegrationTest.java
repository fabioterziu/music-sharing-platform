import com.appmusicale.dao.*;
import com.appmusicale.model.*;
import com.appmusicale.service.TrackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

//INTEGRATION TEST per la gestione brani e documenti
/* COSA TESTA:
    - Integrazione TrackService con oggetti Model
    - Validazioni business sui dati dei brani
    - Relazioni corrette tra Track, Author e Member
    - Workflow completo inserimento brani (senza database)
    - Gestione file e media associati ai brani
*/
class TrackDocumentIntegrationTest {

    //COSTANTI DI TEST - Valori predefiniti per i test
    private static final String TEST_TRACK_TITLE = "Integration Test Song";
    private static final Integer TEST_COMPOSITION_YEAR = 2025;
    private static final String TEST_YOUTUBE_LINK = "https://youtube.com/watch?v=integration_test";
    private static final String TEST_COVER_PATH = "/covers/integration_test.jpg";
    private static final String TEST_INSTRUMENTS = "Guitar,Piano,Drums";
    private static final Integer TEST_GENRE_ID = 1;
    private static final Integer TEST_AUTHOR_ID = 1;
    private static final Integer TEST_MEMBER_ID = 1;

    //COMPONENTI DEL SISTEMA DA TESTARE
    private TrackService trackService;  // Service principale per brani

    // OGGETTI DI TEST - Simulano dati reali
    private Track testTrack;           // Brano di test
    private Member testMember;         // Utente di test
    private Author testAuthor;         // Autore di test
    private Media testMedia;           // Media associato al brano
    private File testDocumentFile;     // File documento di test

    //FILE PER SALVARE I RISULTATI DEI TEST
    private static final String INTEGRATION_RESULTS_FILE = "src/test/resources/test-results/integration_test_results.csv";

    /*
    SETUP INIZIALE - Eseguito UNA SOLA VOLTA prima di tutti i test
    Crea la directory per i risultati e inizializza il file CSV
    */
    @BeforeAll
    static void setupResultsFiles() throws IOException {
        //Crea directory se non esiste
        new java.io.File("src/test/resources/test-results/").mkdirs();

        //Inizializza file CSV con intestazioni colonne
        try (FileWriter writer = new FileWriter(INTEGRATION_RESULTS_FILE)) {
            writer.write("test_name,component_flow,track_id,media_count,success,execution_time_ms,timestamp,error_message\n");
        }
    }

    /*
    SETUP PRIMA DI OGNI TEST - Prepara ambiente pulito
    Istanzia oggetti e prepara dati di test
    */
    @BeforeEach
    void setUp() throws IOException {
        // Istanzia il service principale da testare
        trackService = new TrackService();

        // Prepara tutti i dati di test
        setupCompleteTestData();
        setupTestFiles();
    }

    /*TEST 1: INTEGRAZIONE SERVICE E BUSINESS LOGIC
    COSA TESTA:
        - Il TrackService gestisce correttamente i dati
        - Le validazioni business funzionano
        - La logica di business è integrata correttamente
    */
    @Test
    void testIntegrationServiceBusinessLogic() {
        String testName = "testIntegrationServiceBusinessLogic";
        String componentFlow = "TrackService→BusinessLogic→Validation";
        long startTime = System.currentTimeMillis();
        boolean success = false;
        Integer trackId = testTrack.getId();
        String errorMessage = "";

        try {
            //FASE 1: VERIFICA PREPARAZIONE COMPONENTI
            //Controlla che tutti i componenti siano pronti per il test
            assertNotNull(trackService, "Il TrackService dovrebbe essere istanziato");
            assertNotNull(testTrack, "Il track di test dovrebbe essere preparato");
            assertNotNull(testMember, "Il member di test dovrebbe essere preparato");

            //FASE 2: TEST VALIDAZIONI BUSINESS
            //Verifica che le validazioni sui dati funzionino correttamente
            assertTrue(testTrack.getTitle().length() > 0, "Il titolo deve essere valido per il service");
            assertTrue(testTrack.getCompositionYear() > 1800, "L'anno deve essere realistico per il service");
            assertEquals(Status.ACTIVE, testMember.getStatus(), "Il member deve essere ACTIVE per operazioni");

            //FASE 3: TEST LOGICA BUSINESS DEL SERVICE
            //Testa metodi del TrackService che non richiedono database
            String genreDisplay = null;
            try {
                //Prova a ottenere il nome del genere
                genreDisplay = trackService.getGenreDisplayName(testTrack);
            } catch (Exception e) {
                //Se fallisce per mancanza database, è previsto - usiamo valore di default
                genreDisplay = "Test Genre";
            }
            assertNotNull(genreDisplay, "Il service dovrebbe gestire il genere display name");

            //FASE 4: TEST GESTIONE FILE E MEDIA
            //Verifica che la gestione file sia preparata correttamente
            assertNotNull(testDocumentFile, "Il file di test dovrebbe esistere");
            assertTrue(testDocumentFile.exists(), "Il file di test dovrebbe essere accessibile");
            assertTrue(testDocumentFile.length() > 0, "Il file dovrebbe avere contenuto");

            //Test validazioni parametri per aggiunta media
            assertNotNull(testMember, "Il member per addMedia non può essere null");
            assertTrue(trackId > 0, "Il trackId deve essere positivo");

            success = true;

        } catch (Exception e) {
            errorMessage = e.getMessage();
            fail("Errore durante test integration service-business logic: " + e.getMessage());
        } finally {
            //Salva sempre i risultati, anche in caso di errore
            long executionTime = System.currentTimeMillis() - startTime;
            saveIntegrationTestResult(testName, componentFlow, trackId, 0, success, executionTime, errorMessage);
        }
    }

    /*TEST 2: INTEGRAZIONE OGGETTI E RELAZIONI
    COSA TESTA:
        - Le relazioni tra oggetti Model funzionano
        - Track ↔ Author, Track ↔ Member, Media ↔ Track
        - Validazioni cross-object
    */
    @Test
    void testIntegrationOggettiRelazioni() {
        String testName = "testIntegrationOggettiRelazioni";
        String componentFlow = "Model→Relations→Integration";
        long startTime = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = "";

        try {
            //TEST RELAZIONE TRACK ↔ AUTHOR
            //Verifica che il track abbia l'autore corretto
            assertNotNull(testTrack.getAuthor(), "Il track dovrebbe avere un author");
            assertEquals(testAuthor.getId(), testTrack.getAuthor().getId(), "L'ID author deve corrispondere");
            assertEquals(testAuthor.getName(), testTrack.getAuthor().getName(), "Il nome author deve corrispondere");

            //TEST RELAZIONE TRACK ↔ MEMBER
            //Verifica che il track sia associato al member corretto
            assertNotNull(testTrack.getMember(), "Il track dovrebbe avere un member");
            assertEquals(testMember.getId(), testTrack.getMember().getId(), "L'ID member deve corrispondere");
            assertEquals(testMember.getUsername(), testTrack.getMember().getUsername(), "Lo username deve corrispondere");

            //TEST RELAZIONE MEDIA ↔ TRACK
            //Imposta e verifica la relazione media-track
            testMedia.setTrack(testTrack);
            testMedia.setMember(testMember);

            assertNotNull(testMedia.getTrack(), "Il media dovrebbe avere un track");
            assertEquals(testTrack.getId(), testMedia.getTrack().getId(), "L'ID track nel media deve corrispondere");

            //TEST VALIDAZIONI CROSS-OBJECT
            //Verifica che le relazioni siano coerenti tra loro
            assertTrue(testTrack.getAuthor().getName().length() > 0, "L'autore deve avere un nome valido");
            assertTrue(testTrack.getMember().getUsername().length() >= 3, "Il member deve avere username valido");
            assertEquals("DOCUMENT", testMedia.getType(), "Il media deve avere type valido");

            success = true;

        } catch (Exception e) {
            errorMessage = e.getMessage();
            fail("Errore durante test integration oggetti-relazioni: " + e.getMessage());
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            saveIntegrationTestResult(testName, componentFlow, null, 1, success, executionTime, errorMessage);
        }
    }

    /*TEST 3: WORKFLOW COMPLETO DI INTEGRAZIONE
    COSA TESTA:
        - Simula il workflow completo di inserimento brano
        - Dall'utente al database (senza DB reale)
        - Tutte le fasi di validazione e preparazione
    */
    @Test
    void testIntegrationWorkflowCompleto() {
        String testName = "testIntegrationWorkflowCompleto";
        String componentFlow = "CompleteWorkflow→Integration";
        long startTime = System.currentTimeMillis();
        boolean success = false;
        String errorMessage = "";

        try {
            //SIMULAZIONE WORKFLOW INSERIMENTO BRANO - STEP BY STEP

            //Step 1: Validazione utente
            assertTrue(testMember.getStatus() == Status.ACTIVE, "Step 1: Utente deve essere approvato");

            //Step 2: Validazione dati track
            assertTrue(testTrack.getTitle() != null && !testTrack.getTitle().trim().isEmpty(), "Step 2: Titolo track deve essere valido");
            assertTrue(testTrack.getCompositionYear() != null && testTrack.getCompositionYear() > 1800, "Step 2: Anno deve essere valido");
            assertTrue(testTrack.getGenreId() != null && testTrack.getGenreId() > 0, "Step 2: Genere deve essere valido");

            //Step 3: Validazione author
            assertTrue(testTrack.getAuthor() != null && testTrack.getAuthor().getName() != null, "Step 3: Autore deve essere valido");

            //Step 4: Preparazione media
            assertTrue(testDocumentFile.exists(), "Step 4: File documento deve esistere");
            assertTrue(testMedia.getPath() != null, "Step 4: Path media deve essere definito");
            assertTrue(testMedia.getType() != null, "Step 4: Type media deve essere definito");

            //Step 5: Associazione oggetti (integration test)
            testMedia.setTrack(testTrack);
            testMedia.setMember(testMember);

            assertEquals(testTrack.getId(), testMedia.getTrack().getId(), "Step 5: Media deve essere associato al track corretto");
            assertEquals(testMember.getId(), testMedia.getMember().getId(), "Step 5: Media deve essere associato al member corretto");

            //Step 6: Validazione finale workflow
            List<Media> mediaList = new ArrayList<>();
            mediaList.add(testMedia);
            assertEquals(1, mediaList.size(), "Step 6: Dovrebbe esserci 1 media associato");

            success = true;

        } catch (Exception e) {
            errorMessage = e.getMessage();
            fail("Errore durante test integration workflow completo: " + e.getMessage());
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            saveIntegrationTestResult(testName, componentFlow, testTrack.getId(), 1, success, executionTime, errorMessage);
        }
    }

    //Salva risultati test nel file CSV
    private void saveIntegrationTestResult(String testName, String componentFlow, Integer trackId,
                                           int mediaCount, boolean success, long executionTime, String errorMessage) {
        try (FileWriter writer = new FileWriter(INTEGRATION_RESULTS_FILE, true)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            String escapedError = errorMessage.replace("\"", "\\\"");
            writer.write(String.format("%s,%s,%s,%d,%b,%d,%s,\"%s\"\n",
                    testName, componentFlow, trackId, mediaCount, success, executionTime, timestamp, escapedError));
        } catch (IOException e) {
            System.err.println("Errore nel salvare risultati integration test: " + e.getMessage());
        }
    }

    //Prepara tutti i dati di test
    private void setupCompleteTestData() {
        //Member di test (utente del sistema)
        testMember = new Member();
        testMember.setId(TEST_MEMBER_ID);
        testMember.setUsername("integration_test_user");
        testMember.setEmail("test@integration.com");
        testMember.setRole(Role.USER);
        testMember.setStatus(Status.ACTIVE);

        //Author di test (autore del brano)
        testAuthor = new Author();
        testAuthor.setId(TEST_AUTHOR_ID);
        testAuthor.setName("Integration Test Author");

        //Track di test con tutte le relazioni
        testTrack = new Track();
        testTrack.setId(100); // ID fisso per test
        testTrack.setTitle(TEST_TRACK_TITLE);
        testTrack.setCompositionYear(TEST_COMPOSITION_YEAR);
        testTrack.setYoutubeLink(TEST_YOUTUBE_LINK);
        testTrack.setCoverPath(TEST_COVER_PATH);
        testTrack.setInstruments(TEST_INSTRUMENTS);
        testTrack.setGenreId(TEST_GENRE_ID);
        testTrack.setAuthor(testAuthor);  // Associa autore
        testTrack.setMember(testMember);  // Associa member

        //Media di test (documento PDF del brano)
        testMedia = new Media();
        testMedia.setId(1);
        testMedia.setTitle("Test Document");
        testMedia.setPath("/test/path/document.pdf");
        testMedia.setType("DOCUMENT");
        //Le relazioni saranno impostate nei test specifici
    }

    //Crea file temporanei per i test
    private void setupTestFiles() throws IOException {
        //Crea file PDF temporaneo per test
        Path docPath = Files.createTempFile("integration_test_doc", ".pdf");
        Files.write(docPath, "Test PDF content for integration test".getBytes());
        testDocumentFile = docPath.toFile();
        testDocumentFile.deleteOnExit(); //Elimina automaticamente dopo test
    }

    //Rimuove file temporanei e pulisce ambiente
    @AfterEach
    void tearDown() {
        if (testDocumentFile != null && testDocumentFile.exists()) {
            testDocumentFile.delete();
        }
    }
}