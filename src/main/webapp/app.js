// Le fichier app.js gère l'affichage dynamique des stations-service : géolocalisation, appels API, rendu de la liste et de la carte


// URL de base de l'API backend
const API_BASE = 'http://localhost:8080/Prix_Carburants-services';

// Position de l'utilisateur (mise à jour par la géolocalisation)
let userLat = null;
let userLng = null;

// --------------------------------------------------
// Géolocalisation
// --------------------------------------------------

// Récupère la position GPS de l'utilisateur via l'API Geolocation du navigateur
function getPosition() {
  // Retourne une Promise pour gérer l'asynchrone
  return new Promise((resolve, reject) => {
    // Vérifie si le navigateur supporte la géolocalisation
    if (!navigator.geolocation) {
      reject(new Error("Géolocalisation non supportée par ce navigateur."));
      return;
    }
    // Demande la position actuelle au navigateur
    navigator.geolocation.getCurrentPosition(
      // Callback en cas de succès : extrait latitute et longitude
      pos => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
      // Callback en cas d'erreur
      err => reject(err),
      // Options : timeout de 8 secondes max
      { timeout: 8000 }
    );
  });
}

// --------------------------------------------------
// Appel API
// --------------------------------------------------

// Appelle l'API backend pour récupérer les stations proches de l'utilisateur
// Prend en compte le filtres
async function fetchStations({ lat, lng, carburant, rayon, conso, resTotal, resCourant, avecLavage, avecGonflage }) {
  // Paramètres de requête GET
  const params = new URLSearchParams({
    lat: lat,
    lon: lng,
    radius: rayon,
    carburant: carburant,
    conso: conso,
    resTotal: resTotal,
    resCourant: resCourant,
  });

  // Filtres ajoutés seulement si cochés
  if (avecLavage) params.set('lavage', 'true');
  if (avecGonflage) params.set('gonflage', 'true');

  // Appel HTTP GET vers l'API backend
  const res = await fetch(`${API_BASE}/api/stations/near?${params.toString()}`);
  // Vérifie si la réponse est OK
  if (!res.ok) throw new Error(`Erreur serveur : ${res.status}`);
  // Return le tableau de stations JSON
  return res.json();
}

// --------------------------------------------------
// Rendu principal
// --------------------------------------------------

