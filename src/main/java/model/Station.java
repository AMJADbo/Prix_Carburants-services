package model;

public class Station {

    private long idStation;
    private double latitude;
    private double longitude;
    private String adresse;
    private String ville;
    private String cp;
    private boolean automate;
    private boolean lavage;
    private boolean gonflage;
    private String nomAffiche;

    public Station() {
    }

    public Station(long idStation, double latitude, double longitude, String adresse, String ville, String cp,
                   boolean automate24h, boolean lavage, boolean gonflage, String nomAffiche) {
        this.idStation = idStation;
        this.latitude = latitude;
        this.longitude = longitude;
        this.adresse = adresse;
        this.ville = ville;
        this.cp = cp;
        this.automate = automate;
        this.lavage = lavage;
        this.gonflage = gonflage;
        this.nomAffiche = nomAffiche;
    }

    public long getIdStation() {
        return idStation;
    }

    public void setIdStation(long idStation) {
        this.idStation = idStation;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public boolean isAutomate() {
        return automate;
    }

    public void setAutomate24h(boolean automate24h) {
        this.automate = automate;
    }

    public boolean isLavage() {
        return lavage;
    }
   
    public void setLavage(boolean lavage) {
        this.lavage = lavage;
    }

    public boolean isGonflage() {
        return gonflage;
    }

    public void setGonflage(boolean gonflage) {
        this.gonflage = gonflage;
    }

    public String getNomAffiche() {
        return nomAffiche;
    }

    public void setNomAffiche(String nomAffiche) {
        this.nomAffiche = nomAffiche;
    }

    @Override
    public String toString() {
        return "Station{" +
                "idStation=" + idStation +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", adresse='" + adresse + '\'' +
                ", ville='" + ville + '\'' +
                ", cp='" + cp + '\'' +
                ", automate24h=" + automate +
                ", lavage=" + lavage +
                ", gonflage=" + gonflage +
                ", nomAffiche='" + nomAffiche + '\'' +
                '}';
    }
}