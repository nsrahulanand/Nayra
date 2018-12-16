package com.nayra.theflopguyproductions;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class UserMessageAdapter extends RecyclerView.Adapter<UserMessageAdapter.ViewHolder>{


    private List<UserMessageSetGet> mMessageList;
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public UserMessageAdapter(List<UserMessageSetGet> mMessageList) {

        this.mMessageList = mMessageList;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.msglist ,parent, false);

        return new ViewHolder(v);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView UsermessageText;
        public TextView PartnermessageText;
        public TextView SentTime;
        public TextView ReceiveTime;
        public TextView User;
        //public ImageView UserMessageImage;
        //public ImageView PartnerMessageImage;

        public ViewHolder(View view) {
            super(view);

            UsermessageText = (TextView) view.findViewById(R.id.rightText);
            PartnermessageText = (TextView) view.findViewById(R.id.leftText);
            SentTime = (TextView) view.findViewById(R.id.SentTime);
            ReceiveTime = (TextView) view.findViewById(R.id.ReceiveTime);

            //messageImage = (ImageView) view.findViewById(R.id.message_image_layout);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {

        UserMessageSetGet c = mMessageList.get(i);
        String from_user = c.getUser();
        String message_type = c.getType();
        long timestamp = c.getTime();

        if(from_user.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            if(message_type.equals("Text")) {
                viewHolder.UsermessageText.setText(c.getMessage());
                viewHolder.SentTime.setText(getTimeAgo(timestamp));
                viewHolder.PartnermessageText.setVisibility(View.GONE);
                viewHolder.ReceiveTime.setVisibility(View.GONE);

            }
        }

        else{
            if(message_type.equals("Text")) {
                viewHolder.PartnermessageText.setText(c.getMessage());
                viewHolder.ReceiveTime.setText(getTimeAgo(timestamp));
                viewHolder.UsermessageText.setVisibility(View.GONE);
                viewHolder.SentTime.setVisibility(View.GONE);
            }
        }

    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }
    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "Just Now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "A minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "An hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }
}