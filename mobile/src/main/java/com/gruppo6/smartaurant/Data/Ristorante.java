package com.gruppo6.smartaurant.Data;

/**
 * Created by marco on 14/11/2015.
 */
public class Ristorante {
    public String name="";
    public String address="";
    public double latitude=0;
    public double longitude=0;
    public String id="";

    public Ristorante(String n, String a, double lat, double lon, String i){
        name=n;
        address=a;
        latitude=lat;
        longitude=lon;
        id=i;
    }
}
