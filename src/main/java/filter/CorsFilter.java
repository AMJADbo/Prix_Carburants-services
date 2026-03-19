// Le fichier CorsFilter.java est un filtre Servlet qui ajoute les en-têtes CORS pour permettre les appels API cross-origin

package filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Filtre CORS — à placer dans src/main/java/filter/CorsFilter.java
 * Permet au frontend (servi sur un port différent) d'appeler l'API Tomcat.
 */
@WebFilter("/*")
public class CorsFilter implements Filter {

    // --------------------------------------------------
    // Méthode d'initialisation
    // --------------------------------------------------

    @Override
    // Appelée une fois au démarrage du serveur (initialisation du filtre)
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    // --------------------------------------------------
    // Méthode principale : ajout des en-têtes cors
    // --------------------------------------------------

    @Override
    // Appelée AVANT chaque requête HTTP pour ajouter les en-têtes CORS
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Cast de la réponse générique en HttpServletResponse pour accéder aux en-têtes HTTP
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Autorise toutes les origines (à restreindre en production)
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}