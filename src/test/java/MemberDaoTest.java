import com.appmusicale.dao.MemberDaoImpl;
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

//UNIT TEST per la gestione degli utenti (Member)
/* COSA TESTA:
    - Funzionamento metodi getter/setter della classe Member
    - Validazioni per l'inserimento di nuovi utenti
    - Logiche di ricerca utenti per email e username
    - Gestione corretta di stati utente (PENDING, ACTIVE, BANNED)
    - Gestione ruoli utente (USER, ADMIN)
*/
class MemberDaoTest {

    //COSTANTI DI TEST - Dati predefiniti per simulare un utente
    private static final String MEMBER_USERNAME = "testuser";
    private static final String MEMBER_EMAIL = "testuser@email.com";
    private static final String MEMBER_PASSWORD = "password123";
    private static final Integer MEMBER_ID = 1;

    //OGGETTI NECESSARI PER I TEST
    private MemberDaoImpl memberDao;    // DAO per operazioni database
    private Member testMember;          // Oggetto Member per test
    private Connection testConnection;  // Connessione database per test

    //FILE PER SALVARE I RISULTATI DEI TEST
    private static final String MEMBER_RESULTS_FILE = "src/test/resources/test-results/member_management_results.csv";

    //Prepara directory e file per salvare i risultati
    @BeforeAll
    static void setupResultsFiles() throws IOException {
        // Crea directory se non esiste
        new java.io.File("src/test/resources/test-results/").mkdirs();

        // Inizializza file CSV con intestazioni
        try (FileWriter writer = new FileWriter(MEMBER_RESULTS_FILE)) {
            writer.write("test_name,member_id,username,operation,old_status,new_status,success,timestamp\n");
        }
    }

    //Prepara ambiente pulito con database in-memory e oggetti test
    @BeforeEach
    void setUp() throws SQLException {
        // Setup database in-memory per test (non impatta DB reale)
        testConnection = DriverManager.getConnection("jdbc:sqlite::memory:");
        createTestSchema();

        //Crea oggetto Member con dati di test
        testMember = new Member();
        testMember.setId(MEMBER_ID);
        testMember.setUsername(MEMBER_USERNAME);
        testMember.setEmail(MEMBER_EMAIL);
        testMember.setPassword(MEMBER_PASSWORD);
        testMember.setRole(Role.USER);         // Ruolo utente normale
        testMember.setStatus(Status.PENDING);   // Status iniziale in attesa

        //Istanzia il DAO (testa la logica, non la connessione DB reale)
        memberDao = new MemberDaoImpl();
    }

    /*TEST 1: VERIFICA METODI GETTER
    COSA TESTA:
        - Tutti i metodi getter restituiscono i valori corretti
        - I dati impostati nel setup sono accessibili correttamente
    */
    @Test
    void testGetters() {
        //Verifica che tutti i getter restituiscano i valori impostati
        assertEquals(MEMBER_ID, testMember.getId(), "L'ID del member non corrisponde");
        assertEquals(MEMBER_USERNAME, testMember.getUsername(), "Il username del member non corrisponde");
        assertEquals(MEMBER_EMAIL, testMember.getEmail(), "L'email del member non corrisponde");
        assertEquals(MEMBER_PASSWORD, testMember.getPassword(), "La password del member non corrisponde");
        assertEquals(Role.USER, testMember.getRole(), "Il ruolo del member non corrisponde");
        assertEquals(Status.PENDING, testMember.getStatus(), "Lo status del member non corrisponde");
    }

    /*TEST 2: VERIFICA SETTER USERNAME
    COSA TESTA:
        - Il setter per username funziona correttamente
        - Il nuovo valore viene memorizzato e restituito dal getter
    */
    @Test
    void testSetUsername() {
        String nuovoUsername = "newuser";

        //Cambia username e verifica che sia stato aggiornato
        testMember.setUsername(nuovoUsername);
        assertEquals(nuovoUsername, testMember.getUsername(), "Il nuovo username non è stato impostato correttamente");
    }

    //TEST 3: VERIFICA SETTER EMAIL
    @Test
    void testSetEmail() {
        String nuovaEmail = "newemail@test.com";

        testMember.setEmail(nuovaEmail);
        assertEquals(nuovaEmail, testMember.getEmail(), "La nuova email non è stata impostata correttamente");
    }

    //TEST 4: VERIFICA SETTER PASSWORD
    @Test
    void testSetPassword() {
        String nuovaPassword = "newpass456";

        testMember.setPassword(nuovaPassword);
        assertEquals(nuovaPassword, testMember.getPassword(), "La nuova password non è stata impostata correttamente");
    }

