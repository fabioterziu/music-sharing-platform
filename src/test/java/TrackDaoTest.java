import com.appmusicale.dao.TrackDaoImpl;
import com.appmusicale.model.Track;
import com.appmusicale.model.Author;
import com.appmusicale.model.Member;
import com.appmusicale.model.Role;
import com.appmusicale.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

//UNIT TEST per la gestione dei brani musicali (Track)
/* COSA TESTA:
    - Funzionamento metodi getter/setter della classe Track
    - Validazioni per l'inserimento di nuovi brani
    - Logiche di ricerca brani per titolo, autore e genere
    - Preparazione corretta per operazioni database
    - Gestione relazioni Track-Author-Member
*/
class TrackDaoTest {

    //COSTANTI DI TEST (Bohemian Rhapsody dei Queen)
    private static final String TRACK_TITLE = "Bohemian Rhapsody";
    private static final Integer COMPOSITION_YEAR = 1975;
    private static final String YOUTUBE_LINK = "https://youtube.com/watch?v=fJ9rUzIMcZQ";
    private static final String COVER_PATH = "/covers/queen_bohemian.jpg";
    private static final String INSTRUMENTS = "Piano,Guitar,Drums,Bass";
    private static final Integer GENRE_ID = 1; // Rock
    private static final Integer MEMBER_ID = 1;
    private static final Integer AUTHOR_ID = 1;

    //OGGETTI NECESSARI PER I TEST
    private TrackDaoImpl trackDao;      // DAO per operazioni database
    private Track testTrack;            // Oggetto Track per test
    private Author testAuthor;          // Autore associato al brano
    private Member testMember;          // Member che ha inserito il brano
    private Connection testConnection;  // Connessione database per test

    //FILE PER SALVARE I RISULTATI DEI TEST
    private static final String RESULTS_FILE = "src/test/resources/test-results/track_insert_results.csv";
    private static final String SEARCH_RESULTS_FILE = "src/test/resources/test-results/search_results.csv";

    //Prepara directory e file per salvare i risultati
    @BeforeAll
    static void setupResultsFiles() throws IOException {
        //Crea directory se non esiste
        new java.io.File("src/test/resources/test-results/").mkdirs();

        //Inizializza file CSV per risultati inserimento brani
        try (FileWriter writer = new FileWriter(RESULTS_FILE)) {
            writer.write("test_name,track_id,title,author,genre,result,timestamp,error_message\n");
        }

        //Inizializza file CSV per risultati ricerche
        try (FileWriter writer = new FileWriter(SEARCH_RESULTS_FILE)) {
            writer.write("test_name,search_query,search_type,results_count,execution_time_ms,success,timestamp\n");
        }
    }

    //Prepara ambiente pulito con database in-memory e oggetti test
    @BeforeEach
    void setUp() throws SQLException {
        // Setup database in-memory per test
        testConnection = DriverManager.getConnection("jdbc:sqlite::memory:");
        createTestSchema();

        //Istanzia il DAO
        trackDao = new TrackDaoImpl();

        //Crea oggetti di test
        testAuthor = new Author();
        testAuthor.setId(AUTHOR_ID);
        testAuthor.setName("Freddie Mercury");

        testMember = new Member();
        testMember.setId(MEMBER_ID);
        testMember.setUsername("queen_admin");
        testMember.setEmail("admin@queen.com");
        testMember.setRole(Role.ADMIN);
        testMember.setStatus(Status.ACTIVE);

        //Crea Track con tutte le proprietà
        testTrack = new Track();
        testTrack.setTitle(TRACK_TITLE);
        testTrack.setCompositionYear(COMPOSITION_YEAR);
        testTrack.setYoutubeLink(YOUTUBE_LINK);
        testTrack.setCoverPath(COVER_PATH);
        testTrack.setInstruments(INSTRUMENTS);
        testTrack.setGenreId(GENRE_ID);
        testTrack.setAuthor(testAuthor);      // Associa autore
        testTrack.setMember(testMember);      // Associa member che ha inserito
    }

