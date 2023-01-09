package com.example.multimediav2.Models;

import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.List;

import Modules.Paras;

public class MyAdapter extends BaseAdapter implements SpinnerAdapter {
    /**
     * The internal data (the ArrayList with the Objects).
     */
    private final List<DropData> data;
    private LayoutInflater inflater;
    public MyAdapter(List<DropData> data){
        this.data = data;
    }

    /**
     * Returns the Size of the ArrayList
     */
    @Override
    public int getCount() {
        return data.size();
    }

    /**
     * Returns one Element of the ArrayList
     * at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }
    /**
     * Returns the View that is shown when a element was
     * selected.
     */
    @Override
    public View getView(int position, View recycle, ViewGroup parent) {
        TextView text;
        if (recycle != null){
            // Re-use the recycled view here!
            text = (TextView) recycle;
        } else {
            inflater=LayoutInflater.from(Paras.appContext);
            // No recycled view, inflate the "original" from the platform:
            text = (TextView) inflater.inflate(
                    android.R.layout.simple_dropdown_item_1line, parent, false
            );
        }
        text.setTextColor(Color.BLACK);
        text.setText(data.get(position).getName());
        return text;
    }
    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }
}