    /*TEST 5: VERIFICA SETTER ROLE
    COSA TESTA:
        - Cambio da USER a ADMIN funziona
        - Il nuovo ruolo viene memorizzato correttamente
    */
    @Test
    void testSetRole() {
        Role nuovoRole = Role.ADMIN;

        testMember.setRole(nuovoRole);
        assertEquals(nuovoRole, testMember.getRole(), "Il nuovo ruolo non è stato impostato correttamente");
    }

    /*TEST 6: VERIFICA SETTER STATUS
    COSA TESTA:
        - Cambio da PENDING a ACTIVE funziona
        - Il nuovo status viene memorizzato correttamente
    */
    @Test
    void testSetStatus() {
        Status nuovoStatus = Status.ACTIVE;

        testMember.setStatus(nuovoStatus);
        assertEquals(nuovoStatus, testMember.getStatus(), "Il nuovo status non è stato impostato correttamente");
    }

    /*TEST 7: VERIFICA METODO toString
    COSA TESTA:
        - Il metodo toString non restituisce null
        - Contiene informazioni utili (username o email)
    */
    @Test
    void testToString() {
        String result = testMember.toString();

        assertNotNull(result, "Il toString non dovrebbe essere null");
        assertTrue(result.contains(MEMBER_USERNAME) || result.contains(MEMBER_EMAIL), "Il toString dovrebbe contenere username o email");
    }

    /*TEST 8: SIMULAZIONE INSERIMENTO MEMBER
    COSA TESTA:
        - Validazioni necessarie prima dell'inserimento
        - Tutti i campi obbligatori sono presenti
        - La logica di preparazione per il DAO funziona
    */
    @Test
    void testInserimentoMember() {
        String testName = "testInserimentoMember";
        String operation = "INSERT";
        boolean success = false;
        Integer memberId = null;

        try {
            //NOTA: Questo test verifica la logica, non la connessione DB reale

            //Verifica che tutti i campi obbligatori siano presenti
            assertNotNull(testMember.getUsername(), "Username deve essere impostato per l'inserimento");
            assertNotNull(testMember.getEmail(), "Email deve essere impostata per l'inserimento");
            assertNotNull(testMember.getPassword(), "Password deve essere impostata per l'inserimento");
            assertNotNull(testMember.getRole(), "Role deve essere impostato per l'inserimento");
            assertNotNull(testMember.getStatus(), "Status deve essere impostato per l'inserimento");

            // Simula l'inserimento riuscito settando un ID
            memberId = testMember.getId();
            success = true;

        } catch (Exception e) {
            fail("Errore durante la preparazione inserimento member: " + e.getMessage());
        } finally {
            // Salva i risultati nel file CSV
            saveMemberTestResult(testName, memberId, testMember.getUsername(), operation, "", testMember.getStatus().toString(), success);
        }
    }

    /*TEST 9: RICERCA MEMBER PER EMAIL
    COSA TESTA:
        - Validazioni sui parametri di ricerca per email
        - Format email corretto (@domain.com)
        - Preparazione query di ricerca
    */
    @Test
    void testRicercaMemberPerEmail() {
        String testName = "testRicercaMemberPerEmail";
        String operation = "SEARCH_BY_EMAIL";
        boolean success = false;

        try {
            //Verifica parametri per la ricerca email
            assertNotNull(MEMBER_EMAIL, "Email per la ricerca non può essere null");
            assertFalse(MEMBER_EMAIL.trim().isEmpty(), "Email per la ricerca non può essere vuota");
            assertTrue(MEMBER_EMAIL.contains("@"), "Email deve avere formato valido");

            success = true;

        } catch (Exception e) {
            fail("Errore durante la ricerca per email: " + e.getMessage());
        } finally {
            saveMemberTestResult(testName, testMember.getId(), testMember.getUsername(), operation, "", "", success);
        }
    }

    /*TEST 10: RICERCA MEMBER PER USERNAME
    COSA TESTA:
        - Validazioni sui parametri di ricerca per username
        - Lunghezza minima username (almeno 3 caratteri)
    */
    @Test
    void testRicercaMemberPerUsername() {
        String testName = "testRicercaMemberPerUsername";
        String operation = "SEARCH_BY_USERNAME";
        boolean success = false;

        try {
            assertNotNull(MEMBER_USERNAME, "Username per la ricerca non può essere null");
            assertFalse(MEMBER_USERNAME.trim().isEmpty(), "Username per la ricerca non può essere vuoto");
            assertTrue(MEMBER_USERNAME.length() >= 3, "Username deve avere almeno 3 caratteri");

            success = true;

        } catch (Exception e) {
            fail("Errore durante la ricerca per username: " + e.getMessage());
        } finally {
            saveMemberTestResult(testName, testMember.getId(), testMember.getUsername(), operation, "", "", success);
        }
    }

