#!/bin/bash

# Script de démarrage du conteneur Docker
# Attend que MySQL soit prêt, importe les données OpenData, puis démarre Tomcat

# Active le mode "exit on error" : le script s'arrête dès qu'une commande échoue
set -e

# Message de démarrage
echo "================================================"
echo "  Démarrage de l'application Prix Carburants"
echo "================================================"

# 1. Attente MySQL
echo "Attente de MySQL..."
# Boucle jusqu'à ce que MySQL réponde au ping
until mysqladmin ping -h"$MYSQL_HOST" -u"$DB_USER" -p"$DB_PASS" --silent 2>/dev/null; do
    # Message d'attente
    echo "  MySQL pas encore prêt, nouvelle tentative dans 3 secondes..."
    # Pause de 3 secondes avant la prochaine tentative
    sleep 3
done
# MySQL est maintenant accessible
echo "MySQL prêt !"

# 2. Import des données OpenData
echo "Lancement de l'import OpenData..."
# Exécute le JAR d'impoirt avec le driver MySQL dans le classpath
java -cp "/import/ImportOpenData.jar:/import/mysql-connector.jar" ImportOpenData

# Vérifie le code de retour de la commande précédente
if [ $? -ne 0 ]; then
    # Si l'import échoue (code != 0), affihce un message mais continue
    echo "ERREUR : l'import a échoué. Tomcat démarrera quand même."
fi

# 3. Démarrage Tomcat
echo "Démarrage de Tomcat..."
# Lance Tomcat en mode foreground (remplace le processus bash actuel)
exec catalina.sh run