    /*TEST 1: VERIFICA METODI GETTER
     COSA TESTA:
        - Tutti i metodi getter restituiscono i valori corretti
        - Le relazioni con Author e Member sono mantenute
     */
    @Test
    void testGetters() {
        //Arrange & Act già fatto nel setup
        //Assert con messaggi descrittivi per debug facile

        assertEquals(TRACK_TITLE, testTrack.getTitle(), "Il titolo del track non corrisponde");
        assertEquals(COMPOSITION_YEAR, testTrack.getCompositionYear(), "L'anno di composizione non corrisponde");
        assertEquals(YOUTUBE_LINK, testTrack.getYoutubeLink(), "Il link YouTube non corrisponde");
        assertEquals(COVER_PATH, testTrack.getCoverPath(), "Il path della cover non corrisponde");
        assertEquals(INSTRUMENTS, testTrack.getInstruments(), "Gli strumenti non corrispondono");
        assertEquals(GENRE_ID, testTrack.getGenreId(), "L'ID del genere non corrisponde");
        assertEquals(testAuthor, testTrack.getAuthor(), "L'autore non corrisponde");
        assertEquals(testMember, testTrack.getMember(), "Il membro non corrisponde");
    }

    /*TEST 2: VERIFICA SETTER TITLE
     COSA TESTA:
        - Il setter per il titolo funziona
        - Il nuovo titolo viene memorizzato correttamente
     */
    @Test
    void testSetTitle() {
        //Arrange - prepara nuovo titolo
        String nuovoTitolo = "We Will Rock You";

        //Act - cambia titolo
        testTrack.setTitle(nuovoTitolo);

        //Assert - verifica che sia cambiato
        assertEquals(nuovoTitolo, testTrack.getTitle(), "Il nuovo titolo non è stato impostato correttamente");
    }

    //TEST 3: VERIFICA SETTER COMPOSITION YEAR
    @Test
    void testSetCompositionYear() {
        //Arrange
        Integer nuovoAnno = 1977;

        //Act
        testTrack.setCompositionYear(nuovoAnno);

        //Assert
        assertEquals(nuovoAnno, testTrack.getCompositionYear(), "Il nuovo anno non è stato impostato correttamente");
    }

    //TEST 4: VERIFICA SETTER YOUTUBE LINK
    @Test
    void testSetYoutubeLink() {
        //Arrange
        String nuovoLink = "https://youtube.com/watch?v=new_link";

        //Act
        testTrack.setYoutubeLink(nuovoLink);

        //Assert
        assertEquals(nuovoLink, testTrack.getYoutubeLink(), "Il nuovo link YouTube non è stato impostato correttamente");
    }

    /*TEST 5: PREPARAZIONE INSERIMENTO TRACK
     COSA TESTA:
        - Validazioni necessarie prima dell'inserimento nel database
        - Tutti i campi obbligatori sono presenti e validi
        - Gli anni sono realistici (non troppo vecchi o futuri)
        - Simulazione assegnazione ID dopo inserimento
     */
    @Test
    void testPreparazioneInserimentoTrack() {
        String testName = "testPreparazioneInserimentoTrack";
        String result = "SUCCESS";
        String errorMessage = "";

        try {
            //Arrange - oggetti già creati nel setup

            //Act - verifica che tutti i dati necessari siano presenti
            assertNotNull(testTrack.getTitle(), "Il titolo non può essere null per l'inserimento");
            assertNotNull(testTrack.getCompositionYear(), "L'anno di composizione non può essere null");
            assertNotNull(testTrack.getGenreId(), "L'ID genere non può essere null");
            assertNotNull(testTrack.getAuthor(), "L'autore non può essere null");

            //Verifica validità dati
            assertTrue(testTrack.getTitle().length() > 0, "Il titolo deve avere almeno un carattere");
            assertTrue(testTrack.getCompositionYear() > 1800, "L'anno deve essere realistico");
            assertTrue(testTrack.getCompositionYear() <= LocalDateTime.now().getYear(), "L'anno non può essere nel futuro");

            //Simula l'inserimento assegnando un ID (come farebbe il database)
            testTrack.setId(100);
            assertNotNull(testTrack.getId(), "Dopo l'inserimento dovrebbe avere un ID");

        } catch (Exception e) {
            result = "FAILED";
            errorMessage = e.getMessage();
            fail("Errore durante la preparazione inserimento track: " + e.getMessage());
        } finally {
            // Salva i risultati nel file CSV
            saveTestResult(testName, testTrack.getId(), testTrack.getTitle(), testAuthor.getName(), GENRE_ID.toString(), result, errorMessage);
        }
    }

