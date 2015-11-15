package com.gruppo6.smartaurant.Adapter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by marco on 14/11/2015.
 */



public class InternetAdapter {
    private Context ctx;
    private String url="";
    private String method="";
    private String request="";
    private List<NameValuePair> params;
    private onRequestCompleted func;

    public interface onRequestCompleted{
        void onRequestCompleted(String result);
    }

    public InternetAdapter(Context c,String m, String u, List<NameValuePair> p, onRequestCompleted f){
        ctx=c;
        url=u;
        method=m;
        params=p;
        func=f;
    }

    public InternetAdapter(Context c,String m, String u, String r, onRequestCompleted f){
        ctx=c;
        url=u;
        method=m;
        request=r;
        func=f;
    }

    public void sendRequest(){
        myHandler();
    }


    private void myHandler() {
        // Gets the URL from the UI's text field.
        ConnectivityManager connMgr = (ConnectivityManager) ctx.getSystemService(ctx.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(url);
        } else {
            Toast.makeText(ctx, "Network connection is off.", Toast.LENGTH_LONG).show();
            func.onRequestCompleted("Network connection is off.");
        }
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                Toast.makeText(ctx, "I can't connect to the server.", Toast.LENGTH_LONG).show();
                return "I can't connect to the server.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //textView.setText(result);
            //func(result);
            func.onRequestCompleted(result);
        }
    }

    private String downloadUrl(String myurl) throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 30000;

        try {
            if(method.equals("GET")) {
                if (request.isEmpty()){
                    myurl += "?" + URLEncodedUtils.format(params, "utf-8");
                }else{
                    myurl += "?" + request;
                }
            }
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod(method);
            conn.setDoInput(true);

            //List<NameValuePair> params = new ArrayList<NameValuePair>();
            //params.add(new BasicNameValuePair("firstParam", paramValue1));
            //params.add(new BasicNameValuePair("secondParam", paramValue2));
            //params.add(new BasicNameValuePair("thirdParam", paramValue3));

            if(!method.equals("GET")) {
                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));

                if (request.isEmpty()){
                    writer.write(getQuery(params));
                }else {
                    writer.write(request);
                }

                writer.flush();
                writer.close();
                os.close();
            }

            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            if(response != 200) {
                Toast.makeText(ctx, "Error: "+response, Toast.LENGTH_LONG).show();
                return ""+response;
            }
            is = conn.getInputStream();


            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line);
            }
            String contentAsString=total.toString();
            // Convert the InputStream into a string
            //String contentAsString = readIt(is, len);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);
        return new String(buffer);
    }

    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException
    {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }
}