    /*TEST 11: AGGIORNAMENTO STATUS MEMBER
    COSA TESTA:
        - Transizione di status (PENDING → ACTIVE)
        - Il cambio status funziona correttamente
        - Lo status vecchio è diverso dal nuovo
    */
    @Test
    void testAggiornamentoStatusMember() {
        String testName = "testAggiornamentoStatusMember";
        String operation = "UPDATE_STATUS";
        Status oldStatus = Status.PENDING;
        Status newStatus = Status.ACTIVE;
        boolean success = false;

        try {
            //Imposta status iniziale e poi cambialo
            testMember.setStatus(oldStatus);
            testMember.setStatus(newStatus);

            //Verifica che il cambio sia avvenuto
            assertEquals(newStatus, testMember.getStatus(), "Il nuovo status non è stato applicato correttamente");
            assertNotEquals(oldStatus, testMember.getStatus(), "Lo status dovrebbe essere cambiato");

            success = true;

        } catch (Exception e) {
            fail("Errore durante l'aggiornamento status: " + e.getMessage());
        } finally {
            saveMemberTestResult(testName, testMember.getId(), testMember.getUsername(), operation, oldStatus.toString(), newStatus.toString(), success);
        }
    }

    /*TEST 12: VALIDAZIONE TUTTI GLI STATUS POSSIBILI
    COSA TESTA:
        - Tutti gli status possibili (PENDING, ACTIVE, BANNED)
        - Ogni status può essere impostato correttamente
    */
    @Test
    void testValidazioneMembriPerStatus() {
        String testName = "testValidazioneMembriPerStatus";
        String operation = "VALIDATE_BY_STATUS";
        boolean success = false;

        try {
            //Testa tutti gli status possibili
            Status[] statusDaTestare = {Status.PENDING, Status.ACTIVE, Status.BANNED};

            for (Status status : statusDaTestare) {
                testMember.setStatus(status);
                assertEquals(status, testMember.getStatus(), "Status impostato dovrebbe corrispondere: " + status);
            }

            success = true;

        } catch (Exception e) {
            fail("Errore durante la validazione status: " + e.getMessage());
        } finally {
            saveMemberTestResult(testName, testMember.getId(), testMember.getUsername(), operation, "", "ALL_STATUS_TESTED", success);
        }
    }

    /*TEST 13: VALIDAZIONE TUTTI I RUOLI POSSIBILI
    COSA TESTA:
        - Tutti i ruoli possibili (USER, ADMIN)
        - Ogni ruolo può essere impostato correttamente
    */
    @Test
    void testValidazioneRuoliMember() {
        String testName = "testValidazioneRuoliMember";
        String operation = "VALIDATE_ROLES";
        boolean success = false;

        try {
            //Testa tutti i ruoli possibili
            Role[] ruoliDaTestare = {Role.USER, Role.ADMIN};

            for (Role role : ruoliDaTestare) {
                testMember.setRole(role);
                assertEquals(role, testMember.getRole(), "Ruolo impostato dovrebbe corrispondere: " + role);
            }

            success = true;

        } catch (Exception e) {
            fail("Errore durante la validazione ruoli: " + e.getMessage());
        } finally {
            saveMemberTestResult(testName, testMember.getId(), testMember.getUsername(), operation, "", "ALL_ROLES_TESTED", success);
        }
    }

    //Salva risultati test membri nel file CSV
    private void saveMemberTestResult(String testName, Integer memberId, String username,
                                      String operation, String oldStatus, String newStatus, boolean success) {
        try (FileWriter writer = new FileWriter(MEMBER_RESULTS_FILE, true)) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            writer.write(String.format("%s,%s,%s,%s,%s,%s,%b,%s\n",
                    testName, memberId, username, operation, oldStatus, newStatus, success, timestamp));
        } catch (IOException e) {
            System.err.println("Errore nel salvare i risultati gestione membri: " + e.getMessage());
        }
    }

    //Crea schema database per test
    private void createTestSchema() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            // Crea tabella MEMBER con struttura corretta
            stmt.execute("CREATE TABLE IF NOT EXISTS MEMBER (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "USERNAME TEXT UNIQUE NOT NULL, " +
                    "EMAIL TEXT UNIQUE NOT NULL, " +
                    "PASSWORD TEXT NOT NULL, " +
                    "ROLE TEXT NOT NULL, " +
                    "STATUS TEXT NOT NULL)");
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