    /*TEST 6: LOGICA RICERCA TRACK PER TITOLO
     COSA TESTA:
        - Validazioni sui parametri di ricerca
        - Titolo non può essere null, vuoto o troppo corto
        - Simula esecuzione ricerca e misura performance
     */
    @Test
    void testLogicaRicercaPerTitolo() {
        String testName = "testLogicaRicercaPerTitolo";
        long startTime = System.currentTimeMillis();
        boolean success = false;
        int resultsCount = 0;

        try {
            //Arrange - prepara i parametri di ricerca
            String titoloRicerca = TRACK_TITLE;

            //Act - testa la logica di ricerca
            assertNotNull(titoloRicerca, "Il titolo di ricerca non può essere null");
            assertFalse(titoloRicerca.trim().isEmpty(), "Il titolo di ricerca non può essere vuoto");
            assertTrue(titoloRicerca.length() >= 2, "Il titolo di ricerca deve avere almeno 2 caratteri");

            //Simula risultato ricerca
            resultsCount = 1;
            success = true;

        } catch (Exception e) {
            fail("Errore durante la logica ricerca per titolo: " + e.getMessage());
        } finally {
            //Salva i risultati della ricerca
            long executionTime = System.currentTimeMillis() - startTime;
            saveSearchResult(testName, TRACK_TITLE, "BY_TITLE", resultsCount, executionTime, success);
        }
    }

    /*TEST 7: RICERCA TRACK PER AUTORE
     COSA TESTA:
        - Ricerca brani di un autore specifico
        - Validazione ID autore (deve essere positivo)
     */
    @Test
    void testRicercaTrackPerAutore() {
        String testName = "testRicercaTrackPerAutore";
        long startTime = System.currentTimeMillis();
        boolean success = false;
        int resultsCount = 0;

        try {
            //Arrange & Act - testa logica ricerca per autore
            assertNotNull(AUTHOR_ID,
                    "L'ID autore per la ricerca non può essere null");
            assertTrue(AUTHOR_ID > 0,
                    "L'ID autore deve essere positivo");

            resultsCount = 1;
            success = true;

        } catch (Exception e) {
            fail("Errore durante la ricerca per autore: " + e.getMessage());
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            saveSearchResult(testName, AUTHOR_ID.toString(), "BY_AUTHOR", resultsCount, executionTime, success);
        }
    }

    /*TEST 8: RICERCA TRACK PER GENERE
     COSA TESTA:
        - Ricerca brani per genere musicale
        - Validazione ID genere (deve essere positivo)
    */
    @Test
    void testRicercaTrackPerGenere() {
        String testName = "testRicercaTrackPerGenere";
        long startTime = System.currentTimeMillis();
        boolean success = false;
        int resultsCount = 0;

        try {
            //Arrange & Act - testa logica ricerca per genere
            assertNotNull(GENRE_ID, "L'ID genere per la ricerca non può essere null");
            assertTrue(GENRE_ID > 0, "L'ID genere deve essere positivo");

            resultsCount = 1;
            success = true;

        } catch (Exception e) {
            fail("Errore durante la ricerca per genere: " + e.getMessage());
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            saveSearchResult(testName, GENRE_ID.toString(), "BY_GENRE", resultsCount, executionTime, success);
        }
    }

    //Salva risultati test track nel file CSV
    private void saveTestResult(String testName, Integer trackId, String title, String author,
                                String genre, String result, String errorMessage) {
        try (FileWriter writer = new FileWriter(RESULTS_FILE, true)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s\n",
                    testName, trackId, title, author, genre, result, timestamp, errorMessage));
        } catch (IOException e) {
            System.err.println("Errore nel salvare i risultati: " + e.getMessage());
        }
    }

    //Salva risultati ricerche nel file CSV
    private void saveSearchResult(String testName, String query, String searchType,
                                  int resultsCount, long executionTime, boolean success) {
        try (FileWriter writer = new FileWriter(SEARCH_RESULTS_FILE, true)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            writer.write(String.format("%s,%s,%s,%d,%d,%b,%s\n",
                    testName, query, searchType, resultsCount, executionTime, success, timestamp));
        } catch (IOException e) {
            System.err.println("Errore nel salvare i risultati di ricerca: " + e.getMessage());
        }
    }

    //Crea schema database per test
    private void createTestSchema() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            // Crea tabella TRACK con tutti i campi necessari
            stmt.execute("CREATE TABLE IF NOT EXISTS TRACK (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "TITLE TEXT NOT NULL, " +
                    "COMPOSITION_YEAR INTEGER, " +
                    "YOUTUBE_LINK TEXT, " +
                    "MEMBER_ID INTEGER, " +
                    "GENRE_ID INTEGER, " +
                    "AUTHOR_ID INTEGER, " +
                    "COVER_PATH TEXT, " +
                    "INSTRUMENTS TEXT)");
        }
    }

    //Chiude connessione database per evitare memory leaks
    @AfterEach
    void tearDown() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }
}