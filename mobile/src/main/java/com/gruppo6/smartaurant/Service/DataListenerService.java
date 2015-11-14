package com.gruppo6.smartaurant.Service;

import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DataListenerService extends WearableListenerService {

    public static final String LOG = "DATALISTENER_LOG";
    public static final String TIME_KEY = "TIME";
    public static final String MESSAGE_KEY = "MESSAGE";

    GoogleApiClient mGoogleApiClient;

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (Log.isLoggable(LOG, Log.DEBUG)) {
            Log.d(LOG, "onDataChanged: " + dataEvents);
        }
        final List events = FreezableUtils
                .freezeIterable(dataEvents);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        ConnectionResult connectionResult =
                mGoogleApiClient.blockingConnect(30, TimeUnit.SECONDS);

        if (!connectionResult.isSuccess()) {
            Log.e(LOG, "Failed to connect to GoogleApiClient.");
            return;
        }

        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.d(LOG, "DataItem changed");
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/request") == 0) {
                    final DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    String request = dataMap.getString("REQUEST");
                    sendResponse("RISULTATO");
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    void sendResponse(String response){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/response");
        putDataMapReq.getDataMap().putLong(TIME_KEY, new Date().getTime());
        putDataMapReq.getDataMap().putString(MESSAGE_KEY, response);
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);

        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(final DataApi.DataItemResult result) {
                if(result.getStatus().isSuccess()) {
                    Log.d(LOG, "Data item set: " + result.getDataItem().getUri());
                }
            }
        });
    }
}