FROM maven:3.9-eclipse-temurin-11 AS build

# Étape 1 : Compilation du projet Maven

WORKDIR /app
# Copie le fichier de configuration Maven dans le répertoire courant de l'image
COPY pom.xml .
# Télécharge les dépendances Maven en mode silencieux (-q)
# Continue même en cas d'erreur (|| true) pour optimiser le cache Docker
RUN mvn dependency:resolve -q || true
# Copie tout le code source de l'application dans le répertoire /src de l'image
COPY src/ ./src/
# Compile le projet, saute les tests et s'exécute en mode silencieux
RUN mvn clean package -DskipTests -q

# Compilation de ImportOpenData.java séparément
WORKDIR /import
# Copie le fichier Java d'import de données depuis les sources
COPY src/main/java/ImportOpenData.java .
# Copie le driver JDBC MySQL et le renomme en mysql-connector.jar
COPY src/main/webapp/WEB-INF/lib/mysql-connector-j-9.6.0.jar ./mysql-connector.jar
# Compile les deux fichiers dans le classpath puis crée un exécutable ImportOpenData comme classe principale
RUN javac -cp mysql-connector.jar ImportOpenData.java && \
    jar cfe ImportOpenData.jar ImportOpenData ImportOpenData.class

# --------------------------------------------------

#  Étape 2 : Image finale Tomcat 10 + Java 11
FROM tomcat:10.1-jdk11

# Installe le client MySQL (pour mysqladmin ping dans start.sh)
# Met à jour les paquets, installe MySQL, nettoie le cache pour réduire la taille de l'image
RUN apt-get update -q && \
    apt-get install -y --no-install-recommends default-mysql-client && \
    rm -rf /var/lib/apt/lists/*

# Supprime les apps par défaut de Tomcat
RUN rm -rf /usr/local/tomcat/webapps/*

# Copie le WAR de l'application
COPY --from=build /app/target/*.war \
     /usr/local/tomcat/webapps/Prix_Carburants-services.war

# Copie le JAR d'import et le driver MySQL
COPY --from=build /import/ImportOpenData.jar    /import/ImportOpenData.jar
COPY --from=build /import/mysql-connector.jar   /import/mysql-connector.jar

# Copie et rend exécutable le script de démarrage
COPY start.sh /start.sh
RUN chmod +x /start.sh

# Port par défaut de Tomcat
EXPOSE 8080

# Corrige les fins de ligne pour que le shell soit toujours exécutable
RUN sed -i 's/\r//' /start.sh
# Commande par défaut à exécuter au démarrage du conteneur
CMD ["/start.sh"]