// URL de base de l'API backend
const API_BASE = 'http://localhost:8080/Prix_Carburants-services';

// Position de l'utilisateur (mise à jour par la géolocalisation)
let userLat = null;
let userLng = null;

// Géolocalisation
function getPosition() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error("Géolocalisation non supportée par ce navigateur."));
      return;
    }
    navigator.geolocation.getCurrentPosition(
      pos => resolve({ lat: pos.coords.latitude, lng: pos.coords.longitude }),
      err => reject(err),
      { timeout: 8000 }
    );
  });
}

// Appel API
async function fetchStations({ lat, lng, carburant, rayon, conso, resTotal, resCourant, avecLavage, avecGonflage }) {
  const params = new URLSearchParams({
    lat:        lat,
    lon:        lng,
    radius:     rayon,
    carburant:  carburant,
    conso:      conso,
    resTotal:   resTotal,
    resCourant: resCourant,
  });

  if (avecLavage)   params.set('lavage',   'true');
  if (avecGonflage) params.set('gonflage', 'true');

  const res = await fetch(`${API_BASE}/api/stations/near?${params.toString()}`);
  if (!res.ok) throw new Error(`Erreur serveur : ${res.status}`);
  return res.json(); // tableau de stations
}

// Rendu principal
async function renderStations() {
  const carburant    = document.getElementById('filtre-carburant').value;
  const conso        = parseFloat(document.getElementById('filtre-conso').value);
  const rayon        = parseFloat(document.getElementById('filtre-rayon').value);
  const resTotal     = parseFloat(document.getElementById('filtre-res-total').value);
  const resCourant   = parseFloat(document.getElementById('filtre-res-courant').value);
  const avecLavage   = document.getElementById('filtre-lavage').checked;
  const avecGonflage = document.getElementById('filtre-gonflage').checked;

  const viewListe = document.getElementById('view-liste');
  viewListe.innerHTML = '<p class="placeholder">Chargement en cours…</p>';

  try {
    // Récupère la position si on ne l'a pas encore
    if (userLat === null) {
      try {
        const pos = await getPosition();
        userLat = pos.lat;
        userLng = pos.lng;
      } catch (e) {
        // Position par défaut : Paris
        userLat = 48.866;
        userLng = 2.333;
        console.warn("Géolocalisation échouée, position par défaut utilisée.", e.message);
      }
    }

    const resultats = await fetchStations({
      lat: userLat, lng: userLng,
      carburant, rayon, conso, resTotal, resCourant,
      avecLavage, avecGonflage,
    });

    // Les champs venant de l'API sont en camelCase
    // idStation, latitude, longitude, adresse, ville, cp,
    // automate, lavage, gonflage, nomAffiche,
    // prixCarburant, dateMaj, nomCarburant, coutTotal, distance, rang

    window.currentStationCount = resultats.length;
    if (typeof window.updateBanner === 'function') window.updateBanner();

    viewListe.innerHTML = resultats.length === 0
      ? '<p class="placeholder">Aucune station trouvée.</p>'
      : resultats.map(s => `
        <div class="station-card">
          <div class="card-top">
            <div class="rank">${s.rang}</div>
            <div class="station-name">
              ${escapeHtml(s.adresse)}, ${escapeHtml(s.ville)}
              ${s.rang === 1 ? '<span class="badge-best">Meilleur choix</span>' : ''}
            </div>
          </div>
          <div class="card-price-row">
            <div class="card-price">
              ${s.prixCarburant !== null ? s.prixCarburant.toFixed(3) + ' €/L' : 'Prix indisponible'}
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
            <span class="card-total">Coût total estimé : ${s.coutTotal.toFixed(2)} €</span>
            <div class="card-services">
              ${s.lavage   ? '<span class="service-tag"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-water" viewBox="0 0 16 16" style="vertical-align:middle;margin-right:3px;"><path d="M.036 3.314a.5.5 0 0 1 .65-.278l1.757.703a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.757-.703a.5.5 0 1 1 .372.928l-1.758.703a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0L.314 3.964a.5.5 0 0 1-.278-.65m0 3a.5.5 0 0 1 .65-.278l1.757.703a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.014-.406a2.5 2.5 0 0 1 1.857 0l1.015.406a1.5 1.5 0 0 0 1.114 0l1.757-.703a.5.5 0 1 1 .372.928l-1.758.703a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.014-.406a1.5 1.5 0 0 0-1.114 0l-1.015.406a2.5 2.5 0 0 1-1.857 0l-1.757-.703a.5.5 0 0 1-.278-.65"/></svg> Lavage</span>' : ''}
              ${s.gonflage ? '<span class="service-tag"><svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-wind" viewBox="0 0 16 16"><path d="M12.5 2A2.5 2.5 0 0 0 10 4.5a.5.5 0 0 1-1 0A3.5 3.5 0 1 1 12.5 8H.5a.5.5 0 0 1 0-1h12a2.5 2.5 0 0 0 0-5m-7 1a1 1 0 0 0-1 1 .5.5 0 0 1-1 0 2 2 0 1 1 2 2h-5a.5.5 0 0 1 0-1h5a1 1 0 0 0 0-2M0 9.5A.5.5 0 0 1 .5 9h10.042a3 3 0 1 1-3 3 .5.5 0 0 1 1 0 2 2 0 1 0 2-2H.5a.5.5 0 0 1-.5-.5"/></svg> Gonflage</span>' : ''}
            </div>
          </div>
        </div>
      `).join('');

    if (document.getElementById('map')) {
      renderStationsOnMap(resultats);
    }

  } catch (err) {
    console.error("Erreur lors du chargement des stations :", err);
    viewListe.innerHTML = `<p class="placeholder">Erreur de connexion au serveur.<br><small>${err.message}</small></p>`;
    window.currentStationCount = 0;
    if (typeof window.updateBanner === 'function') window.updateBanner();
  }
}

