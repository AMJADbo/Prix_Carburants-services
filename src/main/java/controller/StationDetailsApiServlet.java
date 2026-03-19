// Le fichier StationDetailsApiServlet.java expose une API REST qui retourne les détails complets d'une station : informations, prix et horaires

package controller;

import java.io.IOException;
import java.util.List;

import dao.HoraireDAO;
import dao.PriceDAO;
import dao.StationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Price;
import model.Station;
import model.horaire;

/**
 * Retourne les détails complets d'une station : infos, prix, horaires.
 * Endpoint : GET /api/stations/details?id={idStation}
 *
 * CORRECTION : le JSON précédent fermait l'objet racine } avant d'écrire
 * ,"horaires":[...], ce qui produisait un JSON invalide.
 */

@WebServlet("/api/stations/details")
public class StationDetailsApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // --------------------------------------------------
    // Méthode GET : Récupération des détails complets d'une station
    // --------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Récupère le paramètre de requête GET "id"
        String idParam = request.getParameter("id");

        // Configure la réponse HTTP en JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Vérifie que l'ID est présent et non vide
        if (idParam == null || idParam.trim().isEmpty()) {
            // Retourne une erreur JSON si le paramètre est manquant
            response.getWriter().write("{\"error\":\"id station manquant\"}");
            return;
        }

        // Variable pour stocker l'ID de la station après conversion
        long idStation;

        // Conversion et validation du paramètre
        try {
            // Convertit la chaîne de caractères en nombre long
            idStation = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            // Si la conversion échoue, retourne une erreur
            response.getWriter().write("{\"error\":\"id station invalide\"}");
            return;
        }

        // Initialisation des DAO
        // Crée les objets d'accès aux données pour les 3 tables concernées
        StationDAO stationDAO = new StationDAO();
        PriceDAO priceDAO = new PriceDAO();
        HoraireDAO horaireDAO = new HoraireDAO();

        // Récupération des informations de la station
        Station station = stationDAO.findById(idStation);
        // Vérifie si la station existe
        if (station == null) {
            // La station n'existe pas en base de données
            response.getWriter().write("{\"error\":\"station introuvable\"}");
            return;
        }

        // Récupère tous les prix de cette station (différents carburants)
        List<Price> prices = priceDAO.findByStationId(idStation);
        // Récupère tous les horaires de cette station (7 jours)
        List<horaire> horaires = horaireDAO.findByStationId(idStation);

        // StringBuilder pour construire efficacement la chaîne JSON
        StringBuilder json = new StringBuilder();

        // --- objet racine OUVERT ---
        json.append("{");

        // station
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

        // prices
        json.append("\"prices\":[");
        for (int i = 0; i < prices.size(); i++) {
            Price p = prices.get(i);
            json.append("{")
                .append("\"idPrix\":").append(p.getIdPrix()).append(",")
                .append("\"idStation\":").append(p.getIdStation()).append(",")
                .append("\"nomCarburant\":\"").append(escapeJson(p.getNomCarburant())).append("\",")
                .append("\"prix\":").append(p.getPrix()).append(",")
                .append("\"dateMaj\":\"").append(escapeJson(p.getDateMaj())).append("\"")
                .append("}");
            if (i < prices.size() - 1) json.append(",");
        }
        json.append("],");

        // horaires
        json.append("\"horaires\":[");
        for (int i = 0; i < horaires.size(); i++) {
            horaire h = horaires.get(i);
            json.append("{")
                .append("\"idHoraire\":").append(h.getIdHoraire()).append(",")
                .append("\"idStation\":").append(h.getIdStation()).append(",")
                .append("\"jour\":").append(h.getJour()).append(",")
                .append("\"ouverture\":\"").append(escapeJson(h.getOuverture())).append("\",")
                .append("\"fermeture\":\"").append(escapeJson(h.getFermeture())).append("\"")
                .append("}");
            if (i < horaires.size() - 1) json.append(",");
        }
        json.append("]");

        // --- objet racine FERMÉ (manquait dans la version originale) ---
        json.append("}");

        response.getWriter().write(json.toString());
    }

    // --------------------------------------------------
    // Méthode utilitaire : Échappement JSON
    // --------------------------------------------------

    // Échappe les caractères spéciaux dans les chaînes JSON pour éviter les injections
    private String escapeJson(String text) {
        // Si le texte est null, retourne une chaîne vide
        if (text == null) return "";
        // Échappe les \ et les "
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}