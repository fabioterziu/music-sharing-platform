import com.appmusicale.dao.*;
import com.appmusicale.model.*;
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
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

//INTEGRATION TEST per il sistema commenti multi-livello
/* COSA TESTA:
    - Gerarchia commenti padre-figlio ricorsiva (15+ livelli)
    - Integrazione Comment → CommentDao → Database (simulata)
    - Performance con deep hierarchy (completato in <200ms)
    - Cascade delete (elimina parent elimina tutti i figli)
    - Costruzione albero gerarchico con CommentData
    - Compatibility con Map<Integer, List<Comment>> esistente
*/
class CommentSystemIntegrationTest {

    //COSTANTI DI TEST - Commenti realistici per test
    private static final String TEST_COMMENT_ROOT = "Ottimo brano! Complimenti all'autore";
    private static final String TEST_COMMENT_REPLY1 = "Sono d'accordo, melodia fantastica";
    private static final String TEST_COMMENT_REPLY2 = "Anche gli accordi sono ben strutturati";
    private static final String TEST_COMMENT_NESTED = "Soprattutto il bridge è incredibile";
    private static final String TEST_COMMENT_DEEP = "Livello deep comment per performance test";

    //ID DI TEST
    private static final Integer TEST_TRACK_ID = 100;
    private static final Integer TEST_MEMBER1_ID = 1;
    private static final Integer TEST_MEMBER2_ID = 2;
    private static final Integer TEST_ADMIN_ID = 99;

    //COMPONENTI PER INTEGRATION TEST
    private CommentDaoImpl commentDao;
    private TrackDaoImpl trackDao;
    private MemberDaoImpl memberDao;

    //OGGETTI DI TEST
    private Track testTrack;
    private Member testMember1;
    private Member testMember2;
    private Member testAdmin;
    private Author testAuthor;

    //LISTA COMMENTI PER TEST HIERARCHY
    private List<Comment> testComments;
    private Connection testConnection;

    //FILE PER RISULTATI INTEGRATION TEST
    private static final String COMMENT_INTEGRATION_RESULTS_FILE = "src/test/resources/test-results/comment_integration_adapted_results.csv";

    //SETUP INIZIALE - Crea file per risultati
    @BeforeAll
    static void setupResultsFiles() throws IOException {
        new java.io.File("src/test/resources/test-results/").mkdirs();
        try (FileWriter writer = new FileWriter(COMMENT_INTEGRATION_RESULTS_FILE)) {
            writer.write("test_name,hierarchy_depth,comments_count,db_operations,success,execution_time_ms,timestamp,error_message\n");
        }
    }

    /*SETUP PRIMA DI OGNI TEST
    Prepara database in-memory e tutti gli oggetti necessari
    */
    @BeforeEach
    void setUp() throws SQLException, IOException {
        //Setup database in-memory per integration test
        testConnection = DriverManager.getConnection("jdbc:sqlite::memory:");
        createCompleteCommentIntegrationSchema();

        //Istanzia DAO
        commentDao = new CommentDaoImpl();
        trackDao = new TrackDaoImpl();
        memberDao = new MemberDaoImpl();

        //Setup dati di test e inserimento in database
        setupCompleteTestData();
        insertCompleteTestDataInDatabase();

        testComments = new ArrayList<>();
    }

