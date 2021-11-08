package com.example.videoplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.VideoView;

import static com.example.videoplayer.MainActivity.VIDEO_ACTIVITY_INTENT;

public class VidPlayer extends AppCompatActivity {
    private VideoView myVideoView = null;
    private int position = 0;
    private VideoControllers mediaControls;
    private MediaPlayer mPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_player);
        if (mediaControls == null) {
            mediaControls = new VideoControllers(VidPlayer.this);
        }
        myVideoView = findViewById(R.id.videoView);
        myVideoView.setKeepScreenOn(true);
        try {
            myVideoView.setMediaController(mediaControls);
            String intentInfo = getIntent().getStringExtra(VIDEO_ACTIVITY_INTENT);
            final SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (intentInfo.equals("continueWatching")) {
                intentInfo = SP.getString("continueWatching", null);
                position = SP.getInt("position", 0);
            } else {
                SP.edit().putString("continueWatching", intentInfo).apply();
            }
            if (intentInfo == null) {
                Intent intent = new Intent(VidPlayer.this,
                        MainActivity.class);
                startActivity(intent);
            }
            myVideoView.setVideoPath(intentInfo);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        myVideoView.requestFocus();
        myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mPlayer = mp;
                if (myVideoView != null) {
                    myVideoView.seekTo(position);
                    if (position == 0) {
                        myVideoView.start();
                    } else if (mp != null) {
                        mp.start();
                    }
                }
                hideSystemUI();
            }

        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(myVideoView!= null){
            outState.putInt("position", myVideoView.getCurrentPosition());
            myVideoView.pause();
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        position =savedInstanceState.getInt("position");
        if(mPlayer!=null){
            mPlayer.seekTo(position);
        }else  if(myVideoView!=null){
            myVideoView.seekTo(position);
        }
    }

    @Override
    protected void onPause() {
        if(mPlayer != null){
            mPlayer.pause();
            if(myVideoView!= null){
                position = myVideoView.getCurrentPosition();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                        .putInt("position",position).apply();
            }
        }else if(myVideoView != null){
            position = myVideoView.getCurrentPosition();
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit()
                    .putInt("position",position).apply();
            myVideoView.pause();

        }
        super.onPause();
    }
@Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                 View.SYSTEM_UI_FLAG_FULLSCREEN
                |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                |View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                |View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                |View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                |View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
public void onBackPressed(){
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which == DialogInterface.BUTTON_POSITIVE){
                moveTaskToBack(true);
            }else if(which == DialogInterface.BUTTON_NEGATIVE) {
                Intent intent = new Intent(VidPlayer.this,MainActivity.class);
                intent.putExtra(VIDEO_ACTIVITY_INTENT, "showContinueWatching");
                startActivity(intent);
            }
        }
    };

AlertDialog.Builder builder = new AlertDialog.Builder(this);
builder.setMessage("Exit?").setPositiveButton("Close App", dialogClickListener)
        .setNegativeButton("Close Video", dialogClickListener).show();
}

}
