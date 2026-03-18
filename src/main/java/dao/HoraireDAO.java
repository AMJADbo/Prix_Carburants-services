package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.horaire;
import util.DBConnection;

public class HoraireDAO {

    public List<horaire> findByStationId(long idStation) {

        List<horaire> horaires = new ArrayList<>();

        String sql = "SELECT * FROM horaires WHERE id_station = ?";

        try {
            Connection conn = DBConnection.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, idStation);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                horaire h = new horaire();

                h.setIdHoraire(rs.getLong("id_horaire"));
                h.setIdStation(rs.getLong("id_station"));
                h.setJour(rs.getInt("jour"));
                h.setOuverture(rs.getString("ouverture"));
                h.setFermeture(rs.getString("fermeture"));

                horaires.add(h);
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return horaires;
    }
}