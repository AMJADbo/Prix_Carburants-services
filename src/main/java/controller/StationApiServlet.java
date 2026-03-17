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

    public StationApiServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String ville = request.getParameter("ville");

        StationDAO dao = new StationDAO();
        List<Station> stations;

        if (ville == null || ville.trim().isEmpty()) {
            stations = dao.findAllStations();
        } else {
            stations = dao.findByVille(ville.trim());
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < stations.size(); i++) {
            Station s = stations.get(i);

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

            if (i < stations.size() - 1) {
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