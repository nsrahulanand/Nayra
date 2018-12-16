package com.nayra.theflopguyproductions;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.materialtextfield.MaterialTextField;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class Invitations extends AppCompatActivity {

    Button Positive, Negative, Logout;
    TextView Intruct;
    ProgressBar InviteProg;
    EditText PartnerMail;
    String PartnerEmailID;
    String PartnerUID;
    private FirebaseAuth mAuth;
    FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mRegProgress;
    MaterialTextField Email;

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitations);

        Positive = (Button) findViewById(R.id.SendButton);
        Intruct = (TextView) findViewById(R.id.SendInstruct);
        InviteProg = (ProgressBar) findViewById(R.id.ProgressInvite);
        PartnerMail = (EditText) findViewById(R.id.PartnerEmail);
        Logout = (Button) findViewById(R.id.logout);
        mAuth = FirebaseAuth.getInstance();
        mRegProgress = new ProgressDialog(this);
        Email = (MaterialTextField) findViewById(R.id.materialTextField);
        final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Email.expand();
                imm.showSoftInput(PartnerMail,0);

                //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            }
        });
        Intruct.setText("Enter your Partner's registered E-Mail ID and send the invite to proceed!");
        Positive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PartnerEmailID = PartnerMail.getText().toString();
                Positive.setVisibility(View.INVISIBLE);
                PartnerMail.setVisibility(View.INVISIBLE);
                Query EmailCheck = FirebaseDatabase.getInstance().getReference().child("mailers").orderByChild("Email").equalTo(PartnerEmailID);
                EmailCheck.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (final DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            PartnerUID = userSnapshot.getKey();
                            FirebaseDatabase.getInstance().getReference().child("Invitations").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .child("RECEIVED").child(PartnerUID).child("ID")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.exists()){
                                                mRegProgress.setTitle("Let's get you connected");
                                                mRegProgress.setMessage("Juust a second");
                                                mRegProgress.setCanceledOnTouchOutside(false);
                                                mRegProgress.show();
                                                String ReceiverID = (String) dataSnapshot.getValue();
                                                Intruct.setText(ReceiverID);
                                                FirebaseDatabase.getInstance().getReference().child("Invitations").child(ReceiverID).removeValue();
                                                FirebaseDatabase.getInstance().getReference().child("Invitations").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
                                                FirebaseDatabase.getInstance().getReference().child("CONNECTED").child(ReceiverID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("ID").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                FirebaseDatabase.getInstance().getReference().child("CONNECTED").child(ReceiverID).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Moment").setValue(ServerValue.TIMESTAMP);
                                                FirebaseDatabase.getInstance().getReference().child("CONNECTED").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(ReceiverID).child("Moment").setValue(ServerValue.TIMESTAMP);
                                                FirebaseDatabase.getInstance().getReference().child("CONNECTED").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(ReceiverID).child("ID").setValue(ReceiverID).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        SharedPreferences.Editor editor = getSharedPreferences("INVITESTAT", MODE_PRIVATE).edit();
                                                        editor.putString("STAT", "CONNECTED");
                                                        editor.commit();
                                                        mRegProgress.hide();
                                                        startActivity(new Intent(Invitations.this, UserChat.class));
                                                        finish();
                                                    }
                                                });
                                            }
                                            else{
                                                mRegProgress.setTitle("Sending the Invite");
                                                mRegProgress.setMessage("Juust a second");
                                                mRegProgress.setCanceledOnTouchOutside(false);
                                                mRegProgress.show();
                                                SharedPreferences.Editor editor = getSharedPreferences("INVITESTAT", MODE_PRIVATE).edit();
                                                editor.putString("Email", PartnerEmailID);
                                                editor.putString("UID", PartnerUID);
                                                editor.putString("STAT", "SENT");
                                                editor.commit();
                                                FirebaseDatabase.getInstance().getReference().child("Invitations").child(PartnerUID).child("RECEIVED").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("ID").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                                FirebaseDatabase.getInstance().getReference().child("Invitations").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("SENT").child(PartnerUID).child("ID").setValue(PartnerUID).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mRegProgress.hide();
                                                        Toast.makeText(Invitations.this, "Invitation successfully sent!", Toast.LENGTH_LONG).show();
                                                        startActivity(new Intent(Invitations.this, InviteCheckStage.class));
                                                    }
                                                });
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

            }
        });

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                SharedPreferences.Editor editor = getSharedPreferences("INVITESTAT", MODE_PRIVATE).edit();
                editor.clear();
                editor.commit();
                startActivity(new Intent(Invitations.this, SignIn.class));

            }
        });

    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.findViewById(android.R.id.content);
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    @Override
    public void onBackPressed() {

        moveTaskToBack(true);
    }
}
