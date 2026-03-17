package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import model.Price;
import util.DBConnection;

public class PriceDAO {

    public List<Price> findByStationId(long idStation) {

        List<Price> prices = new ArrayList<>();

        String sql = "SELECT * FROM prix WHERE id_station = ?";

        try {

            Connection conn = DBConnection.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, idStation);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Price price = new Price();

                price.setIdPrix(rs.getLong("id_prix"));
                price.setIdStation(rs.getLong("id_station"));
                price.setNomCarburant(rs.getString("nom_carburant"));
                price.setPrix(rs.getDouble("prix"));
                price.setDateMaj(rs.getString("date_maj"));

                prices.add(price);
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return prices;
    }
}