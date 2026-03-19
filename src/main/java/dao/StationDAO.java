// Le fichier StationDAO.java gère l'accès aux données de la table "station" (Data Access Object)

package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.Station;
import util.DBConnection;

public class StationDAO {

    // --------------------------------------------------
    // Méthode pour récupérer toutes les stations
    // --------------------------------------------------

    // Récupère toutes les stations de la base de données (sans filtre)
    public List<Station> findAllStations() {
        // Liste qui contiendra toutes les stations trouvées
        List<Station> stations = new ArrayList<>();

        // Requête SQL simple pour sélectionner toutes les stations
        String sql = "SELECT * FROM station";

        try {
            // Connexion à la base de données
            Connection conn = DBConnection.getConnection();

            // Préparation et exécution de la requête
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            // Parcours des résultats
            while (rs.next()) {
                // Crée un nouvel objet Station
                Station station = new Station();

                // Remplit l'objet avec les données de la ligne courante
                station.setIdStation(rs.getLong("id_station"));
                station.setLatitude(rs.getDouble("latitude"));
                station.setLongitude(rs.getDouble("longitude"));
                station.setAdresse(rs.getString("adresse"));
                station.setVille(rs.getString("ville"));
                station.setCp(rs.getString("cp"));
                station.setAutomate24h(rs.getBoolean("automate"));
                station.setLavage(rs.getBoolean("lavage"));
                station.setGonflage(rs.getBoolean("gonflage"));

                // Génère un nom d'affichage composé (ville + adresse)
                String nomAffiche = "Station " + rs.getString("ville") + " - " + rs.getString("adresse");
                station.setNomAffiche(nomAffiche);

                // Ajoute la station à la liste
                stations.add(station);
            }

            // Fermeture des ressources
            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            // En cas d'erreur, affiche la trace
            e.printStackTrace();
        }

        // Retourne la liste de toutes les stations
        return stations;
    }


    // --------------------------------------------------
    // Méthode pour récupérer les stations par ville
    // --------------------------------------------------

    // Récupère toutes les stations d'une ville spécifique
    public List<Station> findByVille(String ville) {
        // Liste qui contiendra les stations de cette ville
        List<Station> stations = new ArrayList<>();

        // Requête SQL avec filtre sur la ville
        String sql = "SELECT * FROM station WHERE ville = ?";

        try {
            // Connexion à la base de données
            Connection conn = DBConnection.getConnection();

            // Préparation de la requête SQL
            PreparedStatement ps = conn.prepareStatement(sql);
            // Remplace le "?" par le nom de la ville
            ps.setString(1, ville);

            // Exécution de la requête
            ResultSet rs = ps.executeQuery();

            // Parcours des résultats
            while (rs.next()) {
                // Crée un nouvel objet Station
                Station station = new Station();

                // Remplit l'objet avec les données
                station.setIdStation(rs.getLong("id_station"));
                station.setLatitude(rs.getDouble("latitude"));
                station.setLongitude(rs.getDouble("longitude"));
                station.setAdresse(rs.getString("adresse"));
                station.setVille(rs.getString("ville"));
                station.setCp(rs.getString("cp"));
                station.setAutomate24h(rs.getBoolean("automate"));
                station.setLavage(rs.getBoolean("lavage"));
                station.setGonflage(rs.getBoolean("gonflage"));

                // Génère un nom d'affichage
                String nomAffiche = "Station " + rs.getString("ville") + " - " + rs.getString("adresse");
                station.setNomAffiche(nomAffiche);

                // Ajoute la station à la liste
                stations.add(station);
            }

            // Fermeture des ressources
            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            // En cas d'erreur, affiche la trace
            e.printStackTrace();
        }

        // Retourne la liste des stations de cette ville
        return stations;
    }


    // --------------------------------------------------
    // Méthode pour récupérer une station par son ID
    // --------------------------------------------------

