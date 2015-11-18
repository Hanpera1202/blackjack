package com.ivoid.bj;

import com.bj.R;

import android.app.Activity;
import android.content.Context;
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
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginBonus extends Activity implements OnClickListener
{
    private GameSettings settings;

    private SharedPreferences preference;
	private SharedPreferences.Editor editor;

    private Long nowTimestamp;

    private int ContinuousLoginDates;
    private int loginBonusPoints;
    private int playGameBonusPoints;
    private int totalPoints;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            setFinishOnTouchOutside(false);
        }

        setContentView(R.layout.login_bonus);

        settings = new GameSettings();

		preference = getSharedPreferences("user_data", MODE_PRIVATE);
		editor = preference.edit();
        nowTimestamp = System.currentTimeMillis();

        // calculate login bonus points
        Long bonusGetTimeStamp = preference.getLong("bonusGetTimeStamp", 0);
        if(bonusGetTimeStamp == 0L) {
            loginBonusPoints = (int)settings.startCash;
            ((TextView)findViewById(R.id.bonusMessageFirst)).setText(
                    "First only " + loginBonusPoints + "pt!!");
            ((TextView)findViewById(R.id.bonusMessageLast)).setText("");
        }else{
            if(bonusGetTimeStamp + 86400 * 1000 < nowTimestamp){
                ContinuousLoginDates = 1;
                ((TextView)findViewById(R.id.bonusMessageLast)).setText(" day continuous.");
            }else {
                ContinuousLoginDates = preference.getInt("ContinuousLoginDates", 0) + 1;
                ((TextView)findViewById(R.id.bonusMessageLast)).setText(" days continuous.");
            }
            if(ContinuousLoginDates > 10) {
                loginBonusPoints = 1000;
            }else{
                loginBonusPoints = 100 * ContinuousLoginDates;
            }
            ((TextView)findViewById(R.id.bonusMessageFirst)).setText(String.valueOf(ContinuousLoginDates));
        }
        ((TextView)findViewById(R.id.loginBonus)).setText(String.valueOf(loginBonusPoints));

        // calculate play games bonus points
        int playGameCounts = preference.getInt("plays", 0);
        playGameBonusPoints = (int)Math.floor(playGameCounts / 10);
        ((TextView)findViewById(R.id.playGamesBonus)).setText(String.valueOf(playGameBonusPoints));

        totalPoints = loginBonusPoints + playGameBonusPoints;
        ((TextView)findViewById(R.id.total)).setText(String.valueOf(totalPoints));

        findViewById(R.id.collect).setOnClickListener(this);
	}

    @Override
	public void onClick(View v) {
        editor.putFloat("gotBonusPoints", (float)totalPoints);

        try {
            String bonusGetTime = new SimpleDateFormat("yyyy-MM-dd").format(nowTimestamp);
            bonusGetTime += " 00:00:00";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            editor.putLong("bonusGetTimeStamp", sdf.parse(bonusGetTime).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        editor.putInt("ContinuousLoginDates", ContinuousLoginDates);
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