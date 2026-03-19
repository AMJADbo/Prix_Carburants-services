// Le fichier StationApiServlet.java expose une API REST qui retourne la liste des stations-service, filtrée optionnellement par ville

package controller;

import java.io.IOException;
import java.util.List;

import dao.StationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Station;

@WebServlet("/api/stations")
public class StationApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Constructeur
    public StationApiServlet() {
        super();
    }

    // --------------------------------------------------
    // Méthode GET : Récupération des stations
    // --------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Récupère le paramètre de requête GET "ville"
        String ville = request.getParameter("ville");

        // Initialise le DAO pour accéder aux stations
        StationDAO dao = new StationDAO();
        // Liste des stations récupérées
        List<Station> stations;

        // Si aucune ville n'est spécifiée (ou si le paramètre est vide)
        if (ville == null || ville.trim().isEmpty()) {
            // Récupère toutes les stations de la base de données
            stations = dao.findAllStations();
        } else {
            // Récupère uniquement les stations de la ville spécifiée
            stations = dao.findByVille(ville.trim());
        }

        // Configure la réponse HTTP en JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder json = new StringBuilder();
        json.append("[");

        // Parcourt chaque station de la liste
        for (int i = 0; i < stations.size(); i++) {
            Station s = stations.get(i);

            // Construit un objet JSON pour cette station
            json.append("{")
                .append("\"idStation\":").append(s.getIdStation()).append(",")
                .append("\"latitude\":").append(s.getLatitude()).append(",")
                .append("\"longitude\":").append(s.getLongitude()).append(",")
                .append("\"adresse\":\"").append(escapeJson(s.getAdresse())).append("\",")
                .append("\"ville\":\"").append(escapeJson(s.getVille())).append("\",")
                .append("\"cp\":\"").append(escapeJson(s.getCp())).append("\",")
                .append("\"automate\":").append(s.isAutomate()).append(",")
                .append("\"lavage\":").append(s.isLavage()).append(",")
                .append("\"gonflage\":").append(s.isGonflage()).append(",")
                .append("\"nomAffiche\":\"").append(escapeJson(s.getNomAffiche())).append("\"")
                .append("}");

            // Ajoute une virgule entre les objets, sauf après le dernier
            if (i < stations.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        // Envoi de la réponse JSON
        response.getWriter().write(json.toString());
    }

    // --------------------------------------------------
    // Méthode utilitaire : Échappement JSON
    // --------------------------------------------------

    // Échappe les caractères spéciaux dans les chaînes JSON pour éviter les injections
    private String escapeJson(String text) {
        // Si le texte est null, retourne une chaîne vide
        if (text == null) {
            return "";
        }
        // Échappe les \ et les "
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}