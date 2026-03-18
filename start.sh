#!/bin/bash
# ============================================================
#  Script de démarrage du container
#  1. Attend que MySQL soit prêt
#  2. Lance l'import des données OpenData
#  3. Démarre Tomcat
# ============================================================

set -e

echo "================================================"
echo "  Démarrage de l'application Prix Carburants"
echo "================================================"

# ── 1. Attente MySQL ─────────────────────────────────────
echo "Attente de MySQL..."
until mysqladmin ping -h"$MYSQL_HOST" -u"$DB_USER" -p"$DB_PASS" --silent 2>/dev/null; do
    echo "  MySQL pas encore prêt, nouvelle tentative dans 3 secondes..."
    sleep 3
done
echo "MySQL prêt !"

# ── 2. Import des données OpenData ──────────────────────
echo "Lancement de l'import OpenData..."
java -cp "/import/ImportOpenData.jar:/import/mysql-connector.jar" ImportOpenData

if [ $? -ne 0 ]; then
    echo "ERREUR : l'import a échoué. Tomcat démarrera quand même."
fi

# ── 3. Démarrage Tomcat ──────────────────────────────────
echo "Démarrage de Tomcat..."
exec catalina.sh run