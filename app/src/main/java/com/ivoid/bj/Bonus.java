package com.ivoid.bj;

import com.bj.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;


import java.text.ParseException;
import java.text.SimpleDateFormat;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Bonus extends Activity implements OnClickListener
{
    private GameSettings settings;

    private SharedPreferences preference;
	private SharedPreferences.Editor editor;

    private Long nowTimestamp;

    private String bonusType;

    private int bonusPoints = 0;
    private int bonusCoins = 0;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            setFinishOnTouchOutside(false);
        }

        setContentView(R.layout.bonus);

        settings = new GameSettings();

		preference = getSharedPreferences("user_data", MODE_PRIVATE);
		editor = preference.edit();
        nowTimestamp = System.currentTimeMillis();

        Intent i = getIntent();
        bonusType = i.getStringExtra("type");

        if(bonusType.equals("login")) {
            ((TextView) findViewById(R.id.bonusMessage)).setText("YOU GOT BONUS");
            // calculate login bonus points
            Long bonusGetTime = preference.getLong("bonusGetTime", 0);
            if (bonusGetTime == 0L) {
                bonusPoints = (int) settings.startCash;
                ((TextView) findViewById(R.id.specialBonus)).setText(String.valueOf(bonusPoints));
                findViewById(R.id.specialBonusArea).setVisibility(View.VISIBLE);
            }
            bonusCoins = settings.loginBonusCoins;
            ((TextView) findViewById(R.id.loginBonus)).setText(String.valueOf(bonusCoins));

        }else{
            ((TextView) findViewById(R.id.bonusMessage)).setText("YOU GOT HINT COIN");
            bonusCoins = 1;
            ((TextView) findViewById(R.id.loginBonus)).setText(String.valueOf(bonusCoins));
        }

        findViewById(R.id.collect).setOnClickListener(this);
	}

    @Override
	public void onClick(View v) {
        String getTime = new SimpleDateFormat("yyyy-MM-dd").format(nowTimestamp);
        getTime += " 00:00:00";
        if(bonusType.equals("login")) {
            if (bonusPoints > 0) {
                editor.putFloat("gotBonusPoints", (float) bonusPoints);
            }
            if (bonusCoins > 0) {
                editor.putInt("gotBonusCoins", bonusCoins);
            }
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                editor.putLong("loginBonusGetTime", sdf.parse(getTime).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else{
            if (bonusCoins > 0) {
                editor.putInt("gotBonusCoins", bonusCoins);
            }
            int gotCoinBonusCounts = preference.getInt("gotCoinBonusCounts", 0);
            editor.putInt("gotCoinBonusCounts", gotCoinBonusCounts + 1);
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                editor.putLong("coinBonusGetTime", sdf.parse(getTime).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
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