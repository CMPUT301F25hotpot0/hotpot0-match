package com.example.hotpot0.section3.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.hotpot0.R;

import java.util.ArrayList;

public class AdminImageAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<String> imageUrls;

    public AdminImageAdapter(Context context, ArrayList<String> urls) {
        this.context = context;
        this.imageUrls = urls;
    }

    public void updateData(ArrayList<String> newUrls) {
        this.imageUrls = newUrls;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public Object getItem(int i) {
        return imageUrls.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.grid_image, parent, false);
        }

        ImageView img = convertView.findViewById(R.id.gridImage);

        Glide.with(context)
                .load(imageUrls.get(position))
                .centerCrop()
                .into(img);

        return convertView;
    }
}
