// Le fichier HoraireDAO.java gère l'accès aux données de la table "horaires" (Data Access Object)

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.horaire;
import util.DBConnection;

public class HoraireDAO {

    // --------------------------------------------------
    // Méthode pour récupérer les horaires d'une station
    // --------------------------------------------------

    // Récupère tous les horaires d'une station spécifique
    public List<horaire> findByStationId(long idStation) {

        // Liste qui contiendra tous les horaires trouvés
        List<horaire> horaires = new ArrayList<>();

        // Requête SQL préparée pour sélectionner les horaires d'une station
        String sql = "SELECT * FROM horaires WHERE id_station = ?";

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
                // Crée un nouvel objet horaire
                horaire h = new horaire();

                // Remplit l'objet avec les données de la ligne courante
                h.setIdHoraire(rs.getLong("id_horaire"));
                h.setIdStation(rs.getLong("id_station"));
                h.setJour(rs.getInt("jour"));
                h.setOuverture(rs.getString("ouverture"));
                h.setFermeture(rs.getString("fermeture"));

                // Ajoute l'horaire à la liste
                horaires.add(h);
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

        // Retourne la liste des horaires (vide si aucun résultat ou si erreur)
        return horaires;
    }
}