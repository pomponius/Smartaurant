package com.gruppo6.smartaurant.Adapter;

/**
 * Created by Luca Sasselli on 14/11/2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.gruppo6.smartaurant.Data.Prodotto;
import com.gruppo6.smartaurant.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class listaMenuAdapter extends ArrayAdapter<Prodotto> {

    String URL = "http://smartaurant.alangiu.com/api.php";
    Context context;
    int layoutResourceId;
    List<Prodotto> data = new ArrayList<Prodotto>();
    int session;

    public listaMenuAdapter(Context _context, int _layoutResourceId, List<Prodotto> _data, int _session) {
        super(_context, _layoutResourceId, _data);
        layoutResourceId = _layoutResourceId;
        session=_session;
        context = _context;
        data.clear();
        data.addAll(_data);

        for(int i=0; i<data.size();i++){
            if(i==0) {
                data.add(i, new Prodotto("0", "0", "-1", "Primo", "Pizza Margherita", "Nata", 1, "0"));
                i++;
            }
            else {
                if(Integer.parseInt(data.get(i).menu_id)!=Integer.parseInt(data.get(i-1).menu_id)) {
                    data.add(i, new Prodotto("0", "0", "-1", "Primo", "Pizza Margherita", "Nata", 1, "0"));
                    i++;
                }
            }

        }

    }

    @Override
    public int getCount(){
        return data.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

       /* if(convertView==null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }*/

        final Prodotto current = data.get(position);
        if(Integer.parseInt(current.menu_id)==-1){

            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.item_lista_menu_header, parent, false);
            TextView view_header = (TextView) convertView.findViewById(R.id.header);
            view_header.setText(data.get(position+1).nome_menu);

            return convertView;

        } else{
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }



        TextView view_nome = (TextView) convertView.findViewById(R.id.prodotto_nome);
        TextView view_prezzo = (TextView) convertView.findViewById(R.id.prodotto_prezzo);

        view_nome.setText(current.nome_prodotto);
        view_prezzo.setText(String.valueOf(current.prezzo)+ " €");


        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                final View dialog_view = LayoutInflater.from(context).inflate(R.layout.dialog_lista_menu, null);
                builder.setTitle(current.nome_prodotto);
                builder.setView(dialog_view);


                final TextView text_descrizione = (TextView) dialog_view.findViewById(R.id.prodotto_descrizione);
                text_descrizione.setText(current.descr);

                final TextView text_quant = (TextView) dialog_view.findViewById(R.id.prodotto_quant);

                final SeekBar seek_quant = (SeekBar) dialog_view.findViewById(R.id.seek_quant);
                seek_quant.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {


                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                        //Toast.makeText(getBaseContext(), "discrete = " + String.valueOf(discrete), Toast.LENGTH_SHORT).show();
                    }


                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub

                    }


                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // TODO Auto-generated method stub
                        text_quant.setText(String.valueOf(progress + 1));

                    }
                });

                seek_quant.setMax(20);
                seek_quant.setProgress(0);

                if (session != -1) {
                    builder.setPositiveButton("ORDINA", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            int a = seek_quant.getProgress()+1;
                            List<NameValuePair> params = new ArrayList<NameValuePair>();
                            params.add(new BasicNameValuePair("s", "order"));
                            params.add(new BasicNameValuePair("a", "new"));
                            params.add(new BasicNameValuePair("restId", current.rist_id));
                            params.add(new BasicNameValuePair("sessId", ""+session));
                            params.add(new BasicNameValuePair("dishId", current.id));
                            params.add(new BasicNameValuePair("qty", ""+a));
                            InternetAdapter buy = new InternetAdapter(context, "GET", URL, params, new InternetAdapter.onRequestCompleted() {
                                @Override
                                public void onRequestCompleted(String result) {
                                    Toast.makeText(context, "Ordine effettuato!", Toast.LENGTH_LONG).show();
                                }
                            });
                            buy.sendRequest();

                        }
                    });
                }
                builder.setNegativeButton("Indietro", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });





                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return convertView;

    }

}