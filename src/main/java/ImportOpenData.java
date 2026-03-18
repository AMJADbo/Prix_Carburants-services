import java.io.*;
import java.sql.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class ImportOpenData {

    static final String URL  = "jdbc:mysql://localhost:3306/carburants";
    static final String USER = "root";
    static final String PASS = "";

    public static void main(String[] args) throws Exception {

        // ← change ce chemin vers ton fichier XML dézippé
        File xmlFile = new File("PrixCarburants_instantane.xml");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(URL, USER, PASS);
        conn.setAutoCommit(false);

        // Vide les tables avant import
        conn.createStatement().executeUpdate("DELETE FROM horaires");
        conn.createStatement().executeUpdate("DELETE FROM prix");
        conn.createStatement().executeUpdate("DELETE FROM station");

        PreparedStatement psStation = conn.prepareStatement(
            "INSERT IGNORE INTO station VALUES (?,?,?,?,?,?,?,?,?,?)"
        );
        PreparedStatement psPrix = conn.prepareStatement(
            "INSERT INTO prix (id_station,nom_carburant,prix,date_maj) VALUES (?,?,?,?)"
        );
        PreparedStatement psHoraire = conn.prepareStatement(
            "INSERT INTO horaires (id_station,jour,ouverture,fermeture) VALUES (?,?,?,?)"
        );

        NodeList pdvList = doc.getElementsByTagName("pdv");
        int count = 0;

        for (int i = 0; i < pdvList.getLength(); i++) {
            Element pdv = (Element) pdvList.item(i);

            long   id        = Long.parseLong(pdv.getAttribute("id"));
            // ← Division par 100 000 pour convertir en degrés décimaux
            double latitude  = Double.parseDouble(pdv.getAttribute("latitude"))  / 100000.0;
            double longitude = Double.parseDouble(pdv.getAttribute("longitude")) / 100000.0;
            String cp        = pdv.getAttribute("cp");
            String ville     = getTagText(pdv, "ville");
            String adresse   = getTagText(pdv, "adresse");

            // Services
            boolean lavage   = hasService(pdv, "Lavage automatique");
            boolean gonflage = hasService(pdv, "Gonflage pneus");
            boolean automate = hasService(pdv, "Automate CB 24/24");

            psStation.setLong   (1, id);
            psStation.setDouble (2, latitude);
            psStation.setDouble (3, longitude);
            psStation.setString (4, adresse);
            psStation.setString (5, ville);
            psStation.setString (6, cp);
            psStation.setBoolean(7, automate);
            psStation.setBoolean(8, lavage);
            psStation.setBoolean(9, gonflage);
            psStation.setString (10, null);
            psStation.executeUpdate();

            // Prix
            NodeList prixList = pdv.getElementsByTagName("prix");
            for (int j = 0; j < prixList.getLength(); j++) {
                Element p = (Element) prixList.item(j);
                String nom   = p.getAttribute("nom");
                String valStr = p.getAttribute("valeur");
                String dateMaj = p.getAttribute("maj");
                if (nom.isEmpty() || valStr.isEmpty()) continue;
                double valeur = Double.parseDouble(valStr.replace(",", ".")) / 1000.0;

                psPrix.setLong  (1, id);
                psPrix.setString(2, nom);
                psPrix.setDouble(3, valeur);
                psPrix.setString(4, dateMaj);
                psPrix.executeUpdate();
            }

            // Horaires
            NodeList jourList = pdv.getElementsByTagName("jour");
            for (int j = 0; j < jourList.getLength(); j++) {
                Element jour = (Element) jourList.item(j);
                int numJour = Integer.parseInt(jour.getAttribute("id"));
                NodeList horaires = jour.getElementsByTagName("horaire");
                for (int k = 0; k < horaires.getLength(); k++) {
                    Element h = (Element) horaires.item(k);
                    String ouv = h.getAttribute("ouverture");
                    String fer = h.getAttribute("fermeture");
                    psHoraire.setLong  (1, id);
                    psHoraire.setInt   (2, numJour);
                    psHoraire.setString(3, ouv);
                    psHoraire.setString(4, fer);
                    psHoraire.executeUpdate();
                }
            }

            count++;
            if (count % 500 == 0) {
                conn.commit();
                System.out.println(count + " stations importées...");
            }
        }

        conn.commit();
        System.out.println("Import terminé : " + count + " stations.");
        conn.close();
    }

    static String getTagText(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() > 0) return list.item(0).getTextContent().trim();
        return "";
    }

    static boolean hasService(Element pdv, String nom) {
        NodeList services = pdv.getElementsByTagName("service");
        for (int i = 0; i < services.getLength(); i++) {
            if (services.item(i).getTextContent().trim().equals(nom)) return true;
        }
        return false;
    }
}