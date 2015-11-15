package com.ivoid.bj;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bj.R;

import java.util.HashMap;
import java.util.Map;

public class LoginBonus extends Activity implements OnClickListener
{

	private SharedPreferences preference;
	private SharedPreferences.Editor editor;

    private int loginBonusPoints;
    private int playGamesBonusPoints;
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

		preference = getSharedPreferences("user_data", MODE_PRIVATE);
		editor = preference.edit();

        loginBonusPoints = 100;
        ((TextView)findViewById(R.id.loginBonus)).setText(String.valueOf(loginBonusPoints));
        playGamesBonusPoints = 100;
        ((TextView)findViewById(R.id.playGamesBonus)).setText(String.valueOf(playGamesBonusPoints));
        totalPoints = loginBonusPoints + playGamesBonusPoints;
        ((TextView)findViewById(R.id.total)).setText(String.valueOf(totalPoints));

        findViewById(R.id.collect).setOnClickListener(this);
	}

    @Override
	public void onClick(View v) {
        editor.putFloat("gotBonusPoints", totalPoints);
        editor.putLong("bonusGetTimeStamp", System.currentTimeMillis());
        editor.commit();
        finish();
        overridePendingTransition(0, 0);

	}

}