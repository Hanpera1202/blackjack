package com.ivoid.bj;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bj.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParsePush;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class Game extends Application
{
    private String mUserId = "";
    private SharedPreferences preference;

    InterstitialAd mInterstitialAd;
    InterstitialAd mMovieAd;

    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        // prepare Preferences
        preference = getSharedPreferences("user_data", MODE_PRIVATE);

        Parse.initialize(this, "RPmKtwXgLxqstB0SPbhDzUGs1gImQVB3wmwS8N76", "eyyQFKpxSFMoGjEsySAxbyLAJrYVwYCniL24ba9k");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        if(preference.getBoolean("push_on", true)) {
            ParsePush.subscribeInBackground("Blackjack");
        }

        //インタースティシャル広告の読み込み
        //adRequest = new AdRequest.Builder().build();
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.inters_ad_unit_id));
        requestNewInterstitial();

        //動画広告の読み込み
        mMovieAd = new InterstitialAd(this);
        mMovieAd.setAdUnitId(getString(R.string.movie_ad_unit_id));
        requestNewMovie();

    }

    public void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(adRequest);
    }

    public void requestNewMovie() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mMovieAd.loadAd(adRequest);
    }

    public void setUserId(String userId){
        mUserId = userId;
        return;
    }

    public String getUserId(){
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
                newIntent = new Intent(this, Playing.class);
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

    public void setHeaderData(Player player, RelativeLayout header) {
        LinearLayout playerCashView = (LinearLayout)header.getChildAt(0);
        TextView playerLevel = (TextView)playerCashView.getChildAt(0);
        TextView playerCash = (TextView)playerCashView.getChildAt(1);
        playerCash.setText(String.valueOf(player.getBalance()));
        playerLevel.setText(String.valueOf(player.getLevel()));
        this.setHeaderNextLevel(player, header);
    }

    public void setHeaderNextLevel(Player player, RelativeLayout header){
        ProgressBar nextLevelBar = (ProgressBar)header.getChildAt(1);
        TextView playerNextLevel = (TextView)header.getChildAt(2);
        nextLevelBar.setMax(player.getNextExp());
        nextLevelBar.setProgress(player.getNowExp());
        playerNextLevel.setText(player.getNowExp() + " / " + player.getNextExp());
    }

}
