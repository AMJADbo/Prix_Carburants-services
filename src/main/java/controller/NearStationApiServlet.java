package controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dao.StationDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Station;
import service.DistanceService;

@WebServlet("/api/stations/near")
public class NearStationApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String latParam = request.getParameter("lat");
        String lonParam = request.getParameter("lon");
        String radiusParam = request.getParameter("radius");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (latParam == null || lonParam == null || radiusParam == null) {
            response.getWriter().write("{\"error\":\"parametres lat, lon et radius obligatoires\"}");
            return;
        }

        double lat;
        double lon;
        double radius;

        try {
            lat = Double.parseDouble(latParam);
            lon = Double.parseDouble(lonParam);
            radius = Double.parseDouble(radiusParam);
        } catch (NumberFormatException e) {
            response.getWriter().write("{\"error\":\"parametres invalides\"}");
            return;
        }

        StationDAO dao = new StationDAO();
        List<Station> toutesLesStations = dao.findAllStations();
        List<Station> stationsProches = new ArrayList<>();

        for (Station station : toutesLesStations) {
            double distance = DistanceService.calculerDistance(
                    lat, lon,
                    station.getLatitude(), station.getLongitude()
            );

            if (distance <= radius) {
                station.setDistance(distance);
                stationsProches.add(station);
            }
        }

        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < stationsProches.size(); i++) {
            Station s = stationsProches.get(i);

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
                .append("\"nomAffiche\":\"").append(escapeJson(s.getNomAffiche())).append("\",")
                .append("\"distance\":").append(s.getDistance())
                .append("}");

            if (i < stationsProches.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");
        response.getWriter().write(json.toString());
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}