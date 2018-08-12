package com.example.satya_05.instagram;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class AddPostFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    DatabaseReference databaseReference;
    DatabaseReference user_reference;
    Button addPost;
    ImageButton uploadImage;
    String user_name;
    String user_photo_url;
    EditText post_desc;
    String post_image_url;

    boolean checkpermission = false;
    private static final int LocationRequestCode = 1234;
    FusedLocationProviderClient fusedLocationProviderClient;
    Geocoder geocoder;
    String displaylocation;
    ProgressBar progressBar;

    Uri postImageUri;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_add_post, container, false);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        addPost = v.findViewById(R.id.add_post_done);
        post_desc = v.findViewById(R.id.add_post_desc);
        uploadImage = v.findViewById(R.id.add_post_image);
        progressBar = v.findViewById(R.id.progressBar_upload_post);

        databaseReference = firebaseDatabase.getReference("Posts/"+ Calendar.getInstance().getTimeInMillis());
        user_reference = firebaseDatabase.getReference("User Details/"+firebaseAuth.getCurrentUser().getUid());
        //storageReference = firebaseStorage.getReference();

        user_reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                EditUser user = dataSnapshot.getValue(EditUser.class);
                if(user != null){
                    user_name = user.getUsername();
                    user_photo_url = user.getProfileImageUrl();
                }else{
                    user_name = "No Name";
                    user_photo_url = "No url";
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child("user_name").setValue(user_name);
                databaseReference.child("profileimageurl").setValue(user_photo_url);
                databaseReference.child("postdescription").setValue(post_desc.getText().toString());
                databaseReference.child("nooflikes").setValue(0);
                databaseReference.child("postimageurl").setValue(post_image_url);
                databaseReference.child("user_id").setValue(firebaseAuth.getCurrentUser().getUid());
                databaseReference.child("location").setValue(displaylocation);
                //Getting Date
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
                databaseReference.child("date").setValue(df.format(c.getTime()));
            }
        });


        uploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), 123);
            }
        });

        //Fetch Location

        getLocationPermission();
        if(checkpermission){
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
            try{
                final com.google.android.gms.tasks.Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task task) {
                        if (task.isSuccessful()) {
                            Location mlocation = (Location) task.getResult();

                            geocoder = new Geocoder(getContext());

                            List<Address> list = new ArrayList<>();

                            try{
                                list = geocoder.getFromLocation(mlocation.getLatitude(),mlocation.getLongitude(),1);
                            }catch(IOException e){
                                e.printStackTrace();
                            }

                            if(list.size() > 0){
                                Address address = list.get(0);
                                displaylocation = address.getLocality() + "," + address.getCountryName();
                                //Toast.makeText(getContext(),displaylocation,Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
            }catch (SecurityException e){
                e.printStackTrace();
            }
        }

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 123 && resultCode == RESULT_OK && data != null && data.getData() != null){
            postImageUri = data.getData();
            uploadImage.setImageURI(postImageUri);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), postImageUri);
                uploadImage.setImageBitmap(bitmap);

                //Upload the image to firebase
                uploadImageToFirebaseStorage();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Toast.makeText(getContext(), "Could not get Image", Toast.LENGTH_SHORT).show();
        }

    }

    private void uploadImageToFirebaseStorage() {
        progressBar.setVisibility(View.VISIBLE);
        storageReference = FirebaseStorage.getInstance().getReference().child("PostImages/"+Calendar.getInstance().getTimeInMillis()+".jpg");
        if(postImageUri != null){
            storageReference.putFile(postImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    post_image_url = taskSnapshot.getDownloadUrl().toString();
                    progressBar.setVisibility(View.GONE);
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    public void getLocationPermission(){
        String permissions[] = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                checkpermission = true;
                //Toast.makeText(getContext(),"Permission given",Toast.LENGTH_SHORT).show();
            }
            else{
                requestPermissions(permissions,LocationRequestCode);
                //Toast.makeText(getContext(),"Permission requested",Toast.LENGTH_SHORT).show();
            }
        }
        else{
            requestPermissions(permissions,LocationRequestCode);
            //Toast.makeText(getContext(),"Permission requested",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        checkpermission = false;

        switch (requestCode){
            case LocationRequestCode:
                if(grantResults.length > 0){
                    for(int i=0;i<grantResults.length;i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            checkpermission = false;
                            //Toast.makeText(getContext(),"Permission not given",Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    checkpermission = true;
                    //Toast.makeText(getContext(),"Permission given",Toast.LENGTH_SHORT).show();
                }
        }
    }

}
