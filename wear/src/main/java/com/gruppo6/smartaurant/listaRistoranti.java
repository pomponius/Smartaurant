package com.gruppo6.smartaurant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.gruppo6.smartaurant.Adapter.InternetAdapter;
import com.gruppo6.smartaurant.Adapter.listaRistorantiAdapter;
import com.gruppo6.smartaurant.Data.Ristorante;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



public class listaRistoranti extends Activity {
    Context ctx;
    ListView myListaRistoranti;
    List<Ristorante> myRestaurants= new ArrayList<>();

    final String LOG = "LISTRISTORANTI_LOG";
    String URL = "http://smartaurant.alangiu.com/api.php";

    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_ristoranti);

        ctx=this;

        //myListaRistoranti=(ListView) findViewById(R.id.list_ristoranti);
        myRestaurants.add(new Ristorante("Rist1", "via doge", 0, 0, "1"));

        progress = ProgressDialog.show(ctx, "Looking for restaurats", "loading...", true, false);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("s", "restaurant"));
        params.add(new BasicNameValuePair("a", "list"));
        InternetAdapter downloadRestaurants = new InternetAdapter(ctx, "GET", URL, params, new InternetAdapter.onRequestCompleted() {
            @Override
            public void onRequestCompleted(String result) {
                Log.d(LOG, "Data item receive: " + result);
                Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();

                myRestaurants.clear();

                try{
                    JSONArray jsonArray = new JSONArray(result);
                    for(int i=0; i<jsonArray.length(); i++){
                        JSONObject jrist = jsonArray.getJSONObject(i);
                        Ristorante buffer = new Ristorante (jrist.getString("Name"),jrist.getString("Address"), Double.valueOf(jrist.getString("Latitude")), Double.valueOf(jrist.getString("Longitude")),jrist.getString("RestId"));
                        myRestaurants.add(buffer);
                    }

                }catch (JSONException e){
                    //
                }

                //listaRistorantiAdapter adapter = new listaRistorantiAdapter(ctx, myRestaurants);
                //myListaRistoranti.setAdapter(adapter);
                //myListaRistoranti.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    //@Override
                    //public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                        //Intent intent = new Intent(ctx, listaMenu.class);
                        //intent.putExtra("idRistorante", myRestaurants.get(position).id);
                        //startActivity(intent);
                   //}
                //});
                progress.dismiss();
            }
        });
        downloadRestaurants.sendRequest();
    }
}