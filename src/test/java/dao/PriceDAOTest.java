// Tests unitaires pour la classe PriceDAO

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import model.Price;
import util.DBConnection;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour PriceDAO")
class PriceDAOTest {

    private PriceDAO priceDAO;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    void initTest() {
        priceDAO = new PriceDAO();
    }

    // ========================================
    // Tests pour findByStationId()
    // ========================================

    @Test
    @DisplayName("findByStationId() - doit retourner les prix d'une station")
    void testFindByStationIdSuccess() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement("SELECT * FROM prix WHERE id_station = ?"))
                    .thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getLong("id_prix")).thenReturn(1L);
            when(mockResultSet.getLong("id_station")).thenReturn(1L);
            when(mockResultSet.getString("nom_carburant")).thenReturn("Essence");
            when(mockResultSet.getDouble("prix")).thenReturn(1.85);
            when(mockResultSet.getString("date_maj")).thenReturn("2026-03-23");

            // Act
            List<Price> prices = priceDAO.findByStationId(1L);

            // Assert
            assertNotNull(prices, "La liste ne doit pas être null");
            assertEquals(1, prices.size(), "La liste doit contenir 1 prix");
            assertEquals("Essence", prices.get(0).getNomCarburant(), "Le carburant doit être Essence");
            assertEquals(1.85, prices.get(0).getPrix(), 0.01, "Le prix doit être 1.85");
            assertEquals("2026-03-23", prices.get(0).getDateMaj(), "La date doit être correcte");
            
            verify(mockPreparedStatement, times(1)).setLong(1, 1L);
        }
    }

    @Test
    @DisplayName("findByStationId() - doit retourner une liste vide quand il n'y a pas de prix")
    void testFindByStationIdEmpty() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            // Act
            List<Price> prices = priceDAO.findByStationId(999L);

            // Assert
            assertNotNull(prices, "La liste ne doit pas être null");
            assertTrue(prices.isEmpty(), "La liste doit être vide");
        }
    }

    @Test
    @DisplayName("findByStationId() - doit retourner plusieurs prix pour une station")
    void testFindByStationIdMultiple() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
            
            when(mockResultSet.getLong("id_prix")).thenReturn(1L).thenReturn(2L);
            when(mockResultSet.getLong("id_station")).thenReturn(1L);
            when(mockResultSet.getString("nom_carburant")).thenReturn("Essence").thenReturn("Diesel");
            when(mockResultSet.getDouble("prix")).thenReturn(1.85).thenReturn(1.75);
            when(mockResultSet.getString("date_maj")).thenReturn("2026-03-23");

            // Act
            List<Price> prices = priceDAO.findByStationId(1L);

            // Assert
            assertNotNull(prices, "La liste ne doit pas être null");
            assertEquals(2, prices.size(), "La liste doit contenir 2 prix");
            assertEquals("Essence", prices.get(0).getNomCarburant());
            assertEquals("Diesel", prices.get(1).getNomCarburant());
        }
    }

    @Test
    @DisplayName("findByStationId() - doit gérer les exceptions")
    void testFindByStationIdException() {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenThrow(new RuntimeException("Connexion échouée"));

            // Act
            List<Price> prices = priceDAO.findByStationId(1L);

            // Assert
            assertNotNull(prices, "La liste ne doit pas être null");
            assertTrue(prices.isEmpty(), "La liste doit être vide en cas d'erreur");
        }
    }

    @Test
    @DisplayName("findByStationId() - doit correctement mapper les données des prix")
    void testFindByStationIdDataMapping() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true).thenReturn(false);
            when(mockResultSet.getLong("id_prix")).thenReturn(42L);
            when(mockResultSet.getLong("id_station")).thenReturn(5L);
            when(mockResultSet.getString("nom_carburant")).thenReturn("Super95");
            when(mockResultSet.getDouble("prix")).thenReturn(1.95);
            when(mockResultSet.getString("date_maj")).thenReturn("2026-03-20");

            // Act
            List<Price> prices = priceDAO.findByStationId(5L);

            // Assert
            assertNotNull(prices, "La liste ne doit pas être null");
            assertEquals(1, prices.size(), "La liste doit contenir 1 prix");
            
            Price p = prices.get(0);
            assertAll("Vérification du mapping des données",
                () -> assertEquals(42L, p.getIdPrix(), "L'ID prix doit être 42"),
                () -> assertEquals(5L, p.getIdStation(), "L'ID station doit être 5"),
                () -> assertEquals("Super95", p.getNomCarburant(), "Le carburant doit être Super95"),
                () -> assertEquals(1.95, p.getPrix(), 0.01, "Le prix doit être 1.95"),
                () -> assertEquals("2026-03-20", p.getDateMaj(), "La date doit être correcte")
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
            priceDAO.findByStationId(1L);

            // Assert - Vérifier que les ressources sont fermées
            verify(mockResultSet, times(1)).close();
            verify(mockPreparedStatement, times(1)).close();
            verify(mockConnection, times(1)).close();
        }
    }

    // ========================================
    // Tests pour findCheapestPriceByVilleAndCarburant()
    // ========================================

    @Test
    @DisplayName("findCheapestPriceByVilleAndCarburant() - doit retourner le prix le moins cher")
    void testFindCheapestPriceSuccess() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(contains("JOIN station"))).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getLong("id_prix")).thenReturn(10L);
            when(mockResultSet.getLong("id_station")).thenReturn(3L);
            when(mockResultSet.getString("nom_carburant")).thenReturn("Essence");
            when(mockResultSet.getDouble("prix")).thenReturn(1.65);
            when(mockResultSet.getString("date_maj")).thenReturn("2026-03-23");

            // Act
            Price price = priceDAO.findCheapestPriceByVilleAndCarburant("Paris", "Essence");

            // Assert
            assertNotNull(price, "Le prix ne doit pas être null");
            assertEquals(10L, price.getIdPrix(), "L'ID prix doit être 10");
            assertEquals("Essence", price.getNomCarburant());
            assertEquals(1.65, price.getPrix(), 0.01);
            
            verify(mockPreparedStatement, times(1)).setString(1, "Paris");
            verify(mockPreparedStatement, times(1)).setString(2, "Essence");
        }
    }

    @Test
    @DisplayName("findCheapestPriceByVilleAndCarburant() - doit retourner null quand aucun prix ne correspond")
    void testFindCheapestPriceNotFound() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            // Act
            Price price = priceDAO.findCheapestPriceByVilleAndCarburant("VilleInexistante", "CarburantInexistant");

            // Assert
            assertNull(price, "Le prix doit être null");
        }
    }

    @Test
    @DisplayName("findCheapestPriceByVilleAndCarburant() - doit filtrer par ville et carburant")
    void testFindCheapestPriceFiltering() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getLong("id_prix")).thenReturn(15L);
            when(mockResultSet.getLong("id_station")).thenReturn(7L);
            when(mockResultSet.getString("nom_carburant")).thenReturn("Diesel");
            when(mockResultSet.getDouble("prix")).thenReturn(1.55);
            when(mockResultSet.getString("date_maj")).thenReturn("2026-03-23");

            // Act
            Price price = priceDAO.findCheapestPriceByVilleAndCarburant("Marseille", "Diesel");

            // Assert
            assertNotNull(price);
            assertEquals("Diesel", price.getNomCarburant());
            
            // Vérifier que les paramètres corrects ont été utilisés
            verify(mockPreparedStatement, times(1)).setString(1, "Marseille");
            verify(mockPreparedStatement, times(1)).setString(2, "Diesel");
        }
    }

    @Test
    @DisplayName("findCheapestPriceByVilleAndCarburant() - doit gérer les exceptions")
    void testFindCheapestPriceException() {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenThrow(new RuntimeException("Connexion échouée"));

            // Act
            Price price = priceDAO.findCheapestPriceByVilleAndCarburant("Paris", "Essence");

            // Assert
            assertNull(price, "Le prix doit être null en cas d'erreur");
        }
    }

    @Test
    @DisplayName("findCheapestPriceByVilleAndCarburant() - doit correctement mapper les données")
    void testFindCheapestPriceDataMapping() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getLong("id_prix")).thenReturn(99L);
            when(mockResultSet.getLong("id_station")).thenReturn(12L);
            when(mockResultSet.getString("nom_carburant")).thenReturn("GPL");
            when(mockResultSet.getDouble("prix")).thenReturn(0.85);
            when(mockResultSet.getString("date_maj")).thenReturn("2026-03-15");

            // Act
            Price price = priceDAO.findCheapestPriceByVilleAndCarburant("Lyon", "GPL");

            // Assert
            assertAll("Vérification du mapping des données",
                () -> assertEquals(99L, price.getIdPrix()),
                () -> assertEquals(12L, price.getIdStation()),
                () -> assertEquals("GPL", price.getNomCarburant()),
                () -> assertEquals(0.85, price.getPrix(), 0.01),
                () -> assertEquals("2026-03-15", price.getDateMaj())
            );
        }
    }

    @Test
    @DisplayName("findCheapestPriceByVilleAndCarburant() - doit fermer les ressources correctement")
    void testFindCheapestPriceResourceClosure() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            when(mockResultSet.next()).thenReturn(false);

            // Act
            priceDAO.findCheapestPriceByVilleAndCarburant("Paris", "Essence");

            // Assert - Vérifier que les ressources sont fermées
            verify(mockResultSet, times(1)).close();
            verify(mockPreparedStatement, times(1)).close();
            verify(mockConnection, times(1)).close();
        }
    }

    @Test
    @DisplayName("findCheapestPriceByVilleAndCarburant() - doit filtrer correctement différentes villes et carburants")
    void testFindCheapestPriceDifferentCombinations() throws Exception {
        try (MockedStatic<DBConnection> mockedDBConnection = mockStatic(DBConnection.class)) {
            // Arrange
            mockedDBConnection.when(DBConnection::getConnection).thenReturn(mockConnection);
            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
            when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
            
            when(mockResultSet.next()).thenReturn(true);
            when(mockResultSet.getLong("id_prix")).thenReturn(50L);
            when(mockResultSet.getLong("id_station")).thenReturn(8L);
            when(mockResultSet.getString("nom_carburant")).thenReturn("Super98");
            when(mockResultSet.getDouble("prix")).thenReturn(2.05);
            when(mockResultSet.getString("date_maj")).thenReturn("2026-03-23");

            // Act
            Price price = priceDAO.findCheapestPriceByVilleAndCarburant("Bordeaux", "Super98");

            // Assert
            assertNotNull(price);
            assertEquals("Super98", price.getNomCarburant());
            assertEquals(2.05, price.getPrix(), 0.01);
            
            // Vérifier les appels avec les bons paramètres
            verify(mockPreparedStatement, times(1)).setString(1, "Bordeaux");
            verify(mockPreparedStatement, times(1)).setString(2, "Super98");
        }
    }
}
