package com.nayra.theflopguyproductions;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.File;
import java.util.HashMap;

public class SignIn extends AppCompatActivity {

    private KenBurnsView AnimateFill;
    private FirebaseAuth mAuth;

    private Button GoogleSignIn;
    GoogleApiClient mGoogleApiClient;
    FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mRegProgress;
    private final static int RC_SIGN_IN = 2;
    private VideoView videobg;
    MediaPlayer mMediaPlayer;
    int mCurrentVideoPosition;
    private DatabaseReference mUserData;
    private ConstraintLayout MainBack;

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/CenturyGothic.ttf");
        TextView textView = findViewById(R.id.curr_location);
        textView.setTypeface(typeface);

        File folder = getFilesDir();
        File f= new File(folder, "Nayra");
        f.mkdir();


        MainBack = (ConstraintLayout) findViewById(R.id.SignInBG);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        videobg = (VideoView) findViewById(R.id.videoView);
        AnimateFill = (KenBurnsView) findViewById(R.id.AnimateFiller);
        AnimateFill.setVisibility(View.INVISIBLE);
        String uriPath = "android.resource://"+getPackageName()+"/"+R.raw.intro;
        Uri uri = Uri.parse(uriPath);
        videobg.setVideoURI(uri);
        videobg.requestFocus();
        videobg.start();
        videobg.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer = mp;
                mMediaPlayer.setLooping(true);

                if(mCurrentVideoPosition !=0){
                    mMediaPlayer.seekTo(mCurrentVideoPosition);
                    mMediaPlayer.start();
                }
            }
        });

        videobg.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mMediaPlayer = mp;
                mMediaPlayer.stop();
                videobg.setVisibility(View.INVISIBLE);
                AnimateFill.setVisibility(View.VISIBLE);
                return true;
            }
        });

        final GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        GoogleSignIn = (Button) findViewById(R.id.google);
        mRegProgress = new ProgressDialog(this);
        GoogleSignIn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                signIn();
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(SignIn.this)
                .enableAutoManage(SignIn.this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Toast.makeText(SignIn.this, "Internet internet, where did you go?",Toast.LENGTH_SHORT).show();
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
        mRegProgress.setTitle("Checking SignIn");
        mRegProgress.setMessage("Hold your horses!");
        mRegProgress.setCanceledOnTouchOutside(false);
        mRegProgress.show();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                if(firebaseAuth.getCurrentUser() != null) {
                    FirebaseDatabase.getInstance().getReference().child("CONNECTED").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChildren()) {
                                Toast.makeText(SignIn.this, "Welcome Back To Nayra!", Toast.LENGTH_LONG).show();
                                Intent GoToChat = new Intent(SignIn.this, UserChat.class);
                                startActivity(GoToChat);
                                finish();
                            }
                            else{
                                SharedPreferences prefs = getSharedPreferences("INVITESTAT", MODE_PRIVATE);
                                String restoredText = prefs.getString("STAT", null);
                                if (restoredText == "SENT") {
                                    Intent mainIntent = new Intent(SignIn.this, InviteCheckStage.class);
                                    startActivity(mainIntent);
                                    finish();
                                }
                                if (restoredText == "CONNECTED") {
                                    Intent mainIntent = new Intent(SignIn.this, UserChat.class);
                                    startActivity(mainIntent);
                                    finish();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                    mRegProgress.dismiss();
                }
                else{
                    mRegProgress.dismiss();
                }
            }
        };
        mAuth = FirebaseAuth.getInstance();

    }
    private void signIn() {
        //mRegProgress.dismiss();
        mRegProgress.setTitle("Getting started");
        mRegProgress.setMessage("Hold your horses!");
        mRegProgress.setCanceledOnTouchOutside(false);
        mRegProgress.show();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);

        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(result.isSuccess()){
                GoogleSignInAccount account=result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
            else{
                Toast.makeText(SignIn.this, "Error: Is it your internet?",Toast.LENGTH_SHORT).show();
                mRegProgress.dismiss();
            }
        }
    }
    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            String Token = account.getIdToken();
                            SharedPreferences.Editor editor = getSharedPreferences("LoginStat", MODE_PRIVATE).edit();
                            editor.putString("STAT", Token);
                            editor.commit();

                            FirebaseDatabase.getInstance().getReference().child("CONNECTED").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChildren()) {
                                        Toast.makeText(SignIn.this, "Welcome Back To Nayra!", Toast.LENGTH_LONG).show();
                                        Intent GoToChat = new Intent(SignIn.this, UserChat.class);
                                        startActivity(GoToChat);
                                        finish();
                                    }
                                    else{
                                        DatabaseReference userNameRef = FirebaseDatabase.getInstance().getReference().child("USERS").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PROFILE");
                                        ValueEventListener eventListener = new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(!dataSnapshot.exists()) {

                                                    HashMap<String, String> userMap = new HashMap<>();
                                                    userMap.put("Email",account.getEmail());
                                                    userMap.put("Name",account.getDisplayName());
                                                    userMap.put("Image","default");
                                                    userMap.put("ThumbImage","default");
                                                    userMap.put("DOB","13/12/2016");
                                                    userMap.put("LastSeen","New user");
                                                    userMap.put("Location","Not Shared");
                                                    FirebaseDatabase.getInstance().getReference().child("mailers").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Email").setValue(account.getEmail());
                                                    FirebaseDatabase.getInstance().getReference().child("USERS").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PROFILE").setValue(userMap);
                                                    FirebaseDatabase.getInstance().getReference().child("USERS").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("SECURE").child("Token").setValue(FirebaseInstanceId.getInstance().getToken());
                                                    Toast.makeText(SignIn.this, "Welcome To Nayra!", Toast.LENGTH_LONG).show();

                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                                Toast.makeText(SignIn.this, "Error: GUCCIGANG",Toast.LENGTH_SHORT).show();

                                            }
                                        };
                                        userNameRef.addListenerForSingleValueEvent(eventListener);
                                        Toast.makeText(SignIn.this, "Welcome to Nayra!",Toast.LENGTH_SHORT).show();
                                        SharedPreferences prefs = getSharedPreferences("INVITESTAT", MODE_PRIVATE);
                                        String restoredText = prefs.getString("STAT", null);
                                        if (restoredText == "SENT") {
                                            Intent mainIntent = new Intent(SignIn.this, InviteCheckStage.class);
                                            startActivity(mainIntent);
                                            finish();
                                        }
                                        if (restoredText == "CONNECTED") {
                                            Intent mainIntent = new Intent(SignIn.this, UserChat.class);
                                            startActivity(mainIntent);
                                            finish();
                                        }
                                        else{
                                            Intent mainIntent = new Intent(SignIn.this, Invitations.class);
                                            startActivity(mainIntent);
                                            finish();
                                        }
                                    }
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(SignIn.this, "Error: Is it your internet?",Toast.LENGTH_SHORT).show();
                                    mRegProgress.dismiss();

                                }
                            });

                        }
                        else {
                            Toast.makeText(SignIn.this, "Error: Is it your internet?",Toast.LENGTH_SHORT).show();
                            mRegProgress.dismiss();
                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        super.onPause();
        videobg.pause();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        videobg.start();
    }

}
