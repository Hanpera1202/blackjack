package com.ivoid.bj;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.bj.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LevelUp extends Activity implements OnClickListener
{
    private SharedPreferences preference;
	private SharedPreferences.Editor editor;

    private Player player;

    private GameSettings settings;
    private int getBonusCoin;

    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            setFinishOnTouchOutside(false);
        }

        setContentView(R.layout.level_up);

        player = new Player(getApplicationContext(), "God");
        settings = new GameSettings();

		preference = getSharedPreferences("user_data", MODE_PRIVATE);
		editor = preference.edit();

        if(player.isLevelUp()) {
            player.levelUp();
            getBonusCoin = settings.levels.get(player.getLevel()).getCoinCnt;
            editor.putInt("gotBonusCoin", getBonusCoin);
        }
        ((TextView) findViewById(R.id.newLevel)).setText("You are now level " + player.getLevel() + ".");
        ((TextView) findViewById(R.id.newMaxBet)).setText(player.getMaxBet() + " pt");
        ((TextView) findViewById(R.id.loginBonus)).setText(String.valueOf(getBonusCoin));

        findViewById(R.id.ok).setOnClickListener(this);
	}

    @Override
	public void onClick(View v) {
        editor.commit();
        finish();
        overridePendingTransition(0, 0);

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