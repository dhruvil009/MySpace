package com.example.satya_05.instagram;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FeedsFragment extends Fragment {
    FirebaseDatabase database;
    DatabaseReference reference;
    DatabaseReference reference2;
    TextView user_name;
    RecyclerView recyclerView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_feeds, container, false);
        recyclerView = v.findViewById(R.id.feed_recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Posts");
        reference2 = database.getReference("User Details/");

        user_name = v.findViewById(R.id.user_post_username);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<PostClass> posts = new ArrayList<PostClass>();
                for(DataSnapshot post: dataSnapshot.getChildren()){
                    PostClass ps = post.getValue(PostClass.class);
                    posts.add(ps);
                }
                AdapterClass adapterClass = new AdapterClass(posts, getContext());
                recyclerView.setAdapter(adapterClass);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return v;
    }
}