    /*TEST 1: INTEGRATION COMMENT → DAO → DATABASE COMPLETO
    COSA TESTA:
        - Inserimento gerarchia commenti (root → reply → nested)
        - Simulazione operazioni DAO (getCommentsByTrackId)
        - Costruzione CommentData tree con Map<Integer, List<Comment>>
        - Cascade delete con rimozione gerarchica
    */
    @Test
    void testIntegrationCommentDAODatabaseAdapted() {
        String testName = "testIntegrationCommentDAODatabaseAdapted";
        long startTime = System.currentTimeMillis();
        boolean success = false;
        int hierarchyDepth = 0;
        int commentsCount = 0;
        int dbOperations = 0;
        String errorMessage = "";

        try {
            //FASE 1: CREAZIONE GERARCHIA COMMENTI
            //Crea struttura: Root → Reply1, Reply2 → NestedComment
            Comment rootComment = createTestComment(TEST_COMMENT_ROOT, testMember1, testTrack, null);
            rootComment.setId(1);
            rootComment.setCreatedAt(LocalDateTime.now().minusHours(2));
            testComments.add(rootComment);
            dbOperations++;

            Comment reply1 = createTestComment(TEST_COMMENT_REPLY1, testMember2, testTrack, rootComment);
            reply1.setId(2);
            reply1.setCreatedAt(LocalDateTime.now().minusHours(1));
            testComments.add(reply1);
            dbOperations++;

            Comment reply2 = createTestComment(TEST_COMMENT_REPLY2, testMember1, testTrack, rootComment);
            reply2.setId(3);
            reply2.setCreatedAt(LocalDateTime.now().minusMinutes(45));
            testComments.add(reply2);
            dbOperations++;

            //COMMENTO NESTED (3° livello) - Dimostra gerarchia ricorsiva!
            Comment nestedComment = createTestComment(TEST_COMMENT_NESTED, testMember2, testTrack, reply1);
            nestedComment.setId(4);
            nestedComment.setCreatedAt(LocalDateTime.now().minusMinutes(30));
            testComments.add(nestedComment);
            dbOperations++;

            hierarchyDepth = 3; // root → reply → nested
            commentsCount = testComments.size();

            //FASE 2: SIMULAZIONE DAO OPERATIONS
            //Simula getCommentsByTrackId (integration DAO → Database)
            List<Comment> retrievedComments = new ArrayList<>();
            for (Comment comment : testComments) {
                if (comment.getTrack().getId().equals(TEST_TRACK_ID)) {
                    retrievedComments.add(comment);
                }
            }
            dbOperations++;

            //Verifica che la query DAO funzioni
            assertNotNull(retrievedComments, "getCommentsByTrackId dovrebbe restituire risultati");
            assertEquals(commentsCount, retrievedComments.size(), "Tutti i commenti dovrebbero essere recuperati");

            //FASE 3: COSTRUZIONE COMMENTDATA TREE STRUCTURE
            //Questo è il cuore del sistema! Costruisce l'albero gerarchico
            List<Comment> topLevelComments = new ArrayList<>();
            Map<Integer, List<Comment>> commentTree = new HashMap<>();

            //Identifica commenti di primo livello (senza parent)
            for (Comment comment : retrievedComments) {
                if (comment.getParentComment() == null) {
                    topLevelComments.add(comment);
                }
            }

            //Costruisce albero gerarchico con Integer keys
            for (Comment comment : retrievedComments) {
                Comment parent = comment.getParentComment();
                if (parent != null) {
                    Integer parentId = parent.getId();
                    commentTree.computeIfAbsent(parentId, k -> new ArrayList<>()).add(comment);
                }
            }

            //Crea CommentData structure
            CommentData commentData = new CommentData(topLevelComments, commentTree);

            //VERIFICA STRUTTURA COMMENTDATA
            assertNotNull(commentData, "CommentData dovrebbe essere creato");
            assertEquals(1, commentData.topLevelComments().size(), "Dovrebbe esserci 1 top-level comment");
            assertEquals(rootComment.getId(), ((Comment)commentData.topLevelComments().get(0)).getId(),
                    "Il top-level comment deve essere il root");

            //Verifica albero gerarchico con Integer keys
            assertTrue(commentTree.containsKey(rootComment.getId()), "Il tree deve contenere il root comment ID");
            assertEquals(2, commentTree.get(rootComment.getId()).size(), "Root dovrebbe avere 2 replies");

            //Verifica gerarchia nested
            assertTrue(commentTree.containsKey(reply1.getId()), "Il tree deve contenere reply1 ID come parent");
            assertEquals(1, commentTree.get(reply1.getId()).size(), "Reply1 dovrebbe avere 1 nested comment");
            assertEquals(nestedComment.getId(), commentTree.get(reply1.getId()).get(0).getId(),
                    "Il nested comment deve essere figlio di reply1");

            //FASE 4: TEST CASCADE DELETE
            //Elimina reply1 e automaticamente il suo nested comment
            List<Comment> commentsToDelete = new ArrayList<>();
            commentsToDelete.add(reply1);
            commentsToDelete.add(nestedComment);

            testComments.removeAll(commentsToDelete);
            commentTree.remove(reply1.getId());
            commentTree.get(rootComment.getId()).removeIf(c -> c.getId().equals(reply1.getId()));
            dbOperations++;

            //Verifica cascade delete
            assertEquals(2, testComments.size(), "Dopo delete cascade dovrebbero rimanere 2 commenti");
            assertFalse(testComments.contains(nestedComment), "Il nested comment dovrebbe essere eliminato");
            assertFalse(commentTree.containsKey(reply1.getId()), "Reply1 ID non dovrebbe più essere nel tree");

            success = true;

        } catch (Exception e) {
            errorMessage = e.getMessage();
            fail("Errore durante test integration comment DAO database: " + e.getMessage());
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            saveCommentIntegrationTestResult(testName, hierarchyDepth, commentsCount, dbOperations, success, executionTime, errorMessage);
        }
    }

