package com.example.currencyconverter.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.currencyconverter.MainActivity;
import com.example.currencyconverter.R;
import com.example.currencyconverter.model.Geonames;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends BaseAdapter {
    Context context;
    ArrayList<Geonames> array;

    public CustomAdapter(Context context, ArrayList<Geonames> array) {
        this.context = context;
        this.array = array;
    }

    @Override
    public int getCount() {
        return array.size();
    }

    @Override
    public Object getItem(int position) {
        return array.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Geonames geonames = (Geonames) this.getItem(position);
        View rowView =LayoutInflater.from(parent.getContext()).inflate(R.layout.item_destinationmoney,parent,false);
        TextView textViewItemName = (TextView) rowView.findViewById(R.id.textView_item_name);
        TextView textView_item_percent = (TextView) rowView.findViewById(R.id.textView_item_percent);
        ImageView img_hinhanh = rowView.findViewById(R.id.img_hinhanh);

        textViewItemName.setText(geonames.getCountryName());
        textView_item_percent.setText(geonames.getCurrencyCode());
        Glide.with(context).load("http://img.geonames.org/flags/x/"+geonames.countryCode.toLowerCase()+".gif").into(img_hinhanh);

        return rowView;
    }
}
