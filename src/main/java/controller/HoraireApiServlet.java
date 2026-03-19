// Le fichier HoraireApiServlet.java expose une API REST qui retourne les horaires d'ouverture d'une station-service donnée

package controller;

import java.io.IOException;
import java.util.List;

import dao.HoraireDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.horaire;

@WebServlet("/api/horaires")
public class HoraireApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // --------------------------------------------------
    // Méthode GET
    // --------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Récupère le paramètre de requête GET "stationID"
        String stationIdParam = request.getParameter("stationId");

        // Configure la réponse HTTP en JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Vérifie que stationID est présent et non vide
        if (stationIdParam == null || stationIdParam.trim().isEmpty()) {
            // Return une erreur JSON si le paramètre est manquant
            response.getWriter().write("{\"error\":\"stationId manquant\"}");
            return;
        }

        // Variable pour stocker l'ID de la station
        long stationId;

        // Conversion et validation du paramètre
        try {
            // Convertit la chaîne de caractères en nombre long
            stationId = Long.parseLong(stationIdParam);
        } catch (NumberFormatException e) {
            // Si la conversion échoue, return une erreur
            response.getWriter().write("{\"error\":\"stationId invalide\"}");
            return;
        }

        // Initialise le DAO pour accéder aux horaires
        HoraireDAO dao = new HoraireDAO();
        // Récupère la liste de tous les horaires de cette station
        List<horaire> horaires = dao.findByStationId(stationId);

        // Construction du JSON
        StringBuilder json = new StringBuilder();
        json.append("[");

        // Parcourt chaque horaire de la liste
        for (int i = 0; i < horaires.size(); i++) {
            horaire h = horaires.get(i);

            // Construit un objet JSON pour cet horaire
            json.append("{")
                .append("\"idHoraire\":").append(h.getIdHoraire()).append(",")
                .append("\"idStation\":").append(h.getIdStation()).append(",")
                .append("\"jour\":").append(h.getJour()).append(",")
                .append("\"ouverture\":\"").append(escapeJson(h.getOuverture())).append("\",")
                .append("\"fermeture\":\"").append(escapeJson(h.getFermeture())).append("\"")
                .append("}");

            // Ajoute une virgule entre les objets sauf après le dernier
            if (i < horaires.size() - 1) {
                json.append(",");
            }
        }

        // Fin du JSON
        json.append("]");

        // Envoi du JSON
        response.getWriter().write(json.toString());
    }

    // --------------------------------------------------
    // Méthode utilitaire : échappement JSON
    // --------------------------------------------------

    // Échappe les caractères spéciaux dans les chaînes JSON pour éviter les injections
    private String escapeJson(String text) {
        // Si le texte est null : return une chaîne vide
        if (text == null) {
            return "";
        }
        // Échappe les \ et " 
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}