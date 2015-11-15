package com.gruppo6.smartaurant.Data;

/**
 * Created by Luca Sasselli on 14/11/2015.
 */
public class Prodotto {
    public String id;
    public String rist_id;
    public String menu_id;
    public String nome_menu;
    public String nome_prodotto;
    public String descr;
    public String hot;
    public double prezzo;

    public Prodotto(String _id, String _rist_id, String _menu_id, String _nome_menu, String _nome_prodotto, String _descr, double _prezzo, String _hot){
        id=_id;
        rist_id=_rist_id;
        menu_id=_menu_id;
        nome_prodotto=_nome_prodotto;
        descr=_descr;
        prezzo=_prezzo;
        nome_menu=_nome_menu;
        hot=_hot;
    }
}
