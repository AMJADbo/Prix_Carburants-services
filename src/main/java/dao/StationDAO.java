package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.Station;
import util.DBConnection;

public class StationDAO {

    public List<Station> findAllStations() {
        List<Station> stations = new ArrayList<>();

        String sql = "SELECT * FROM station";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Station station = new Station();

                station.setIdStation(rs.getLong("id_station"));
                station.setLatitude(rs.getDouble("latitude"));
                station.setLongitude(rs.getDouble("longitude"));
                station.setAdresse(rs.getString("adresse"));
                station.setVille(rs.getString("ville"));
                station.setCp(rs.getString("cp"));
                station.setAutomate24h(rs.getBoolean("automate"));
                station.setLavage(rs.getBoolean("lavage"));
                station.setGonflage(rs.getBoolean("gonflage"));

                String nomAffiche = "Station " + rs.getString("ville") + " - " + rs.getString("adresse");
                station.setNomAffiche(nomAffiche);

                stations.add(station);
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return stations;
    }

    public List<Station> findByVille(String ville) {
        List<Station> stations = new ArrayList<>();

        String sql = "SELECT * FROM station WHERE ville = ?";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, ville);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Station station = new Station();

                station.setIdStation(rs.getLong("id_station"));
                station.setLatitude(rs.getDouble("latitude"));
                station.setLongitude(rs.getDouble("longitude"));
                station.setAdresse(rs.getString("adresse"));
                station.setVille(rs.getString("ville"));
                station.setCp(rs.getString("cp"));
                station.setAutomate24h(rs.getBoolean("automate"));
                station.setLavage(rs.getBoolean("lavage"));
                station.setGonflage(rs.getBoolean("gonflage"));

                String nomAffiche = "Station " + rs.getString("ville") + " - " + rs.getString("adresse");
                station.setNomAffiche(nomAffiche);

                stations.add(station);
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return stations;
    }
    public Station findById(long idStation) {
        Station station = null;

        String sql = "SELECT * FROM station WHERE id_station = ?";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, idStation);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                station = new Station();

                station.setIdStation(rs.getLong("id_station"));
                station.setLatitude(rs.getDouble("latitude"));
                station.setLongitude(rs.getDouble("longitude"));
                station.setAdresse(rs.getString("adresse"));
                station.setVille(rs.getString("ville"));
                station.setCp(rs.getString("cp"));
                station.setAutomate24h(rs.getBoolean("automate"));
                station.setLavage(rs.getBoolean("lavage"));
                station.setGonflage(rs.getBoolean("gonflage"));

                String nomAffiche = "Station " + rs.getString("ville") + " - " + rs.getString("adresse");
                station.setNomAffiche(nomAffiche);
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return station;
    }
    public List<Station> findStationsWithFilters(String ville, Boolean lavage, Boolean gonflage, Boolean automate) {

        List<Station> stations = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT * FROM station WHERE 1=1");

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

            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql.toString());

            int index = 1;

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

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Station station = new Station();

                station.setIdStation(rs.getLong("id_station"));
                station.setLatitude(rs.getDouble("latitude"));
                station.setLongitude(rs.getDouble("longitude"));
                station.setAdresse(rs.getString("adresse"));
                station.setVille(rs.getString("ville"));
                station.setCp(rs.getString("cp"));
                station.setAutomate24h(rs.getBoolean("automate"));
                station.setLavage(rs.getBoolean("lavage"));
                station.setGonflage(rs.getBoolean("gonflage"));

                String nomAffiche = "Station " + rs.getString("ville") + " - " + rs.getString("adresse");
                station.setNomAffiche(nomAffiche);

                stations.add(station);
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return stations;
    }
   

            
}