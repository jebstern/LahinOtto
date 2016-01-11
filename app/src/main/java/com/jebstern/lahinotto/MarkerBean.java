package com.jebstern.lahinotto;


import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MarkerBean implements ClusterItem {

    String malli;
    String numero;
    String osoite;
    String postinumero;
    String postitoimipaikka;
    String tyyppi;
    String sijaintipaikka;
    String aukioloaika;
    String lisatiedot;
    double latitude;
    double longitude;
    private final LatLng mPosition;

    public MarkerBean(String malli, String numero, String osoite, String postinumero, String postitoimipaikka, String tyyppi, String sijaintipaikka, String aukioloaika, String lisatiedot, double latitude, double longitude) {
        this.malli = malli;
        this.numero = numero;
        this.osoite = osoite;
        this.postinumero = postinumero;
        this.postitoimipaikka = postitoimipaikka;
        this.tyyppi = tyyppi;
        this.sijaintipaikka = sijaintipaikka;
        this.aukioloaika = aukioloaika;
        this.lisatiedot = lisatiedot;
        this.latitude = latitude;
        this.longitude = longitude;
        mPosition = new LatLng(latitude, longitude);
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    public String getMalli() {
        return malli;
    }

    public void setMalli(String malli) {
        this.malli = malli;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getOsoite() {
        return osoite;
    }

    public void setOsoite(String osoite) {
        this.osoite = osoite;
    }

    public String getPostinumero() {
        return postinumero;
    }

    public void setPostinumero(String postinumero) {
        this.postinumero = postinumero;
    }

    public String getPostitoimipaikka() {
        return postitoimipaikka;
    }

    public void setPostitoimipaikka(String postitoimipaikka) {
        this.postitoimipaikka = postitoimipaikka;
    }

    public String getTyyppi() {
        return tyyppi;
    }

    public void setTyyppi(String tyyppi) {
        this.tyyppi = tyyppi;
    }

    public String getSijaintipaikka() {
        return sijaintipaikka;
    }

    public void setSijaintipaikka(String sijaintipaikka) {
        this.sijaintipaikka = sijaintipaikka;
    }

    public String getAukioloaika() {
        return aukioloaika;
    }

    public void setAukioloaika(String aukioloaika) {
        this.aukioloaika = aukioloaika;
    }

    public String getLisatiedot() {
        return lisatiedot;
    }

    public void setLisatiedot(String lisatiedot) {
        this.lisatiedot = lisatiedot;
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
}
