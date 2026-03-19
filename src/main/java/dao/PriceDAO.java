// Le fichier PriceDAO.java gère l'accès aux données de la table "prix" (Data Access Object)
package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.Price;
import util.DBConnection;

public class PriceDAO {

    // --------------------------------------------------
    // Méthode pour récupérer tous les prix d'une station
    // --------------------------------------------------

    // Récupère tous les prix de carburants d'une station spécifique
    public List<Price> findByStationId(long idStation) {

        // Liste qui contiendra tous les prix trouvés
        List<Price> prices = new ArrayList<>();

        // Requête SQL préparée pour sélectionner les prix d'une station
        // Le "?" sera remplacé par l'ID de la station (protection contre les injections SQL)
        String sql = "SELECT * FROM prix WHERE id_station = ?";

        try {
            // Connexion à la base de données
            Connection conn = DBConnection.getConnection();

            // Préparation de la requête SQL
            PreparedStatement ps = conn.prepareStatement(sql);
            // Remplace le premier "?" par l'ID de la station
            ps.setLong(1, idStation);

            // Exécution de la requête
            ResultSet rs = ps.executeQuery();

            // Parcours des résultats
            while (rs.next()) {

                // Crée un nouvel objet Price
                Price price = new Price();

                // Remplit l'objet avec les données de la ligne courante
                price.setIdPrix(rs.getLong("id_prix"));
                price.setIdStation(rs.getLong("id_station"));
                price.setNomCarburant(rs.getString("nom_carburant"));
                price.setPrix(rs.getDouble("prix"));
                price.setDateMaj(rs.getString("date_maj"));

                // Ajoute le prix à la liste
                prices.add(price);
            }

            // Fermeture des ressources
            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            // En cas d'erreur (connexion échouée, SQL invalide, etc.)
            // Affiche la trace complète de l'erreur dans la console
            e.printStackTrace();
        }

        // Retourne la liste des prix (vide si aucun résultat ou si erreur)
        return prices;
    }

    // --------------------------------------------------
    // Méthode pour trouver le prix le moins cher par ville et carburant
    // --------------------------------------------------
    
    // Trouve le prix le moins cher pour un type de carburant dans une ville donnée
    public Price findCheapestPriceByVilleAndCarburant(String ville, String carburant) {

        // Variable qui contiendra le prix le moins cher (null si aucun résultat)
        Price price = null;

        // Requête SQL avec jointure entre les tables "prix" et "station"
        // ORDER BY p.prix ASC : trie par prix croissant (du moins cher au plus cher)
        // LIMIT 1 : ne retourne que le premier résultat (le moins cher)
        String sql = "SELECT p.id_prix, p.id_station, p.nom_carburant, p.prix, p.date_maj "
                + "FROM prix p "
                + "JOIN station s ON p.id_station = s.id_station "
                + "WHERE s.ville = ? AND p.nom_carburant = ? "
                + "ORDER BY p.prix ASC "
                + "LIMIT 1";

        try {
            // Connexion à la base de données
            Connection conn = DBConnection.getConnection();

            // Préparation de la requête SQL
            PreparedStatement ps = conn.prepareStatement(sql);
            // Remplace le premier "?" par le nom de la ville
            ps.setString(1, ville);
            // Remplace le deuxième "?" par le type de carburant
            ps.setString(2, carburant);

            // Exécution de la requête
            ResultSet rs = ps.executeQuery();

            // Traitement du résultat (un seul résultat maximum grâce à LIMIT 1)
            // Si un résultat est trouvé
            if (rs.next()) {
                // Crée un objet Price
                price = new Price();

                // Remplit l'objet avec les données du prix le moins cher
                price.setIdPrix(rs.getLong("id_prix"));
                price.setIdStation(rs.getLong("id_station"));
                price.setNomCarburant(rs.getString("nom_carburant"));
                price.setPrix(rs.getDouble("prix"));
                price.setDateMaj(rs.getString("date_maj"));
            }

            // Fermeture des ressources
            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            // En cas d'erreur, affiche la trace
            e.printStackTrace();
        }

        // Retourne le prix le moins cher (ou null si aucun résultat)
        return price;
    }
}
