package controller;

import java.io.IOException;
import java.util.ArrayList;
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
import service.DistanceService;

/**
 * Retourne les stations dans un rayon donné, avec les prix du carburant demandé
 * et le coût total estimé (plein + trajet aller-retour).
 *
 * Endpoint : GET /api/stations/near
 *
 * Paramètres obligatoires :
 *   lat       – latitude de l'utilisateur (degrés décimaux)
 *   lon       – longitude de l'utilisateur (degrés décimaux)
 *   radius    – rayon de recherche en km
 *
 * Paramètres optionnels :
 *   carburant     – ex. "SP95" (si absent, retourne toutes les stations sans info prix)
 *   lavage        – "true" pour filtrer les stations avec lavage
 *   gonflage      – "true" pour filtrer les stations avec gonflage
 *   automate      – "true" pour filtrer les stations avec automate 24h/24
 *   conso         – consommation du véhicule en L/100km (défaut 7.0)
 *   resTotal      – capacité totale du réservoir en litres (défaut 50.0)
 *   resCourant    – niveau courant du réservoir en litres (défaut 20.0)
 */
@WebServlet("/api/stations/near")
public class NearStationApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String latParam      = request.getParameter("lat");
        String lonParam      = request.getParameter("lon");
        String radiusParam   = request.getParameter("radius");
        String carburant     = request.getParameter("carburant");
        String lavageParam   = request.getParameter("lavage");
        String gonflageParam = request.getParameter("gonflage");
        String automateParam = request.getParameter("automate");
        String consoParam    = request.getParameter("conso");
        String resTotalParam = request.getParameter("resTotal");
        String resCourantParam = request.getParameter("resCourant");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (latParam == null || lonParam == null || radiusParam == null) {
            response.getWriter().write("{\"error\":\"parametres lat, lon et radius obligatoires\"}");
            return;
        }

        double lat, lon, radius;
        try {
            lat    = Double.parseDouble(latParam);
            lon    = Double.parseDouble(lonParam);
            radius = Double.parseDouble(radiusParam);
        } catch (NumberFormatException e) {
            response.getWriter().write("{\"error\":\"parametres invalides\"}");
            return;
        }

        // Paramètres optionnels avec valeurs par défaut
        double conso      = parseDoubleOrDefault(consoParam, 7.0);
        double resTotal   = parseDoubleOrDefault(resTotalParam, 50.0);
        double resCourant = parseDoubleOrDefault(resCourantParam, 20.0);

        boolean filterLavage   = "true".equalsIgnoreCase(lavageParam);
        boolean filterGonflage = "true".equalsIgnoreCase(gonflageParam);
        boolean filterAutomate = "true".equalsIgnoreCase(automateParam);

        StationDAO stationDAO = new StationDAO();
        PriceDAO priceDAO     = new PriceDAO();

        List<Station> toutesLesStations = stationDAO.findAllStations();
        List<StationResult> resultats   = new ArrayList<>();

        for (Station station : toutesLesStations) {

            // Filtres services
            if (filterLavage   && !station.isLavage())   continue;
            if (filterGonflage && !station.isGonflage()) continue;
            if (filterAutomate && !station.isAutomate())  continue;

            // Distance
            double distance = DistanceService.calculerDistance(
                    lat, lon,
                    station.getLatitude(), station.getLongitude()
            );
            if (distance > radius) continue;

            // Prix du carburant demandé
            Price prix = null;
            if (carburant != null && !carburant.trim().isEmpty()) {
                List<Price> prices = priceDAO.findByStationId(station.getIdStation());
                for (Price p : prices) {
                    if (carburant.trim().equalsIgnoreCase(p.getNomCarburant())) {
                        prix = p;
                        break;
                    }
                }
                // Si le carburant demandé n'est pas disponible, on ignore la station
                if (prix == null) continue;
            }

            station.setDistance(distance);
            resultats.add(new StationResult(station, prix, conso, resTotal, resCourant));
        }

        // Tri par coût total croissant (ou par distance si pas de carburant)
        resultats.sort((a, b) -> Double.compare(a.coutTotal, b.coutTotal));

        // Construction JSON
        StringBuilder json = new StringBuilder();
        json.append("[");

        for (int i = 0; i < resultats.size(); i++) {
            StationResult r = resultats.get(i);
            Station s = r.station;

            json.append("{")
                .append("\"rang\":").append(i + 1).append(",")
                .append("\"idStation\":").append(s.getIdStation()).append(",")
                .append("\"latitude\":").append(s.getLatitude()).append(",")
                .append("\"longitude\":").append(s.getLongitude()).append(",")
                .append("\"adresse\":\"").append(escapeJson(s.getAdresse())).append("\",")
                .append("\"ville\":\"").append(escapeJson(s.getVille())).append("\",")
                .append("\"cp\":\"").append(escapeJson(s.getCp())).append("\",")
                .append("\"nomAffiche\":\"").append(escapeJson(s.getNomAffiche())).append("\",")
                .append("\"automate\":").append(s.isAutomate()).append(",")
                .append("\"lavage\":").append(s.isLavage()).append(",")
                .append("\"gonflage\":").append(s.isGonflage()).append(",")
                .append("\"distance\":").append(Math.round(r.station.getDistance() * 10.0) / 10.0).append(",");

            if (r.prix != null) {
                json.append("\"prixCarburant\":").append(r.prix.getPrix()).append(",")
                    .append("\"dateMaj\":\"").append(escapeJson(r.prix.getDateMaj())).append("\",")
                    .append("\"nomCarburant\":\"").append(escapeJson(r.prix.getNomCarburant())).append("\",");
            } else {
                json.append("\"prixCarburant\":null,")
                    .append("\"dateMaj\":null,")
                    .append("\"nomCarburant\":null,");
            }

            json.append("\"coutTotal\":").append(Math.round(r.coutTotal * 100.0) / 100.0)
                .append("}");

            if (i < resultats.size() - 1) json.append(",");
        }

        json.append("]");
        response.getWriter().write(json.toString());
    }

    // ── Classe interne pour calculer le coût total ──────────────────────────
    private static class StationResult {
        final Station station;
        final Price   prix;
        final double  coutTotal;

        StationResult(Station station, Price prix, double conso, double resTotal, double resCourant) {
            this.station = station;
            this.prix    = prix;

            if (prix != null) {
                // Formule : (litres à remplir + litres aller-retour) × prix/L
                double litresPlein  = resTotal - resCourant;
                double litresTrajet = (2 * station.getDistance() / 100.0) * conso;
                this.coutTotal = Math.round((litresPlein + litresTrajet) * prix.getPrix() * 100.0) / 100.0;
            } else {
                // Tri par distance si pas de carburant sélectionné
                this.coutTotal = station.getDistance();
            }
        }
    }

    private double parseDoubleOrDefault(String val, double def) {
        if (val == null || val.trim().isEmpty()) return def;
        try { return Double.parseDouble(val); } catch (NumberFormatException e) { return def; }
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
