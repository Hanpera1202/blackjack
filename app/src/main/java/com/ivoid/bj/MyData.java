package com.ivoid.bj;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bj.R;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MyData extends Activity {

    Game game;

	private SharedPreferences preference;
    private Player player;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        game = (Game) this.getApplication();

        setContentView(R.layout.mydata);

		preference = getSharedPreferences("user_data", MODE_PRIVATE);

        player = new Player(getApplicationContext(), "God");
        game.setHeaderData(player, (RelativeLayout) findViewById(R.id.header));

        float plays = preference.getInt("plays", 0);
        float wins = preference.getInt("wins", 0);
        float blackjacks = preference.getInt("blackjacks", 0);
        float splits = preference.getInt("splits", 0);
        float doubles = preference.getInt("doubles", 0);
        float doublewins = preference.getInt("doublewins", 0);

        ((TextView)findViewById(R.id.plays)).setText(String.valueOf((int)plays));
        ((TextView)findViewById(R.id.wins)).setText(String.valueOf((int)wins + "(" + (int)(wins/plays*100) + "%)"));
        ((TextView)findViewById(R.id.blackjacks)).setText(String.valueOf((int)blackjacks + "(" + (int)(blackjacks/plays*100) + "%)"));
        ((TextView)findViewById(R.id.splits)).setText(String.valueOf((int)splits + "(" + (int)(splits/plays*100) + "%)"));
        ((TextView)findViewById(R.id.doubles)).setText(String.valueOf((int) doubles + "(" + (int)(doubles/plays*100) + "%)"));
        ((TextView)findViewById(R.id.doublewins)).setText(String.valueOf((int)doublewins + "(" + (int)(doublewins/plays*100) + "%)"));

	}

    @Override
    public void onResume(){
        super.onResume();
    }

	public void onClickHeader(View view) {
        startActivity(game.getNewIntent(view));
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