// Charge et affiche la liste des stations en fonction des filtres
async function renderStations() {
  // Récupère les valeurs des filtres depuis les éléments HTML
  const carburant = document.getElementById('filtre-carburant').value;
  const conso = parseFloat(document.getElementById('filtre-conso').value);
  const rayon = parseFloat(document.getElementById('filtre-rayon').value);
  const resTotal = parseFloat(document.getElementById('filtre-res-total').value);
  const resCourant = parseFloat(document.getElementById('filtre-res-courant').value);
  const avecLavage = document.getElementById('filtre-lavage').checked;
  const avecGonflage = document.getElementById('filtre-gonflage').checked;

  // Sélectionne la zone d'affichage de la liste
  const viewListe = document.getElementById('view-liste');
  // Affiche un message de chargement
  viewListe.innerHTML = '<p class="placeholder">Chargement en cours…</p>';

  try {
    // Récupère la position si on ne l'a pas encore
    if (userLat === null) {
      try {
        // Essaye de récupérer la position réelle de l'utilisateur
        const pos = await getPosition();
        userLat = pos.lat;
        userLng = pos.lng;
      } catch (e) {
        // En cas d'échec : position par défaut : Paris
        userLat = 48.866;
        userLng = 2.333;
        console.warn("Géolocalisation échouée, position par défaut utilisée.", e.message);
      }
    }

    // Appelle l'API pour récupérer les stations
    const resultats = await fetchStations({
      lat: userLat, lng: userLng,
      carburant, rayon, conso, resTotal, resCourant,
      avecLavage, avecGonflage,
    });

    // Structure des données retournées par l'API (camelcase)
    // idStation, latitude, longitude, adresse, ville, cp,
    // automate, lavage, gonflage, nomAffiche,
    // prixCarburant, dateMaj, nomCarburant, coutTotal, distance, rang

    // Met à jour le compteur des stations utilisé dans la bannière
    window.currentStationCount = resultats.length;
    // Met à jour la baniière
    if (typeof window.updateBanner === 'function') window.updateBanner();

    // Affiche les résultats (message si vide)
    viewListe.innerHTML = resultats.length === 0
      ? '<p class="placeholder">Aucune station trouvée.</p>'
      : resultats.map(s => `
        <div class="station-card">
          <div class="card-top">
            <div class="rank">${s.rang ?? '-'}</div>
            <div class="station-name">
              ${escapeHtml(s.adresse)}, ${escapeHtml(s.ville)}
              ${s.rang === 1 ? '<span class="badge-best">Meilleur choix</span>' : ''}
            </div>
          </div>
          <div class="card-price-row">
            <div class="card-price">
              ${s.prixCarburant !== null ? Number(s.prixCarburant).toFixed(3) + ' €/L' : 'Prix indisponible'}
            </div>
            <div class="card-distance">
              <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" fill="currentColor" class="bi bi-cursor" viewBox="0 0 16 16">
                <path d="M14.082 2.182a.5.5 0 0 1 .103.557L8.528 15.467a.5.5 0 0 1-.917-.007L5.57 10.694.803 8.652a.5.5 0 0 1-.006-.916l12.728-5.657a.5.5 0 0 1 .556.103zM2.25 8.184l3.897 1.67a.5.5 0 0 1 .262.263l1.67 3.897L12.743 3.52z"/>
              </svg>
              ${s.distance} km
            </div>
          </div>
          <div class="card-update">
            <svg xmlns="http://www.w3.org/2000/svg" width="10" height="10" fill="currentColor" class="bi bi-clock" viewBox="0 0 16 16">
              <path d="M8 3.5a.5.5 0 0 0-1 0V9a.5.5 0 0 0 .252.434l3.5 2a.5.5 0 0 0 .496-.868L8 8.71z"/>
              <path d="M8 16A8 8 0 1 0 8 0a8 8 0 0 0 0 16m7-8A7 7 0 1 1 1 8a7 7 0 0 1 14 0"/>
            </svg>
            ${s.dateMaj ? getUpdateText(s.dateMaj) : 'Date inconnue'}
          </div>
          <div class="card-footer">
            <span class="card-total">Coût total estimé : ${s.coutTotal != null ? Number(s.coutTotal).toFixed(2) + ' €' : 'Non calculé'}</span>
            <div class="card-services">
              ${s.lavage ? '<span class="service-tag"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-water" viewBox="0 0 16 16" style="vertical-align:middle;margin-right:3px;"><path d="M.036 3.314a.5.5 0 0 1 .65-.278l1.757.703a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.757-.703a.5.5 0 1 1 .372.928l-1.758.703a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0L.314 3.964a.5.5 0 0 1-.278-.65m0 3a.5.5 0 0 1 .65-.278l1.757.703a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.757-.703a.5.5 0 1 1 .372.928l-1.758.703a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.757-.703a.5.5 0 0 1-.278-.65"/></svg> Lavage</span>' : ''}
              ${s.gonflage ? '<span class="service-tag"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-wind" viewBox="0 0 16 16"><path d="M12.5 2A2.5 2.5 0 0 0 10 4.5a.5.5 0 0 1-1 0A3.5 3.5 0 1 1 12.5 8H.5a.5.5 0 0 1 0-1h12a2.5 2.5 0 0 0 0-5m-7 1a1 1 0 0 0-1 1 .5.5 0 0 1-1 0 2 2 0 1 1 2 2h-5a.5.5 0 0 1 0-1h5a1 1 0 0 0 0-2M0 9.5A.5.5 0 0 1 .5 9h10.042a3 3 0 1 1-3 3 .5.5 0 0 1 1 0 2 2 0 1 0 2-2H.5a.5.5 0 0 1-.5-.5"/></svg> Gonflage</span>' : ''}
            </div>
          </div>
        </div>
      `).join('');

    // Si la carte existe, affiche les stations dessus
    if (document.getElementById('map')) {
      renderStationsOnMap(resultats);
    }

  } catch (err) {
    // Si erreur : affiche un message
    console.error("Erreur lors du chargement des stations :", err);
    viewListe.innerHTML = `<p class="placeholder">Erreur de connexion au serveur.<br><small>${err.message}</small></p>`;
    // Réinitialise le compteur de stations
    window.currentStationCount = 0;
    // Met à jour la bannière
    if (typeof window.updateBanner === 'function') window.updateBanner();
  }
}

