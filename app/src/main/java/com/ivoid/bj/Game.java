package com.ivoid.bj;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.view.View;

import com.bj.R;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParsePush;

public class Game extends Application
{
    private String mUserId = "";
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();

        // prepare Preferences
        preference = getSharedPreferences("user_data", MODE_PRIVATE);

        Parse.initialize(this, "RPmKtwXgLxqstB0SPbhDzUGs1gImQVB3wmwS8N76", "eyyQFKpxSFMoGjEsySAxbyLAJrYVwYCniL24ba9k");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        if(preference.getBoolean("push_on", true)) {
            ParsePush.subscribeInBackground("Blackjack");
        }

    }

    public void setUesrId(String userId){
        mUserId = userId;
        return;
    }

    public String getUesrId(){
        return mUserId;
    }

    public float getSoundVol(){
        if(preference.getBoolean("sound_on", true)) {
            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            return audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        }else{
            return 0f;
        }
    }

    public Intent getNewIntent(final View view) {
        Intent newIntent;
        switch (view.getId()) {
            case R.id.game: {
                newIntent = new Intent(this, Dealer.class);
                break;
            }
            case R.id.competition: {
                newIntent = new Intent(this, Competition.class);
                break;
            }
            case R.id.result: {
                newIntent = new Intent(this, Result.class);
                break;
            }
            case R.id.checkMyData: {
                newIntent = new Intent(this, MyData.class);
                break;
            }
            case R.id.setting: {
                newIntent = new Intent(this, Setting.class);
                break;
            }
            default:{
                newIntent = new Intent(this, Title.class);
            }
        }
        return newIntent;
    }

}
