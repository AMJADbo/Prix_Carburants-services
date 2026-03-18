import java.io.*;
import java.sql.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

public class ImportOpenData {

    static final String DB_URL  = "jdbc:mysql://localhost:3306/carburants";
    static final String DB_USER = "root";
    static final String DB_PASS = "root";

    // Chemin exact d'après tes captures
    static final String XML_PATH = "/Users/amjadbouicha/Desktop/opendata/PrixCarburants_instantane.xml";

    public static void main(String[] args) throws Exception {

        System.out.println("Lecture du fichier XML...");
        File xmlFile = new File(XML_PATH);

        if (!xmlFile.exists()) {
            System.err.println("ERREUR : fichier introuvable -> " + XML_PATH);
            return;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        System.out.println("Connexion a MySQL...");
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        conn.setAutoCommit(false);

        System.out.println("Nettoyage des tables...");
        conn.createStatement().executeUpdate("DELETE FROM horaires");
        conn.createStatement().executeUpdate("DELETE FROM prix");
        conn.createStatement().executeUpdate("DELETE FROM station");

        PreparedStatement psStation = conn.prepareStatement(
            "INSERT IGNORE INTO station " +
            "(id_station, latitude, longitude, adresse, ville, cp, automate, lavage, gonflage, nom_affiche) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?)"
        );
        PreparedStatement psPrix = conn.prepareStatement(
            "INSERT INTO prix (id_station, nom_carburant, prix, date_maj) VALUES (?,?,?,?)"
        );
        PreparedStatement psHoraire = conn.prepareStatement(
            "INSERT INTO horaires (id_station, jour, ouverture, fermeture) VALUES (?,?,?,?)"
        );

        NodeList pdvList = doc.getElementsByTagName("pdv");
        int total  = pdvList.getLength();
        int count  = 0;
        int errors = 0;

        System.out.println("Import de " + total + " stations...");

        for (int i = 0; i < total; i++) {
            try {
                Element pdv = (Element) pdvList.item(i);

                long   id        = Long.parseLong(pdv.getAttribute("id"));
                // Coordonnees en PTV_GEODECIMAL -> diviser par 100 000
                double latitude  = parseDoubleSafe(pdv.getAttribute("latitude"))  / 100000.0;
                double longitude = parseDoubleSafe(pdv.getAttribute("longitude")) / 100000.0;
                String cp        = pdv.getAttribute("cp");
                String ville     = getTagText(pdv, "ville");
                String adresse   = getTagText(pdv, "adresse");

                // Noms exacts des services dans le XML du gouvernement
                boolean gonflage = hasService(pdv, "Station de gonflage");
                boolean lavage   = hasService(pdv, "Lavage automatique")
                                || hasService(pdv, "Lavage manuel");
                boolean automate = isAutomate(pdv);

                psStation.setLong   (1, id);
                psStation.setDouble (2, latitude);
                psStation.setDouble (3, longitude);
                psStation.setString (4, adresse);
                psStation.setString (5, ville);
                psStation.setString (6, cp);
                psStation.setBoolean(7, automate);
                psStation.setBoolean(8, lavage);
                psStation.setBoolean(9, gonflage);
                psStation.setNull   (10, Types.VARCHAR);
                psStation.executeUpdate();

                // Prix : les valeurs sont DEJA en euros/litre (ex: 2.090)
                // PAS de division par 1000 !
                NodeList prixList = pdv.getElementsByTagName("prix");
                for (int j = 0; j < prixList.getLength(); j++) {
                    Element p       = (Element) prixList.item(j);
                    String  nom     = p.getAttribute("nom");
                    String  valStr  = p.getAttribute("valeur");
                    String  dateMaj = p.getAttribute("maj");
                    if (nom.isEmpty() || valStr.isEmpty()) continue;

                    double valeur = parseDoubleSafe(valStr.replace(",", "."));
                    psPrix.setLong  (1, id);
                    psPrix.setString(2, nom);
                    psPrix.setDouble(3, valeur);
                    psPrix.setString(4, dateMaj.isEmpty() ? null : dateMaj);
                    psPrix.executeUpdate();
                }

                // Horaires
                NodeList jourList = pdv.getElementsByTagName("jour");
                for (int j = 0; j < jourList.getLength(); j++) {
                    Element jour = (Element) jourList.item(j);
                    int numJour;
                    try { numJour = Integer.parseInt(jour.getAttribute("id")); }
                    catch (NumberFormatException e) { continue; }

                    // ferme="1" = station fermee ce jour
                    if ("1".equals(jour.getAttribute("ferme"))) continue;

                    NodeList horaires = jour.getElementsByTagName("horaire");
                    if (horaires.getLength() > 0) {
                        for (int k = 0; k < horaires.getLength(); k++) {
                            Element h   = (Element) horaires.item(k);
                            String  ouv = h.getAttribute("ouverture");
                            String  fer = h.getAttribute("fermeture");
                            if (ouv.isEmpty() || fer.isEmpty()) continue;
                            psHoraire.setLong  (1, id);
                            psHoraire.setInt   (2, numJour);
                            psHoraire.setString(3, ouv);
                            psHoraire.setString(4, fer);
                            psHoraire.executeUpdate();
                        }
                    } else {
                        // Pas d'heure precise : on note ouvert toute la journee
                        psHoraire.setLong  (1, id);
                        psHoraire.setInt   (2, numJour);
                        psHoraire.setString(3, "00:00");
                        psHoraire.setString(4, "23:59");
                        psHoraire.executeUpdate();
                    }
                }

                count++;
                if (count % 500 == 0) {
                    conn.commit();
                    System.out.println("  -> " + count + " / " + total + " stations importees...");
                }

            } catch (Exception e) {
                errors++;
                if (errors <= 5) System.err.println("Erreur index " + i + " : " + e.getMessage());
            }
        }

        conn.commit();
        conn.close();

        System.out.println("=========================================");
        System.out.println("Import termine !");
        System.out.println("Stations importees : " + count);
        System.out.println("Erreurs ignorees   : " + errors);
        System.out.println("=========================================");
        System.out.println("Va verifier dans phpMyAdmin puis lance Tomcat !");
    }

    static boolean isAutomate(Element pdv) {
        NodeList hn = pdv.getElementsByTagName("horaires");
        if (hn.getLength() > 0) {
            String val = ((Element) hn.item(0)).getAttribute("automate-24-24");
            return val != null && !val.isEmpty();
        }
        return false;
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

    static double parseDoubleSafe(String val) {
        if (val == null || val.trim().isEmpty()) return 0.0;
        try { return Double.parseDouble(val.trim()); }
        catch (NumberFormatException e) { return 0.0; }
    }
}