package com.example.satya_05.instagram;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by satya_05 on 18/4/18.
 */

public class AdapterClass extends RecyclerView.Adapter<AdapterClass.PostViewHolder>{

    ArrayList<PostClass> list;
    Context context;

    public AdapterClass(ArrayList<PostClass> list, Context context){
        this.context = context;
        this.list = list;
    }
    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.user_post_layout, parent, false);
        return new PostViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        position = list.size()-position-1;
        holder.user_name.setText(list.get(position).user_name);
        holder.user_post_desc.setText(list.get(position).getPostdescription());
        Picasso.get().load(list.get(position).getPostimageurl()).into(holder.user_post_image);
        Picasso.get().load(list.get(position).getProfileimageurl()).into(holder.user_image);
        holder.date.setText(list.get(position).getDate());
        holder.no_likes.setText(list.get(position).getNooflikes()+" Likes");
        holder.location.setText(list.get(position).getLocation());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        TextView user_name;
        ImageView user_post_image;
        ImageView user_image;
        TextView user_post_desc;
        TextView date;
        TextView location;
        TextView no_likes;
        ImageButton like_button;
        public PostViewHolder(View itemView) {
            super(itemView);
            user_name = itemView.findViewById(R.id.user_post_username);
            user_post_image = itemView.findViewById(R.id.user_post_postImage);
            user_image = itemView.findViewById(R.id.user_post_userimage);
            user_post_desc = itemView.findViewById(R.id.user_post_postDescription);
            date = itemView.findViewById(R.id.user_post_date);
            no_likes = itemView.findViewById(R.id.user_post_likes);
            location = itemView.findViewById(R.id.user_post_location);
        }
    }
}
