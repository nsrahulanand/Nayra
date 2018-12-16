package com.nayra.theflopguyproductions;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.florent37.materialtextfield.MaterialTextField;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class InviteCheckStage extends AppCompatActivity {

    Button Negative, LogoutCheck;
    TextView Intruct;
    ProgressBar InviteProg;
    private FirebaseAuth mAuth;
    String PartnerEmailID;
    String PartnerUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_check_stage);

        LogoutCheck = (Button) findViewById(R.id.logoutCheck);
        mAuth = FirebaseAuth.getInstance();
        Intruct =(TextView) findViewById(R.id.CheckText);
        Negative = (Button) findViewById(R.id.DeclineButton);
        InviteProg = (ProgressBar) findViewById(R.id.ProgressInvite);

        SharedPreferences prefs = getSharedPreferences("INVITESTAT", MODE_PRIVATE);
        String restoredText = prefs.getString("Email", null);
        if (restoredText != null) {
            PartnerEmailID = prefs.getString("Email", "No name defined");//"No name defined" is the default value.
            PartnerUID = prefs.getString("UID", "No name defined");//"No name defined" is the default value.
            Intruct.setText("Invite has been sent to :"+PartnerEmailID+". We will now await their response, feel free to get back to your work. I shall notify once they accept your request.");

            startService(new Intent(this,InviteCheck.class));

            Negative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FirebaseDatabase.getInstance().getReference().child("Invitations").child(PartnerUID).child("RECEIVED").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("ID").removeValue();
                    Negative.setVisibility(View.INVISIBLE);
                    InviteProg.setVisibility(View.INVISIBLE);
                    FirebaseDatabase.getInstance().getReference().child("Invitations").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("SENT").child(PartnerUID).child("ID").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(InviteCheckStage.this, "Invitation successfully revoked!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(InviteCheckStage.this,Invitations.class));

                        }
                    });
                }
            });

        }
        else{
            startActivity(new Intent(InviteCheckStage.this, Invitations.class));
        }
        LogoutCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                SharedPreferences.Editor editor = getSharedPreferences("INVITESTAT", MODE_PRIVATE).edit();
                editor.clear();
                editor.commit();

                stopService(new Intent(InviteCheckStage.this,InviteCheck.class));
                startActivity(new Intent(InviteCheckStage.this, SignIn.class));


            }
        });



    }
    @Override
    public void onBackPressed() {

        moveTaskToBack(true);
    }
}
