package com.nayra.theflopguyproductions;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ethanhua.skeleton.Skeleton;
import com.ethanhua.skeleton.SkeletonScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class UserChat extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "Nayra";
    String Count;
    FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    Boolean flagFab = true;
    Animation animFadein;
    Animation animPanin;
    Animation animPanout;
    Animation animFadeout;
    Animation animRecordOn,animRecordOff;
    Button ShareBtn;
    Button CameraBtn;
    ImageView EmojiBtn;
    EditText ChatMessage;
    RelativeLayout ChataddBtn;
    ImageView fab_img,PartLargeDp;
    RecyclerView UserChatRecyclerView;
    DatabaseReference ref;
    private static final int CAMERA_REQUEST_CODE=1;
    TextView UserNameText, PartnerName;
    CircularImageView PartnerImage, UserImage;
    TextView UserLocationText, NayraButton;
    private MediaRecorder mRecorder;
    private String mFileName = null, mFileNameText;
    private static final String LOG_TAG = "";
    String ReceivedID;
    private StorageReference mStorage;
    private static final int requestCode= 100;
    private ProgressDialog mProgressbar;
    private TextView TimerVoice, LastSeen;
    private ShimmerTextView TimerText;
    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    Handler handler;
    int Seconds, Minutes, MilliSeconds ;
    private float x1,x2;
    static final int MIN_DISTANCE = 150;
    public static final int RECORD_AUDIO = 0;
    Shimmer shimmer;
    Button VoiceBtn;
    MediaPlayer VoiceNotePlayer;
    private Handler VoiceNoteHandler;
    int pauseCurrentPosition = 0;
    int FLAG = 0;
    DatabaseReference refdb, refrc;
    private final List<UserMessageSetGet> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private UserMessageAdapter MessageAdapter;

    //LOCATION SHIT INITIALISE
    Double latitude,longitude;
    SharedPreferences mPref;
    SharedPreferences.Editor medit;
    Geocoder geocoder;

    //LASTSEEN SHIT INITIALISE
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            medit.putString("service", "").commit();
            mAuth.addAuthStateListener(mAuthListener);
            FirebaseDatabase.getInstance().getReference().child("USERS").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PROFILE").child("LastSeen").setValue("Online");
        }
        else{
            startActivity(new Intent(UserChat.this, SignIn.class));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);

        String MainPath = Environment.getDataDirectory().getAbsolutePath().toString() + "/storage/emulated/0/Nayra";
        String DocPath = Environment.getDataDirectory().getAbsolutePath().toString() + "/storage/emulated/0/Nayra/Media/Nayra Documents";
        String VoicePath = Environment.getDataDirectory().getAbsolutePath().toString() + "/storage/emulated/0/Nayra/Media/Nayra VoiceNotes";
        String VideoPath = Environment.getDataDirectory().getAbsolutePath().toString() + "/storage/emulated/0/Nayra/Media/Nayra Video";
        String PhotoPath = Environment.getDataDirectory().getAbsolutePath().toString() + "/storage/emulated/0/Nayra/Media/Nayra Photos";
        String PhotoReceivePath = Environment.getDataDirectory().getAbsolutePath().toString() + "/storage/emulated/0/Nayra/Media/Nayra Photos/Received";
        String VoiceReceivePath = Environment.getDataDirectory().getAbsolutePath().toString() + "/storage/emulated/0/Nayra/Media/Nayra VoiceNotes/Received";
        String ProfilePic = Environment.getDataDirectory().getAbsolutePath().toString() + "/storage/emulated/0/Nayra/Media/Profile Images";
        File Main = new File(MainPath);
        File Voice = new File(VoicePath);
        File Doc = new File(DocPath);
        File Video = new File(VideoPath);
        File Photo = new File(PhotoPath);
        File PhotoRec = new File(PhotoReceivePath);
        File ProfilePicPath = new File(ProfilePic);
        File VoiceReceive = new File(VoiceReceivePath);
        if (!Main.exists() || !Voice.exists() || !Doc.exists() || !Video.exists() || !Photo.exists() || !VoiceReceive.exists() || !ProfilePicPath.exists() || !PhotoRec.exists()) {
            Main.mkdir();
            Video.mkdir();
            Voice.mkdir();
            Doc.mkdir();
            Photo.mkdir();
            PhotoRec.mkdir();
            VoiceReceive.mkdir();
            ProfilePicPath.mkdir();
        }

        File ProfilePics = new File("/sdcard/Nayra/Media/Profile Images");
        File Directory = new File("/sdcard/Nayra/");
        File Videos = new File("/sdcard/Nayra/Media/Nayra Video");
        File Document = new File("/sdcard/Nayra/Media/Nayra Documents");
        File Photos = new File("/sdcard/Nayra/Media/Nayra Photos");
        File PhotosRec = new File("/sdcard/Nayra/Media/Nayra Photos/Received");
        File VoiceNotes = new File("/sdcard/Nayra/Media/Nayra VoiceNotes");
        File VoiceReceiveDir = new File ("/sdcard/Nayra/Media/Nayra VoiceNotes/Received");

        VoiceReceiveDir.mkdirs();
        ProfilePics.mkdirs();
        Directory.mkdirs();
        Videos.mkdirs();
        Document.mkdirs();
        PhotosRec.mkdirs();
        Photos.mkdirs();
        VoiceNotes.mkdirs();

        //TOOLBAR INITIALISE
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //DRAWER LAYOUT INITIALISE
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        //BUTTONS VIEWS BULLSHIT INITIALISE
        VoiceBtn = (Button) findViewById(R.id.CheckVoice);
        PartnerImage = (CircularImageView) findViewById(R.id.PartnerDp);
        PartLargeDp = (ImageView) findViewById(R.id.largePartDP);
        UserImage = (CircularImageView) header.findViewById(R.id.userDp);
        UserNameText = (TextView) header.findViewById(R.id.UserName);
        UserLocationText = (TextView) header.findViewById(R.id.UserLocation);
        EmojiBtn = (ImageView) findViewById(R.id.ChatEmoji);
        ChataddBtn = (RelativeLayout)findViewById(R.id.ChatAddBtn);
        ChataddBtn.setVisibility(View.GONE);
        ChatMessage = (EditText)findViewById(R.id.ChatText);
        ShareBtn = (Button) findViewById(R.id.ChatAttach);
        animFadeout = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pan_right);
        animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pan_left);
        animPanin = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pan_in);
        animPanout = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.pan_out);
        CameraBtn = (Button) findViewById(R.id.ChatCamera);
        NayraButton = (TextView) findViewById(R.id.NayraChat);
        mProgressbar = new ProgressDialog(this);
        TimerVoice = (TextView) findViewById(R.id.TimerText);
        TimerText = (ShimmerTextView) findViewById(R.id.SwipeToCancel);
        TimerText.setVisibility(View.INVISIBLE);
        TimerVoice.setVisibility(View.INVISIBLE);
        handler = new Handler() ;
        shimmer = new Shimmer();
        shimmer.setDirection(Shimmer.ANIMATION_DIRECTION_RTL);
        shimmer.setDuration(1500);
        shimmer.start(TimerText);
        LastSeen = (TextView) findViewById(R.id.LastSeenText);

        //CHAT VIEW SHIT INITIALISE
        UserChatRecyclerView = (RecyclerView) findViewById(R.id.UserChatMessageList);
        mLinearLayout = new LinearLayoutManager(this);
        UserChatRecyclerView.setHasFixedSize(true);
        mLinearLayout.setReverseLayout(true);
        UserChatRecyclerView.setLayoutManager(mLinearLayout);
        MessageAdapter = new UserMessageAdapter(messagesList);
        UserChatRecyclerView.setAdapter(MessageAdapter);


        //FIREBASE SHIT INITIALISE
        ref = FirebaseDatabase.getInstance().getReference();
        ref.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();

        //LOCATION SHIT INITIALISE
        mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        medit = mPref.edit();
        geocoder = new Geocoder(this, Locale.getDefault());

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null)
                {
                    startActivity(new Intent(UserChat.this, SignIn.class));
                }
                else if (firebaseAuth.getCurrentUser() != null)
                {
                    FirebaseDatabase.getInstance().getReference().child("CONNECTED").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                final FirebaseStorage storage = FirebaseStorage.getInstance();
                                refdb = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("CHAT");
                                FirebaseDatabase.getInstance().getReference().child("USERS").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PROFILE").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String Name = dataSnapshot.child("Name").getValue().toString();
                                        //String Location = dataSnapshot.child("Location").getValue().toString();
                                        UserNameText.setText(Name);
                                        UserLocationText.setText("Tap to refresh");

                                        final String Image = dataSnapshot.child("Image").getValue().toString();
                                        final String ThumbImage = dataSnapshot.child("ThumbImage").getValue().toString();

                                        if (!Image.equals("default")) {

                                            // Picasso.get().load(ThumbImage).placeholder(R.mipmap.ic_user).into(setDp);
                                            Picasso.get().load(ThumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.ic_user).into(UserImage, new Callback() {
                                                @Override
                                                public void onSuccess() {

                                                }

                                                @Override
                                                public void onError(Exception e) {
                                                    Picasso.get().load(ThumbImage).placeholder(R.drawable.ic_user).into(UserImage);
                                                }
                                            });

                                        } else {
                                            Picasso.get().load(ThumbImage).placeholder(R.drawable.ic_user).into(UserImage);
                                        }

                                        UserImage.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                startActivity(new Intent(UserChat.this, Profile.class));
                                            }
                                        });

                                        if (!mPref.getString("Location","").matches("")) {
                                            UserLocationText.setText(mPref.getString("Location",""));
                                        }

                                        UserLocationText.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                UserLocationText.setText("Refreshing");
                                                if (ActivityCompat.checkSelfPermission(UserChat.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                                    ActivityCompat.requestPermissions(UserChat.this, new String[]{
                                                                    Manifest.permission.ACCESS_FINE_LOCATION
                                                            },
                                                            10);
                                                    UserLocationText.setText("Tap to refresh");

                                                } else {
                                                    if (mPref.getString("service", "").matches("") || mPref.getString("Location","").matches("")) {
                                                        medit.putString("service", "service").commit();
                                                        Intent intent = new Intent(getApplicationContext(), LocationProvider.class);
                                                        startService(intent);
                                                    } else {
                                                        UserLocationText.setText(mPref.getString("Location",""));
                                                    }
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                FirebaseDatabase.getInstance().getReference().child("CONNECTED").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChildren()) {
                                            for (DataSnapshot uniqueKeySnapshot : dataSnapshot.getChildren()) {
                                                //Toast.makeText(UserChat.this, uniqueKeySnapshot.getKey(), Toast.LENGTH_LONG).show();
                                                ReceivedID = uniqueKeySnapshot.getKey().toString();
                                                loadMessages();

                                                FirebaseDatabase.getInstance().getReference().child("USERS").child(ReceivedID).child("PROFILE").addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        final String Image = dataSnapshot.child("Image").getValue().toString();
                                                        final String ThumbImage = dataSnapshot.child("ThumbImage").getValue().toString();

                                                        if (!Image.equals("default")) {
                                                            // Picasso.get().load(ThumbImage).placeholder(R.mipmap.ic_user).into(setDp);
                                                            Picasso.get().load(ThumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.ic_user).into(PartnerImage, new Callback() {
                                                                @Override
                                                                public void onSuccess() {
                                                                }
                                                                @Override
                                                                public void onError(Exception e) {
                                                                    Picasso.get().load(ThumbImage).placeholder(R.drawable.ic_user).into(PartnerImage);
                                                                    Picasso.get().load(ThumbImage).placeholder(R.drawable.ic_user).into(PartLargeDp);
                                                                }
                                                            });
                                                        } else {
                                                            Picasso.get().load(ThumbImage).placeholder(R.drawable.ic_user).into(PartnerImage);
                                                            Picasso.get().load(Image).placeholder(R.drawable.ic_user).into(PartLargeDp);
                                                        }
                                                    }
                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                            }

                                            FirebaseDatabase.getInstance().getReference().child("USERS").child(ReceivedID).child("PROFILE").child("LastSeen").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    if (dataSnapshot.getValue().equals("Online")) {
                                                        LastSeen.setText("Online");
                                                        stopService(new Intent(UserChat.this,GetTimeAgo.class));
                                                        PartnerImage.setBorderColor(getResources().getColor(R.color.Online));
                                                    } else {
                                                        PartnerImage.setBorderColor(getResources().getColor(R.color.Offline));
                                                        //GetTimeAgo getTimeAgo = new GetTimeAgo();
                                                        long lastTime = Long.parseLong(dataSnapshot.getValue().toString());
                                                        startService(new Intent(UserChat.this,GetTimeAgo.class));
                                                        String lastSeenTime = GetTimeAgo.getTimeAgo(lastTime,getApplicationContext());
                                                        LastSeen.setText(lastSeenTime);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                ShareBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                    }
                                });
                                ChataddBtn.setOnClickListener(new View.OnClickListener() {
                                    @SuppressLint("StaticFieldLeak")
                                    @Override
                                    public void onClick(View v) {
                                        String message = ChatMessage.getText().toString().trim();
                                        ChatMessage.setText("");
                                    }
                                });

                                VoiceBtn.setOnTouchListener(new View.OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {

                                        switch (event.getAction()) {
                                            case MotionEvent.ACTION_DOWN:
                                                ChataddBtn.setVisibility(View.VISIBLE);
                                                ShareBtn.setVisibility(View.INVISIBLE);
                                                CameraBtn.setVisibility(View.INVISIBLE);
                                                ChatMessage.setVisibility(View.INVISIBLE);
                                                EmojiBtn.setVisibility(View.INVISIBLE);
                                                x1 = event.getX();
                                                VoiceBtn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                                                if (ActivityCompat.checkSelfPermission(UserChat.this, Manifest.permission.RECORD_AUDIO)
                                                        != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(UserChat.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                        != PackageManager.PERMISSION_GRANTED) {
                                                    ActivityCompat.requestPermissions(UserChat.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                            10);
                                                } else {
                                                    handler.postDelayed(runnable, 0);
                                                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
                                                    String now = formatter.format(new Date());
                                                    mFileNameText = "Nayra_" + now + ".amr";
                                                    mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Nayra/Media/Nayra VoiceNotes/" + mFileNameText;
                                                    startRecording();
                                                }
                                                break;
                                            case MotionEvent.ACTION_UP:

                                                x2 = event.getX();
                                                ChataddBtn.setVisibility(View.GONE);
                                                CameraBtn.setVisibility(View.VISIBLE);
                                                ChatMessage.setVisibility(View.VISIBLE);
                                                ShareBtn.setVisibility(View.VISIBLE);
                                                EmojiBtn.setVisibility(View.VISIBLE);
                                                float deltaX = x2 - x1;
                                                if (Math.abs(deltaX) > MIN_DISTANCE) {
                                                    Toast.makeText(UserChat.this, "Recording Cancelled", Toast.LENGTH_SHORT).show();
                                                    TimerVoice.setVisibility(View.GONE);
                                                    TimerText.setVisibility(View.GONE);
                                                    VoiceBtn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                                                    MillisecondTime = 0L;
                                                    StartTime = 0L;
                                                    TimeBuff = 0L;
                                                    UpdateTime = 0L;
                                                    Seconds = 0;
                                                    Minutes = 0;
                                                    MilliSeconds = 0;
                                                    handler.removeCallbacks(runnable);
                                                    mRecorder.release();
                                                } else {
                                                    TimerText.setVisibility(View.GONE);
                                                    TimerVoice.setVisibility(View.GONE);
                                                    VoiceBtn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                                                    MillisecondTime = 0L;
                                                    StartTime = 0L;
                                                    TimeBuff = 0L;
                                                    UpdateTime = 0L;
                                                    Seconds = 0;
                                                    Minutes = 0;
                                                    MilliSeconds = 0;
                                                    handler.removeCallbacks(runnable);
                                                    stopRecording();
                                                }
                                                break;
                                            case MotionEvent.ACTION_MOVE:
                                                x2 = event.getX();
                                                float deltaaX = x2 - x1;
                                                if (Math.abs(deltaaX) > MIN_DISTANCE) {
                                                    VoiceBtn.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                                                }
                                                break;
                                        }
                                        return false;
                                    }
                                });
                                CameraBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (ActivityCompat.checkSelfPermission(UserChat.this, Manifest.permission.CAMERA)
                                                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(UserChat.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                != PackageManager.PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(UserChat.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                    10);
                                        } else {
                                            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                                            if (intent.resolveActivity(getPackageManager()) != null) {
                                                FirebaseDatabase.getInstance().getReference().child("USERS").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PROFILE").child("LastSeen").setValue("Online");
                                                startActivityForResult(intent, CAMERA_REQUEST_CODE);
                                            }
                                        }
                                    }
                                });
                                ChatMessage.addTextChangedListener(new TextWatcher() {
                                    @Override
                                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                    }

                                    @Override
                                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                                        final Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                FirebaseDatabase.getInstance().getReference().child("USERS").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PROFILE").child("LastSeen").setValue("Typing...");
                                                final Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        FirebaseDatabase.getInstance().getReference().child("USERS").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PROFILE").child("LastSeen").setValue("Online");
                                                    }
                                                }, 1000);
                                            }
                                        }, 1000);

                                        if (s.toString().trim().length() != 0 && flagFab) {
                                            ChataddBtn.setVisibility(View.VISIBLE);
                                            VoiceBtn.setVisibility(View.INVISIBLE);
                                            ShareBtn.startAnimation(animPanout);
                                            animPanout.setFillAfter(true);
                                            CameraBtn.startAnimation(animFadeout);
                                            CameraBtn.setVisibility(View.INVISIBLE);
                                            Count = String.valueOf(s.length());
                                            flagFab = false;

                                        } else if (s.toString().trim().length() == 0) {
                                            VoiceBtn.setVisibility(View.VISIBLE);
                                            ChataddBtn.setVisibility(View.GONE);
                                            FirebaseDatabase.getInstance().getReference().child("USERS").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PROFILE").child("LastSeen").setValue("Online");
                                            CameraBtn.setVisibility(View.VISIBLE);
                                            ShareBtn.startAnimation(animPanin);
                                            CameraBtn.startAnimation(animFadein);
                                            flagFab = true;
                                        }
                                    }
                                    @Override
                                    public void afterTextChanged(Editable s) {
                                    }
                                });

                                PartnerImage.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        PartLargeDp.setVisibility(View.VISIBLE);
                                        PartnerImage.setVisibility(View.INVISIBLE);
                                    }
                                });
                                PartLargeDp.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        PartLargeDp.setVisibility(View.INVISIBLE);
                                        PartnerImage.setVisibility(View.VISIBLE);
                                    }
                                });
                                NayraButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toasty.info(UserChat.this, "Nayra Chat Coming Soon!", Toast.LENGTH_SHORT, true).show();
                                    }
                                });


                                ChataddBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        sendMessage();
                                    }
                                });

                            }
                            else{
                                startActivity(new Intent(UserChat.this,SignIn.class));
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
    private boolean IsRecyclerViewAtTop()   {
        if(UserChatRecyclerView.getChildCount() == 0)
            return true;
        return UserChatRecyclerView.getChildAt(0).getTop() == 0;
    }

    private void loadMessages() {

        FirebaseDatabase.getInstance().getReference().child("CHAT").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(ReceivedID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                UserMessageSetGet message = dataSnapshot.getValue(UserMessageSetGet.class);
                messagesList.add(message);
                MessageAdapter.notifyDataSetChanged();

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {
        String Message = ChatMessage.getText().toString();
        if(!TextUtils.isEmpty(Message)){

            String CurrentUserReference = "CHAT/"+FirebaseAuth.getInstance().getCurrentUser().getUid()+"/"+ReceivedID;
            String PartnerReference = "CHAT/"+ReceivedID+"/"+FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference UserMessagePush = FirebaseDatabase.getInstance().getReference().child("CHAT").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(ReceivedID)
                    .push();
            String PushID = UserMessagePush.getKey();
            DatabaseReference PartnerMessagePush = FirebaseDatabase.getInstance().getReference().child("CHAT").child(ReceivedID).child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .push();
            Map messageMap = new HashMap();
            messageMap.put("Message",Message);
            messageMap.put("Seen",false);
            messageMap.put("Type","Text");
            messageMap.put("Time",ServerValue.TIMESTAMP);
            messageMap.put("User",FirebaseAuth.getInstance().getCurrentUser().getUid());

            Map MessageAddMap = new HashMap();
            MessageAddMap.put(CurrentUserReference+"/"+PushID,messageMap);
            MessageAddMap.put(PartnerReference+"/"+PushID,messageMap);
            ChatMessage.setText("");
            FirebaseDatabase.getInstance().getReference().updateChildren(MessageAddMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                    if(databaseError!=null){
                        Log.d("MESSAGE",databaseError.getMessage().toString());

                    }
                }
            });
        }
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            latitude = Double.valueOf(intent.getStringExtra("latutide"));
            longitude = Double.valueOf(intent.getStringExtra("longitude"));
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                String cityName = addresses.get(0).getLocality();
                String stateName = addresses.get(0).getAdminArea();
                String countryName = addresses.get(0).getCountryName();
                String LocationReceived = cityName+", "+stateName+", "+countryName;
                UserLocationText.setText(LocationReceived);
                FirebaseDatabase.getInstance().getReference().child("USERS").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PROFILE").child("Location").setValue(LocationReceived);
                medit.putString("Location", LocationReceived).commit();

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            registerReceiver(broadcastReceiver, new IntentFilter(LocationProvider.str_receiver));

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }else{
                //User denied Permission.
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null) {
            medit.putString("service", "").commit();
        }
    }

    private void startRecording() {

        mStorage = FirebaseStorage.getInstance().getReference();
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        try {
            mRecorder.prepare();
            TimerVoice.setVisibility(View.VISIBLE);
            TimerText.setVisibility(View.VISIBLE);
            StartTime = SystemClock.uptimeMillis();
            mRecorder.start();
            } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopRecording() {
        try{
            mRecorder.stop();
            uploadAudio();
        }catch (RuntimeException e) {
        }
        if (mRecorder != null) {
            // clear recorder configuration
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }
        shimmer.cancel();
    }

    public Runnable runnable = new Runnable() {
        public void run() {
            MillisecondTime = SystemClock.uptimeMillis() - StartTime;
            UpdateTime = TimeBuff + MillisecondTime;
            Seconds = (int) (UpdateTime / 1000);
            Minutes = Seconds / 60;
            Seconds = Seconds % 60;
            MilliSeconds = (int) (UpdateTime % 1000);
            TimerVoice.setText("" + Minutes + ":" + String.format("%02d", Seconds)+" ");
            handler.postDelayed(this, 0);
        }
    };

    private void uploadAudio() {
        //ChatMessage chatMessage = new ChatMessage("Uploading", FirebaseAuth.getInstance().getCurrentUser().getUid(), formattedDate, "VoiceUp", mFileName);
        //FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("CHAT").push().setValue(chatMessage);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            FirebaseDatabase.getInstance().getReference().child("USERS").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PROFILE").child("LastSeen").setValue(ServerValue.TIMESTAMP);
            moveTaskToBack(true);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_logout) {
            medit.putString("service", "").commit();
            medit.putString("Location", "").commit();
            FirebaseDatabase.getInstance().getReference().child("USERS").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("PROFILE").child("LastSeen").setValue(ServerValue.TIMESTAMP);
            mAuth.signOut();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}

