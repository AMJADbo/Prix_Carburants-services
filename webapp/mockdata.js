// ============================================================
//  MOCK DATA — Structure basée sur la BDD MySQL (DCD_4)
//  Tables : STATIONS, HORAIRES, PRIX
//  Note : latitude/longitude stockées en entier (÷ 100 000
//         pour obtenir les degrés décimaux WGS84)
// ============================================================

// ----- TABLE : STATIONS ---------------------------------
const stations = [
  {
    id_station: 1001,
    latitude:   4887220,   // → 48.87220° N  (Paris 18e)
    longitude:  236580,    // →  2.36580° E
    adresse:    "12 Rue Marx Dormoy",
    ville:      "Paris",
    cp:         "75018",
    automate:   true,
    lavage:     false,
    gonflage:   true,
  },
  {
    id_station: 1002,
    latitude:   4884500,   // → 48.84500° N  (Paris 13e)
    longitude:  233100,    // →  2.33100° E
    adresse:    "54 Boulevard de l'Hôpital",
    ville:      "Paris",
    cp:         "75013",
    automate:   true,
    lavage:     true,
    gonflage:   true,
  },
  {
    id_station: 1003,
    latitude:   4890100,   // → 48.90100° N  (Saint-Denis)
    longitude:  234800,    // →  2.34800° E
    adresse:    "3 Avenue du Président Wilson",
    ville:      "Saint-Denis",
    cp:         "93200",
    automate:   false,
    lavage:     true,
    gonflage:   false,
  },
  {
    id_station: 1004,
    latitude:   4882300,   // → 48.82300° N  (Montrouge)
    longitude:  231900,    // →  2.31900° E
    adresse:    "77 Avenue de la République",
    ville:      "Montrouge",
    cp:         "92120",
    automate:   true,
    lavage:     false,
    gonflage:   true,
  },
  {
    id_station: 1005,
    latitude:   4886700,   // → 48.86700° N  (Paris 11e)
    longitude:  238400,    // →  2.38400° E
    adresse:    "101 Boulevard Voltaire",
    ville:      "Paris",
    cp:         "75011",
    automate:   true,
    lavage:     true,
    gonflage:   true,
  },

  {
    id_station: 1001,
    latitude:   4887220,   // → 48.87220° N  (Paris 18e)
    longitude:  236580,    // →  2.36580° E
    adresse:    "12 Rue Marx Dormoy",
    ville:      "Paris",
    cp:         "75018",
    automate:   true,
    lavage:     false,
    gonflage:   true,
  },
  {
    id_station: 1002,
    latitude:   4884500,   // → 48.84500° N  (Paris 13e)
    longitude:  233100,    // →  2.33100° E
    adresse:    "54 Boulevard de l'Hôpital",
    ville:      "Paris",
    cp:         "75013",
    automate:   true,
    lavage:     true,
    gonflage:   true,
  },
  {
    id_station: 1003,
    latitude:   4890100,   // → 48.90100° N  (Saint-Denis)
    longitude:  234800,    // →  2.34800° E
    adresse:    "3 Avenue du Président Wilson",
    ville:      "Saint-Denis",
    cp:         "93200",
    automate:   false,
    lavage:     true,
    gonflage:   false,
  },
  {
    id_station: 1004,
    latitude:   4882300,   // → 48.82300° N  (Montrouge)
    longitude:  231900,    // →  2.31900° E
    adresse:    "77 Avenue de la République",
    ville:      "Montrouge",
    cp:         "92120",
    automate:   true,
    lavage:     false,
    gonflage:   true,
  },
  {
    id_station: 1005,
    latitude:   4886700,   // → 48.86700° N  (Paris 11e)
    longitude:  238400,    // →  2.38400° E
    adresse:    "101 Boulevard Voltaire",
    ville:      "Paris",
    cp:         "75011",
    automate:   true,
    lavage:     true,
    gonflage:   true,
  },


  
];