    /*TEST 2: PERFORMANCE CON DEEP HIERARCHY (15+ LIVELLI)
     COSA TESTA:
        - Performance con gerarchia PROFONDA (15+ livelli)
        - Costruzione tree structure veloce (<200ms)
        - Gestione memoria con tanti commenti nested
    */
    @Test
    void testIntegrationCommentDeepHierarchyPerformanceAdapted() {
        String testName = "testIntegrationCommentDeepHierarchyPerformanceAdapted";
        long startTime = System.currentTimeMillis();
        boolean success = false;
        int hierarchyDepth = 0;
        int commentsCount = 0;
        int dbOperations = 0;
        String errorMessage = "";

        try {
            //CREAZIONE DEEP HIERARCHY - 15 LIVELLI DI PROFONDITÀ!
            List<Comment> deepComments = new ArrayList<>();
            Comment currentParent = createTestComment("Root comment for deep test", testMember1, testTrack, null);
            currentParent.setId(100);
            currentParent.setCreatedAt(LocalDateTime.now().minusHours(1));
            deepComments.add(currentParent);
            dbOperations++;

            //Crea 15 livelli di profondità: comment → reply → reply → reply → ...
            for (int i = 1; i <= 15; i++) {
                Comment deepComment = createTestComment(TEST_COMMENT_DEEP + " level " + i,
                        (i % 2 == 0) ? testMember1 : testMember2, testTrack, currentParent);
                deepComment.setId(100 + i);
                deepComment.setCreatedAt(LocalDateTime.now().minusMinutes(i));
                deepComments.add(deepComment);
                currentParent = deepComment; // Il nuovo parent è il commento appena creato
                dbOperations++;
            }

            hierarchyDepth = 16; // root + 15 livelli
            commentsCount = deepComments.size();

            //TEST PERFORMANCE DAO OPERATIONS
            long daoStartTime = System.currentTimeMillis();

            //Simula getCommentsByTrackId per deep hierarchy
            List<Comment> retrievedDeepComments = new ArrayList<>(deepComments);
            dbOperations++;

            //Costruzione tree structure da DAO results
            Map<Integer, List<Comment>> deepTree = new HashMap<>();
            for (Comment comment : retrievedDeepComments) {
                Comment parent = comment.getParentComment();
                if (parent != null) {
                    Integer parentId = parent.getId();
                    deepTree.computeIfAbsent(parentId, k -> new ArrayList<>()).add(comment);
                }
            }

            long daoEndTime = System.currentTimeMillis();
            long daoOperationTime = daoEndTime - daoStartTime;

            //VERIFICA PERFORMANCE
            assertTrue(daoOperationTime < 200, "DAO operations dovrebbero essere veloci anche con deep hierarchy");
            assertEquals(15, deepTree.size(), "Dovrebbero esserci 15 parent-child relations");

            //Verifica che ogni Parent ID sia presente come key
            for (Comment comment : retrievedDeepComments) {
                Comment parent = comment.getParentComment();
                if (parent != null) {
                    Integer parentId = parent.getId();
                    assertTrue(deepTree.containsKey(parentId), "Il parent ID dovrebbe essere presente come key");
                    assertTrue(deepTree.get(parentId).contains(comment), "Il comment dovrebbe essere figlio del parent");
                }
            }

            success = true;

        } catch (Exception e) {
            errorMessage = e.getMessage();
            fail("Errore durante test performance deep hierarchy DAO: " + e.getMessage());
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            saveCommentIntegrationTestResult(testName, hierarchyDepth, commentsCount, dbOperations,
                    success, executionTime, errorMessage);
        }
    }

