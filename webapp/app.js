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
            ${s.lavage ? '<span class="service-tag"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-water" viewBox="0 0 16 16" style="vertical-align:middle;margin-right:3px;"><path d="M.036 3.314a.5.5 0 0 1 .65-.278l1.757.703a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.757-.703a.5.5 0 1 1 .372.928l-1.758.703a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0L.314 3.964a.5.5 0 0 1-.278-.65m0 3a.5.5 0 0 1 .65-.278l1.757.703a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.757-.703a.5.5 0 1 1 .372.928l-1.758.703a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.757-.703a.5.5 0 0 1-.278-.65m0 3a.5.5 0 0 1 .65-.278l1.757.703a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.757-.703a.5.5 0 1 1 .372.928l-1.758.703a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.757-.703a.5.5 0 0 1-.278-.65"/></svg> Lavage</span>' : ''}
            ${s.gonflage ? '<span class="service-tag"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-wind" viewBox="0 0 16 16"><path d="M12.5 2A2.5 2.5 0 0 0 10 4.5a.5.5 0 0 1-1 0A3.5 3.5 0 1 1 12.5 8H.5a.5.5 0 0 1 0-1h12a2.5 2.5 0 0 0 0-5m-7 1a1 1 0 0 0-1 1 .5.5 0 0 1-1 0 2 2 0 1 1 2 2h-5a.5.5 0 0 1 0-1h5a1 1 0 0 0 0-2M0 9.5A.5.5 0 0 1 .5 9h10.042a3 3 0 1 1-3 3 .5.5 0 0 1 1 0 2 2 0 1 0 2-2H.5a.5.5 0 0 1-.5-.5"/></svg> Gonflage</span>' : ''}

          </div>
        </div>
      </div>
    `).join('');

  // Met à jour la carte si elle existe
  if (document.getElementById('map')) {
    renderStationsOnMap(resultats);
  }
}

// Fonction pour afficher les stations sur la carte Leaflet
function renderStationsOnMap(stations) {
  // Si la carte n'est pas déjà initialisée, on la crée et on la stocke dans window._leafletMap
  if (!window._leafletMap) {
    window._leafletMap = L.map('map').setView([48.866, 2.333], 12);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(window._leafletMap);
    // Attend 100ms pour laisser le CSS finir, puis recalcule la taille
    setTimeout(() => {
      window._leafletMap.invalidateSize();
    }, 100);
  }
  const map = window._leafletMap;

  // Supprime les anciens marqueurs si présents
  if (window._leafletMarkers) {
    window._leafletMarkers.forEach(m => map.removeLayer(m));
  }
  window._leafletMarkers = [];

  // Ajoute le pin bleu pour la position géolocalisée de l'utilisateur
  if (window._leafletUserMarker) {
    map.removeLayer(window._leafletUserMarker);
  }
  // Pin bleu Leaflet par défaut pour l'utilisateur
  window._leafletUserMarker = L.marker([48.866, 2.333], {
    icon: L.icon({
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
      shadowSize: [41, 41]
    })
  }).addTo(map);

  // Ajoute un triangle vert inversé pour chaque station + rectangle d'information
  stations.forEach(station => {
    // Triangle vert inversé
    const triangleIcon = L.divIcon({
      className: 'station-triangle',
      html: `<svg width='24' height='24' viewBox='0 0 24 24' style='display:block'><polygon points='12,20 4,4 20,4' fill='#00c950'/></svg>`,
      iconSize: [24, 24],
      iconAnchor: [12, 20], // Pointe du triangle
    });
    L.marker([station.latitude, station.longitude], { icon: triangleIcon }).addTo(map);

    // Rectangle d'information au-dessus
    const label = L.divIcon({
      className: 'station-label',
      html: `<div>${station.adresse}<br><span>${station.prix_carburant.toFixed(3)} €/L</span></div>`,
      iconSize: [120, 40],
      iconAnchor: [60, 90], // Position du label au-dessus du triangle
    });
    L.marker([station.latitude, station.longitude], { icon: label }).addTo(map);
  });

  // Centre la carte sur la première station si elle existe
  if (stations.length > 0) {
    map.setView([stations[0].latitude, stations[0].longitude], 12);
  }

  // Attend 100ms pour laisser le CSS finir, puis recalcule la taille
  setTimeout(() => {
    map.invalidateSize();
  }, 100);
}

// Ajoute un observer pour recalculer la carte quand #map devient visible
function observeMapVisibility() {
  const mapDiv = document.getElementById('map');
  if (!mapDiv) return;
  const observer = new MutationObserver(() => {
    if (!mapDiv.classList.contains('hidden') && window._leafletMap) {
      setTimeout(() => {
        window._leafletMap.invalidateSize();
      }, 100);
    }
  });
  observer.observe(mapDiv.parentElement, { attributes: true, attributeFilter: ['class'] });
}

// Lancer au chargement et à chaque changement de filtre
// (DOMContentLoaded = page chargée)
document.addEventListener('DOMContentLoaded', () => {
  renderStations(); // Affiche les stations au chargement
  // Ajoute un listener sur tous les inputs/selects pour relancer le rendu à chaque modification
  document.querySelectorAll('#card-filters input, #card-filters select')
    .forEach(el => el.addEventListener('change', renderStations));
  // Lance l'observation de la visibilité de la carte
  observeMapVisibility();
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