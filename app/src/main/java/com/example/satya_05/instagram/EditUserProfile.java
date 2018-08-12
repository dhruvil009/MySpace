package com.example.satya_05.instagram;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class EditUserProfile extends AppCompatActivity {

    Button done;
    ImageButton user_image;
    EditText user_name, name, user_desc;
    private static final int CHOOSE_IMAGE_CODE = 123;
    FirebaseDatabase database;
    DatabaseReference ref;
    FirebaseAuth auth;
    StorageReference storageReference;
    ProgressBar image_progressbar;
    Uri uriProfileImage;
    String user_profile_image_url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        done = (Button)findViewById(R.id.done);
        user_image = (ImageButton)findViewById(R.id.edit_profile_user_image);
        user_name = findViewById(R.id.edit_profile_user_name);
        user_desc = findViewById(R.id.edit_profile_user_desc);
        name = findViewById(R.id.edit_profile_name);
        image_progressbar = findViewById(R.id.image_upload_progressbar);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        ref = database.getReference("User Details/" + auth.getCurrentUser().getUid());

        if(!getIntent().getBooleanExtra("new user",false)) {
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    EditUser user = dataSnapshot.getValue(EditUser.class);
                    if(user != null){
                        name.setText(user.getName());
                        user_name.setText(user.getUsername());
                        user_desc.setText(user.getDescription());
                        user_profile_image_url = user.getProfileImageUrl();
                        Picasso.get().load(user.getProfileImageUrl()).into(user_image);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user_id = auth.getCurrentUser().getUid();
                ref.child("username").setValue(user_name.getText().toString());
                ref.child("name").setValue(name.getText().toString());
                ref.child("description").setValue(user_desc.getText().toString());
                if(user_profile_image_url != null && !user_profile_image_url.equals("")){
                    ref.child("profileImageUrl").setValue(user_profile_image_url);
                }else{
                    ref.child("profileImageUrl").setValue("https://firebasestorage.googleapis.com/v0/b/instagram-64367.appspot.com/o/ProfilePics%2Fprofile-pictures.jpg?alt=media&token=ae73e3ca-5a9e-495c-81ec-1885119337f1");
                }

                Intent intent = new Intent(EditUserProfile.this,UserFeed.class);
                notification();
                intent.putExtra("fragment",true);
                startActivity(intent);
                finish();
            }
        });

        user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), CHOOSE_IMAGE_CODE);
            }
        });
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CHOOSE_IMAGE_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                user_image.setImageBitmap(bitmap);

                //Upload the image to firebase
                uploadImageToFirebaseStorage();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebaseStorage() {
        storageReference = FirebaseStorage.getInstance().getReference().child("ProfilePics/"+auth.getCurrentUser().getUid()+".jpg");
        if(uriProfileImage != null){
            image_progressbar.setVisibility(View.VISIBLE);
            storageReference.putFile(uriProfileImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    image_progressbar.setVisibility(View.GONE);
                    user_profile_image_url = taskSnapshot.getDownloadUrl().toString();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(), "Select image again", Toast.LENGTH_SHORT).show();
                    image_progressbar.setVisibility(View.GONE);
                }
            });
        }
    }
    private void notification() {
        // Set Notification Title
        String strtitle = getString(R.string.notificationtitle);
        // Set Notification Text
        String strtext = getString(R.string.notificationtext);

        // Open  Class on Notification Click
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.putExtra("title", strtitle);
        intent.putExtra("text", strtext);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.logo)
                .setTicker(getString(R.string.notificationticker))
                .setContentTitle(getString(R.string.notificationtitle))
                .setContentText(getString(R.string.notificationtext))
                .setContentIntent(pIntent)
                .setAutoCancel(true);
        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationmanager.notify(0, builder.build());
    }
}
class EditUser{
    String name = "No name";
    String username;
    String description;
    String profileImageUrl;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}


