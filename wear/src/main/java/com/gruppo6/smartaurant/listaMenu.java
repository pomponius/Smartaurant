package com.gruppo6.smartaurant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.gruppo6.smartaurant.Adapter.listaMenuAdapter;
import com.gruppo6.smartaurant.Adapter.listaRistorantiAdapter;
import com.gruppo6.smartaurant.Data.Prodotto;
import com.gruppo6.smartaurant.Data.Ristorante;
import com.gruppo6.smartaurant.Utils.Common;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class listaMenu extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    final String LOG = "LISTAMENU_LOG";
    String URL = "http://smartaurant.alangiu.com/api.php";

    public int CONNECTION_TIME_OUT_MS = 10000;

    GoogleApiClient mGoogleApiClient;

    ProgressDialog progress;

    String nodeId;

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

        mGoogleApiClient = getGoogleApiClient(ctx);
        mGoogleApiClient.connect();

        retrieveDeviceNode();

        ///////////
        //Prodotto test = new Prodotto("0", "0", "0", "Pizza Margherita", "Nata a Napoli nel 1889, dall’estro del pizzaiolo napoletano Raffaele Esposito, la pizza margherita fu creata in occasione della visita della Regina Margherita, allora sovrana d’Italia insieme al Re Umberto I, alla meravigliosa città di Napoli.", 3.5);

        //prodotti.add(test);
        ////


        listProdotti = (ListView) findViewById(R.id.listProdotti);

        listAdapter = new listaMenuAdapter(this, R.layout.item_lista_menu, prodotti);

    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {

                String result = messageEvent.getPath();

                //Toast.makeText(ctx, result, Toast.LENGTH_LONG).show();

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

                progress.dismiss();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.MessageApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.unregisterConnectionCallbacks(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progress != null) {
            progress.dismiss();
            progress = null;
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }

    private void retrieveDeviceNode() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult result =
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                List<Node> nodes = result.getNodes();
                if (nodes.size() > 0) {
                    nodeId = nodes.get(0).getId();
                    Log.d(LOG, "NodeId: " + nodeId);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("s", "dish"));
                    params.add(new BasicNameValuePair("a", "all"));
                    params.add(new BasicNameValuePair("restId", id_ristorante));

                    try {
                        sendPackage(Common.params2string(params));
                    }catch (UnsupportedEncodingException e){
                        //
                    }
                }
                //mGoogleApiClient.disconnect();
            }
        }).start();
    }

    private GoogleApiClient getGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

    }

    private void sendPackage(final String str) {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, str, null);
                    Log.d(LOG, "Package sent to " +nodeId + " data was " + str);
                    //mGoogleApiClient.disconnect();
                }
            }).start();
        }else{
            Log.d(LOG, "NODEID IS NULL!!!!!!!!!!!!");
        }
    }
}
