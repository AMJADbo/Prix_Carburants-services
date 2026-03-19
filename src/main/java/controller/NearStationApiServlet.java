// Le fichier NearStationApiServlet.java expose une API REST qui retourne les stations-service proches de l'utilisateur, triées par coût total (prix + trajet)

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

    // --------------------------------------------------
    // Méthode GET : Récupération des stations proches
    // --------------------------------------------------

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Récupération des paramètres de requête GET
        // Paramètres obligatoires
        String latParam      = request.getParameter("lat");
        String lonParam      = request.getParameter("lon");
        String radiusParam   = request.getParameter("radius");
        // Paramètre soptionnels
        String carburant     = request.getParameter("carburant");
        String lavageParam   = request.getParameter("lavage");
        String gonflageParam = request.getParameter("gonflage");
        String automateParam = request.getParameter("automate");
        String consoParam    = request.getParameter("conso");
        String resTotalParam = request.getParameter("resTotal");
        String resCourantParam = request.getParameter("resCourant");

        // Configure la réponse HTTP en JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Validation des paramètres obligatoires
        if (latParam == null || lonParam == null || radiusParam == null) {
            response.getWriter().write("{\"error\":\"parametres lat, lon et radius obligatoires\"}");
            return;
        }

        // Variables pour stocker les valeurs converties
        double lat, lon, radius;

        // Conversion et validation des paramètres obligatoires
        try {
            lat    = Double.parseDouble(latParam);
            lon    = Double.parseDouble(lonParam);
            radius = Double.parseDouble(radiusParam);
        } catch (NumberFormatException e) {
            // Si la conversion échoue, return une erreur
            response.getWriter().write("{\"error\":\"parametres invalides\"}");
            return;
        }

        // Paramètres optionnels avec valeurs par défaut
        double conso      = parseDoubleOrDefault(consoParam, 7.0);
        double resTotal   = parseDoubleOrDefault(resTotalParam, 50.0);
        double resCourant = parseDoubleOrDefault(resCourantParam, 20.0);

        // Conversion des filtres booléens
        boolean filterLavage   = "true".equalsIgnoreCase(lavageParam);
        boolean filterGonflage = "true".equalsIgnoreCase(gonflageParam);
        boolean filterAutomate = "true".equalsIgnoreCase(automateParam);

        // Initialisation des DAO
        StationDAO stationDAO = new StationDAO();
        PriceDAO priceDAO     = new PriceDAO();

        // Récupération de toutes les stations depuis la base de données
        List<Station> toutesLesStations = stationDAO.findAllStations();
        List<StationResult> resultats   = new ArrayList<>();

        // Filtrage et calcul pour chaque station
        for (Station station : toutesLesStations) {

            // Filtres services
            // Ignore la station si le filtre est actif et que le service n'est pas disponible
            if (filterLavage   && !station.isLavage())   continue;
            if (filterGonflage && !station.isGonflage()) continue;
            if (filterAutomate && !station.isAutomate())  continue;

            // Distance entre l'utilisateur et la station
            double distance = DistanceService.calculerDistance(
                    lat, lon,
                    station.getLatitude(), station.getLongitude()
            );
            // Ignore la station si elle est hors du rayon
            if (distance > radius) continue;

            // Prix du carburant demandé
            Price prix = null;
            if (carburant != null && !carburant.trim().isEmpty()) {
                // Récupère tous les prix de cette station
                List<Price> prices = priceDAO.findByStationId(station.getIdStation());
                // Cherche le prix correspondant au type de carburant demandé
                for (Price p : prices) {
                    if (carburant.trim().equalsIgnoreCase(p.getNomCarburant())) {
                        prix = p;
                        break;
                    }
                }
                // Si le carburant demandé n'est pas disponible, on ignore la station
                if (prix == null) continue;
            }

            // Stocke la distance calculée dans l'objet station
            station.setDistance(distance);
            // Ajoute la station aux résultats avec calcul du coût total
            resultats.add(new StationResult(station, prix, conso, resTotal, resCourant));
        }

        // Tri par coût total croissant (ou par distance si pas de carburant)
        resultats.sort((a, b) -> Double.compare(a.coutTotal, b.coutTotal));

        // Construction JSON
        StringBuilder json = new StringBuilder();
        json.append("[");

        // Parcourt chaque résultat pour construire un objet JSON
        for (int i = 0; i < resultats.size(); i++) {
            StationResult r = resultats.get(i);
            Station s = r.station;

            // Construit un objet JSON pour cette station
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

            // Informations sur le prix si dispo
            if (r.prix != null) {
                json.append("\"prixCarburant\":").append(r.prix.getPrix()).append(",")
                    .append("\"dateMaj\":\"").append(escapeJson(r.prix.getDateMaj())).append("\",")
                    .append("\"nomCarburant\":\"").append(escapeJson(r.prix.getNomCarburant())).append("\",");
            } else {
                // Si pas de prix, return null
                json.append("\"prixCarburant\":null,")
                    .append("\"dateMaj\":null,")
                    .append("\"nomCarburant\":null,");
            }
            
            // Coût total estimé (arrondi à 2 décimales)
            json.append("\"coutTotal\":").append(Math.round(r.coutTotal * 100.0) / 100.0)
                .append("}");

            // Ajoute une virgule entre les objets, sauf après le dernier
            if (i < resultats.size() - 1) json.append(",");
        }

        json.append("]");

        // Envoi de la réponse JSON
        response.getWriter().write(json.toString());
    }

    // --------------------------------------------------
    // Classe interne : Calcule du coût total
    // --------------------------------------------------

    // Classe interne pour encapsuler une station, son prix et le coût total calculé
    private static class StationResult {
        final Station station;
        final Price   prix;
        final double  coutTotal;

        // Construction qui calcule automatique le coût total
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

    // --------------------------------------------------
    // Méthodes utilitaires
    // --------------------------------------------------

    // Parse un paramètre en double, return une valeur par défaut en cas d'erreur
    private double parseDoubleOrDefault(String val, double def) {
        if (val == null || val.trim().isEmpty()) return def;
        try { return Double.parseDouble(val); } catch (NumberFormatException e) { return def; }
    }

    // Échappe les caractères spéciaux dans les chaînes JSON pour éviter les injections
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