// ----- TABLE : HORAIRES ---------------------------------
const horaires = [
  // Station 1001 — ouverte 24h/24 (automate)
  { id_horaire: 1, id_station: 1001, jour: 1, ouverture: "00:00", fermeture: "23:59" },
  { id_horaire: 2, id_station: 1001, jour: 2, ouverture: "00:00", fermeture: "23:59" },
  { id_horaire: 3, id_station: 1001, jour: 3, ouverture: "00:00", fermeture: "23:59" },
  { id_horaire: 4, id_station: 1001, jour: 4, ouverture: "00:00", fermeture: "23:59" },
  { id_horaire: 5, id_station: 1001, jour: 5, ouverture: "00:00", fermeture: "23:59" },
  { id_horaire: 6, id_station: 1001, jour: 6, ouverture: "00:00", fermeture: "23:59" },
  { id_horaire: 7, id_station: 1001, jour: 7, ouverture: "00:00", fermeture: "23:59" },

  // Station 1002 — Lun–Sam 07:00–21:00, Dim fermée
  { id_horaire: 8,  id_station: 1002, jour: 1, ouverture: "07:00", fermeture: "21:00" },
  { id_horaire: 9,  id_station: 1002, jour: 2, ouverture: "07:00", fermeture: "21:00" },
  { id_horaire: 10, id_station: 1002, jour: 3, ouverture: "07:00", fermeture: "21:00" },
  { id_horaire: 11, id_station: 1002, jour: 4, ouverture: "07:00", fermeture: "21:00" },
  { id_horaire: 12, id_station: 1002, jour: 5, ouverture: "07:00", fermeture: "21:00" },
  { id_horaire: 13, id_station: 1002, jour: 6, ouverture: "08:00", fermeture: "20:00" },
  // Dimanche (jour 7) absent → station fermée ce jour-là

  // Station 1003 — Lun–Ven 06:30–22:00
  { id_horaire: 14, id_station: 1003, jour: 1, ouverture: "06:30", fermeture: "22:00" },
  { id_horaire: 15, id_station: 1003, jour: 2, ouverture: "06:30", fermeture: "22:00" },
  { id_horaire: 16, id_station: 1003, jour: 3, ouverture: "06:30", fermeture: "22:00" },
  { id_horaire: 17, id_station: 1003, jour: 4, ouverture: "06:30", fermeture: "22:00" },
  { id_horaire: 18, id_station: 1003, jour: 5, ouverture: "06:30", fermeture: "22:00" },

  // Station 1004 — 24h/24 automate
  { id_horaire: 19, id_station: 1004, jour: 1, ouverture: "00:00", fermeture: "23:59" },
  { id_horaire: 20, id_station: 1004, jour: 2, ouverture: "00:00", fermeture: "23:59" },
  { id_horaire: 21, id_station: 1004, jour: 3, ouverture: "00:00", fermeture: "23:59" },
  { id_horaire: 22, id_station: 1004, jour: 4, ouverture: "00:00", fermeture: "23:59" },
  { id_horaire: 23, id_station: 1004, jour: 5, ouverture: "00:00", fermeture: "23:59" },
  { id_horaire: 24, id_station: 1004, jour: 6, ouverture: "00:00", fermeture: "23:59" },
  { id_horaire: 25, id_station: 1004, jour: 7, ouverture: "00:00", fermeture: "23:59" },

  // Station 1005 — Lun–Dim 07:00–22:00
  { id_horaire: 26, id_station: 1005, jour: 1, ouverture: "07:00", fermeture: "22:00" },
  { id_horaire: 27, id_station: 1005, jour: 2, ouverture: "07:00", fermeture: "22:00" },
  { id_horaire: 28, id_station: 1005, jour: 3, ouverture: "07:00", fermeture: "22:00" },
  { id_horaire: 29, id_station: 1005, jour: 4, ouverture: "07:00", fermeture: "22:00" },
  { id_horaire: 30, id_station: 1005, jour: 5, ouverture: "07:00", fermeture: "22:00" },
  { id_horaire: 31, id_station: 1005, jour: 6, ouverture: "08:00", fermeture: "21:00" },
  { id_horaire: 32, id_station: 1005, jour: 7, ouverture: "09:00", fermeture: "19:00" },
];

// ----- TABLE : PRIX ---------------------------------
const prix = [
  // Station 1001
  { id_prix: 1,  id_station: 1001, nom_carburant: "SP95",    prix: 1.789, date_maj: "2026-03-13T08:00:00" },
  { id_prix: 2,  id_station: 1001, nom_carburant: "SP98",    prix: 1.869, date_maj: "2026-03-13T08:00:00" },
  { id_prix: 3,  id_station: 1001, nom_carburant: "Diesel",  prix: 1.659, date_maj: "2026-03-13T08:00:00" },
  { id_prix: 4,  id_station: 1001, nom_carburant: "E85",     prix: 0.899, date_maj: "2026-03-13T08:00:00" },

  // Station 1002
  { id_prix: 5,  id_station: 1002, nom_carburant: "SP95",    prix: 1.759, date_maj: "2026-03-13T07:45:00" },
  { id_prix: 6,  id_station: 1002, nom_carburant: "SP98",    prix: 1.849, date_maj: "2026-03-13T07:45:00" },
  { id_prix: 7,  id_station: 1002, nom_carburant: "E5",      prix: 1.769, date_maj: "2026-03-13T07:45:00" },
  { id_prix: 8,  id_station: 1002, nom_carburant: "Diesel",  prix: 1.649, date_maj: "2026-03-13T07:45:00" },
  { id_prix: 9,  id_station: 1002, nom_carburant: "GPL",     prix: 0.849, date_maj: "2026-03-13T07:45:00" },

  // Station 1003
  { id_prix: 10, id_station: 1003, nom_carburant: "SP95",    prix: 1.799, date_maj: "2026-03-13T09:10:00" },
  { id_prix: 11, id_station: 1003, nom_carburant: "Diesel",  prix: 1.679, date_maj: "2026-03-13T09:10:00" },
  { id_prix: 12, id_station: 1003, nom_carburant: "E85",     prix: 0.919, date_maj: "2026-03-13T09:10:00" },

  // Station 1004
  { id_prix: 13, id_station: 1004, nom_carburant: "SP95",    prix: 1.749, date_maj: "2026-03-13T06:30:00" },
  { id_prix: 14, id_station: 1004, nom_carburant: "SP98",    prix: 1.829, date_maj: "2026-03-13T06:30:00" },
  { id_prix: 15, id_station: 1004, nom_carburant: "E5",      prix: 1.759, date_maj: "2026-03-13T06:30:00" },
  { id_prix: 16, id_station: 1004, nom_carburant: "Diesel",  prix: 1.639, date_maj: "2026-03-13T06:30:00" },

  // Station 1005
  { id_prix: 17, id_station: 1005, nom_carburant: "SP95",    prix: 1.779, date_maj: "2026-03-13T08:20:00" },
  { id_prix: 18, id_station: 1005, nom_carburant: "SP98",    prix: 1.859, date_maj: "2026-03-13T08:20:00" },
  { id_prix: 19, id_station: 1005, nom_carburant: "E5",      prix: 1.789, date_maj: "2026-03-13T08:20:00" },
  { id_prix: 20, id_station: 1005, nom_carburant: "Diesel",  prix: 1.669, date_maj: "2026-03-13T08:20:00" },
  { id_prix: 21, id_station: 1005, nom_carburant: "E85",     prix: 0.909, date_maj: "2026-03-13T08:20:00" },
  { id_prix: 22, id_station: 1005, nom_carburant: "GPL",     prix: 0.859, date_maj: "2026-03-13T08:20:00" },
];