    // Récupère une station spécifique par son ID unique
    public Station findById(long idStation) {
        // Variable qui contiendra la station (null si non trouvée)
        Station station = null;

        // Requête SQL avec filtre sur l'ID
        String sql = "SELECT * FROM station WHERE id_station = ?";

        try {
            // Connexion à la base de données
            Connection conn = DBConnection.getConnection();

            // Préparation de la requête SQL
            PreparedStatement ps = conn.prepareStatement(sql);
            // Remplace le "?" par l'ID de la station
            ps.setLong(1, idStation);

            // Exécution de la requête
            ResultSet rs = ps.executeQuery();

            // Traitement du résultat (un seul résultat maximum car ID est unique)
            if (rs.next()) {
                // Crée un nouvel objet Station
                station = new Station();

                // Remplit l'objet avec les données
                station.setIdStation(rs.getLong("id_station"));
                station.setLatitude(rs.getDouble("latitude"));
                station.setLongitude(rs.getDouble("longitude"));
                station.setAdresse(rs.getString("adresse"));
                station.setVille(rs.getString("ville"));
                station.setCp(rs.getString("cp"));
                station.setAutomate24h(rs.getBoolean("automate"));
                station.setLavage(rs.getBoolean("lavage"));
                station.setGonflage(rs.getBoolean("gonflage"));

                // Génère un nom d'affichage
                String nomAffiche = "Station " + rs.getString("ville") + " - " + rs.getString("adresse");
                station.setNomAffiche(nomAffiche);
            }

            // Fermeture des ressources
            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            // En cas d'erreur, affiche la trace
            e.printStackTrace();
        }

        // Retourne la station (ou null si non trouvée)
        return station;
    }

    
    // --------------------------------------------------
    // Méthode pour récupérer les stations avec filtres dynamiques
    // --------------------------------------------------

    // Récupère les stations en appliquant des filtres optionnels (ville, lavage, gonflage, automate)
    public List<Station> findStationsWithFilters(String ville, Boolean lavage, Boolean gonflage, Boolean automate) {

        // Liste qui contiendra les stations filtrées
        List<Station> stations = new ArrayList<>();

        // Construction dynamique de la requête SQL
        StringBuilder sql = new StringBuilder("SELECT * FROM station WHERE 1=1");

        // Ajoute chaque filtre uniquement s'il est fourni (non null)
        if (ville != null && !ville.isEmpty()) {
            sql.append(" AND ville = ?");
        }

        if (lavage != null) {
            sql.append(" AND lavage = ?");
        }

        if (gonflage != null) {
            sql.append(" AND gonflage = ?");
        }

        if (automate != null) {
            sql.append(" AND automate = ?");
        }

        try {

            // Connexion à la base de données
            Connection conn = DBConnection.getConnection();

            // Préparation de la requête SQL dynamique
            PreparedStatement ps = conn.prepareStatement(sql.toString());

            // Injection des paramètres dans le PreparedStatement
            int index = 1;

            // Injecte les paramètres dans le même ordre que dans la requête SQL
            if (ville != null && !ville.isEmpty()) {
                ps.setString(index++, ville);
            }

            if (lavage != null) {
                ps.setBoolean(index++, lavage);
            }

            if (gonflage != null) {
                ps.setBoolean(index++, gonflage);
            }

            if (automate != null) {
                ps.setBoolean(index++, automate);
            }

            // Exécution de la requête
            ResultSet rs = ps.executeQuery();

            // Parcours des résultats
            while (rs.next()) {

                // Crée un nouvel objet Station
                Station station = new Station();

                // Remplit l'objet avec les données
                station.setIdStation(rs.getLong("id_station"));
                station.setLatitude(rs.getDouble("latitude"));
                station.setLongitude(rs.getDouble("longitude"));
                station.setAdresse(rs.getString("adresse"));
                station.setVille(rs.getString("ville"));
                station.setCp(rs.getString("cp"));
                station.setAutomate24h(rs.getBoolean("automate"));
                station.setLavage(rs.getBoolean("lavage"));
                station.setGonflage(rs.getBoolean("gonflage"));

                // Génère un nom d'affichage
                String nomAffiche = "Station " + rs.getString("ville") + " - " + rs.getString("adresse");
                station.setNomAffiche(nomAffiche);

                // Ajoute la station à la liste
                stations.add(station);
            }

            // Fermeture des ressources
            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            // En cas d'erreur, affiche la trace
            e.printStackTrace();
        }

        // Retourne la liste des stations filtrées
        return stations;
    }            
}