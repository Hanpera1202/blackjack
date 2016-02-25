package com.ivoid.bj;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.bj.R;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by nakazato on 2015/10/27.
 */
public class Setting extends Activity implements CompoundButton.OnCheckedChangeListener {

    Game game;

    private Player player;

    private SharedPreferences preference;
    private SharedPreferences.Editor editor;

    private Boolean notification_on, sound_on;

    ToggleButton tglNotification, tglSound;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        game = (Game) this.getApplication();

        setContentView(R.layout.setting);

        //プリファレンスの準備
        preference = getSharedPreferences("user_data", MODE_PRIVATE);
        editor = preference.edit();

        notification_on = preference.getBoolean("Notification_on", true);
        sound_on = preference.getBoolean("sound_on", true);

        player = new Player(getApplicationContext(), "Richard");
        game.setHeaderData(player, (RelativeLayout) findViewById(R.id.header));

        // ToggleButtonの取得
        tglNotification = (ToggleButton) findViewById(R.id.tglNotification);
        tglSound = (ToggleButton) findViewById(R.id.tglSound);

        // リスナーの登録
        tglNotification.setOnCheckedChangeListener(this);
        tglSound.setOnCheckedChangeListener(this);

        // 初期値の設定
        tglNotification.setChecked(notification_on);
        tglSound.setChecked(sound_on);
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.tglNotification) {
            boolean on = buttonView.isChecked();
            if(on) {
                ParsePush.subscribeInBackground("Blackjack", new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        editor.putBoolean("notification_on", true);
                        editor.commit();
                    }
                });
            }else{
                ParsePush.unsubscribeInBackground("Blackjack", new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        editor.putBoolean("notification_on", false);
                        editor.commit();
                    }
                });
            }
        } else if (buttonView.getId() == R.id.tglSound) {
            boolean on = buttonView.isChecked();
            if(on) {
                editor.putBoolean("sound_on", true);
                editor.commit();
            }else{
                editor.putBoolean("sound_on", false);
                editor.commit();
            }
        }
    }

    public void onClickHeader(final View view) {
        startActivity(game.getNewIntent(view));
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
