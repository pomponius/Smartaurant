package com.gruppo6.smartaurant.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gruppo6.smartaurant.Data.Prodotto;
import com.gruppo6.smartaurant.R;
import com.gruppo6.smartaurant.confermaOrdine;
import com.gruppo6.smartaurant.listaMenu;

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
                Intent intent = new Intent(context, confermaOrdine.class);
                context.startActivity(intent);
            }
        });

        return convertView;

    }

}