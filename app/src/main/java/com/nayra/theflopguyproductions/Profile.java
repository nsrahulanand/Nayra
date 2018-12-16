package com.nayra.theflopguyproductions;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class Profile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    CircularImageView UsDPProfile;
    TextView UserProfile;
    private static final int GALLERY_PICK = 1;

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        Toolbar Profiletoolbar = (Toolbar) findViewById(R.id.ProfileToolbar);
        setSupportActionBar(Profiletoolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.ProfileLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, Profiletoolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        UsDPProfile = (CircularImageView) findViewById(R.id.UserDpProfile);
        UserProfile = (TextView) findViewById(R.id.UserNameProfile);



        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    startActivity(new Intent(Profile.this, SignIn.class));
                }
                else{
                    //ENTIRE PROFILE SETUP HERE
                    UsDPProfile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent galleryIntent = new Intent();
                            galleryIntent.setType("image/*");
                            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                            startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLERY_PICK);
                        }
                    });
                    FirebaseDatabase.getInstance().getReference().child("USERS").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PROFILE").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String UserNameProfile = dataSnapshot.child("Name").getValue().toString();
                            UserProfile.setText(UserNameProfile);
                            String Image = dataSnapshot.child("Image").getValue().toString();
                            final String ThumbImage = dataSnapshot.child("ThumbImage").getValue().toString();

                            if (!Image.equals("default")) {

                                // Picasso.get().load(ThumbImage).placeholder(R.mipmap.ic_user).into(setDp);
                                Picasso.get().load(ThumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.ic_user).into(UsDPProfile, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Picasso.get().load(ThumbImage).placeholder(R.drawable.ic_user).into(UsDPProfile);
                                    }
                                });

                            }
                            else{
                                Picasso.get().load(ThumbImage).placeholder(R.drawable.ic_user).into(UsDPProfile);
                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        };

    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.ProfileLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.ProfileLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(this);

            //Toast.makeText(SettingsActivity.this, imageUri, Toast.LENGTH_LONG).show();

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {


                Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());
                String current_userid=FirebaseAuth.getInstance().getCurrentUser().getUid();

                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
                final byte[] ThumbByte = baos.toByteArray();


                StorageReference filepath = FirebaseStorage.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profile_images").child(current_userid+".jpg");

                final StorageReference thumbPath = FirebaseStorage.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("profile_images").child("thumbs").child(current_userid+".jpg");


                filepath.putFile(resultUri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                if (task.isSuccessful()) {
                                    final String download_url = task.getResult().getDownloadUrl().toString();
                                    UploadTask uploadTask = thumbPath.putBytes(ThumbByte);
                                    uploadTask
                                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> ThumbTask) {
                                                    String thumbDownload = ThumbTask.getResult().getDownloadUrl().toString();
                                                    if (ThumbTask.isSuccessful()) {

                                                        Map update_hashMap = new HashMap();
                                                        update_hashMap.put("Image", download_url);
                                                        update_hashMap.put("ThumbImage", thumbDownload);

                                                        FirebaseDatabase.getInstance().getReference().child("USERS").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PROFILE").updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(Profile.this, "Profile Picture Changed!", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(Profile.this, "Error in upload thumb", Toast.LENGTH_LONG).show();

                                                    }
                                                }
                                            });


                                } else {
                                    Toast.makeText(Profile.this, "Error in upload", Toast.LENGTH_LONG).show();


                                }
                            }

                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                int progress = (int) ((100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount());
                            }
                        });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
