package com.gruppo6.smartaurant.Service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
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
import com.gruppo6.smartaurant.Adapter.InternetAdapter;
import com.gruppo6.smartaurant.Adapter.listaRistorantiAdapter;
import com.gruppo6.smartaurant.Data.Ristorante;
import com.gruppo6.smartaurant.listaMenu;

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

public class DataListenerService extends WearableListenerService {

    public static final String LOG = "DATALISTENER_LOG";
    public int CONNECTION_TIME_OUT_MS = 10000;

    String URL = "http://smartaurant.alangiu.com/api.php";

    GoogleApiClient mGoogleApiClient;
    String nodeId;

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        mGoogleApiClient = getGoogleApiClient(this);
        mGoogleApiClient.connect();

        String request = messageEvent.getPath();
        nodeId = messageEvent.getSourceNodeId();
        Log.d(LOG, "Request received: " + request);
        InternetAdapter downloadRestaurants = new InternetAdapter(this, "GET", URL, request, new InternetAdapter.onRequestCompleted() {
            @Override
            public void onRequestCompleted(String result) {
                sendPackage(result);
            }
        });
        downloadRestaurants.sendRequest();
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
                }
                //mGoogleApiClient.disconnect();
            }
        }).start();
    }

    private GoogleApiClient getGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    private void sendPackage(final String str) {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                    PendingResult<MessageApi.SendMessageResult> pendingResult = Wearable.MessageApi.sendMessage(mGoogleApiClient, nodeId, str, null);
                    pendingResult.setResultCallback(new ResultCallback<MessageApi.SendMessageResult> () {
                        @Override
                        public void onResult(final MessageApi.SendMessageResult result) {
                            if(result.getStatus().isSuccess()) {
                                Log.d(LOG, "Response sent to " + nodeId);
                            }else{
                                Log.d(LOG, "Can't send package!");
                            }
                        }
                    });
                }
            }).start();
        }else{
            Log.d(LOG, "NODEID IS NULL!!!!!!!!!!!!");
        }
    }
}