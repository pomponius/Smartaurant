package com.gruppo6.smartaurant.Adapter;

/**
 * Created by Luca Sasselli on 14/11/2015.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gruppo6.smartaurant.Data.Prodotto;
import com.gruppo6.smartaurant.R;

import java.util.ArrayList;
import java.util.List;

public class listaMenuAdapter extends ArrayAdapter<Prodotto> {

    Context context;
    int layoutResourceId;
    List<Prodotto> data = new ArrayList<Prodotto>();

    public listaMenuAdapter(Context _context, int _layoutResourceId, List<Prodotto> _data) {
        super(_context, _layoutResourceId, _data);
        layoutResourceId = _layoutResourceId;
        context = _context;
        data.clear();
        data.addAll(_data);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if(convertView==null){
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        final Prodotto current = data.get(position);

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
                builder.setPositiveButton("Compra", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                    }
                });
                builder.setNegativeButton("Indietro", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

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
                        text_quant.setText(String.valueOf(progress+1));

                    }
                });

                seek_quant.setMax(20);
                seek_quant.setProgress(0);

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return convertView;

    }

}