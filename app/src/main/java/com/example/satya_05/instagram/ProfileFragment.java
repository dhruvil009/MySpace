package com.example.satya_05.instagram;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    ImageButton editProfile;
    TextView user_name, name, user_desc;
    ImageView user_profile_image;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference ref;
    LinearLayout wholeProfile;
    ProgressBar progressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_profile, container, false);
        editProfile = view.findViewById(R.id.editProfile);
        user_name = view.findViewById(R.id.user_profile_user_name);
        name = view.findViewById(R.id.user_profile_name);
        user_desc = view.findViewById(R.id.user_profile_desc);
        user_profile_image = view.findViewById(R.id.user_profile_image);
        progressBar = view.findViewById(R.id.progressBar_load);
        progressBar.setVisibility(View.VISIBLE);
        wholeProfile = view.findViewById(R.id.whole_profile);

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        ref = firebaseDatabase.getReference("User Details/"+auth.getCurrentUser().getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                User user = dataSnapshot.getValue(User.class);
                if(user == null){
                    ref.child("username").setValue(user_name.getText().toString());
                    ref.child("name").setValue(name.getText().toString());
                    ref.child("description").setValue(user_desc.getText().toString());
                    ref.child("profileImageUrl").setValue("https://firebasestorage.googleapis.com/v0/b/instagram-64367.appspot.com/o/ProfilePics%2Fprofile-pictures.jpg?alt=media&token=ae73e3ca-5a9e-495c-81ec-1885119337f1");
                }else{
                    name.setText(user.getName());
                    user_name.setText(user.getUsername());
                    user_desc.setText(user.getDescription());
                    Picasso.get().load(user.getProfileImageUrl()).into(user_profile_image);
                }
                progressBar.setVisibility(View.GONE);
                wholeProfile.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), EditUserProfile.class));
            }
        });

        return view;
    }
}
