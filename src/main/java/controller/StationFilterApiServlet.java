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

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String ville = request.getParameter("ville");
        String lavageParam = request.getParameter("lavage");
        String gonflageParam = request.getParameter("gonflage");
        String automateParam = request.getParameter("automate");

        Boolean lavage = lavageParam != null ? Boolean.parseBoolean(lavageParam) : null;
        Boolean gonflage = gonflageParam != null ? Boolean.parseBoolean(gonflageParam) : null;
        Boolean automate = automateParam != null ? Boolean.parseBoolean(automateParam) : null;

        StationDAO dao = new StationDAO();

        List<Station> stations = dao.findStationsWithFilters(ville, lavage, gonflage, automate);

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
                    .append("\"adresse\":\"").append(s.getAdresse()).append("\",")
                    .append("\"ville\":\"").append(s.getVille()).append("\",")
                    .append("\"cp\":\"").append(s.getCp()).append("\",")
                    .append("\"automate\":").append(s.isAutomate()).append(",")
                    .append("\"lavage\":").append(s.isLavage()).append(",")
                    .append("\"gonflage\":").append(s.isGonflage()).append(",")
                    .append("\"nomAffiche\":\"").append(s.getNomAffiche()).append("\"")
                    .append("}");

            if (i < stations.size() - 1) {
                json.append(",");
            }
        }

        json.append("]");

        response.getWriter().write(json.toString());
    }
}