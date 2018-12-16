package com.nayra.theflopguyproductions;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InviteCheck extends Service {

    private boolean Check = false;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        FirebaseDatabase.getInstance().getReference().child("CONNECTED").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Check=true;
                    sendNotification();
                    onDestroy();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return START_NOT_STICKY;
    }
    public void sendNotification() {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this);

        //Create the intent that’ll fire when the user taps the notification//

        Intent intent = new Intent(InviteCheck.this,UserChat.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        mBuilder.setContentIntent(pendingIntent);

        mBuilder.setSmallIcon(R.mipmap.logo_icon);
        mBuilder.setContentTitle("Let's get started!");
        mBuilder.setContentText("Your invite has been accepted");

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }
}
