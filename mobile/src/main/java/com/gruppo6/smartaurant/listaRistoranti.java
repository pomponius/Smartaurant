package com.gruppo6.smartaurant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
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
    int session;
    String id_ristorante;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_ristoranti);

        ctx=this;

        SharedPreferences prefs = getSharedPreferences("MY_PREF", MODE_PRIVATE);
        session = prefs.getInt("session", -1);
        id_ristorante = prefs.getString("restId", "0");

        if(session!=-1){
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("s", "session"));
            params.add(new BasicNameValuePair("a", "closed"));
            params.add(new BasicNameValuePair("restId", ""+id_ristorante));
            params.add(new BasicNameValuePair("sessId", "" + session));
            InternetAdapter connected = new InternetAdapter(ctx, "GET", URL, params, new InternetAdapter.onRequestCompleted() {
                @Override
                public void onRequestCompleted(String result) {
                    for( int i=1; i<result.length(); i++ ) {
                        if( result.charAt(i)<'0'||result.charAt(i)>'9') {
                            result=result.substring(1, i);
                            break;
                        }
                    }
                    int closed=0;
                    try {
                        closed = Integer.parseInt(result);

                    } catch(NumberFormatException e){
                        Toast.makeText(ctx, "Errore! non riesco a capire se sei in un ristorante", Toast.LENGTH_LONG).show();
                    }
                    if(closed==0){
                        Intent intent = new Intent(ctx, listaMenu.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("idRistorante", id_ristorante);
                        startActivity(intent);
                        finish();
                    } else {
                        session=-1;
                        SharedPreferences.Editor editor = getSharedPreferences("MY_PREF", MODE_PRIVATE).edit();
                        editor.putInt("session", -1);
                        editor.commit();
                    }



                }
            });
            connected.sendRequest();
        }




        myListaRistoranti=(ListView) findViewById(R.id.list_ristoranti);

        progress = ProgressDialog.show(ctx, "Cerco ristoranti nei paraggi", "caricamento...", true, false);

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("s", "restaurant"));
        params.add(new BasicNameValuePair("a", "list"));
        InternetAdapter downloadRestaurants = new InternetAdapter(ctx, "GET", URL, params, new InternetAdapter.onRequestCompleted() {
            @Override
            public void onRequestCompleted(String result) {
                Log.d(LOG, "Data item receive: " + result);
                //Log.d(LOG, "Data item receive: " + result);
                //Toast.makeText(ctx, result, Toast.LENGTH_SHORT).show();

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

                listaRistorantiAdapter adapter = new listaRistorantiAdapter(ctx, myRestaurants);
                myListaRistoranti.setAdapter(adapter);
                myListaRistoranti.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                        Intent intent = new Intent(ctx, listaMenu.class);
                        intent.putExtra("idRistorante", myRestaurants.get(position).id);
                        startActivity(intent);
                    }
                });
                progress.dismiss();
            }
        });
        downloadRestaurants.sendRequest();
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
        if (id == R.id.cameriere) {


            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
