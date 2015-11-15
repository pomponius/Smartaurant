package com.gruppo6.smartaurant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.gruppo6.smartaurant.Adapter.listaRistorantiAdapter;
import com.gruppo6.smartaurant.Data.Ristorante;
import com.gruppo6.smartaurant.Utils.Common;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class listaRistoranti extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    Context ctx;
    ListView myListaRistoranti;

    List<Ristorante> myRestaurants = new ArrayList<>();

    final String LOG = "LISTRISTORANTI_WEAR_LOG";

    public int CONNECTION_TIME_OUT_MS = 10000;

    GoogleApiClient mGoogleApiClient;

    ProgressDialog progress;

    String nodeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_ristoranti);

        ctx=this;

        mGoogleApiClient = getGoogleApiClient(ctx);
        mGoogleApiClient.connect();

        retrieveDeviceNode();

        myListaRistoranti=(ListView) findViewById(R.id.list_ristoranti);

        progress = ProgressDialog.show(ctx, "Cerco ristoranti nei paraggi...", "Attendere...", true, false);
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {

                String response = messageEvent.getPath();

                Log.d(LOG, "Response received: " + response);

                myRestaurants.clear();

                try {
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jrist = jsonArray.getJSONObject(i);
                        Ristorante buffer = new Ristorante(jrist.getString("Name"), jrist.getString("Address"), Double.valueOf(jrist.getString("Latitude")), Double.valueOf(jrist.getString("Longitude")), jrist.getString("RestId"));
                        myRestaurants.add(buffer);
                    }

                } catch (JSONException e) {
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
                    Log.d(LOG,"NodeId: " + nodeId);

                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("s", "restaurant"));
                    params.add(new BasicNameValuePair("a", "list"));

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