// Attend que le DOM soit chargé avant d'exécuter le script
document.addEventListener('DOMContentLoaded', function() {
  // Récupère le bouton de l'onglet "Liste"
  const tabListe = document.getElementById('tab-liste');
  // Récupère le bouton de l'onglet "Carte"
  const tabCarte = document.getElementById('tab-carte');
  // Récupère la vue de la liste des stations
  const viewListe = document.getElementById('view-liste');
  // Récupère la vue de la carte
  const viewCarte = document.getElementById('view-carte');

  // Fonction pour sélectionner un onglet et afficher la vue correspondante
  function selectTab(tab) {
    if(tab === 'liste') {
      // Active l'onglet "Liste" et désactive "Carte"
      tabListe.classList.add('active');
      tabCarte.classList.remove('active');
      // Affiche la vue liste, masque la vue carte
      viewListe.classList.remove('hidden');
      viewCarte.classList.add('hidden');
    } else {
      // Active l'onglet "Carte" et désactive "Liste"
      tabCarte.classList.add('active');
      tabListe.classList.remove('active');
      // Affiche la vue carte, masque la vue liste
      viewCarte.classList.remove('hidden');
      viewListe.classList.add('hidden');
    }
  }

  // Ajoute un écouteur de clic sur l'onglet "Liste"
  tabListe.addEventListener('click', function() {
    selectTab('liste'); // Sélectionne la vue liste
  });
  // Ajoute un écouteur de clic sur l'onglet "Carte"
  tabCarte.addEventListener('click', function() {
    selectTab('carte'); // Sélectionne la vue carte
  });

  // État initial : sélectionne l'onglet actif au chargement
  selectTab(document.querySelector('.tab.active')?.id === 'tab-carte' ? 'carte' : 'liste');
});