// Carte Leaflet
function renderStationsOnMap(stations) {
  const centerLat = userLat || 48.866;
  const centerLng = userLng || 2.333;

  if (!window._leafletMap) {
    window._leafletMap = L.map('map').setView([centerLat, centerLng], 12);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap contributors'
    }).addTo(window._leafletMap);
    setTimeout(() => window._leafletMap.invalidateSize(), 100);
  }
  const map = window._leafletMap;

  // Marqueur utilisateur
  if (window._leafletUserMarker) map.removeLayer(window._leafletUserMarker);
  window._leafletUserMarker = L.marker([centerLat, centerLng], {
    icon: L.icon({
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34],
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
      shadowSize: [41, 41]
    })
  }).addTo(map);

  if (!window._markerClusterGroup) {
    window._markerClusterGroup = L.markerClusterGroup();
    map.addLayer(window._markerClusterGroup);
  } else {
    window._markerClusterGroup.clearLayers();
  }

  stations.forEach(station => {
    const combinedIcon = L.divIcon({
      className: 'station-combined-marker',
      html: `
        <div class="marker-label-container">
          <div class="marker-label-box">
            ${escapeHtml(station.adresse)}<br>
            <span class="marker-price">
              ${station.prixCarburant !== null ? station.prixCarburant.toFixed(3) + ' €/L' : '–'}
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
    window._markerClusterGroup.addLayer(marker);
  });

  setTimeout(() => map.invalidateSize(), 100);

  // Légende (créée une seule fois)
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
    document.getElementById('map').appendChild(legend);
  }
}

// Recalcule la carte quand elle devient visible
function observeMapVisibility() {
  const mapDiv = document.getElementById('map');
  if (!mapDiv) return;
  const observer = new MutationObserver(() => {
    if (!mapDiv.classList.contains('hidden') && window._leafletMap) {
      setTimeout(() => window._leafletMap.invalidateSize(), 100);
    }
  });
  observer.observe(mapDiv.parentElement, { attributes: true, attributeFilter: ['class'] });
}

// Utilitaires
function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

function getUpdateText(dateStr) {
  const dateMaj = new Date(dateStr);
  const now = new Date();
  const diffH = Math.floor((now - dateMaj) / (1000 * 60 * 60));
  const diffD = Math.floor(diffH / 24);
  if (diffD >= 1) return diffD === 1 ? "Mis à jour il y a environ 1 jour" : `Mis à jour il y a environ ${diffD} jours`;
  if (diffH < 1)  return "Mis à jour il y a moins d'1 heure";
  if (diffH === 1) return "Mis à jour il y a environ 1 heure";
  return `Mis à jour il y a environ ${diffH} heures`;
}

// Initialisation
document.addEventListener('DOMContentLoaded', () => {
  const lastTab = localStorage.getItem('activeTab');
  if (lastTab === 'carte') {
    document.getElementById('tab-carte').classList.add('active');
    document.getElementById('tab-liste').classList.remove('active');
    document.getElementById('view-carte').classList.remove('hidden');
    document.getElementById('view-liste').classList.add('hidden');
  }

  renderStations();

  // Relance le rendu à chaque changement de filtre
  document.querySelectorAll('#card-filters input, #card-filters select')
    .forEach(el => el.addEventListener('change', renderStations));

  observeMapVisibility();
});

// Sauvegarde de l'onglet actif
const tabListe = document.getElementById('tab-liste');
const tabCarte = document.getElementById('tab-carte');
if (tabListe && tabCarte) {
  tabListe.addEventListener('click', () => localStorage.setItem('activeTab', 'liste'));
  tabCarte.addEventListener('click', () => localStorage.setItem('activeTab', 'carte'));
}
