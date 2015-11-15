package com.gruppo6.smartaurant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.gruppo6.smartaurant.Adapter.InternetAdapter;
import com.gruppo6.smartaurant.Adapter.listaMenuAdapter;
import com.gruppo6.smartaurant.Data.Prodotto;
import com.gruppo6.smartaurant.Data.Ristorante;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

public class listaMenu extends Activity {

    ProgressDialog progress;

    final String LOG = "LISTAMENU_LOG";
    String URL = "http://smartaurant.alangiu.com/api.php";

    ListView listProdotti;
    List<Prodotto> prodotti = new ArrayList<Prodotto>();
    Context ctx;
    String id_ristorante;
    ListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_menu);
        ctx=this;

        progress = ProgressDialog.show(ctx, "Cerco il menu'", "caricamento...", true, false);

        Intent intent = getIntent();
        id_ristorante = intent.getStringExtra("idRistorante");
        //Toast.makeText(ctx, id_ristorante, Toast.LENGTH_LONG).show();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("s", "dish"));
        params.add(new BasicNameValuePair("a", "all"));
        params.add(new BasicNameValuePair("restId", id_ristorante));
        InternetAdapter downloadRestaurants = new InternetAdapter(ctx, "GET", URL, params, new InternetAdapter.onRequestCompleted() {
            @Override
            public void onRequestCompleted(String result) {
                //Toast.makeText(ctx, result, Toast.LENGTH_LONG).show();

                progress.dismiss();
                prodotti.clear();

                try{
                    JSONArray jsonArray = new JSONArray(result);
                    for(int i=0; i<jsonArray.length(); i++){
                        JSONObject jrist = jsonArray.getJSONObject(i);
                        Prodotto buffer = new Prodotto(jrist.getString("DishId"),id_ristorante, jrist.getString("MenuId"), jrist.getString("MenuName"), jrist.getString("DishName"),jrist.getString("Description"),Double.valueOf(jrist.getString("Price")),jrist.getString("HotProduct"));
                        prodotti.add(buffer);
                    }

                }catch (JSONException e){
                    //
                }

                List<Prodotto> prodotti_sorted = new ArrayList<Prodotto>();
                int i=0;
                while(prodotti.size()>0){
                    for(int j=0; j<prodotti.size();j++){
                        if(Integer.parseInt(prodotti.get(j).menu_id)==i){
                            prodotti_sorted.add(prodotti.get(j));
                            prodotti.remove(j);
                            j--;
                        }
                    }
                    i++;
                }
                prodotti.clear();
                for(i=0;i<prodotti_sorted.size();i++){
                    prodotti.add(prodotti_sorted.get(i));
                }

                listAdapter = new listaMenuAdapter(ctx, R.layout.item_lista_menu, prodotti);
                listProdotti.setAdapter(listAdapter);
            }
        });
        downloadRestaurants.sendRequest();

        ///////////
        //Prodotto test = new Prodotto("0", "0", "0", "Pizza Margherita", "Nata a Napoli nel 1889, dall’estro del pizzaiolo napoletano Raffaele Esposito, la pizza margherita fu creata in occasione della visita della Regina Margherita, allora sovrana d’Italia insieme al Re Umberto I, alla meravigliosa città di Napoli.", 3.5);

        //prodotti.add(test);
        ////


        listProdotti = (ListView) findViewById(R.id.listProdotti);

        listAdapter = new listaMenuAdapter(this, R.layout.item_lista_menu, prodotti);

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
