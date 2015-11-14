package com.ivoid.bj;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.bj.R;
import org.json.JSONException;
import org.json.JSONObject;

public class Title extends Activity {
    Game game;

    private final String registUrl = "http://blackjack.uh-oh.jp/user/regist/%s";
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;

    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        game = (Game) this.getApplication();

        // prepare Preferences
        preference = getSharedPreferences("user_data", MODE_PRIVATE);
        editor = preference.edit();

        setContentView(R.layout.title);

    }

    @Override
    public void onResume() {
        super.onResume();

        // displya title only first
        if(game.getUesrId().equals("")){
            if ((preference.getString("user_id", "")).equals("")) {
                registUser();
            } else {
                game.setUesrId(preference.getString("user_id", ""));
                showActionButton();
            }
        }else {
            Intent intent = new Intent(this, Dealer.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    public void registUser(){
        AsyncJsonLoader asyncJsonLoader = new AsyncJsonLoader(this, new AsyncJsonLoader.AsyncCallback() {
            // 実行後
            public boolean postExecute(JSONObject result) {
                try {
                    editor.putString("user_id", result.getString("user_id"));
                    editor.commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                game.setUesrId(preference.getString("user_id", ""));
                showActionButton();
                return true;
            }
        }, null);
        asyncJsonLoader.execute(String.format(registUrl, "test"));
    }
    
	public void onClickHeader(final View view) {
        startActivity(game.getNewIntent(view));
        finish();
        overridePendingTransition(0, 0);
    }

    void showActionButton(){
        (findViewById(R.id.game)).setVisibility(Button.VISIBLE);
        (findViewById(R.id.competition)).setVisibility(Button.VISIBLE);
        (findViewById(R.id.result)).setVisibility(Button.VISIBLE);
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