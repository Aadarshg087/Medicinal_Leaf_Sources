package com.example.leafdetectionapp;


import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

// 8- Creating and configuring This class (ADAPTER)
public class RecyclerViewAdaptery extends
        RecyclerView.Adapter<RecyclerViewAdaptery.ImageViewHolder> {

    // Variables
    Context mContext;
    List<row> mData;

    // Constructor
    public RecyclerViewAdaptery(Context mContext, List<row> mData) {
        this.mContext = mContext;
        this.mData = mData;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 9- ImageView Holder - Binding views
        // 10- Creating RecyclerView Item Layout
        View view = LayoutInflater.from(mContext).
                inflate(R.layout.recyclerview_item,
                parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {

        // Adding Glide library to load the images faster
        // Binding image here
        // USING GLIDE LIBRARY

        row r = mData.get(position);

        holder.txt.setText(r.getName());
        holder.img.setImageResource(r.getImg());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }



    public class ImageViewHolder extends RecyclerView.ViewHolder implements
            View.OnClickListener{

        ImageView img;

        TextView txt;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imageView);
            txt = itemView.findViewById(R.id.textView3);

            // Click Listeners
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int position = getLayoutPosition();

            if(position == 0){
                Intent i = new Intent(view.getContext(), Camera.class);
                view.getContext().startActivity(i);
            }else if(position == 1){
                Intent i = new Intent(view.getContext(), Gallery.class);
                view.getContext().startActivity(i);
            }


        }
    }
}
