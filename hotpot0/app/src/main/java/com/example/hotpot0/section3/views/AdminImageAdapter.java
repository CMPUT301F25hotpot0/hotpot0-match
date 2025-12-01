package com.example.hotpot0.section3.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hotpot0.R;

import java.util.ArrayList;
import java.util.Map;

public class AdminImageAdapter extends RecyclerView.Adapter<AdminImageAdapter.ViewHolder> {

    public interface DeleteCallback {
        void onDelete(String url);
    }

    public interface OnItemClickListener {
        void onItemClick(String url);
    }

    private Context context;
    private ArrayList<String> urls;
    private DeleteCallback deleteCallback;
    private OnItemClickListener clickListener;
    private Map<Integer, String> eventNames;
    private java.util.function.Function<String, Integer> extractEventId;

    public AdminImageAdapter(Context context,
                             ArrayList<String> urls,
                             DeleteCallback deleteCallback,
                             OnItemClickListener clickListener,
                             Map<Integer, String> eventNames,
                             java.util.function.Function<String, Integer> extractEventId) {
        this.context = context;
        this.urls = urls;
        this.deleteCallback = deleteCallback;
        this.clickListener = clickListener;
        this.eventNames = eventNames;
        this.extractEventId = extractEventId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.admin_images_blob, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = urls.get(position);

        Glide.with(context).load(url).into(holder.imageView);

        int eventId = extractEventId.apply(url);
        String eventName = eventNames.get(eventId);

        // Safely set text
        if (eventName != null) {
            holder.filename.setText(eventName);
        } else if (eventId != -1) {
            holder.filename.setText(String.valueOf(eventId));
        } else {
            holder.filename.setText("Unknown Event");
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(url);
        });

    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView filename;
        ImageButton deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            filename = itemView.findViewById(R.id.imageTitleTextView);
            deleteBtn = itemView.findViewById(R.id.remove_image_button);
        }
    }
}

