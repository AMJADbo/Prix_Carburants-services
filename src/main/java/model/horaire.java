package model;

public class horaire {

    private long idHoraire;
    private long idStation;
    private int jour;
    private String ouverture;
    private String fermeture;

    public horaire() {
    }

    public horaire(long idHoraire, long idStation, int jour, String ouverture, String fermeture) {
        this.idHoraire = idHoraire;
        this.idStation = idStation;
        this.jour = jour;
        this.ouverture = ouverture;
        this.fermeture = fermeture;
    }

    public long getIdHoraire() {
        return idHoraire;
    }

    public void setIdHoraire(long idHoraire) {
        this.idHoraire = idHoraire;
    }

    public long getIdStation() {
        return idStation;
    }

    public void setIdStation(long idStation) {
        this.idStation = idStation;
    }

    public int getJour() {
        return jour;
    }

    public void setJour(int jour) {
        this.jour = jour;
    }

    public String getOuverture() {
        return ouverture;
    }

    public void setOuverture(String ouverture) {
        this.ouverture = ouverture;
    }

    public String getFermeture() {
        return fermeture;
    }

    public void setFermeture(String fermeture) {
        this.fermeture = fermeture;
    }

    @Override
    public String toString() {
        return "horaire{" +
                "idHoraire=" + idHoraire +
                ", idStation=" + idStation +
                ", jour=" + jour +
                ", ouverture='" + ouverture + '\'' +
                ", fermeture='" + fermeture + '\'' +
                '}';
    }
}