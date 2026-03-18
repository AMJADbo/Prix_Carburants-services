package util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    // En local (sans Docker) : utilise les valeurs hardcodées
    // En Docker : les variables d'environnement DB_URL / DB_USER / DB_PASS
    //             définies dans docker-compose.yml prennent le dessus
    private static final String URL  = System.getenv("DB_URL")  != null
            ? System.getenv("DB_URL")
            : "jdbc:mysql://localhost:3306/carburants?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Paris";

    private static final String USER = System.getenv("DB_USER") != null
            ? System.getenv("DB_USER")
            : "root";

    private static final String PASSWORD = System.getenv("DB_PASS") != null
            ? System.getenv("DB_PASS")
            : "root";

    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}