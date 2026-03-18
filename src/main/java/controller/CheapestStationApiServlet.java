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

@WebServlet("/api/stations/cheapest")
public class CheapestStationApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String ville = request.getParameter("ville");
        String carburant = request.getParameter("carburant");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (ville == null || ville.trim().isEmpty() || carburant == null || carburant.trim().isEmpty()) {
            response.getWriter().write("{\"error\":\"parametres ville et carburant obligatoires\"}");
            return;
        }

        PriceDAO priceDAO = new PriceDAO();
        StationDAO stationDAO = new StationDAO();

        Price cheapestPrice = priceDAO.findCheapestPriceByVilleAndCarburant(ville.trim(), carburant.trim());

        if (cheapestPrice == null) {
            response.getWriter().write("{\"error\":\"aucune station trouvee\"}");
            return;
        }

        Station station = stationDAO.findById(cheapestPrice.getIdStation());

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

        json.append("\"price\":{")
            .append("\"idPrix\":").append(cheapestPrice.getIdPrix()).append(",")
            .append("\"idStation\":").append(cheapestPrice.getIdStation()).append(",")
            .append("\"nomCarburant\":\"").append(escapeJson(cheapestPrice.getNomCarburant())).append("\",")
            .append("\"prix\":").append(cheapestPrice.getPrix()).append(",")
            .append("\"dateMaj\":\"").append(escapeJson(cheapestPrice.getDateMaj())).append("\"")
            .append("}");

        json.append("}");

        response.getWriter().write(json.toString());
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}