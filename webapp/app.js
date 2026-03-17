// Pour brancher le backend, il suffira de remplacer la fonction `window.mockData.getMockStations` par un appel à l'API réelle (ex: `fetch('/api/stations?...')`), et d'adapter le format des données si besoin.

// Fonction principale pour afficher les stations
function renderStations() {
  // Récupère la valeur sélectionnée pour le carburant
  const carburant = document.getElementById('filtre-carburant').value;
  // Récupère la consommation saisie
  const consommation = parseFloat(document.getElementById('filtre-conso').value);
  // Récupère le rayon saisi
  const rayon = parseFloat(document.getElementById('filtre-rayon').value);
  // Récupère la capacité totale du réservoir
  const reservoirTotal = parseFloat(document.getElementById('filtre-res-total').value);
  // Récupère le niveau courant du réservoir
  const reservoirCourant = parseFloat(document.getElementById('filtre-res-courant').value);
  // Vérifie si le filtre "station avec lavage" est coché
  const avecLavage = document.getElementById('filtre-lavage').checked;
  // Vérifie si le filtre "station avec gonflage" est coché
  const avecGonflage = document.getElementById('filtre-gonflage').checked;

  // Appelle la fonction mock pour obtenir la liste des stations filtrées
  const resultats = window.mockData.getMockStations({
    userLat: 48.866, userLng: 2.333, // ← à remplacer par la géoloc réelle
    carburant, rayon, consommation,
    reservoirTotal, reservoirCourant,
    avecLavage, avecGonflage,
  });

  // Met à jour le compteur de résultats
  document.getElementById('results-count').innerHTML =
    `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-send" viewBox="0 0 16 16" style="vertical-align:middle;margin-right:6px;">
    <path d="M15.854.146a.5.5 0 0 1 .11.54l-5.819 14.547a.75.75 0 0 1-1.329.124l-3.178-4.995L.643 7.184a.75.75 0 0 1 .124-1.33L15.314.037a.5.5 0 0 1 .54.11ZM6.636 10.07l2.761 4.338L14.13 2.576zm6.787-8.201L1.591 6.602l4.339 2.76z"/>
  </svg>
  Stations à proximité (${resultats.length})`;

  // Rendu HTML des cartes de stations
  const viewListe = document.getElementById('view-liste');
  viewListe.innerHTML = resultats.length === 0
    // Affiche un message si aucune station trouvée
    ? '<p class="placeholder">Aucune station trouvée.</p>'
    // Sinon, génère le HTML pour chaque station
    : resultats.map(s => `
      <div class="station-card">
        <div class="card-top">
          <div class="rank">${s.rang}</div>
          <div class="station-name">
            ${s.adresse}, ${s.ville}
            ${s.rang === 1 ? '<span class="badge-best">Meilleur choix</span>' : ''}
          </div>
        </div>
        <div class="card-price-row">
          <div class="card-price">${s.prix_carburant.toFixed(3)} €/L</div>
          <div class="card-distance">
          <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" fill="currentColor" class="bi bi-send" viewBox="0 0 16 16">
            <path d="M15.854.146a.5.5 0 0 1 .11.54l-5.819 14.547a.75.75 0 0 1-1.329.124l-3.178-4.995L.643 7.184a.75.75 0 0 1 .124-1.33L15.314.037a.5.5 0 0 1 .54.11ZM6.636 10.07l2.761 4.338L14.13 2.576zm6.787-8.201L1.591 6.602l4.339 2.76z"/>
          </svg>
          ${s.distance} km</div>
        </div>
        <div class="card-update">
        <svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" fill="currentColor" class="bi bi-clock" viewBox="0 0 16 16">
            <path d="M8 3.5a.5.5 0 0 0-1 0V9a.5.5 0 0 0 .252.434l3.5 2a.5.5 0 0 0 .496-.868L8 8.71z"/>
            <path d="M8 16A8 8 0 1 0 8 0a8 8 0 0 0 0 16m7-8A7 7 0 1 1 1 8a7 7 0 0 1 14 0"/>
        </svg>
          ${getUpdateText(s.date_maj)}
        </div>
        <div class="card-footer">
          <span class="card-total">Coût total estimé : ${s.cout_total.toFixed(2)} €</span>
          <div class="card-services">
            ${s.lavage ? '<span class="service-tag"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-water" viewBox="0 0 16 16" style="vertical-align:middle;margin-right:3px;"><path d="M.036 3.314a.5.5 0 0 1 .65-.278l1.757.703a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.757-.703a.5.5 0 1 1 .372.928l-1.758.703a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0L.314 3.964a.5.5 0 0 1-.278-.65m0 3a.5.5 0 0 1 .65-.278l1.757.703a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.757-.703a.5.5 0 1 1 .372.928l-1.758.703a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0L.314 6.964a.5.5 0 0 1-.278-.65m0 3a.5.5 0 0 1 .65-.278l1.757.703a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.757-.703a.5.5 0 1 1 .372.928l-1.758.703a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.757-.703a.5.5 0 0 1-.278-.65m0 3a.5.5 0 0 1 .65-.278l1.757.703a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.757-.703a.5.5 0 1 1 .372.928l-1.758.703a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.757-.703a.5.5 0 0 1-.278-.65"/></svg> Lavage</span>' : ''}
            ${s.gonflage ? '<span class="service-tag"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-wind" viewBox="0 0 16 16"><path d="M12.5 2A2.5 2.5 0 0 0 10 4.5a.5.5 0 0 1-1 0A3.5 3.5 0 1 1 12.5 8H.5a.5.5 0 0 1 0-1h12a2.5 2.5 0 0 0 0-5m-7 1a1 1 0 0 0-1 1 .5.5 0 0 1-1 0 2 2 0 1 1 2 2h-5a.5.5 0 0 1 0-1h5a1 1 0 0 0 0-2M0 9.5A.5.5 0 0 1 .5 9h10.042a3 3 0 1 1-3 3 .5.5 0 0 1 1 0 2 2 0 1 0 2-2H.5a.5.5 0 0 1-.5-.5"/></svg> Gonflage</span>' : ''}

          </div>
        </div>
      </div>
    `).join('');
}

// Lancer au chargement et à chaque changement de filtre
// (DOMContentLoaded = page chargée)
document.addEventListener('DOMContentLoaded', () => {
  renderStations(); // Affiche les stations au chargement
  // Ajoute un listener sur tous les inputs/selects pour relancer le rendu à chaque modification
  document.querySelectorAll('#card-filters input, #card-filters select')
    .forEach(el => el.addEventListener('change', renderStations));
});

// Fonction utilitaire pour afficher le texte "mise à jour il y a x heures"
function getUpdateText(dateStr) {
  const dateMaj = new Date(dateStr);
  const now = new Date();
  const diffMs = now - dateMaj;
  const diffH = Math.floor(diffMs / (1000 * 60 * 60));
  const diffD = Math.floor(diffH / 24);
  if (diffD >= 1) {
    if (diffD === 1) return "Mis à jour il y a environ 1 jour";
    return `Mis à jour il y a environ ${diffD} jours`;
  }
  if (diffH < 1) return "Mis à jour il y a moins d'1 heure";
  if (diffH === 1) return "Mis à jour il y a environ 1 heure";
  return `Mis à jour il y a environ ${diffH} heures`;
}