package com.gruppo6.smartaurant;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Luca Sasselli on 15/11/2015.
 */
public class internetActivity extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
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

        mGoogleApiClient = getGoogleApiClient(ctx);
        mGoogleApiClient.connect();
    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        String response = messageEvent.getPath();
        onMessageReceived(response);

    }

    public void onMessageReceived(String string) {;

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


    private GoogleApiClient getGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .build();

    }

    public void sendMessage(final String str) {
        if (nodeId != null) {
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
                            sendMessage(Common.params2string(params));
                        }catch (UnsupportedEncodingException e){
                            //
                        }
                    }
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
