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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String stationIdParam = request.getParameter("stationId");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (stationIdParam == null || stationIdParam.trim().isEmpty()) {
            response.getWriter().write("{\"error\":\"stationId manquant\"}");
            return;
        }

        long stationId;

        try {
            stationId = Long.parseLong(stationIdParam);
        } catch (NumberFormatException e) {
            response.getWriter().write("{\"error\":\"stationId invalide\"}");
            return;
        }

        HoraireDAO dao = new HoraireDAO();
        List<horaire> horaires = dao.findByStationId(stationId);

        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < horaires.size(); i++) {
            horaire h = horaires.get(i);

            json.append("{")
                .append("\"idHoraire\":").append(h.getIdHoraire()).append(",")
                .append("\"idStation\":").append(h.getIdStation()).append(",")
                .append("\"jour\":").append(h.getJour()).append(",")
                .append("\"ouverture\":\"").append(escapeJson(h.getOuverture())).append("\",")
                .append("\"fermeture\":\"").append(escapeJson(h.getFermeture())).append("\"")
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