// --------------------------------------------------
// Carte Leaflet
// --------------------------------------------------

// Affiche les stations sur une carte interactive Leaflet
function renderStationsOnMap(stations) {
  // Utilise la position de l'utilisateur ou Paris par défaut
  const centerLat = userLat || 48.866;
  const centerLng = userLng || 2.333;

  // Initialise la carte Leaflet si elle n'existe pas encore
  if (!window._leafletMap) {
    // Crée la carte centrée sur la position de l'utilisateur
    window._leafletMap = L.map('map').setView([centerLat, centerLng], 12);
    // Ajoute la couche OpenStreetMap
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(window._leafletMap);
    // Recalcule la taille de la carte après un court délai
    setTimeout(() => window._leafletMap.invalidateSize(), 100);
  }
  const map = window._leafletMap;

  // Ajoute ou met à jour le marqueur de position de l'utilisateur
  if (window._leafletUserMarker) map.removeLayer(window._leafletUserMarker);
  window._leafletUserMarker = L.marker([centerLat, centerLng], {
    // Icône par défaut de Leaflet (marqueur bleu)
    icon: L.icon({
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34],
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
      shadowSize: [41, 41]
    })
  }).addTo(map);

  // Initialise le groupe de marqueurs avec clusters si nécessaires
  if (!window._markerClusterGroup) {
    window._markerClusterGroup = L.markerClusterGroup();
    map.addLayer(window._markerClusterGroup);
  } else {
    // Supprime tous les marqueurs de stations précédents
    window._markerClusterGroup.clearLayers();
  }

  // Ajoute un marqueur pour chaque station
  stations.forEach(station => {
    // Crée une icône personnalisée combinant l'adresse et le prix
    const combinedIcon = L.divIcon({
      className: 'station-combined-marker',
      html: `
        <div class="marker-label-container">
          <div class="marker-label-box">
            ${escapeHtml(station.adresse)}<br>
            <span class="marker-price">
              ${station.prixCarburant !== null ? Number(station.prixCarburant).toFixed(3) + ' €/L' : '-'}
            </span>
          </div>
        </div>
        <svg class="marker-triangle-svg" width='24' height='24' viewBox='0 0 24 24'>
          <polygon points='12,20 4,4 20,4' fill='#00c950'/>
        </svg>
      `,
      iconSize: [0, 0]
    });

    // Les coordonnées viennent déjà en degrés décimaux depuis le backend
    const marker = L.marker([station.latitude, station.longitude], { icon: combinedIcon });
    // Ajoute le marqueur au cluster
    window._markerClusterGroup.addLayer(marker);
  });

  // Recalcule la taille de la carte
  setTimeout(() => map.invalidateSize(), 100);

  // Crée la légende de la carte (créée une seule fois)
  if (!document.getElementById('map-legend')) {
    const legend = document.createElement('div');
    legend.id = 'map-legend';
    legend.className = 'map-legend';
    legend.innerHTML = `
      <div class="legend-item">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="#1d6bff" class="bi bi-geo-alt" viewBox="0 0 16 16">
          <path d="M12.166 8.94c-.524 1.062-1.234 2.12-1.96 3.07A32 32 0 0 1 8 14.58a32 32 0 0 1-2.206-2.57c-.726-.95-1.436-2.008-1.96-3.07C3.304 7.867 3 6.862 3 6a5 5 0 0 1 10 0c0 .862-.305 1.867-.834 2.94M8 16s6-5.686 6-10A6 6 0 0 0 2 6c0 4.314 6 10 6 10"/>
          <path d="M8 8a2 2 0 1 1 0-4 2 2 0 0 1 0 4m0 1a3 3 0 1 0 0-6 3 3 0 0 0 0 6"/>
        </svg>
        <span>Votre position</span>
      </div>
      <div class="legend-item">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="#00c950" viewBox="0 0 16 16">
          <circle cx="8" cy="8" r="8"/>
        </svg>
        <span>Stations essence</span>
      </div>
    `;
    // Ajoute la légende à la carte
    document.getElementById('map').appendChild(legend);
  }
}

