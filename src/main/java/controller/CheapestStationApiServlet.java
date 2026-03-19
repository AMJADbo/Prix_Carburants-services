// Le fichier CheapestStationApiServlet.java expose une API REST qui retourne la station la moins chère pour une ville et un type de carburant donnés

package controller;

import java.io.IOException;

import dao.PriceDAO;
import dao.StationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Price;
import model.Station;

// Mappe le servlet à l'URL
@WebServlet("/api/stations/cheapest")
public class CheapestStationApiServlet extends HttpServlet {
    // Identifiant de version pour la sérialisation (requis pour les servlets)
    private static final long serialVersionUID = 1L;

    // --------------------------------------------------
    // Méthode GET : Récupération de la station la moins chère
    // --------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Récupère les paramètres de la requête GET
        String ville = request.getParameter("ville");
        String carburant = request.getParameter("carburant");

        // Configure la réponse HTTP en JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Validation des paramètres
        // Vérifie que ville et carburant sont présents et non vides
        if (ville == null || ville.trim().isEmpty() || carburant == null || carburant.trim().isEmpty()) {
            // Return une rreur JSON si les paramètres sont manquants
            response.getWriter().write("{\"error\":\"parametres ville et carburant obligatoires\"}");
            return;
        }

        // Accès au DAO
        // Initialise les objets d'accès aux données
        PriceDAO priceDAO = new PriceDAO();
        StationDAO stationDAO = new StationDAO();

        // Recherche le prix le moins cher
        Price cheapestPrice = priceDAO.findCheapestPriceByVilleAndCarburant(ville.trim(), carburant.trim());

        // Vérifie si un prix a été trouvé
        if (cheapestPrice == null) {
            // Aucune station ne correspond
            response.getWriter().write("{\"error\":\"aucune station trouvee\"}");
            return;
        }

        // Récupérations des détails de la station via son ID
        Station station = stationDAO.findById(cheapestPrice.getIdStation());

        // Vérifie si la station existe
        if (station == null) {
            // La station n'existe pas dans la BDD
            response.getWriter().write("{\"error\":\"station introuvable\"}");
            return;
        }

        // Construction du JSON
        StringBuilder json = new StringBuilder();

        // Début du JSON
        json.append("{");

        // Objet Station
        json.append("\"station\":{")
            .append("\"idStation\":").append(station.getIdStation()).append(",")
            .append("\"latitude\":").append(station.getLatitude()).append(",")
            .append("\"longitude\":").append(station.getLongitude()).append(",")
            .append("\"adresse\":\"").append(escapeJson(station.getAdresse())).append("\",")
            .append("\"ville\":\"").append(escapeJson(station.getVille())).append("\",")
            .append("\"cp\":\"").append(escapeJson(station.getCp())).append("\",")
            .append("\"automate\":").append(station.isAutomate()).append(",")
            .append("\"lavage\":").append(station.isLavage()).append(",")
            .append("\"gonflage\":").append(station.isGonflage()).append(",")
            .append("\"nomAffiche\":\"").append(escapeJson(station.getNomAffiche())).append("\"")
            .append("},");

        // Objet Price
        json.append("\"price\":{")
            .append("\"idPrix\":").append(cheapestPrice.getIdPrix()).append(",")
            .append("\"idStation\":").append(cheapestPrice.getIdStation()).append(",")
            .append("\"nomCarburant\":\"").append(escapeJson(cheapestPrice.getNomCarburant())).append("\",")
            .append("\"prix\":").append(cheapestPrice.getPrix()).append(",")
            .append("\"dateMaj\":\"").append(escapeJson(cheapestPrice.getDateMaj())).append("\"")
            .append("}");

        // Fin du JSON
        json.append("}");

        // Envoi du JSON
        response.getWriter().write(json.toString());
    }

    // --------------------------------------------------
    // Méthode utilitaire
    // --------------------------------------------------

    // Échappe les caractères spéciaux dans les chaînes JSON pour éviter les injections
    private String escapeJson(String text) {
        // Si le texte est null, return une chaîne vide
        if (text == null) {
            return "";
        }
        // Échappe les \ et les "
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}