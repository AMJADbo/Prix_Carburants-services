// Tests unitaires pour la classe HoraireDAO

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import model.horaire;
import util.DBConnection;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour HoraireDAO")
class HoraireDAOTest {

    private HoraireDAO horaireDAO;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    void initTest() {
        horaireDAO = new HoraireDAO();
    }

    // ========================================
    // Tests pour findByStationId()
    // ========================================

    @Test
    @DisplayName("findByStationId() - doit retourner les horaires d'une station")
    void testFindByStationIdSuccess() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement("SELECT * FROM horaires WHERE id_station = ?"))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getLong("id_horaire")).thenReturn(1L);
            when(mockResultSet.getLong("id_station")).thenReturn(1L);
            when(mockResultSet.getInt("jour")).thenReturn(1);
            when(mockResultSet.getString("ouverture")).thenReturn("08:00");
            when(mockResultSet.getString("fermeture")).thenReturn("20:00");

            // Act
            List<horaire> horaires = horaireDAO.findByStationId(1L);

            // Assert
            assertNotNull(horaires, "La liste ne doit pas être null");
            assertEquals(1, horaires.size(), "La liste doit contenir 1 horaire");
            assertEquals(1, horaires.get(0).getJour(), "Le jour doit être 1");
            assertEquals("08:00", horaires.get(0).getOuverture(), "L'heure d'ouverture doit être 08:00");
            assertEquals("20:00", horaires.get(0).getFermeture(), "L'heure de fermeture doit être 20:00");
            
            verify(mockPreparedStatement, times(1)).setLong(1, 1L);
        }
    }

    @Test
    @DisplayName("findByStationId() - doit retourner une liste vide quand il n'y a pas d'horaires")
    void testFindByStationIdEmpty() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            // Act
            List<horaire> horaires = horaireDAO.findByStationId(999L);

            // Assert
            assertNotNull(horaires, "La liste ne doit pas être null");
            assertTrue(horaires.isEmpty(), "La liste doit être vide");
        }
    }

    @Test
    @DisplayName("findByStationId() - doit retourner les horaires pour les 7 jours de la semaine")
    void testFindByStationIdFullWeek() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next())
                    .thenReturn(true)   // jour 1
                    .thenReturn(true)   // jour 2
                    .thenReturn(true)   // jour 3
                    .thenReturn(true)   // jour 4
                    .thenReturn(true)   // jour 5
                    .thenReturn(true)   // jour 6
                    .thenReturn(true)   // jour 7
                    .thenReturn(false); // fin
            
            when(mockResultSet.getLong("id_horaire"))
                    .thenReturn(1L).thenReturn(2L).thenReturn(3L)
                    .thenReturn(4L).thenReturn(5L).thenReturn(6L).thenReturn(7L);
            when(mockResultSet.getLong("id_station")).thenReturn(1L);
            when(mockResultSet.getInt("jour"))
                    .thenReturn(1).thenReturn(2).thenReturn(3)
                    .thenReturn(4).thenReturn(5).thenReturn(6).thenReturn(7);
            when(mockResultSet.getString("ouverture")).thenReturn("08:00");
            when(mockResultSet.getString("fermeture")).thenReturn("20:00");

            // Act
            List<horaire> horaires = horaireDAO.findByStationId(1L);

            // Assert
            assertNotNull(horaires, "La liste ne doit pas être null");
            assertEquals(7, horaires.size(), "La liste doit contenir 7 horaires");
            
            for (int i = 0; i < 7; i++) {
                assertEquals(i + 1, horaires.get(i).getJour(), "Le jour " + (i + 1) + " doit être correct");
            }
        }
    }

    @Test
    @DisplayName("findByStationId() - doit gérer les exceptions")
    void testFindByStationIdException() {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenThrow(new RuntimeException("Connexion échouée"));

            // Act
            List<horaire> horaires = horaireDAO.findByStationId(1L);

            // Assert
            assertNotNull(horaires, "La liste ne doit pas être null");
            assertTrue(horaires.isEmpty(), "La liste doit être vide en cas d'erreur");
        }
    }

    @Test
    @DisplayName("findByStationId() - doit correctement mapper les données des horaires")
    void testFindByStationIdDataMapping() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getLong("id_horaire")).thenReturn(42L);
            when(mockResultSet.getLong("id_station")).thenReturn(5L);
            when(mockResultSet.getInt("jour")).thenReturn(3);
            when(mockResultSet.getString("ouverture")).thenReturn("07:30");
            when(mockResultSet.getString("fermeture")).thenReturn("22:15");

            // Act
            List<horaire> horaires = horaireDAO.findByStationId(5L);

            // Assert
            assertNotNull(horaires, "La liste ne doit pas être null");
            assertEquals(1, horaires.size(), "La liste doit contenir 1 horaire");
            
            horaire h = horaires.get(0);
            assertAll("Vérification du mapping des données",
                () -> assertEquals(42L, h.getIdHoraire(), "L'ID horaire doit être 42"),
                () -> assertEquals(5L, h.getIdStation(), "L'ID station doit être 5"),
                () -> assertEquals(3, h.getJour(), "Le jour doit être 3"),
                () -> assertEquals("07:30", h.getOuverture(), "L'heure d'ouverture doit être 07:30"),
                () -> assertEquals("22:15", h.getFermeture(), "L'heure de fermeture doit être 22:15")
            );
        }
    }

    @Test
    @DisplayName("findByStationId() - doit créer correctement les objets horaire")
    void testFindByStationIdObjectCreation() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getLong("id_horaire")).thenReturn(10L);
            when(mockResultSet.getLong("id_station")).thenReturn(2L);
            when(mockResultSet.getInt("jour")).thenReturn(5);
            when(mockResultSet.getString("ouverture")).thenReturn("09:00");
            when(mockResultSet.getString("fermeture")).thenReturn("21:00");

            // Act
            List<horaire> horaires = horaireDAO.findByStationId(2L);

            // Assert
            assertEquals(1, horaires.size());
            horaire h = horaires.get(0);
            assertNotNull(h, "L'objet horaire ne doit pas être null");
            assertTrue(h instanceof horaire, "L'objet doit être une instance de horaire");
            assertAll("Vérification des attributs de horaire",
                () -> assertNotNull(h.getIdHoraire()),
                () -> assertNotNull(h.getIdStation()),
                () -> assertTrue(h.getJour() >= 1 && h.getJour() <= 7, "Le jour doit être entre 1 et 7"),
                () -> assertNotNull(h.getOuverture()),
                () -> assertNotNull(h.getFermeture())
            );
        }
    }

    @Test
    @DisplayName("findByStationId() - doit fermer les ressources correctement")
    void testFindByStationIdResourceClosure() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            // Act
            horaireDAO.findByStationId(1L);

            // Assert - Vérifier que les ressources sont fermées
            verify(mockResultSet, times(1)).close();
            verify(mockPreparedStatement, times(1)).close();
            verify(mockConnection, times(1)).close();
        }
    }
}
