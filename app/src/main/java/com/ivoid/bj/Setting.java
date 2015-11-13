package com.ivoid.bj;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bj.R;

/**
 * Created by nakazato on 2015/10/27.
 */
public class Setting extends Activity implements CompoundButton.OnCheckedChangeListener {

    private Player player;
    private TextView playerCash;

    private SharedPreferences preference;
    private SharedPreferences.Editor editor;

    private String user_id;
    private Boolean push_on, sound_on;

    ToggleButton tglPush, tglSound;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);

        //プリファレンスの準備
        preference = getSharedPreferences("user_data", MODE_PRIVATE);
        editor = preference.edit();

        user_id = preference.getString("user_id", "");
        push_on = preference.getBoolean("push_on", true);
        sound_on = preference.getBoolean("sound_on", true);

        player = new Player(getApplicationContext(), "Richard");
        playerCash=(TextView)findViewById(R.id.playerCash);
        playerCash.setText(String.valueOf((int) player.getBalance()));

        // ToggleButtonの取得
        tglPush = (ToggleButton) findViewById(R.id.tglPush);
        tglSound = (ToggleButton) findViewById(R.id.tglSound);

        // リスナーの登録
        tglPush.setOnCheckedChangeListener(this);
        tglSound.setOnCheckedChangeListener(this);

        // 初期値の設定
        tglPush.setChecked(push_on);
        tglSound.setChecked(sound_on);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.tglPush) {
            boolean on = buttonView.isChecked();
            if(on) {
                editor.putBoolean("push_on", true);
                editor.commit();
            }else{
                editor.putBoolean("push_on", false);
                editor.commit();
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

    public void onClickHeader(final View view)
    {
        switch (view.getId()) {
            case R.id.game: {
                Intent intent = new Intent(this, Dealer.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.competition: {
                Intent intent = new Intent(this, Competition.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.result: {
                Intent intent = new Intent(this, Result.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.checkMyData: {
                Intent intent = new Intent(this, MyData.class);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return false;
    }
}
