// Tests unitaires pour la classe StationDAO

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import model.Station;
import util.DBConnection;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour StationDAO")
class StationDAOTest {

    private StationDAO stationDAO;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    void initTest() {
        stationDAO = new StationDAO();
    }

    // ========================================
    // Tests pour findAllStations()
    // ========================================

    @Test
    @DisplayName("findAllStations() - doit retourner une liste vide quand il n'y a pas de stations")
    void testFindAllStationsEmpty() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement("SELECT * FROM station")).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            // Act
            List<Station> stations = stationDAO.findAllStations();

            // Assert
            assertNotNull(stations, "La liste ne doit pas être null");
            assertTrue(stations.isEmpty(), "La liste doit être vide");
            verify(mockResultSet, times(1)).close();
            verify(mockPreparedStatement, times(1)).close();
            verify(mockConnection, times(1)).close();
        }
    }

    @Test
    @DisplayName("findAllStations() - doit retourner toutes les stations")
    void testFindAllStationsSuccess() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement("SELECT * FROM station")).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getLong("id_station")).thenReturn(1L);
            when(mockResultSet.getDouble("latitude")).thenReturn(48.8566);
            when(mockResultSet.getDouble("longitude")).thenReturn(2.3522);
            when(mockResultSet.getString("adresse")).thenReturn("123 Rue de l'Exemple");
            when(mockResultSet.getString("ville")).thenReturn("Paris");
            when(mockResultSet.getString("cp")).thenReturn("75001");
            when(mockResultSet.getBoolean("automate")).thenReturn(true);
            when(mockResultSet.getBoolean("lavage")).thenReturn(false);
            when(mockResultSet.getBoolean("gonflage")).thenReturn(true);

            // Act
            List<Station> stations = stationDAO.findAllStations();

            // Assert
            assertNotNull(stations, "La liste ne doit pas être null");
            assertEquals(1, stations.size(), "La liste doit contenir 1 station");
            
            Station station = stations.get(0);
            assertEquals(1L, station.getIdStation(), "L'ID doit être 1");
            assertEquals("Paris", station.getVille(), "La ville doit être Paris");
            assertEquals(48.8566, station.getLatitude(), "La latitude doit être correcte");
            
            verify(mockConnection, times(1)).close();
        }
    }

    @Test
    @DisplayName("findAllStations() - doit retourner plusieurs stations")
    void testFindAllStationsMultiple() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
            when(mockResultSet.getLong("id_station")).thenReturn(1L).thenReturn(2L);
            when(mockResultSet.getDouble("latitude")).thenReturn(48.8566).thenReturn(43.2965);
            when(mockResultSet.getDouble("longitude")).thenReturn(2.3522).thenReturn(5.3698);
            when(mockResultSet.getString("adresse")).thenReturn("123 Rue de l'Exemple").thenReturn("456 Avenue du Port");
            when(mockResultSet.getString("ville")).thenReturn("Paris").thenReturn("Marseille");
            when(mockResultSet.getString("cp")).thenReturn("75001").thenReturn("13000");
            when(mockResultSet.getBoolean("automate")).thenReturn(true);
            when(mockResultSet.getBoolean("lavage")).thenReturn(false);
            when(mockResultSet.getBoolean("gonflage")).thenReturn(true);

            // Act
            List<Station> stations = stationDAO.findAllStations();

            // Assert
            assertEquals(2, stations.size(), "La liste doit contenir 2 stations");
            assertEquals("Paris", stations.get(0).getVille());
            assertEquals("Marseille", stations.get(1).getVille());
        }
    }

    @Test
    @DisplayName("findAllStations() - doit gérer les exceptions")
    void testFindAllStationsException() {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenThrow(new RuntimeException("Connexion échouée"));

            // Act
            List<Station> stations = stationDAO.findAllStations();

            // Assert
            assertNotNull(stations, "La liste ne doit pas être null");
            assertTrue(stations.isEmpty(), "La liste doit être vide en cas d'erreur");
        }
    }

    // ========================================
    // Tests pour findByVille()
    // ========================================

    @Test
    @DisplayName("findByVille() - doit retourner les stations d'une ville spécifique")
    void testFindByVilleSuccess() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement("SELECT * FROM station WHERE ville = ?"))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getLong("id_station")).thenReturn(1L);
            when(mockResultSet.getDouble("latitude")).thenReturn(48.8566);
            when(mockResultSet.getDouble("longitude")).thenReturn(2.3522);
            when(mockResultSet.getString("adresse")).thenReturn("123 Rue de l'Exemple");
            when(mockResultSet.getString("ville")).thenReturn("Paris");
            when(mockResultSet.getString("cp")).thenReturn("75001");
            when(mockResultSet.getBoolean("automate")).thenReturn(true);
            when(mockResultSet.getBoolean("lavage")).thenReturn(false);
            when(mockResultSet.getBoolean("gonflage")).thenReturn(true);

            // Act
            List<Station> stations = stationDAO.findByVille("Paris");

            // Assert
            assertNotNull(stations, "La liste ne doit pas être null");
            assertEquals(1, stations.size(), "La liste doit contenir 1 station");
            assertEquals("Paris", stations.get(0).getVille(), "La ville doit être Paris");
            
            verify(mockPreparedStatement, times(1)).setString(1, "Paris");
        }
    }

    @Test
    @DisplayName("findByVille() - doit retourner une liste vide quand aucune station n'existe pour une ville")
    void testFindByVilleEmpty() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            // Act
            List<Station> stations = stationDAO.findByVille("VilleInexistante");

            // Assert
            assertNotNull(stations, "La liste ne doit pas être null");
            assertTrue(stations.isEmpty(), "La liste doit être vide");
        }
    }

    @Test
    @DisplayName("findByVille() - doit retourner plusieurs stations d'une même ville")
    void testFindByVilleMultiple() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
            when(mockResultSet.getLong("id_station")).thenReturn(1L).thenReturn(2L);
            when(mockResultSet.getDouble("latitude")).thenReturn(48.8566).thenReturn(48.8600);
            when(mockResultSet.getDouble("longitude")).thenReturn(2.3522).thenReturn(2.3550);
            when(mockResultSet.getString("adresse")).thenReturn("123 Rue A").thenReturn("456 Rue B");
            when(mockResultSet.getString("ville")).thenReturn("Paris");
            when(mockResultSet.getString("cp")).thenReturn("75001").thenReturn("75002");
            when(mockResultSet.getBoolean("automate")).thenReturn(true);
            when(mockResultSet.getBoolean("lavage")).thenReturn(false);
            when(mockResultSet.getBoolean("gonflage")).thenReturn(true);

            // Act
            List<Station> stations = stationDAO.findByVille("Paris");

            // Assert
            assertEquals(2, stations.size(), "La liste doit contenir 2 stations");
            assertTrue(stations.stream().allMatch(s -> "Paris".equals(s.getVille())));
        }
    }

    // ========================================
    // Tests pour findById()
    // ========================================

    @Test
    @DisplayName("findById() - doit retourner une station par son ID")
    void testFindByIdSuccess() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement("SELECT * FROM station WHERE id_station = ?"))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getLong("id_station")).thenReturn(1L);
            when(mockResultSet.getDouble("latitude")).thenReturn(48.8566);
            when(mockResultSet.getDouble("longitude")).thenReturn(2.3522);
            when(mockResultSet.getString("adresse")).thenReturn("123 Rue de l'Exemple");
            when(mockResultSet.getString("ville")).thenReturn("Paris");
            when(mockResultSet.getString("cp")).thenReturn("75001");
            when(mockResultSet.getBoolean("automate")).thenReturn(true);
            when(mockResultSet.getBoolean("lavage")).thenReturn(false);
            when(mockResultSet.getBoolean("gonflage")).thenReturn(true);

            // Act
            Station station = stationDAO.findById(1L);

            // Assert
            assertNotNull(station, "La station ne doit pas être null");
            assertEquals(1L, station.getIdStation(), "L'ID doit être 1");
            assertEquals("Paris", station.getVille(), "La ville doit être Paris");
            
            verify(mockPreparedStatement, times(1)).setLong(1, 1L);
        }
    }

    @Test
    @DisplayName("findById() - doit retourner null quand la station n'existe pas")
    void testFindByIdNotFound() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            // Act
            Station station = stationDAO.findById(999L);

            // Assert
            assertNull(station, "La station doit être null");
        }
    }

    // ========================================
    // Tests pour findStationsWithFilters()
    // ========================================

    @Test
    @DisplayName("findStationsWithFilters() - doit retourner les stations filtrées par ville")
    void testFindStationsWithFiltersVille() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(contains("ville = ?")))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getLong("id_station")).thenReturn(1L);
            when(mockResultSet.getDouble("latitude")).thenReturn(48.8566);
            when(mockResultSet.getDouble("longitude")).thenReturn(2.3522);
            when(mockResultSet.getString("adresse")).thenReturn("123 Rue de l'Exemple");
            when(mockResultSet.getString("ville")).thenReturn("Paris");
            when(mockResultSet.getString("cp")).thenReturn("75001");
            when(mockResultSet.getBoolean("automate")).thenReturn(true);
            when(mockResultSet.getBoolean("lavage")).thenReturn(false);
            when(mockResultSet.getBoolean("gonflage")).thenReturn(true);

            // Act
            List<Station> stations = stationDAO.findStationsWithFilters("Paris", null, null, null);

            // Assert
            assertNotNull(stations, "La liste ne doit pas être null");
            assertEquals(1, stations.size(), "La liste doit contenir 1 station");
            assertEquals("Paris", stations.get(0).getVille(), "La ville doit être Paris");
        }
    }

    @Test
    @DisplayName("findStationsWithFilters() - doit retourner les stations filtrées par services")
    void testFindStationsWithFiltersServices() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(contains("lavage = ?")))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getLong("id_station")).thenReturn(2L);
            when(mockResultSet.getDouble("latitude")).thenReturn(43.2965);
            when(mockResultSet.getDouble("longitude")).thenReturn(5.3698);
            when(mockResultSet.getString("adresse")).thenReturn("456 Avenue du Port");
            when(mockResultSet.getString("ville")).thenReturn("Marseille");
            when(mockResultSet.getString("cp")).thenReturn("13000");
            when(mockResultSet.getBoolean("automate")).thenReturn(false);
            when(mockResultSet.getBoolean("lavage")).thenReturn(true);
            when(mockResultSet.getBoolean("gonflage")).thenReturn(false);

            // Act
            List<Station> stations = stationDAO.findStationsWithFilters(null, true, null, null);

            // Assert
            assertNotNull(stations, "La liste ne doit pas être null");
            assertEquals(1, stations.size(), "La liste doit contenir 1 station");
            assertTrue(stations.get(0).isLavage(), "La station doit avoir un service de lavage");
        }
    }

    @Test
    @DisplayName("findStationsWithFilters() - doit retourner les stations avec tous les filtres appliqués")
    void testFindStationsWithFiltersMultiple() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString()))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getLong("id_station")).thenReturn(1L);
            when(mockResultSet.getDouble("latitude")).thenReturn(48.8566);
            when(mockResultSet.getDouble("longitude")).thenReturn(2.3522);
            when(mockResultSet.getString("adresse")).thenReturn("123 Rue de l'Exemple");
            when(mockResultSet.getString("ville")).thenReturn("Paris");
            when(mockResultSet.getString("cp")).thenReturn("75001");
            when(mockResultSet.getBoolean("automate")).thenReturn(true);
            when(mockResultSet.getBoolean("lavage")).thenReturn(false);
            when(mockResultSet.getBoolean("gonflage")).thenReturn(true);

            // Act
            List<Station> stations = stationDAO.findStationsWithFilters("Paris", false, true, true);

            // Assert
            assertNotNull(stations, "La liste ne doit pas être null");
            assertEquals(1, stations.size(), "La liste doit contenir 1 station");
            
            Station station = stations.get(0);
            assertEquals("Paris", station.getVille(), "La ville doit être Paris");
            assertFalse(station.isLavage(), "La station ne doit pas avoir de lavage");
            assertTrue(station.isGonflage(), "La station doit avoir un service de gonflage");
            assertTrue(station.isAutomate(), "La station doit être automatisée");
        }
    }

    @Test
    @DisplayName("findStationsWithFilters() - doit retourner une liste vide quand aucun filtre ne correspond")
    void testFindStationsWithFiltersEmpty() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            // Act
            List<Station> stations = stationDAO.findStationsWithFilters("VilleInexistante", null, null, null);

            // Assert
            assertNotNull(stations, "La liste ne doit pas être null");
            assertTrue(stations.isEmpty(), "La liste doit être vide");
        }
    }

    @Test
    @DisplayName("findStationsWithFilters() - doit gérer les filtres null")
    void testFindStationsWithFiltersNullFilters() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement("SELECT * FROM station WHERE 1=1"))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            
            when(mockResultSet.getLong("id_station")).thenReturn(1L);
            when(mockResultSet.getDouble("latitude")).thenReturn(48.8566);
            when(mockResultSet.getDouble("longitude")).thenReturn(2.3522);
            when(mockResultSet.getString("adresse")).thenReturn("123 Rue de l'Exemple");
            when(mockResultSet.getString("ville")).thenReturn("Paris");
            when(mockResultSet.getString("cp")).thenReturn("75001");
            when(mockResultSet.getBoolean("automate")).thenReturn(true);
            when(mockResultSet.getBoolean("lavage")).thenReturn(false);
            when(mockResultSet.getBoolean("gonflage")).thenReturn(true);

            // Act
            List<Station> stations = stationDAO.findStationsWithFilters(null, null, null, null);

            // Assert
            assertNotNull(stations, "La liste ne doit pas être null");
            assertEquals(1, stations.size(), "La liste doit contenir 1 station");
        }
    }

    @Test
    @DisplayName("findStationsWithFilters() - doit gérer les exceptions")
    void testFindStationsWithFiltersException() {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenThrow(new RuntimeException("Connexion échouée"));

            // Act
            List<Station> stations = stationDAO.findStationsWithFilters("Paris", null, null, null);

            // Assert
            assertNotNull(stations, "La liste ne doit pas être null");
            assertTrue(stations.isEmpty(), "La liste doit être vide en cas d'erreur");
        }
    }

    // ========================================
    // Tests de validation des données
    // ========================================

    @Test
    @DisplayName("Les données de station retournées doivent être correctement mappées")
    void testStationDataMapping() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getLong("id_station")).thenReturn(5L);
            when(mockResultSet.getDouble("latitude")).thenReturn(43.2965);
            when(mockResultSet.getDouble("longitude")).thenReturn(5.3698);
            when(mockResultSet.getString("adresse")).thenReturn("Test Avenue");
            when(mockResultSet.getString("ville")).thenReturn("Marseille");
            when(mockResultSet.getString("cp")).thenReturn("13000");
            when(mockResultSet.getBoolean("automate")).thenReturn(true);
            when(mockResultSet.getBoolean("lavage")).thenReturn(true);
            when(mockResultSet.getBoolean("gonflage")).thenReturn(false);

            // Act
            Station station = stationDAO.findById(5L);

            // Assert
            assertAll("Vérification du mapping des données",
                () -> assertEquals(5L, station.getIdStation()),
                () -> assertEquals(43.2965, station.getLatitude()),
                () -> assertEquals(5.3698, station.getLongitude()),
                () -> assertEquals("Test Avenue", station.getAdresse()),
                () -> assertEquals("Marseille", station.getVille()),
                () -> assertEquals("13000", station.getCp()),
                () -> assertTrue(station.isAutomate()),
                () -> assertTrue(station.isLavage()),
                () -> assertFalse(station.isGonflage()),
                () -> assertTrue(station.getNomAffiche().contains("Marseille"))
            );
        }
    }

    @Test
    @DisplayName("Le nom d'affichage doit être correctement généré")
    void testNomAfficheGeneration() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getLong("id_station")).thenReturn(1L);
            when(mockResultSet.getDouble("latitude")).thenReturn(48.8566);
            when(mockResultSet.getDouble("longitude")).thenReturn(2.3522);
            when(mockResultSet.getString("adresse")).thenReturn("123 Rue de l'Exemple");
            when(mockResultSet.getString("ville")).thenReturn("Paris");
            when(mockResultSet.getString("cp")).thenReturn("75001");
            when(mockResultSet.getBoolean("automate")).thenReturn(true);
            when(mockResultSet.getBoolean("lavage")).thenReturn(false);
            when(mockResultSet.getBoolean("gonflage")).thenReturn(true);

            // Act
            Station station = stationDAO.findById(1L);

            // Assert
            assertNotNull(station.getNomAffiche(), "Le nom d'affichage ne doit pas être null");
            assertTrue(station.getNomAffiche().contains("Station"), "Doit contenir 'Station'");
            assertTrue(station.getNomAffiche().contains("Paris"), "Doit contenir la ville");
            assertTrue(station.getNomAffiche().contains("123 Rue de l'Exemple"), "Doit contenir l'adresse");
            assertEquals("Station Paris - 123 Rue de l'Exemple", station.getNomAffiche());
        }
    }
}
