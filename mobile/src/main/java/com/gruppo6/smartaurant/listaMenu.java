package com.gruppo6.smartaurant;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.gruppo6.smartaurant.Adapter.InternetAdapter;
import com.gruppo6.smartaurant.Adapter.listaMenuAdapter;
import com.gruppo6.smartaurant.Data.Prodotto;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class listaMenu extends Activity {

    String URL = "http://smartaurant.alangiu.com/api-php";

    ListView listProdotti;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_menu);

        Intent intent = getIntent();
        String id_ristorante = intent.getStringExtra("idRistorante");

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("s", "dish"));
        params.add(new BasicNameValuePair("a", "menu_cat"));
        InternetAdapter downloadRestaurants = new InternetAdapter(this, "GET", URL, params, new InternetAdapter.onRequestCompleted() {
            @Override
            public void onRequestCompleted(String result) {

            }
        });
        downloadRestaurants.sendRequest();

        List<Prodotto> prodotti = new ArrayList<Prodotto>();

        ///////////
        Prodotto test = new Prodotto();
        test.nome_prodotto = "Pizza Margerita";
        test.prezzo = 3;
        test.descr = "Un grande classico...";

        prodotti.add(test);

        ////


        listProdotti = (ListView) findViewById(R.id.listProdotti);

        ListAdapter listAdapter = new listaMenuAdapter(this, R.layout.item_lista_menu, prodotti);
        listProdotti.setAdapter(listAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lista_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