// ============================================================
//  FONCTION UTILITAIRE
//  Simule la réponse de GET /api/stations en assemblant
//  les 3 tables exactement comme le ferait le back-end.
//
//  Paramètres attendus (même chose que l'endpoint réel) :
//    - userLat, userLng  → position de l'utilisateur (degrés décimaux)
//    - carburant         → ex. "SP95"
//    - rayon             → km
//    - consommation      → L/100km
//    - reservoirTotal    → litres
//    - reservoirCourant  → litres
//    - avecLavage        → boolean
//    - avecGonflage      → boolean
// ============================================================

function haversineKm(lat1, lon1, lat2, lon2) {
  const R = 6371;
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(lat1 * Math.PI / 180) *
    Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) ** 2;
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
}

function getMockStations({
  userLat = 48.866,
  userLng = 2.333,
  carburant = "SP95",
  rayon = 25,
  consommation = 7,
  reservoirTotal = 50,
  reservoirCourant = 20,
  avecLavage = false,
  avecGonflage = false,
} = {}) {
  const results = [];

  for (const station of stations) {
    // Filtres services
    if (avecLavage  && !station.lavage)   continue;
    if (avecGonflage && !station.gonflage) continue;

    // Conversion coordonnées (÷ 100 000)
    const lat = station.latitude  / 100000;
    const lng = station.longitude / 100000;

    // Distance
    const distance = haversineKm(userLat, userLng, lat, lng);
    if (distance > rayon) continue;

    // Prix du carburant demandé
    const prixEntry = prix.find(
      p => p.id_station === station.id_station && p.nom_carburant === carburant
    );
    if (!prixEntry) continue; // station ne propose pas ce carburant

    // Coût total (formule DCD_4) :
    // ((réservoir_total - réservoir_courant) + (2D/100 * conso)) * prix
    const litresPlein     = reservoirTotal - reservoirCourant;
    const litresTrajet    = (2 * distance / 100) * consommation;
    const coutTotal       = (litresPlein + litresTrajet) * prixEntry.prix;

    // Horaires de la station
    const horairesStation = horaires.filter(h => h.id_station === station.id_station);

    // Tous les carburants dispo sur cette station
    const carburantsDispos = prix
      .filter(p => p.id_station === station.id_station)
      .map(p => ({ nom: p.nom_carburant, prix: p.prix, date_maj: p.date_maj }));

    results.push({
      id_station:       station.id_station,
      adresse:          station.adresse,
      ville:            station.ville,
      cp:               station.cp,
      latitude:         lat,
      longitude:        lng,
      automate:         station.automate,
      lavage:           station.lavage,
      gonflage:         station.gonflage,
      distance:         Math.round(distance * 10) / 10,   // arrondi 1 décimale
      prix_carburant:   prixEntry.prix,
      date_maj:         prixEntry.date_maj,
      cout_total:       Math.round(coutTotal * 100) / 100,
      horaires:         horairesStation,
      carburants:       carburantsDispos,
    });
  }

  // Tri par coût total croissant (= classement "rentabilité")
  results.sort((a, b) => a.cout_total - b.cout_total);

  // Ajout du rang
  results.forEach((r, i) => r.rang = i + 1);

  return results;
}

// ── Export (à adapter selon le module bundler utilisé) ──────
// CommonJS  → module.exports = { stations, horaires, prix, getMockStations };
// ESModule  → export { stations, horaires, prix, getMockStations };
// Navigateur (script tag) → window.mockData = { ... };

// Pour un usage direct dans le navigateur sans bundler :
if (typeof window !== "undefined") {
  window.mockData = { stations, horaires, prix, getMockStations };
}