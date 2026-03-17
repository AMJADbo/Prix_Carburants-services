package controller;

import java.io.IOException;
import java.util.List;

import dao.PriceDAO;
import dao.StationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Price;
import model.Station;
import dao.HoraireDAO;
import model.horaire;

@WebServlet("/api/stations/details")
public class StationDetailsApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (idParam == null || idParam.trim().isEmpty()) {
            response.getWriter().write("{\"error\":\"id station manquant\"}");
            return;
        }

        long idStation;

        try {
            idStation = Long.parseLong(idParam);
        } catch (NumberFormatException e) {
            response.getWriter().write("{\"error\":\"id station invalide\"}");
            return;
        }

        StationDAO stationDAO = new StationDAO();
        PriceDAO priceDAO = new PriceDAO();
        HoraireDAO horaireDAO = new HoraireDAO();
        
        Station station = stationDAO.findById(idStation);
        List<Price> prices = priceDAO.findByStationId(idStation);
        
        
        List<horaire> horaires = horaireDAO.findByStationId(idStation);
        if (station == null) {
            response.getWriter().write("{\"error\":\"station introuvable\"}");
            return;
        }

        StringBuilder json = new StringBuilder();

        json.append("{");

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

            if (i < prices.size() - 1) {
                json.append(",");
            }
        }
        json.append("]");

        json.append("}");
        json.append(",\"horaires\":[");
        for (int i = 0; i < horaires.size(); i++) {
            horaire h = horaires.get(i);

            json.append("{")
                .append("\"idHoraire\":").append(h.getIdHoraire()).append(",")
                .append("\"jour\":\"").append(h.getJour()).append("\",")
                .append("\"ouverture\":\"").append(h.getOuverture()).append("\",")
                .append("\"fermeture\":\"").append(h.getFermeture()).append("\"")
                .append("}");

            if (i < horaires.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        response.getWriter().write(json.toString());
    }
         
    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
    
}