// --------------------------------------------------
// Observer pour recalculer la carte
// --------------------------------------------------

// Observe les changements de visibilité de la carte pour la redimensionner automatiquement
function observeMapVisibility() {
  const mapDiv = document.getElementById('map');
  if (!mapDiv) return;
  // Crée un observateur de mutations DOM
  const observer = new MutationObserver(() => {
    // Si la carte devient visible (classe "hidden" retirée)
    if (!mapDiv.classList.contains('hidden') && window._leafletMap) {
      // Recalcule la taille de la carte
      setTimeout(() => window._leafletMap.invalidateSize(), 100);
    }
  });
  // Observe les changements de classe sur le parent de la carte
  observer.observe(mapDiv.parentElement, { attributes: true, attributeFilter: ['class'] });
}

// --------------------------------------------------
// Utilitaires
// --------------------------------------------------

// Échappe les caractères HTML pour éviter les injections XSS
function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

// Convertit une date de mise à jour en texte lisible
function getUpdateText(dateStr) {
  // Parse la date de mise à jour
  const dateMaj = new Date(dateStr);
  const now = new Date();
  // Calcule la différence en heures
  const diffH = Math.floor((now - dateMaj) / (1000 * 60 * 60));
  // Calcule la différence en jours
  const diffD = Math.floor(diffH / 24);
  // Return le message correspondant
  if (diffD >= 1) return diffD === 1 ? "Mis à jour il y a environ 1 jour" : `Mis à jour il y a environ ${diffD} jours`;
  if (diffH < 1) return "Mis à jour il y a moins d'1 heure";
  if (diffH === 1) return "Mis à jour il y a environ 1 heure";
  return `Mis à jour il y a environ ${diffH} heures`;
}

// --------------------------------------------------
// Initialisation au chargement de la page
// --------------------------------------------------

// S'exécute quand le DOM est complètement chargé
document.addEventListener('DOMContentLoaded', () => {
  // Restaure l'onglet actif depuis le localStorage (liste / carte)
  const lastTab = localStorage.getItem('activeTab');
  if (lastTab === 'carte') {
    document.getElementById('tab-carte').classList.add('active');
    document.getElementById('tab-liste').classList.remove('active');
    document.getElementById('view-carte').classList.remove('hidden');
    document.getElementById('view-liste').classList.add('hidden');
  }

  // Lance le premier rendu des stations
  renderStations();

  // Relance le rendu à chaque changement de filtre
  document.querySelectorAll('#card-filters input, #card-filters select')
    .forEach(el => el.addEventListener('change', renderStations));

    // Acive l'observation de la visibilité de la carte
  observeMapVisibility();
});


// --------------------------------------------------
// Sauvegarde de l'onglet actif
// --------------------------------------------------

// Sélectionne les boutons d'onglets
const tabListe = document.getElementById('tab-liste');
const tabCarte = document.getElementById('tab-carte');
if (tabListe && tabCarte) {
  // Sauvegarde de l'onglet "liste" quand il est cliqué
  tabListe.addEventListener('click', () => localStorage.setItem('activeTab', 'liste'));
  // Sauvegarde de l'onglet "carte" quand il est cliqué
  tabCarte.addEventListener('click', () => localStorage.setItem('activeTab', 'carte'));
}

// --------------------------------------------------
// Écouteurs d'événements pour les filtres
// --------------------------------------------------

// S'exécute quand le DOM est complètement chargé
document.addEventListener('DOMContentLoaded', () => {
  // Lance le rendu initial
  renderStations();
  // Ajoute les écouteurs d'événements sur chaque filtre
  document.getElementById('filtre-carburant')?.addEventListener('change', renderStations);
  document.getElementById('filtre-conso')?.addEventListener('input', renderStations);
  document.getElementById('filtre-rayon')?.addEventListener('input', renderStations);
  document.getElementById('filtre-res-total')?.addEventListener('input', renderStations);
  document.getElementById('filtre-res-courant')?.addEventListener('input', renderStations);
  document.getElementById('filtre-lavage')?.addEventListener('change', renderStations);
  document.getElementById('filtre-gonflage')?.addEventListener('change', renderStations);
});
