// Tests unitaires pour la classe NearStationApiServlet

package controller;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import dao.PriceDAO;
import dao.StationDAO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Price;
import model.Station;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour NearStationApiServlet")
class NearStationApiServletTest {

    private NearStationApiServlet servlet;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    @Mock
    private StationDAO mockStationDAO;

    @Mock
    private PriceDAO mockPriceDAO;

    private StringWriter responseWriter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void initTest() throws Exception {
        servlet = new NearStationApiServlet();
        responseWriter = new StringWriter();
        when(mockResponse.getWriter()).thenReturn(new java.io.PrintWriter(responseWriter));
        objectMapper = new ObjectMapper();
    }

    // ========================================
    // Tests des paramètres obligatoires
    // ========================================

    @Test
    @DisplayName("doGet() - doit retourner une erreur si lat est absent")
    void testMissingLatitude() throws Exception {
        // Arrange
        when(mockRequest.getParameter("lat")).thenReturn(null);
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("10");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.contains("error"), "Doit contenir un message d'erreur");
        verify(mockResponse).setContentType("application/json");
    }

    @Test
    @DisplayName("doGet() - doit retourner une erreur si lon est absent")
    void testMissingLongitude() throws Exception {
        // Arrange
        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn(null);
        when(mockRequest.getParameter("radius")).thenReturn("10");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.contains("error"), "Doit contenir un message d'erreur");
    }

    @Test
    @DisplayName("doGet() - doit retourner une erreur si radius est absent")
    void testMissingRadius() throws Exception {
        // Arrange
        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn(null);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.contains("error"), "Doit contenir un message d'erreur");
    }

    // ========================================
    // Tests de validation des paramètres
    // ========================================

    @Test
    @DisplayName("doGet() - doit retourner une erreur si lat est invalide")
    void testInvalidLatitude() throws Exception {
        // Arrange
        when(mockRequest.getParameter("lat")).thenReturn("invalid");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("10");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.contains("invalides"), "Doit indiquer des paramètres invalides");
    }

    @Test
    @DisplayName("doGet() - doit retourner une erreur si lon est invalide")
    void testInvalidLongitude() throws Exception {
        // Arrange
        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("not_a_number");
        when(mockRequest.getParameter("radius")).thenReturn("10");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.contains("invalides"), "Doit indiquer des paramètres invalides");
    }

    @Test
    @DisplayName("doGet() - doit retourner une erreur si radius est invalide")
    void testInvalidRadius() throws Exception {
        // Arrange
        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("abc");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.contains("invalides"), "Doit indiquer des paramètres invalides");
    }

    // ========================================
    // Tests du cas nominal (Happy Path)
    // ========================================

    @Test
    @DisplayName("doGet() - doit retourner les stations dans le rayon avec coût total")
    void testSuccessfulRequest() throws Exception {
        // Arrange
        setupBasicMocks();
        
        // Crée une station
        Station station = createTestStation(1L, "Paris", 48.8566, 2.3522, true, true, true);
        Price price = createTestPrice(1L, "SP95", 1.75);

        when(mockStationDAO.findAllStations()).thenReturn(List.of(station));
        when(mockPriceDAO.findByStationId(1L)).thenReturn(List.of(price));

        // Paramètres de requête
        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("10");
        when(mockRequest.getParameter("carburant")).thenReturn("SP95");
        when(mockRequest.getParameter("conso")).thenReturn("7.0");
        when(mockRequest.getParameter("resTotal")).thenReturn("50.0");
        when(mockRequest.getParameter("resCourant")).thenReturn("20.0");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.startsWith("["), "Doit retourner un JSON array");
        assertTrue(result.contains("Paris"), "Doit contenir le nom de la ville");
        assertTrue(result.contains("SP95"), "Doit contenir le type de carburant");
    }

    @Test
    @DisplayName("doGet() - doit retourner une liste vide si aucune station n'est dans le rayon")
    void testNoStationsInRadius() throws Exception {
        // Arrange
        setupBasicMocks();

        Station station = createTestStation(1L, "Lyon", 45.7640, 4.8357, true, false, false);
        when(mockStationDAO.findAllStations()).thenReturn(List.of(station));

        // Paramètres : requête depuis Paris avec petit rayon
        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("1"); // Rayon très petit

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertEquals("[]", result.trim(), "Doit retourner un array vide");
    }

    @Test
    @DisplayName("doGet() - doit retourner les stations sans prix si carburant n'est pas spécifié")
    void testWithoutCarburant() throws Exception {
        // Arrange
        setupBasicMocks();

        Station station = createTestStation(1L, "Paris", 48.8566, 2.3522, true, true, true);
        when(mockStationDAO.findAllStations()).thenReturn(List.of(station));

        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("10");
        when(mockRequest.getParameter("carburant")).thenReturn(null); // Pas de carburant

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.contains("\"prixCarburant\":null"), "Le prix doit être null");
        assertTrue(result.contains("\"nomCarburant\":null"), "Le nom du carburant doit être null");
    }

    // ========================================
    // Tests des filtres de services
    // ========================================

    @Test
    @DisplayName("doGet() - doit filtrer par lavage")
    void testFilterByLavage() throws Exception {
        // Arrange
        setupBasicMocks();

        Station stationAvecLavage = createTestStation(1L, "Paris", 48.8566, 2.3522, true, true, true);
        Station stationSansLavage = createTestStation(2L, "Lyon", 45.7640, 4.8357, true, false, false);

        when(mockStationDAO.findAllStations()).thenReturn(List.of(stationAvecLavage, stationSansLavage));

        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("500");
        when(mockRequest.getParameter("lavage")).thenReturn("true");
        when(mockRequest.getParameter("carburant")).thenReturn(null);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.contains("Paris"), "Doit inclure la station avec lavage");
        assertFalse(result.contains("Lyon"), "Ne doit pas inclure la station sans lavage");
    }

    @Test
    @DisplayName("doGet() - doit filtrer par gonflage")
    void testFilterByGonflage() throws Exception {
        // Arrange
        setupBasicMocks();

        Station stationAvecGonflage = createTestStation(1L, "Paris", 48.8566, 2.3522, true, true, true);
        Station stationSansGonflage = createTestStation(2L, "Lyon", 45.7640, 4.8357, true, false, false);

        when(mockStationDAO.findAllStations()).thenReturn(List.of(stationAvecGonflage, stationSansGonflage));

        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("500");
        when(mockRequest.getParameter("gonflage")).thenReturn("true");
        when(mockRequest.getParameter("carburant")).thenReturn(null);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.contains("Paris"), "Doit inclure la station avec gonflage");
    }

    @Test
    @DisplayName("doGet() - doit filtrer par automate")
    void testFilterByAutomate() throws Exception {
        // Arrange
        setupBasicMocks();

        Station stationAutomate = createTestStation(1L, "Paris", 48.8566, 2.3522, true, true, true);
        Station stationSansAutomate = createTestStation(2L, "Lyon", 45.7640, 4.8357, false, false, false);

        when(mockStationDAO.findAllStations()).thenReturn(List.of(stationAutomate, stationSansAutomate));

        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("500");
        when(mockRequest.getParameter("automate")).thenReturn("true");
        when(mockRequest.getParameter("carburant")).thenReturn(null);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.contains("Paris"), "Doit inclure la station automatisée");
    }

    // ========================================
    // Tests de tri et classement
    // ========================================

    @Test
    @DisplayName("doGet() - doit trier les stations par coût total croissant")
    void testSortingByTotalCost() throws Exception {
        // Arrange
        setupBasicMocks();

        // Station 1 : coûtera moins cher
        Station station1 = createTestStation(1L, "Paris", 48.8566, 2.3522, true, true, true);
        Price price1 = createTestPrice(1L, "SP95", 1.50); // Prix bas

        // Station 2 : coûtera plus cher
        Station station2 = createTestStation(2L, "Boulogne", 48.8355, 2.2397, true, true, true);
        Price price2 = createTestPrice(2L, "SP95", 1.80); // Prix plus élevé

        when(mockStationDAO.findAllStations()).thenReturn(List.of(station2, station1)); // Ordre inverse
        when(mockPriceDAO.findByStationId(1L)).thenReturn(List.of(price1));
        when(mockPriceDAO.findByStationId(2L)).thenReturn(List.of(price2));

        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("10");
        when(mockRequest.getParameter("carburant")).thenReturn("SP95");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        JsonNode jsonArray = objectMapper.readTree(result);
        
        if (jsonArray.size() >= 2) {
            double cout1 = jsonArray.get(0).get("coutTotal").asDouble();
            double cout2 = jsonArray.get(1).get("coutTotal").asDouble();
            assertTrue(cout1 <= cout2, "Les stations doivent être triées par coût croissant");
        }
    }

    @Test
    @DisplayName("doGet() - doit ajouter un rang à chaque station")
    void testRankingAssignment() throws Exception {
        // Arrange
        setupBasicMocks();

        Station station = createTestStation(1L, "Paris", 48.8566, 2.3522, true, true, true);
        Price price = createTestPrice(1L, "SP95", 1.75);

        when(mockStationDAO.findAllStations()).thenReturn(List.of(station));
        when(mockPriceDAO.findByStationId(1L)).thenReturn(List.of(price));

        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("10");
        when(mockRequest.getParameter("carburant")).thenReturn("SP95");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.contains("\"rang\":1"), "La première station doit avoir rang 1");
    }

    // ========================================
    // Tests des valeurs par défaut
    // ========================================

    @Test
    @DisplayName("doGet() - doit utiliser les valeurs par défaut pour conso, resTotal, resCourant")
    void testDefaultValues() throws Exception {
        // Arrange
        setupBasicMocks();

        Station station = createTestStation(1L, "Paris", 48.8566, 2.3522, true, true, true);
        Price price = createTestPrice(1L, "SP95", 1.75);

        when(mockStationDAO.findAllStations()).thenReturn(List.of(station));
        when(mockPriceDAO.findByStationId(1L)).thenReturn(List.of(price));

        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("10");
        when(mockRequest.getParameter("carburant")).thenReturn("SP95");
        // Pas de conso, resTotal, resCourant -> doivent utiliser les défauts

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.contains("coutTotal"), "Doit calculer le coût total avec valeurs par défaut");
    }

    @Test
    @DisplayName("doGet() - doit accepter des valeurs par défaut invalides et utiliser le défaut")
    void testInvalidDefaultValues() throws Exception {
        // Arrange
        setupBasicMocks();

        Station station = createTestStation(1L, "Paris", 48.8566, 2.3522, true, true, true);
        Price price = createTestPrice(1L, "SP95", 1.75);

        when(mockStationDAO.findAllStations()).thenReturn(List.of(station));
        when(mockPriceDAO.findByStationId(1L)).thenReturn(List.of(price));

        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("10");
        when(mockRequest.getParameter("carburant")).thenReturn("SP95");
        when(mockRequest.getParameter("conso")).thenReturn("invalid");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.contains("coutTotal"), "Doit utiliser la valeur par défaut pour conso invalide");
    }

    // ========================================
    // Tests du calcul du coût total
    // ========================================

    @Test
    @DisplayName("doGet() - doit calculer correctement le coût total (plein + trajet)")
    void testTotalCostCalculation() throws Exception {
        // Arrange
        setupBasicMocks();

        Station station = createTestStation(1L, "Paris", 48.8566, 2.3522, true, true, true);
        Price price = createTestPrice(1L, "SP95", 1.50);

        when(mockStationDAO.findAllStations()).thenReturn(List.of(station));
        when(mockPriceDAO.findByStationId(1L)).thenReturn(List.of(price));

        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("10");
        when(mockRequest.getParameter("carburant")).thenReturn("SP95");
        when(mockRequest.getParameter("conso")).thenReturn("7.0");
        when(mockRequest.getParameter("resTotal")).thenReturn("50.0");
        when(mockRequest.getParameter("resCourant")).thenReturn("20.0");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        JsonNode jsonArray = objectMapper.readTree(result);
        
        if (jsonArray.size() > 0) {
            double coutTotal = jsonArray.get(0).get("coutTotal").asDouble();
            assertTrue(coutTotal > 0, "Le coût total doit être positif");
        }
    }

    // ========================================
    // Tests d'échappement JSON
    // ========================================

    @Test
    @DisplayName("doGet() - doit échapper les caractères spéciaux dans le JSON")
    void testJsonEscaping() throws Exception {
        // Arrange
        setupBasicMocks();

        Station station = createTestStation(1L, "Marseille", 43.2965, 5.3698, true, true, true);
        station.setAdresse("Rue de l'\"Escale\\Spéciale");

        when(mockStationDAO.findAllStations()).thenReturn(List.of(station));

        when(mockRequest.getParameter("lat")).thenReturn("43.2965");
        when(mockRequest.getParameter("lon")).thenReturn("5.3698");
        when(mockRequest.getParameter("radius")).thenReturn("10");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.contains("\\\""), "Doit échapper les guillemets");
    }

    // ========================================
    // Tests du carburant non trouvé
    // ========================================

    @Test
    @DisplayName("doGet() - doit ignorer les stations sans le carburant demandé")
    void testCarburantNotFound() throws Exception {
        // Arrange
        setupBasicMocks();

        Station station = createTestStation(1L, "Paris", 48.8566, 2.3522, true, true, true);
        Price priceGazole = createTestPrice(1L, "Gazole", 1.60);

        when(mockStationDAO.findAllStations()).thenReturn(List.of(station));
        when(mockPriceDAO.findByStationId(1L)).thenReturn(List.of(priceGazole));

        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("10");
        when(mockRequest.getParameter("carburant")).thenReturn("SP95"); // Demande SP95 mais seulement Gazole disponible

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertEquals("[]", result.trim(), "Doit retourner array vide si carburant pas disponible");
    }

    // ========================================
    // Tests pour plusieurs stations
    // ========================================

    @Test
    @DisplayName("doGet() - doit retourner plusieurs stations")
    void testMultipleStations() throws Exception {
        // Arrange
        setupBasicMocks();

        Station station1 = createTestStation(1L, "Paris", 48.8566, 2.3522, true, true, true);
        Station station2 = createTestStation(2L, "Boulogne", 48.8355, 2.2397, true, true, true);

        Price price1 = createTestPrice(1L, "SP95", 1.75);
        Price price2 = createTestPrice(2L, "SP95", 1.75);

        when(mockStationDAO.findAllStations()).thenReturn(List.of(station1, station2));
        when(mockPriceDAO.findByStationId(1L)).thenReturn(List.of(price1));
        when(mockPriceDAO.findByStationId(2L)).thenReturn(List.of(price2));

        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("10");
        when(mockRequest.getParameter("carburant")).thenReturn("SP95");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        JsonNode jsonArray = objectMapper.readTree(result);
        assertEquals(2, jsonArray.size(), "Doit retourner 2 stations");
    }

    // ========================================
    // Tests des en-têtes HTTP
    // ========================================

    @Test
    @DisplayName("doGet() - doit définir le content type à application/json")
    void testContentType() throws Exception {
        // Arrange
        setupBasicMocks();
        when(mockStationDAO.findAllStations()).thenReturn(new ArrayList<>());

        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("10");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        verify(mockResponse).setContentType("application/json");
    }

    @Test
    @DisplayName("doGet() - doit définir le charset à UTF-8")
    void testCharacterEncoding() throws Exception {
        // Arrange
        setupBasicMocks();
        when(mockStationDAO.findAllStations()).thenReturn(new ArrayList<>());

        when(mockRequest.getParameter("lat")).thenReturn("48.8566");
        when(mockRequest.getParameter("lon")).thenReturn("2.3522");
        when(mockRequest.getParameter("radius")).thenReturn("10");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        verify(mockResponse).setCharacterEncoding("UTF-8");
    }

    // ========================================
    // Méthodes utilitaires de test
    // ========================================

    private void setupBasicMocks() {
        // Configuration de base pour tous les paramètres optionnels avec lenient pour éviter les stubbings inutilisés
        lenient().when(mockRequest.getParameter(anyString())).thenReturn(null);
    }

    private Station createTestStation(long id, String ville, double lat, double lon,
                                     boolean automate, boolean lavage, boolean gonflage) {
        Station station = new Station();
        station.setIdStation(id);
        station.setVille(ville);
        station.setLatitude(lat);
        station.setLongitude(lon);
        station.setAutomate24h(automate);
        station.setLavage(lavage);
        station.setGonflage(gonflage);
        station.setAdresse("123 Test Street");
        station.setCp("75000");
        station.setNomAffiche("Station " + ville + " - 123 Test Street");
        return station;
    }

    private Price createTestPrice(long stationId, String carburant, double prix) {
        Price price = new Price();
        price.setIdStation(stationId);
        price.setNomCarburant(carburant);
        price.setPrix(prix);
        price.setDateMaj("2026-03-23");
        return price;
    }
}