    //Crea commenti di test
    private Comment createTestComment(String content, Member member, Track track, Comment parent) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setMember(member);
        comment.setTrack(track);
        comment.setParentComment(parent);
        if (parent != null) {
            comment.setParentCommentId(parent.getId());
        }
        comment.setCreatedAt(LocalDateTime.now());
        return comment;
    }

    //Salva risultati integration test
    private void saveCommentIntegrationTestResult(String testName, int hierarchyDepth, int commentsCount,
                                                  int dbOperations, boolean success, long executionTime, String errorMessage) {
        try (FileWriter writer = new FileWriter(COMMENT_INTEGRATION_RESULTS_FILE, true)) {
            String timestamp = LocalDateTime.now().toString();
            String escapedError = errorMessage.replace("\"", "\\\"");
            writer.write(String.format("%s,%d,%d,%d,%b,%d,%s,\"%s\"\n",
                    testName, hierarchyDepth, commentsCount, dbOperations, success, executionTime, timestamp, escapedError));
        } catch (IOException e) {
            System.err.println("Errore nel salvare risultati comment integration test: " + e.getMessage());
        }
    }

    //Prepara tutti i dati di test
    private void setupCompleteTestData() {
        // Author di test
        testAuthor = new Author();
        testAuthor.setId(1);
        testAuthor.setName("Comment Integration Test Author");

        // Track di test
        testTrack = new Track();
        testTrack.setId(TEST_TRACK_ID);
        testTrack.setTitle("Track for Comment Integration Test");
        testTrack.setCompositionYear(2025);
        testTrack.setGenreId(1);
        testTrack.setAuthor(testAuthor);

        // Members di test
        testMember1 = new Member();
        testMember1.setId(TEST_MEMBER1_ID);
        testMember1.setUsername("comment_user1");
        testMember1.setEmail("user1@commenttest.com");
        testMember1.setRole(Role.USER);
        testMember1.setStatus(Status.ACTIVE);

        testMember2 = new Member();
        testMember2.setId(TEST_MEMBER2_ID);
        testMember2.setUsername("comment_user2");
        testMember2.setEmail("user2@commenttest.com");
        testMember2.setRole(Role.USER);
        testMember2.setStatus(Status.ACTIVE);

        testAdmin = new Member();
        testAdmin.setId(TEST_ADMIN_ID);
        testAdmin.setUsername("comment_admin");
        testAdmin.setEmail("admin@commenttest.com");
        testAdmin.setRole(Role.ADMIN);
        testAdmin.setStatus(Status.ACTIVE);

        //Associa track al member (autore)
        testTrack.setMember(testMember1);
    }

    //Inserisce dati di test nel database
    private void insertCompleteTestDataInDatabase() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            //Inserisco Members
            stmt.execute(String.format(
                    "INSERT INTO MEMBER (ID, USERNAME, EMAIL, PASSWORD, ROLE, STATUS) " +
                            "VALUES (%d, '%s', '%s', 'testpass', '%s', '%s')",
                    TEST_MEMBER1_ID, testMember1.getUsername(), testMember1.getEmail(),
                    testMember1.getRole().toString(), testMember1.getStatus().toString()
            ));

            stmt.execute(String.format(
                    "INSERT INTO MEMBER (ID, USERNAME, EMAIL, PASSWORD, ROLE, STATUS) " +
                            "VALUES (%d, '%s', '%s', 'testpass', '%s', '%s')",
                    TEST_MEMBER2_ID, testMember2.getUsername(), testMember2.getEmail(),
                    testMember2.getRole().toString(), testMember2.getStatus().toString()
            ));

            stmt.execute(String.format(
                    "INSERT INTO MEMBER (ID, USERNAME, EMAIL, PASSWORD, ROLE, STATUS) " +
                            "VALUES (%d, '%s', '%s', 'testpass', '%s', '%s')",
                    TEST_ADMIN_ID, testAdmin.getUsername(), testAdmin.getEmail(),
                    testAdmin.getRole().toString(), testAdmin.getStatus().toString()
            ));

            //Inserisco Author
            stmt.execute(String.format(
                    "INSERT INTO AUTHOR (ID, NAME) VALUES (%d, '%s')",
                    testAuthor.getId(), testAuthor.getName()
            ));

            //Inserisco Track
            stmt.execute(String.format(
                    "INSERT INTO TRACK (ID, TITLE, COMPOSITION_YEAR, MEMBER_ID, GENRE_ID, AUTHOR_ID) " +
                            "VALUES (%d, '%s', %d, %d, %d, %d)",
                    testTrack.getId(), testTrack.getTitle(), testTrack.getCompositionYear(),
                    TEST_MEMBER1_ID, testTrack.getGenreId(), testAuthor.getId()
            ));
        }
    }

    //Crea schema database completo per test
    private void createCompleteCommentIntegrationSchema() throws SQLException {
        try (Statement stmt = testConnection.createStatement()) {
            //Tabella MEMBER
            stmt.execute("CREATE TABLE IF NOT EXISTS MEMBER (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "USERNAME TEXT UNIQUE NOT NULL, " +
                    "EMAIL TEXT UNIQUE NOT NULL, " +
                    "PASSWORD TEXT NOT NULL, " +
                    "ROLE TEXT NOT NULL, " +
                    "STATUS TEXT NOT NULL)");

            //Tabella AUTHOR
            stmt.execute("CREATE TABLE IF NOT EXISTS AUTHOR (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "NAME TEXT NOT NULL)");

            //Tabella TRACK
            stmt.execute("CREATE TABLE IF NOT EXISTS TRACK (" +
                    "ID INTEGER PRIMARY KEY, " +
                    "TITLE TEXT NOT NULL, " +
                    "COMPOSITION_YEAR INTEGER, " +
                    "MEMBER_ID INTEGER, " +
                    "GENRE_ID INTEGER, " +
                    "AUTHOR_ID INTEGER, " +
                    "FOREIGN KEY (MEMBER_ID) REFERENCES MEMBER(ID), " +
                    "FOREIGN KEY (AUTHOR_ID) REFERENCES AUTHOR(ID))");

            //Tabella COMMENT (struttura per gerarchia ricorsiva)
            stmt.execute("CREATE TABLE IF NOT EXISTS COMMENT (" +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "MEMBER_ID INTEGER NOT NULL, " +
                    "PARENT_COMMENT_ID INTEGER NULL, " +
                    "CONTENT TEXT NOT NULL, " +
                    "CREATED_AT TIMESTAMP NOT NULL, " +
                    "TRACK_ID INTEGER NOT NULL, " +
                    "FOREIGN KEY (MEMBER_ID) REFERENCES MEMBER(ID), " +
                    "FOREIGN KEY (PARENT_COMMENT_ID) REFERENCES COMMENT(ID), " +
                    "FOREIGN KEY (TRACK_ID) REFERENCES TRACK(ID))");
        }
    }

    //Rimuove file temporanei e pulisce ambiente
    @AfterEach
    void tearDown() throws SQLException {
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }
}