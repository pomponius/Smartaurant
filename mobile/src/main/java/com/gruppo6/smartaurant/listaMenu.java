package com.gruppo6.smartaurant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    String table;
    ListAdapter listAdapter;
    int session=-1;
    String cameriere="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_menu);
        ctx=this;

        progress = ProgressDialog.show(ctx, "Cerco il menu'", "caricamento...", true, false);

        Intent intent = getIntent();

        SharedPreferences prefs = getSharedPreferences("MY_PREF", MODE_PRIVATE);
        session = prefs.getInt("session", -1);



        if(/*1==0*/getIntent().getData()!=null){//check if intent is not null
            Uri data = getIntent().getData();//set a variable for the Intent
            //String scheme = data.getScheme();//get the scheme (http,https)
            String fullPath = data.getEncodedSchemeSpecificPart();//get the full path -scheme - fragments
            Uri uri=Uri.parse(fullPath);
            id_ristorante=uri.getQueryParameter("restId");

            SharedPreferences.Editor editor = getSharedPreferences("MY_PREF", MODE_PRIVATE).edit();
            editor.putString("restId", id_ristorante);
            editor.commit();

            table=uri.getQueryParameter("tableId");

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("s", "session"));
            params.add(new BasicNameValuePair("a", "new"));
            params.add(new BasicNameValuePair("restId", id_ristorante));
            params.add(new BasicNameValuePair("tableId", table));
            InternetAdapter loginTable = new InternetAdapter(ctx, "GET", URL, params, new InternetAdapter.onRequestCompleted() {
                @Override
                public void onRequestCompleted(String result) {
                    for( int i=0; i<result.length(); i++ ) {
                        if( result.charAt(i)<'0'||result.charAt(i)>'9') {
                            result=result.substring(0, i);
                            break;
                        }
                    }
                    try {

                        session=Integer.parseInt(result);

                        SharedPreferences.Editor editor = getSharedPreferences("MY_PREF", MODE_PRIVATE).edit();
                        editor.putInt("session", session);
                        editor.commit();
                        Toast.makeText(ctx, "Tavolo inserito! Buon appetito!", Toast.LENGTH_LONG).show();
                    } catch(NumberFormatException e){
                        Toast.makeText(ctx, "Non hai ancora pagato!", Toast.LENGTH_LONG).show();

                    }

                    downloadMenu();
                }
            });
            loginTable.sendRequest();


        } else {
            id_ristorante = intent.getStringExtra("idRistorante");
            SharedPreferences.Editor editor = getSharedPreferences("MY_PREF", MODE_PRIVATE).edit();
            editor.putString("restId", id_ristorante);
            editor.commit();
            downloadMenu();
        }







        ///////////
        //Prodotto test = new Prodotto("0", "0", "0", "Pizza Margherita", "Nata a Napoli nel 1889, dall’estro del pizzaiolo napoletano Raffaele Esposito, la pizza margherita fu creata in occasione della visita della Regina Margherita, allora sovrana d’Italia insieme al Re Umberto I, alla meravigliosa città di Napoli.", 3.5);

        //prodotti.add(test);
        ////


        listProdotti = (ListView) findViewById(R.id.listProdotti);

        listAdapter = new listaMenuAdapter(this, R.layout.item_lista_menu, prodotti, session);

    }


    private void downloadMenu(){
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

                listAdapter = new listaMenuAdapter(ctx, R.layout.item_lista_menu, prodotti, session);
                listProdotti.setAdapter(listAdapter);
            }
        });
        downloadRestaurants.sendRequest();

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
        if (id == R.id.cameriere) {

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            final View dialog_view = LayoutInflater.from(ctx).inflate(R.layout.dialog_login_cameriere, null);
            builder.setView(dialog_view);

            final EditText text_username = (EditText) dialog_view.findViewById(R.id.txtUsername);
            final EditText text_password = (EditText) dialog_view.findViewById(R.id.txtPassword);

            builder.setPositiveButton("LOGIN", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    String usr=text_username.getText().toString();
                    String pass=text_password.getText().toString();

                    List<NameValuePair> params = new ArrayList<NameValuePair>();

                    params.add(new BasicNameValuePair("s", "staff"));
                    params.add(new BasicNameValuePair("a", "list"));
                    params.add(new BasicNameValuePair("restId", id_ristorante));
                    params.add(new BasicNameValuePair("user", usr));
                    params.add(new BasicNameValuePair("pass", sha1Hash(pass)));
                    InternetAdapter buy = new InternetAdapter(ctx, "GET", URL, params, new InternetAdapter.onRequestCompleted() {
                        @Override
                        public void onRequestCompleted(String result) {

                            Toast.makeText(ctx, result, Toast.LENGTH_LONG).show();
                            List<String> names = new ArrayList<String>();
                            final List<String> ids = new ArrayList<String>();

                            try{
                                JSONArray jsonArray = new JSONArray(result);
                                for(int i=0; i<jsonArray.length(); i++){
                                    JSONObject jrist = jsonArray.getJSONObject(i);
                                    names.add(jrist.getString("Name"));
                                    ids.add(jrist.getString("StaffId"));
                                }

                                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                                final View dialog_view = LayoutInflater.from(ctx).inflate(R.layout.cameriere_dialog, null);
                                builder.setView(dialog_view);
                                ListView camlist = (ListView) dialog_view.findViewById(R.id.listacameriere);

                                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(ctx,android.R.layout.simple_list_item_1, names);
                                camlist.setAdapter(adapter);
                                camlist.setOnItemClickListener(new AdapterView.OnItemClickListener()
                                {
                                    @Override
                                    public void onItemClick(AdapterView<?> arg0, View arg1,int position, long arg3)
                                    {
                                        cameriere=ids.get(position);
                                        SharedPreferences.Editor editor = getSharedPreferences("MY_PREF", MODE_PRIVATE).edit();
                                        editor.putString("cameriere", cameriere);
                                        editor.commit();
                                    }
                                });

                                AlertDialog dialog = builder.create();
                                dialog.show();

                            }catch (JSONException e){
                                //
                            }



                        }
                    });
                    buy.sendRequest();

                }
            });

            builder.setNegativeButton("Indietro", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });


            AlertDialog dialog = builder.create();
            dialog.show();


            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    String sha1Hash( String toHash )
    {
        String hash = null;
        try
        {
            MessageDigest digest = MessageDigest.getInstance( "SHA-1" );
            byte[] bytes = toHash.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            bytes = digest.digest();

            // This is ~55x faster than looping and String.formating()
            hash = bytesToHex( bytes );
        }
        catch( NoSuchAlgorithmException e )
        {
            e.printStackTrace();
        }
        catch( UnsupportedEncodingException e )
        {
            e.printStackTrace();
        }
        return hash;
    }

    // http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex( byte[] bytes )
    {
        char[] hexChars = new char[ bytes.length * 2 ];
        for( int j = 0; j < bytes.length; j++ )
        {
            int v = bytes[ j ] & 0xFF;
            hexChars[ j * 2 ] = hexArray[ v >>> 4 ];
            hexChars[ j * 2 + 1 ] = hexArray[ v & 0x0F ];
        }
        return new String( hexChars );
    }
}
