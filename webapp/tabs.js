// Les icônes SVG sont stockées dans des variables pour éviter de les répéter
const svgListe = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-cursor" viewBox="0 0 16 16"><path d="M14.082 2.182a.5.5 0 0 1 .103.557L8.528 15.467a.5.5 0 0 1-.917-.007L5.57 10.694.803 8.652a.5.5 0 0 1-.006-.916l12.728-5.657a.5.5 0 0 1 .556.103zM2.25 8.184l3.897 1.67a.5.5 0 0 1 .262.263l1.67 3.897L12.743 3.52z"/></svg>`;
const svgCarte = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-geo-alt" viewBox="0 0 16 16"><path d="M12.166 8.94c-.524 1.062-1.234 2.12-1.96 3.07A32 32 0 0 1 8 14.58a32 32 0 0 1-2.206-2.57c-.726-.95-1.436-2.008-1.96-3.07C3.304 7.867 3 6.862 3 6a5 5 0 0 1 10 0c0 .862-.305 1.867-.834 2.94M8 16s6-5.686 6-10A6 6 0 0 0 2 6c0 4.314 6 10 6 10"/><path d="M8 8a2 2 0 1 1 0-4 2 2 0 0 1 0 4m0 1a3 3 0 1 0 0-6 3 3 0 0 0 0 6"/></svg>`;

// Fonction globale pour mettre à jour les textes et couleurs du bandeau
window.updateBanner = function () {
  // Récupère le nombre de stations (défini dans app.js), par défaut 0
  const count = window.currentStationCount || 0;

  // Définit si on met un "s" ou non (si count est strictement supérieur à 1)
  const pluriel = count > 1 ? "s" : "";

  // Récupération des éléments HTML
  const resultsCountEl = document.getElementById('results-count');
  const bannerSubEl = document.getElementById('banner-sub');
  const bannerEl = document.querySelector('.results-banner'); // Le bandeau complet

  // Vérifie si l'onglet carte est actif
  const isCarte = document.getElementById('tab-carte').classList.contains('active');

  if (!isCarte) {
    // Vue Liste : svgListe, bleu
    if (resultsCountEl) resultsCountEl.innerHTML = `${svgListe} Station${pluriel} à proximité (${count})`;
    if (bannerSubEl) bannerSubEl.textContent = "Classées par rentabilité (prix + coût du trajet)";
    if (bannerEl) bannerEl.style.backgroundColor = 'var(--accent-color)';
  } else {
    // Vue Carte : svgCarte, vert
    if (resultsCountEl) resultsCountEl.innerHTML = `${svgCarte} Carte des stations`;
    if (bannerSubEl) bannerSubEl.textContent = `${count} station${pluriel} trouvée${pluriel}`;
    if (bannerEl) bannerEl.style.backgroundColor = 'var(--green)';
  }
};

document.addEventListener('DOMContentLoaded', function () {
  const tabListe = document.getElementById('tab-liste');
  const tabCarte = document.getElementById('tab-carte');
  const viewListe = document.getElementById('view-liste');
  const viewCarte = document.getElementById('view-carte');

  function selectTab(tab) {
    if (tab === 'liste') {
      tabListe.classList.add('active');
      tabCarte.classList.remove('active');
      viewListe.classList.remove('hidden');
      viewCarte.classList.add('hidden');
    } else {
      tabCarte.classList.add('active');
      tabListe.classList.remove('active');
      viewCarte.classList.remove('hidden');
      viewListe.classList.add('hidden');

      if (window._leafletMap) {
        setTimeout(() => { window._leafletMap.invalidateSize(); }, 100);
      }
    }
    // Met à jour les textes et couleurs à chaque changement d'onglet
    window.updateBanner();
  }

  tabListe.addEventListener('click', () => selectTab('liste'));
  tabCarte.addEventListener('click', () => selectTab('carte'));

  const currentActive = document.querySelector('.tab.active')?.id;
  selectTab(currentActive === 'tab-carte' ? 'carte' : 'liste');
});