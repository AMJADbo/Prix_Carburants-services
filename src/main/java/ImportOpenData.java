import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.zip.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;

/**
 * Télécharge le flux instantané des prix carburants depuis le site gouvernemental,
 * dézippe le XML en mémoire et importe les données dans MySQL.
 *
 * Utilisé au démarrage du container Docker via start.sh.
 */
public class ImportOpenData {

    // Connexion BDD : variables d'environnement Docker ou valeurs locales par défaut
    static final String DB_URL  = System.getenv("DB_URL")  != null
            ? System.getenv("DB_URL")
            : "jdbc:mysql://localhost:3306/carburants?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Paris";
    static final String DB_USER = System.getenv("DB_USER") != null
            ? System.getenv("DB_USER") : "root";
    static final String DB_PASS = System.getenv("DB_PASS") != null
            ? System.getenv("DB_PASS") : "root";

    // URL du flux instantané gouvernemental (mis à jour toutes les ~10 minutes)
    static final String OPENDATA_URL = "https://donnees.roulez-eco.fr/opendata/instantane";

    public static void main(String[] args) throws Exception {

        System.out.println("================================================");
        System.out.println("  Import OpenData Prix Carburants");
        System.out.println("================================================");
        System.out.println("Téléchargement du fichier ZIP depuis :");
        System.out.println("  " + OPENDATA_URL);

        // ── 1. Téléchargement et dézippage en mémoire ────────
        InputStream xmlStream = null;
        try {
            URL url = new URL(OPENDATA_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(30_000);
            conn.setReadTimeout(60_000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            int status = conn.getResponseCode();
            if (status != 200) {
                System.err.println("ERREUR HTTP : " + status);
                System.exit(1);
            }

            ZipInputStream zip = new ZipInputStream(conn.getInputStream());
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.getName().endsWith(".xml")) {
                    System.out.println("Fichier XML trouvé dans le ZIP : " + entry.getName());
                    // Charge le XML en mémoire (évite de fermer le stream trop tôt)
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    byte[] chunk = new byte[8192];
                    int len;
                    while ((len = zip.read(chunk)) != -1) buffer.write(chunk, 0, len);
                    xmlStream = new ByteArrayInputStream(buffer.toByteArray());
                    break;
                }
            }
            zip.close();
        } catch (Exception e) {
            System.err.println("ERREUR lors du téléchargement : " + e.getMessage());
            System.exit(1);
        }

        if (xmlStream == null) {
            System.err.println("ERREUR : aucun fichier XML trouvé dans le ZIP.");
            System.exit(1);
        }

        System.out.println("Téléchargement OK. Parsing XML...");

        // ── 2. Parsing XML ───────────────────────────────────
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlStream);
        doc.getDocumentElement().normalize();

        // ── 3. Connexion MySQL ───────────────────────────────
        System.out.println("Connexion à MySQL...");
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        conn.setAutoCommit(false);

        // ── 4. Nettoyage des tables ──────────────────────────
        System.out.println("Nettoyage des tables...");
        conn.createStatement().executeUpdate("DELETE FROM horaires");
        conn.createStatement().executeUpdate("DELETE FROM prix");
        conn.createStatement().executeUpdate("DELETE FROM station");

        // ── 5. Préparation des requêtes ──────────────────────
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

        // ── 6. Import des stations ───────────────────────────
        NodeList pdvList = doc.getElementsByTagName("pdv");
        int total  = pdvList.getLength();
        int count  = 0;
        int errors = 0;

        System.out.println("Import de " + total + " stations...");

        for (int i = 0; i < total; i++) {
            try {
                Element pdv = (Element) pdvList.item(i);

                long   id        = Long.parseLong(pdv.getAttribute("id"));
                double latitude  = parseDoubleSafe(pdv.getAttribute("latitude"))  / 100000.0;
                double longitude = parseDoubleSafe(pdv.getAttribute("longitude")) / 100000.0;
                String cp        = pdv.getAttribute("cp");
                String ville     = getTagText(pdv, "ville");
                String adresse   = getTagText(pdv, "adresse");

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

                // Prix
                NodeList prixList = pdv.getElementsByTagName("prix");
                for (int j = 0; j < prixList.getLength(); j++) {
                    Element p      = (Element) prixList.item(j);
                    String  nom    = p.getAttribute("nom");
                    String  valStr = p.getAttribute("valeur");
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
                            psHoraire.setString(3, ouv.replace(".", ":"));
                            psHoraire.setString(4, fer.replace(".", ":"));
                            psHoraire.executeUpdate();
                        }
                    } else {
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
                    System.out.println("  -> " + count + " / " + total + " stations importées...");
                }

            } catch (Exception e) {
                errors++;
                if (errors <= 5) System.err.println("Erreur index " + i + " : " + e.getMessage());
            }
        }

        conn.commit();
        conn.close();

        System.out.println("================================================");
        System.out.println("Import terminé !");
        System.out.println("Stations importées : " + count);
        System.out.println("Erreurs ignorées   : " + errors);
        System.out.println("================================================");
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