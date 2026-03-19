// Le fichier StationFilterApiServlet.java expose une API REST qui retourne les stations filtrées par ville et/ou services disponibles (lavage, gonflage, automate)

package controller;

import dao.StationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import model.Station;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/stations/filter")
public class StationFilterApiServlet extends HttpServlet {

    // --------------------------------------------------
    // Méthode GET : Récupération des stations filtrées
    // --------------------------------------------------

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Récupération des paramètres de requête GET
        String ville = request.getParameter("ville");
        String lavageParam = request.getParameter("lavage");
        String gonflageParam = request.getParameter("gonflage");
        String automateParam = request.getParameter("automate");

        // Conversion des paramètres String en Boolean (avec gestion des null)
        Boolean lavage = lavageParam != null ? Boolean.parseBoolean(lavageParam) : null;
        Boolean gonflage = gonflageParam != null ? Boolean.parseBoolean(gonflageParam) : null;
        Boolean automate = automateParam != null ? Boolean.parseBoolean(automateParam) : null;

        // Accès au DAO
        StationDAO dao = new StationDAO();

        // Recherche des stations avec filtres
        // Appelle la méthode du DAO qui applique les filtres (ville, lavage, gonflage, automate)
        // Les paramètres null sont ignorés dans la requête SQL
        List<Station> stations = dao.findStationsWithFilters(ville, lavage, gonflage, automate);

        // Configure la réponse HTTP en JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // StringBuilder pour construire efficacement la chaîne JSON
        StringBuilder json = new StringBuilder();
        json.append("[");

        // Parcourt chaque station de la liste filtrée
        for (int i = 0; i < stations.size(); i++) {

            Station s = stations.get(i);

            // Construit un objet JSON pour cette station
            json.append("{")
                    .append("\"idStation\":").append(s.getIdStation()).append(",")
                    .append("\"latitude\":").append(s.getLatitude()).append(",")
                    .append("\"longitude\":").append(s.getLongitude()).append(",")
                    .append("\"adresse\":\"").append(s.getAdresse()).append("\",")
                    .append("\"ville\":\"").append(s.getVille()).append("\",")
                    .append("\"cp\":\"").append(s.getCp()).append("\",")
                    .append("\"automate\":").append(s.isAutomate()).append(",")
                    .append("\"lavage\":").append(s.isLavage()).append(",")
                    .append("\"gonflage\":").append(s.isGonflage()).append(",")
                    .append("\"nomAffiche\":\"").append(s.getNomAffiche()).append("\"")
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
}