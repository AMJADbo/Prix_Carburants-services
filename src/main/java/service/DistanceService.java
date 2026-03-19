// Le fichier DistanceService.java fournit une méthode pour calculer la distance entre deux points GPS (formule de Haversine)

package service;

public class DistanceService {

    // --------------------------------------------------
    // Méthode de calcul de distance GPS
    // --------------------------------------------------

    /**
     * Calcule la distance "à vol d'oiseau" entre deux points GPS en utilisant la formule de Haversine.
     * Cette formule prend en compte la courbure de la Terre pour un calcul précis.
     * 
     * @param lat1 Latitude du point 1 (en degrés décimaux, ex: 48.8566)
     * @param lon1 Longitude du point 1 (en degrés décimaux, ex: 2.3522)
     * @param lat2 Latitude du point 2 (en degrés décimaux, ex: 48.8606)
     * @param lon2 Longitude du point 2 (en degrés décimaux, ex: 2.3376)
     * @return Distance en kilomètres (ex: 2.5 km)
     */

    public static double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // rayon Terre en km

        // Calcul des différences de latitude et longitude
        // Conversion des degrés en radians (nécessaire pour les fonctions trigonométriques)
        // Math.toRadians() convertit : degrés × (π / 180) = radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Formule de Haversine (étape 1)
        // Calcule "a" : un terme intermédiaire de la formule
        // Cette formule prend en compte la courbure de la Terre
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        // Formule de Haversine (étape 2)
        // Calcule "c" : l'angle central entre les deux points (en radians)
        // atan2(y, x) calcule l'arc tangente de y/x en prenant en compte les quadrants
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Calcul final de la distance
        // Distance = Rayon de la Terre × Angle central
        // Retourne la distance en kilomètres
        return R * c;
    }
}