package model;

public class Price {

    private long idPrix;
    private long idStation;
    private String nomCarburant;
    private double prix;
    private String dateMaj;

    public Price() {
    }

    public Price(long idPrix, long idStation, String nomCarburant, double prix, String dateMaj) {
        this.idPrix = idPrix;
        this.idStation = idStation;
        this.nomCarburant = nomCarburant;
        this.prix = prix;
        this.dateMaj = dateMaj;
    }

    public long getIdPrix() {
        return idPrix;
    }

    public void setIdPrix(long idPrix) {
        this.idPrix = idPrix;
    }

    public long getIdStation() {
        return idStation;
    }

    public void setIdStation(long idStation) {
        this.idStation = idStation;
    }

    public String getNomCarburant() {
        return nomCarburant;
    }

    public void setNomCarburant(String nomCarburant) {
        this.nomCarburant = nomCarburant;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getDateMaj() {
        return dateMaj;
    }

    public void setDateMaj(String dateMaj) {
        this.dateMaj = dateMaj;
    }

    @Override
    public String toString() {
        return "Price{" +
                "idPrix=" + idPrix +
                ", idStation=" + idStation +
                ", nomCarburant='" + nomCarburant + '\'' +
                ", prix=" + prix +
                ", dateMaj='" + dateMaj + '\'' +
                '}';
    }
}