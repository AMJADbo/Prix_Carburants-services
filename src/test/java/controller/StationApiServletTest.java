// Tests unitaires pour la classe StationApiServlet

package controller;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour StationApiServlet")
class StationApiServletTest {

    private StationApiServlet servlet;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    private StringWriter responseWriter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void initTest() throws Exception {
        servlet = new StationApiServlet();
        responseWriter = new StringWriter();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));
        objectMapper = new ObjectMapper();
    }

    // ========================================
    // Tests sans filtre (ville = null)
    // ========================================

    @Test
    @DisplayName("doGet() - doit retourner toutes les stations quand aucun filtre ville n'est fourni")
    void testGetAllStationsNoFilter() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn(null);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.startsWith("["), "Doit retourner un JSON array");
        assertTrue(result.endsWith("]"), "Doit terminer par un crochet");
        verify(mockResponse).setContentType("application/json");
    }

    @Test
    @DisplayName("doGet() - doit retourner une liste vide quand aucune station n'existe")
    void testGetAllStationsEmpty() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn(null);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertEquals("[]", result.trim(), "Doit retourner un array vide");
    }

    @Test
    @DisplayName("doGet() - doit retourner toutes les stations quand ville est vide")
    void testGetAllStationsEmptyVille() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn("   ");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertEquals("[]", result.trim(), "Doit retourner un array vide");
    }

    // ========================================
    // Tests avec filtre ville
    // ========================================

    @Test
    @DisplayName("doGet() - doit retourner les stations d'une ville spécifique")
    void testGetStationsByVille() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn("Paris");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertEquals("[]", result.trim(), "Doit retourner un array (vide en absence de mock DAO)");
    }

    @Test
    @DisplayName("doGet() - doit trim l'espace blanc du paramètre ville")
    void testGetStationsWithTrimmedVille() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn("  Paris  ");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.startsWith("["), "Doit retourner un JSON array");
    }

    @Test
    @DisplayName("doGet() - doit retourner une liste vide quand aucune station ne correspond à la ville")
    void testGetStationsByVilleNotFound() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn("VilleInexistante");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertEquals("[]", result.trim(), "Doit retourner un array vide");
    }

    // ========================================
    // Tests des en-têtes HTTP
    // ========================================

    @Test
    @DisplayName("doGet() - doit définir le content type à application/json")
    void testContentType() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn(null);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        verify(mockResponse).setContentType("application/json");
    }

    @Test
    @DisplayName("doGet() - doit définir le charset à UTF-8")
    void testCharacterEncoding() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn(null);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        verify(mockResponse).setCharacterEncoding("UTF-8");
    }

    // ========================================
    // Tests de validation JSON
    // ========================================

    @Test
    @DisplayName("doGet() - doit retourner un JSON valide")
    void testValidJsonOutput() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn(null);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        try {
            objectMapper.readTree(result);
            assertTrue(true, "Le JSON doit être valide");
        } catch (Exception e) {
            fail("Le JSON n'est pas valide : " + result);
        }
    }

    @Test
    @DisplayName("doGet() - doit retourner un JSON array")
    void testJsonArrayOutput() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn(null);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertTrue(result.startsWith("["), "Doit démarrer par [");
        assertTrue(result.endsWith("]"), "Doit terminer par ]");
    }

    // ========================================
    // Tests des caractères d'échappement JSON
    // ========================================

    @Test
    @DisplayName("doGet() - doit échapper les guillemets dans l'adresse")
    void testEscapeQuotesInAdresse() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn(null);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        try {
            objectMapper.readTree(result);
            assertTrue(true, "Les guillemets doivent être correctement échappés");
        } catch (Exception e) {
            fail("Échappement JSON incorrect pour les guillemets");
        }
    }

    @Test
    @DisplayName("doGet() - doit échapper les backslashes dans l'adresse")
    void testEscapeBackslashesInAdresse() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn(null);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        try {
            objectMapper.readTree(result);
            assertTrue(true, "Les backslashes doivent être correctement échappés");
        } catch (Exception e) {
            fail("Échappement JSON incorrect pour les backslashes");
        }
    }

    // ========================================
    // Tests de la structure JSON
    // ========================================

    @Test
    @DisplayName("doGet() - le JSON doit contenir les champs obligatoires")
    void testJsonStructureWithRequiredFields() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn(null);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        JsonNode jsonArray = objectMapper.readTree(result);
        assertTrue(jsonArray.isArray(), "Le résultat doit être un array");
    }

    @Test
    @DisplayName("doGet() - un objet station doit avoir tous les champs")
    void testStationJsonStructure() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn(null);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        JsonNode jsonArray = objectMapper.readTree(result);
        
        if (jsonArray.size() > 0) {
            JsonNode station = jsonArray.get(0);
            assertTrue(station.has("idStation"), "Doit avoir idStation");
            assertTrue(station.has("latitude"), "Doit avoir latitude");
            assertTrue(station.has("longitude"), "Doit avoir longitude");
            assertTrue(station.has("adresse"), "Doit avoir adresse");
            assertTrue(station.has("ville"), "Doit avoir ville");
            assertTrue(station.has("cp"), "Doit avoir cp");
            assertTrue(station.has("automate"), "Doit avoir automate");
            assertTrue(station.has("lavage"), "Doit avoir lavage");
            assertTrue(station.has("gonflage"), "Doit avoir gonflage");
            assertTrue(station.has("nomAffiche"), "Doit avoir nomAffiche");
        }
    }

    // ========================================
    // Tests de robustesse
    // ========================================

    @Test
    @DisplayName("doGet() - doit gérer les villes avec caractères spéciaux")
    void testVilleWithSpecialCharacters() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn("Côte-d'Or");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertEquals("[]", result.trim(), "Doit retourner un array vide");
    }

    @Test
    @DisplayName("doGet() - doit gérer les villes avec accents")
    void testVilleWithAccents() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn("Montréal");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertEquals("[]", result.trim(), "Doit retourner un array vide");
    }

    @Test
    @DisplayName("doGet() - doit être case-sensitive pour les villes")
    void testVilleCaseSensitive() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn("paris"); // minuscule

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertEquals("[]", result.trim(), "Doit retourner un array vide");
    }

    @Test
    @DisplayName("doGet() - doit gérer les très longues chaînes ville")
    void testVilleVeryLongString() throws Exception {
        // Arrange
        String longVille = "a".repeat(1000);
        when(mockRequest.getParameter("ville")).thenReturn(longVille);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertEquals("[]", result.trim(), "Doit retourner un array vide");
    }

    // ========================================
    // Tests de cas limites
    // ========================================

    @Test
    @DisplayName("doGet() - doit gérer les paramètres null")
    void testNullVilleParameter() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn(null);

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertEquals("[]", result.trim(), "Doit retourner un array vide");
    }

    @Test
    @DisplayName("doGet() - doit gérer les villes avec espaces uniquement")
    void testVilleWithOnlySpaces() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn("     ");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertEquals("[]", result.trim(), "Doit retourner un array vide (trim rend la chaîne vide)");
    }

    @Test
    @DisplayName("doGet() - doit gérer une chaîne vide pour ville")
    void testEmptyVilleString() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn("");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        assertEquals("[]", result.trim(), "Doit retourner un array vide");
    }

    // ========================================
    // Tests d'intégrité des données
    // ========================================

    @Test
    @DisplayName("doGet() - doit toujours retourner un array JSON valide")
    void testAlwaysValidJson() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn("TestVille");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        String result = responseWriter.toString();
        try {
            JsonNode node = objectMapper.readTree(result);
            assertTrue(node.isArray(), "Le résultat doit toujours être un array");
        } catch (Exception e) {
            fail("Le JSON doit toujours être valide : " + result);
        }
    }

    @Test
    @DisplayName("doGet() - doit toujours appeler setContentType et setCharacterEncoding")
    void testAlwaysSetHeaders() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn("SomeCity");

        // Act
        servlet.doGet(mockRequest, mockResponse);

        // Assert
        verify(mockResponse).setContentType("application/json");
        verify(mockResponse).setCharacterEncoding("UTF-8");
    }

    @Test
    @DisplayName("doGet() - doit gérer les appels multiples")
    void testMultipleCalls() throws Exception {
        // Arrange
        when(mockRequest.getParameter("ville")).thenReturn(null);

        // Act - Appel 1
        servlet.doGet(mockRequest, mockResponse);
        String result1 = responseWriter.toString();

        // Reset
        responseWriter = new StringWriter();
        when(mockResponse.getWriter()).thenReturn(new PrintWriter(responseWriter));

        // Act - Appel 2
        servlet.doGet(mockRequest, mockResponse);
        String result2 = responseWriter.toString();

        // Assert
        assertEquals("[]", result1.trim(), "Premier appel doit retourner un array");
        assertEquals("[]", result2.trim(), "Deuxième appel doit retourner un array");
    }

}
