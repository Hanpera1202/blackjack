package com.ivoid.bj;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.bj.R;

public class MyData extends Activity implements OnClickListener
{
	private SharedPreferences preference;


	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.mydata);

		preference = getSharedPreferences("user_data", MODE_PRIVATE);

        float plays = preference.getInt("plays", 0);
        float wins = preference.getInt("wins", 0);
        float blackjacks = preference.getInt("blackjacks", 0);
        float splits = preference.getInt("splits", 0);
        float doubles = preference.getInt("doubles", 0);
        float doublewins = preference.getInt("doublewins", 0);

        (findViewById(R.id.close_button)).setOnClickListener(this);
        ((TextView)findViewById(R.id.plays)).setText(String.valueOf((int)plays));
        ((TextView)findViewById(R.id.wins)).setText(String.valueOf((int)wins + "(" + (int)(wins/plays*100) + "%)"));
        ((TextView)findViewById(R.id.blackjacks)).setText(String.valueOf((int)blackjacks + "(" + (int)(blackjacks/plays*100) + "%)"));
        ((TextView)findViewById(R.id.splits)).setText(String.valueOf((int)splits + "(" + (int)(splits/plays*100) + "%)"));
        ((TextView)findViewById(R.id.doubles)).setText(String.valueOf((int) doubles + "(" + (int)(doubles/plays*100) + "%)"));
        ((TextView)findViewById(R.id.doublewins)).setText(String.valueOf((int)doublewins + "(" + (int)(doublewins/plays*100) + "%)"));

	}

	@Override
	public void onClick(View v) {
		finish();
	}

}