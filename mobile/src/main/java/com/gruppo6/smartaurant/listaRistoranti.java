package com.gruppo6.smartaurant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



public class listaRistoranti extends Activity {

    Context ctx;
    ListView myListaRistoranti;
    List<Ristorante> myRestaurants= new ArrayList<>();

    String URL="http://smartaurant.alangiu.com/api.php";

    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_ristoranti);

        ctx=this;

        myListaRistoranti=(ListView) findViewById(R.id.list_ristoranti);




        myRestaurants.add(new Ristorante("Rist1", "via doge", 0, 0, "1"));

        progress = ProgressDialog.show(ctx, "Looking for restaurats", "loading...", true, false);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("s", "restaurant"));
        params.add(new BasicNameValuePair("a", "list"));
        InternetAdapter downloadRestaurants = new InternetAdapter(ctx, "GET", URL, params, new InternetAdapter.onRequestCompleted() {
            @Override
            public void onRequestCompleted(String result) {
                progress.dismiss();

                myRestaurants.add(new Ristorante("Rist2", "via doge", 0, 0, "2"));

                listaRistorantiAdapter adapter = new listaRistorantiAdapter(ctx, myRestaurants);
                myListaRistoranti.setAdapter(adapter);

            }
        });
        downloadRestaurants.sendRequest();




        listaRistorantiAdapter adapter = new listaRistorantiAdapter(ctx, myRestaurants);
        myListaRistoranti.setAdapter(adapter);
        myListaRistoranti.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                Intent intent = new Intent(ctx, listaMenu.class);

                //intent.putExtra("idRistorante", myRestaurants.get(position).id);
                Toast.makeText(ctx, myRestaurants.get(position).name, Toast.LENGTH_SHORT).show();
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lista_ristoranti, menu);
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
