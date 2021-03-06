package com.ivoid.bj;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bj.R;

import java.util.HashMap;
import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FreeChips extends Activity implements OnClickListener
{
    Game game;

    private GameSettings settings;

    private SoundPool mSoundPool;
    private int mCardfall;

    private Player player;

	private int flipCnt;
	private int gotPoint;
	private FreeChipsDeck shoe;
	private Card card;

	private Map<Integer, Button> flipCards;

	private SharedPreferences preference;
	private SharedPreferences.Editor editor;

	private final Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        game = (Game) this.getApplication();

		setContentView(R.layout.free_chips);

        player = new Player(getApplicationContext(), "God");

		preference = getSharedPreferences("user_data", MODE_PRIVATE);
		editor = preference.edit();
        settings = new GameSettings();
		shoe = new FreeChipsDeck(player.getLevel());

		flipCards = new HashMap<Integer, Button>();
		flipCards.put((R.id.card1), (Button)findViewById(R.id.card1));
		flipCards.put((R.id.card2), (Button)findViewById(R.id.card2));
		flipCards.put((R.id.card3), (Button)findViewById(R.id.card3));
		flipCards.put((R.id.card4), (Button)findViewById(R.id.card4));
		flipCards.put((R.id.card5), (Button)findViewById(R.id.card5));
		flipCards.put((R.id.card6), (Button)findViewById(R.id.card6));
		flipCards.put((R.id.card7), (Button)findViewById(R.id.card7));
		flipCards.put((R.id.card8), (Button)findViewById(R.id.card8));
		flipCards.put((R.id.card9), (Button)findViewById(R.id.card9));
		flipCards.put((R.id.card10), (Button)findViewById(R.id.card10));
		flipCards.put((R.id.card11), (Button)findViewById(R.id.card11));
		flipCards.put((R.id.card12), (Button)findViewById(R.id.card12));
		flipCards.put((R.id.card13), (Button)findViewById(R.id.card13));
		flipCards.put((R.id.card14), (Button)findViewById(R.id.card14));
		flipCards.put((R.id.card15), (Button)findViewById(R.id.card15));
		flipCards.put((R.id.card16), (Button)findViewById(R.id.card16));
		flipCards.put((R.id.card17), (Button)findViewById(R.id.card17));
		flipCards.put((R.id.card18), (Button)findViewById(R.id.card18));
		flipCards.put((R.id.card19), (Button)findViewById(R.id.card19));
		flipCards.put((R.id.card20), (Button)findViewById(R.id.card20));

		for (final Integer entry : flipCards.keySet()) {
			((Button) findViewById(entry)).setOnClickListener(this);
		}

		flipCnt = 0;
		gotPoint = 0;
	}

    @Override
    protected void onResume(){
        super.onResume();

        // 予め音声データを読み込む
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        }
        else {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            mSoundPool = new SoundPool.Builder()
                    .setAudioAttributes(attr)
                    .setMaxStreams(1)
                    .build();
        }
        mCardfall = mSoundPool.load(getApplicationContext(), R.raw.cardfall, 1);
    }


    @Override
	public void onClick(View v) {
        if(flipCnt < settings.freeChipsFlips) {
            flipCnt++;
            flip((Button) findViewById(v.getId()));
        }
        if(flipCnt == settings.freeChipsFlips) {
            editor.putLong("freeChipsGetTime", System.currentTimeMillis());
            editor.commit();
            for (final Integer entry : flipCards.keySet()) {
                ((Button) findViewById(entry)).setOnClickListener(null);
            }
            RelativeLayout getBonus = (RelativeLayout) findViewById(R.id.getbonus);
            getBonus.setVisibility(TextView.VISIBLE);
            ScaleAnimation buttonanim =
                    new ScaleAnimation(
                    0.0f,1.0f,0.0f,1.0f,
                    getBonus.getWidth()/2,
                    getBonus.getHeight()/2);
            buttonanim.setStartOffset(100);
            buttonanim.setDuration(500);
            getBonus.startAnimation(buttonanim);

            handler.postDelayed(new Runnable() {
                public void run() {
                    finish();
                    overridePendingTransition(0, R.anim.out_down);
                }
            }, 1000);
        }

	}

	public void flip(Button button){
		if(flipCnt == settings.freeChipsFlips && gotPoint < 10) {
			do{
				card = shoe.drawCard();
			}while(gotPoint * card.getValue() < 10);
		}else{
			card = shoe.drawCard();
		}
		button.setBackgroundResource(card.getImage());
		TextView getpoints = (TextView) findViewById(R.id.getbonuspoints);

		if(gotPoint == 0) {
			gotPoint = card.getValue();
		}else{
			gotPoint *= card.getValue();
		}
		getpoints.setText(gotPoint + "pt\nGET!!");
		editor.putInt("gotBonusPoint", gotPoint);
		editor.putLong("freeChipsGetTime", System.currentTimeMillis());
		editor.commit();
		button.setOnClickListener(null);

        mSoundPool.play(mCardfall, game.getSoundVol(), game.getSoundVol(), 0, 0, 1.0F);
	}

	@Override
	protected void attachBaseContext(Context newBase) {
		super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
	}

}