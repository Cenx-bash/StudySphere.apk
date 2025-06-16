package com.example.studysphere;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

public class ProfileUpdateReceiver extends BroadcastReceiver {
    private ImageView profileImageView;

    public ProfileUpdateReceiver(ImageView profileImageView) {
        this.profileImageView = profileImageView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String imagePath = intent.getStringExtra("imagePath");
        if (imagePath != null && !imagePath.isEmpty()) {
            // Ensure UI update happens on the main thread
            new Handler(Looper.getMainLooper()).post(() ->
                    profileImageView.setImageURI(Uri.parse(imagePath))
            );
        }
    }
}
