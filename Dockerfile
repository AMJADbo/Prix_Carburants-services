# ═══════════════════════════════════════════════════════════
#  Étape 1 : compilation du projet Maven
# ═══════════════════════════════════════════════════════════
FROM maven:3.9-eclipse-temurin-11 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:resolve -q || true
COPY src/ ./src/
RUN mvn clean package -DskipTests -q

# ─── Compilation de ImportOpenData.java séparément ─────────
WORKDIR /import
COPY src/main/java/ImportOpenData.java .
COPY src/main/webapp/WEB-INF/lib/mysql-connector-j-9.6.0.jar ./mysql-connector.jar
RUN javac -cp mysql-connector.jar ImportOpenData.java && \
    jar cfe ImportOpenData.jar ImportOpenData ImportOpenData.class

# ═══════════════════════════════════════════════════════════
#  Étape 2 : image finale Tomcat 10 + Java 11
# ═══════════════════════════════════════════════════════════
FROM tomcat:10.1-jdk11

# Installe le client MySQL (pour mysqladmin ping dans start.sh)
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

EXPOSE 8080

CMD ["/start.sh"]