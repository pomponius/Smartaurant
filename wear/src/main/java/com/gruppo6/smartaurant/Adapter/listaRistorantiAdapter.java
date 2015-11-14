package com.gruppo6.smartaurant.Adapter;

/**
 * Created by marco on 14/11/2015.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.gruppo6.smartaurant.Data.Ristorante;
import com.gruppo6.smartaurant.R;

import java.util.List;

public class listaRistorantiAdapter extends ArrayAdapter<Ristorante> {
    private Context context;
    List<Ristorante> restaurants;


    public listaRistorantiAdapter(Context context, List<Ristorante> r) {
        super(context, R.layout.item_lista_ristoranti, r);
        this.context = context;
        this.restaurants = r;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_lista_ristoranti, parent, false);
        TextView name = (TextView) rowView.findViewById(R.id.rist_name);
        TextView address = (TextView) rowView.findViewById(R.id.rist_address);
        TextView distance = (TextView) rowView.findViewById(R.id.rist_distance);
        name.setText(restaurants.get(position).name);
        address.setText(restaurants.get(position).address);
        distance.setText("Distance");

        return rowView;
    }
}