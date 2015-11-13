package com.ivoid.bj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.bj.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;


/*
 * I'm God.
 */

/*TODO
 * Pause in between dealCard()s in act()
 * Show amount in pot
 *  
 */

public class Dealer extends Activity implements OnClickListener
{
    private String user_id;

	private Vibrator vibrator;
    private SoundPool mSoundPool;
    private int mCardfall;
    private int mPush;
    private int mLose;
    private int mWin;
    private int mBlackjack;
    private int mInsuranceWin;
    private int mInsuranceLose;

	private GameSettings settings; 
	private Deck shoe;
	private BJHand dealerHand;
	private TextView dealerSum;
    private Card faceDownCard;

	private Player player; 
	private BJHand currentPlayerHand;
	private RelativeLayout currentPlayerHandView;
    private RelativeLayout currentPlayerSumView;
	private LinearLayout currentPlayerBetView;
	private TextView currentPlayerBetNumView;
	private boolean betting, getNextAction;
	
	private RelativeLayout dealerHandView;
    private RelativeLayout dealerSumView;

	private ArrayList<RelativeLayout> playerHandViews;
    private ArrayList<RelativeLayout> playerSumViews;
	private ArrayList<LinearLayout> playerBetViews;
	private ArrayList<TextView> playerBetNumViews;
	private ArrayList<RelativeLayout> playerOverViews;
	private ArrayList<ImageView> playerResults;

	private TextView playerBet;
	private TextView playerSum;
	private TextView playerCash;

	private Map<Integer, Action> buttons;
	private Map<BJHand, RelativeLayout> hands;

	private final Handler handler = new Handler();

	private byte currentPlayerIndex;
    private byte nextHandtoPlay;

    private SharedPreferences preference;
    private SharedPreferences.Editor editor;

    AdRequest adRequest;
    InterstitialAd mInterstitialAd;

    private boolean insurancingFlg;
    private boolean clickable;
    private int waittime;
    private byte global_b;

    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        adRequest = new AdRequest.Builder().build();

        //プリファレンスの準備
        preference = getSharedPreferences("user_data", MODE_PRIVATE);
        editor = preference.edit();

        editor.putBoolean("TitleLaunched", false);
        editor.commit();

        settings = new GameSettings();
		shoe = new Deck( (byte)settings.decks,
						 (byte)settings.burns, 
						 (byte)settings.shuffleTimes 
					   ); 
		dealerHand = new BJHand("Dealer");

        if (preference.getBoolean("Launched", false)==false) {
            //Initial start-up
            editor.putBoolean("Launched", true);
            editor.commit();
            player = new Player(getApplicationContext(), "Richard", settings.startCash);
        }else {
            player = new Player(getApplicationContext(), "Richard");
        }
        currentPlayerHand = player.getHand((byte) 0);
        betting = true;
        clickable = true;

        setContentView(R.layout.playing);

        setViews();
        //playerCash.setText(String.valueOf((int) player.getBalance()));
        //initUI();
    }

    @Override
    public void onResume(){
        super.onResume();

        // アニメーションスタート時間のリセット
        waittime = 0;

        //インタースティシャル広告の読み込み
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.inters_ad_unit_id));
        requestNewInterstitial();

        // 予め音声データを読み込む
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 1);
        }
        else {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();

            mSoundPool = new SoundPool.Builder()
                    .setAudioAttributes(attr)
                    .setMaxStreams(2)
                    .build();
        }
        mCardfall = mSoundPool.load(getApplicationContext(), R.raw.cardfall, 1);
        mPush = mSoundPool.load(getApplicationContext(), R.raw.push, 1);
        mLose = mSoundPool.load(getApplicationContext(), R.raw.lose, 1);
        mWin = mSoundPool.load(getApplicationContext(), R.raw.win, 1);
        mBlackjack = mSoundPool.load(getApplicationContext(), R.raw.blackjack, 1);
        mInsuranceWin = mSoundPool.load(getApplicationContext(), R.raw.insurance_win, 1);
        mInsuranceLose = mSoundPool.load(getApplicationContext(), R.raw.insurance_lose, 1);

/*
        if (preference.getFloat("gotBonusPoints", 0f) > 0f) {
            player.deposit(preference.getFloat("gotBonusPoints", 0f));
            editor.putFloat("gotBonusPoints", 0f);
            editor.commit();
            updatePlayerCashlbl();
            if (player.getBalance() >= 10.0f) {
                Button bonus = (Button) findViewById(R.id.bonus);
                bonus.setAnimation(null);
                bonus.setVisibility(Button.INVISIBLE);
                initUI();
            }
        }else{
            playerCash.setText(String.valueOf((int) player.getBalance()));
            if (betting) {
                //playerBet の再表示
                float beforeBet = player.getInitBet();
                clearBet();
                collectBet(beforeBet);
            }else{
                // dd,split,insuranceボタン表示の再判定
                if(insurancingFlg){
                    if(!isInsurable()){
                        endInsurance();
                    }
                }else {
                    checkddbutton();
                    checksplitbutton();
                }
            }
        }
        */


        if (betting){
            playerCash.setText(String.valueOf((int) player.getBalance()));
            if (preference.getFloat("gotBonusPoints", 0f) > 0f) {
                player.deposit(preference.getFloat("gotBonusPoints", 0f));
                editor.putFloat("gotBonusPoints", 0f);
                editor.commit();
                updatePlayerCashlbl();
                initUI();
            }else if(player.getInitBet() == 0) {
                initUI();
            }else if(player.getBalance() < player.getInitBet()) {
                clearBet();
            }
        }else{
            playerCash.setText(String.valueOf((int) player.getBalance()));
            // dd,split,insuranceボタン表示の再判定
            if(insurancingFlg){
                if(!isInsurable()){
                    endInsurance();
                }
            }else {
                checkddbutton();
                checksplitbutton();
            }
        }

        //Titleが起動していない、user_idがセットされていない場合はTitleを起動
        if (preference.getBoolean("TitleLaunched", false)==false ||
                preference.getString("user_id", "").equals("")) {
            //TitleLaunchedフラグをオンにする
            editor.putBoolean("TitleLaunched", true);
            editor.commit();
            //TitleActivityを起動
            Intent intent = new Intent(this, Title.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // リリース
        mSoundPool.release();
    }

    private void setViews(){

        dealerHandView = (RelativeLayout) findViewById(R.id.dealerHand);
        dealerSumView = (RelativeLayout) findViewById(R.id.dealerSum);

        playerHandViews=new ArrayList<RelativeLayout>();
        playerHandViews.add((RelativeLayout) findViewById(R.id.playerHand1));
        playerHandViews.add((RelativeLayout) findViewById(R.id.playerHand2));

        playerSumViews=new ArrayList<RelativeLayout>();
        playerSumViews.add((RelativeLayout) findViewById(R.id.playerSum1));
        playerSumViews.add((RelativeLayout) findViewById(R.id.playerSum2));

        playerBetViews = new ArrayList<LinearLayout>();
        playerBetViews.add((LinearLayout) findViewById(R.id.playerBet1));
        playerBetViews.add((LinearLayout) findViewById(R.id.playerBet2));

        playerBetNumViews = new ArrayList<TextView>();
        playerBetNumViews.add((TextView) findViewById(R.id.playerBetNum1));
        playerBetNumViews.add((TextView) findViewById(R.id.playerBetNum2));

        playerOverViews = new ArrayList<RelativeLayout>();
        playerOverViews.add((RelativeLayout) findViewById(R.id.playerOver1));
        playerOverViews.add((RelativeLayout) findViewById(R.id.playerOver2));

        playerResults = new ArrayList<ImageView>();
        playerResults.add((ImageView) findViewById(R.id.playerResult1));
        playerResults.add((ImageView) findViewById(R.id.playerResult2));

        playerCash=(TextView)findViewById(R.id.playerCash);
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("YOUR_DEVICE_HASH")
                .build();
        mInterstitialAd.loadAd(adRequest);
    }

	private void initUI()
	{
        buttons = new HashMap<Integer, Action>();

        if (betting)
		{
			buttons.put(R.id.clearButton, Action.CLEAR);
			buttons.put(R.id.dealButton, Action.DEAL);

			//buttons.put(R.id.bet_1, Action.ONE);
			buttons.put(R.id.bet_10, Action.TEN);
			buttons.put(R.id.bet_50, Action.FIFTY);
			buttons.put(R.id.bet_100, Action.ONEHUNDRED);
			buttons.put(R.id.bet_500, Action.FIVEHUNDRED);
            buttons.put(R.id.bet_all, Action.ALL);
            buttons.put(R.id.rebet, Action.REBET);
			//buttons.put(R.id.bet_1000, Action.ONETHOUSAND);

			playerBet=(TextView) findViewById(R.id.playerBet);

            if(player.getBalance() < 10.0f) {
                Button bonus = (Button) findViewById(R.id.bonus);
                bonus.setVisibility(Button.VISIBLE);
                AlphaAnimation buttonanim = new AlphaAnimation(1, 0.0f);
                buttonanim.setDuration(800);
                buttonanim.setRepeatCount(Animation.INFINITE);
                buttonanim.setRepeatMode(Animation.REVERSE);
                bonus.startAnimation(buttonanim);
                buttons.put(R.id.bonus, Action.BONUS);
            }else{
                Button bonus = (Button) findViewById(R.id.bonus);
                bonus.setAnimation(null);
                bonus.setVisibility(Button.INVISIBLE);
            }

            collectBet(player.getRebet());

		}
		else
		{
			getNextAction=true;
            player.withdraw(currentPlayerHand.getBet().getValue());
			
			buttons.put(R.id.standButton, Action.STAND);
			buttons.put(R.id.hitButton, Action.HIT);
            buttons.put(R.id.surrenderButton, Action.SURRENDER);
			buttons.put(R.id.ddButton, Action.DOUBLEDOWN);
			buttons.put(R.id.splitButton, Action.SPLIT);
            buttons.put(R.id.insuranceYes, Action.INSURANCE_YES);
            buttons.put(R.id.insuranceNo, Action.INSURANCE_NO);

            hands=new HashMap<BJHand, RelativeLayout>();

			hands.put(player.getHand((byte) 0), playerHandViews.get(0));
			hands.put(dealerHand, dealerHandView);

			currentPlayerIndex = 0;
			currentPlayerHandView=playerHandViews.get(currentPlayerIndex);
            currentPlayerSumView=playerSumViews.get(currentPlayerIndex);
			currentPlayerBetView=playerBetViews.get(currentPlayerIndex);
			currentPlayerBetNumView=playerBetNumViews.get(currentPlayerIndex);

			dealerSum=(TextView) dealerSumView.getChildAt(1);

            setBet((LinearLayout) currentPlayerBetView.getChildAt(0), (int) player.getInitBet());
			updatePlayerBetlblForPlaying();
            updatePlayerCashlbl();

        }

		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


		for (final Integer entry : buttons.keySet()) {
			((Button) findViewById(entry)).setOnClickListener(this);
		}

	}

	void payTransaction ( float betValue, float ratio )
	{
		player.deposit(betValue);
		
		//calculate reward based on hand's performance in this bj round (indicated by ratio)
		float reward = betValue * ratio;
		
		player.deposit(reward);

        //updatePlayerCashlblForPlaying();
    }

    void setPlaying(BJHand hand, boolean b) {
        hand.setPlaying(b);
    }

    void payout ()
	{
		byte dealerValue=dealerHand.getBJValue(); // the dealer's hand's value
		
		for ( BJHand hand: player.getHands() )
		{	
			if ( !hand.toBePayed() )	continue; //if the hand still needs to be payed

			byte handValue = hand.getBJValue(); //the hand's hard value
			float betValue = hand.getBet().getValue(); //the hand's bet

			if ( player.howManyHands() == 1 && hand.hasBJ()){
				if(!dealerHand.hasBJ()) {
					payTransaction(betValue, settings.bjPay);
					hand.setStatus(Status.BLACKJACK);
                    editor.putInt("blackjacks", preference.getInt("blackjacks", 0) + 1);
                    editor.commit();
				}else{
					payTransaction(betValue, 0f);
					hand.setStatus(Status.PUSH);
                    editor.putInt("pushs", preference.getInt("pushs", 0) + 1);
                    editor.commit();
                }
			}
			else if ( dealerHand.didBust() || handValue > dealerValue ) {
				payTransaction(betValue, settings.winPay);
				hand.setStatus(Status.WON);
                editor.putInt("wins", preference.getInt("wins", 0) + 1);
                if(hand.didDD()){
                    editor.putInt("doublewins", preference.getInt("doublewins", 0) + 1);
                }
                editor.commit();
			}
			else if( dealerHand.hasBJ() || handValue < dealerValue) {
				hand.setStatus(Status.LOST);
			}
			else {
				payTransaction(betValue, 0f); /* if the hands tie in value, the player simply get his money back */
				hand.setStatus(Status.PUSH);
                editor.putInt("pushs", preference.getInt("pushs", 0) + 1);
                editor.commit();
			}

		}
        updatePlayerResultlbl();
	}

    void insurancePayout ()
    {

        float insuranceBetValue = player.getInsuranceBetValue();

        if (dealerHand.hasBJ())
        {
            payTransaction(insuranceBetValue, settings.insurancePay);
            editor.putInt("insurance_wins", preference.getInt("insurance_wins", 0) + 1);
            handler.postDelayed(new Runnable() {
                public void run() {
                    ((ImageView)findViewById(R.id.insuranceResult)).setImageResource(R.drawable.win);
                    findViewById(R.id.insuranceResult).setVisibility(ImageView.VISIBLE);
                    ScaleAnimation buttonanim =
                            new ScaleAnimation(
                                    0.0f,1.0f,0.0f,1.0f,
                                    findViewById(R.id.insuranceResult).getWidth()/2,
                                    findViewById(R.id.insuranceResult).getHeight()/2);
                    buttonanim.setDuration(300);
                    findViewById(R.id.insuranceResult).startAnimation(buttonanim);

                    AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    int musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                    mSoundPool.play(mInsuranceWin, (float) musicVol, (float) musicVol, 0, 0, 1.0F);
                }
            },waittime);
        }
        else
        {
            editor.putInt("insurance_loses", preference.getInt("insurance_loses", 0) + 1);
            handler.postDelayed(new Runnable() {
                public void run() {
                    ((ImageView)findViewById(R.id.insuranceResult)).setImageResource(R.drawable.lose);
                    findViewById(R.id.insuranceResult).setVisibility(ImageView.VISIBLE);
                    AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                    int musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                    mSoundPool.play(mInsuranceLose, (float) musicVol, (float) musicVol, 0, 0, 1.0F);
                }
            }, waittime);
        }
    }

	void newGame()
	{
        findViewById(R.id.betting).setVisibility(LinearLayout.VISIBLE);
        dealerHand.update();
		player.update();
		currentPlayerHand=player.getHand((byte) 0);
		betting=true;
		getNextAction=false;
		// update playerBet to 0
        updatePlayerBetlbl();
        initUI();
	}
		
	void clearBet()
	{
		BJHand hand = player.getHand((byte) 0);
		//float betValue=hand.getBet().getValue();
        hand.clearBet(); //remove the bet from the table
        //player.deposit(betValue); //put bet back into player's wallet
        updatePlayerBetlbl();
        //updatePlayerCashlbl();
	}
	
	void collectBet(float stake)
	{
        float betValue=currentPlayerHand.getBet().getValue();
        if( stake > player.getBalance() - betValue ||
                stake + betValue > settings.tableMax
        ) {
            return;
		}

		player.getHand((byte) 0).incrementBet(stake);

        updatePlayerBetlbl();
        //updatePlayerCashlbl();
        //player.withdraw(stake); //take the bet out of player's wallet
    }

	void checkPlayerHand(BJHand hand)
	{
		hand.checkIfBusted();
		hand.checkIfHasBJ();
        hand.checkIfHas21();

		if(hand.didBust()) // bust animation
		{
            hand.setStatus(Status.LOST);
            hand.setToBePayed(false);
			hand.setPlaying(false);

            updatePlayerResultlbl();
		} 
		else if(hand.hasBJ() || hand.has21()) {
            hand.setPlaying(false);
        }
	}

    void surrender(BJHand hand)
    {
        if (hand.getCardCount()==2)
        {
            float betValue = hand.getBet().getValue();
            //hand.update(); // clear hand
            payTransaction(betValue, settings.surrenderPay); //player gets half his bet back via negative ratio
            hand.setStatus(Status.SURRENDERED);
            hand.setToBePayed(false);
            hand.setPlaying(false);
            updatePlayerResultlbl();
            updatePlayerCashlbl();
        }
    }

	void dd(BJHand hand)
	{
		if (hand.getCardCount()==2)
		{
			//check 10/11
			byte handHardBJValue = hand.getHardBJValue(); 
			
			if (handHardBJValue == 10 || handHardBJValue==11)
				if (!settings.dd1011)
					return;
			
			hand.takeDD();
			
			float betValue = hand.getBet().getValue();
			
			hand.incrementBet(betValue);
			
			player.withdraw(betValue);

			//updatePlayerCashlblForPlaying();
            updatePlayerCashlbl();

            dealCard(hand);

            setBet((LinearLayout) currentPlayerBetView.getChildAt(1), (int) betValue);
			updatePlayerBetlblForPlaying();

			hand.setPlaying(false);

            checkPlayerHand(hand);
		}
	}
	
	void split(BJHand hand)
	{
		if (hand.splitable(settings.aceResplit) && player.howManyHands()<(settings.splits+1))
		{
			//remove the second card from the first hand and make a new hand with it
			float betValue=hand.getBet().getValue();
			Card poppedCard = hand.popCard((byte) 1);
			((RelativeLayout)(currentPlayerHandView.getChildAt(0))).removeViewAt(1);
            //playerHandViews.get(0).getChildAt(1).setVisibility(TextView.INVISIBLE);
            playerSumViews.get(0).setVisibility(RelativeLayout.INVISIBLE);

			BJHand splitHand = new BJHand(hand.ownerName, poppedCard, betValue);
            player.addHand(splitHand);
            player.withdraw(betValue);
            //updatePlayerCashlblForPlaying();
            updatePlayerCashlbl();

            hands.put(splitHand, playerHandViews.get(1));
            findViewById(R.id.CenterView).setVisibility(View.VISIBLE);

			setBet((LinearLayout) playerBetViews.get(1).getChildAt(0), (int) player.getInitBet());
            currentPlayerBetNumView=playerBetNumViews.get(1);
            updatePlayerBetlblForPlaying();
            currentPlayerBetNumView=playerBetNumViews.get(0);

	    	ImageView cardImage=new ImageView(this);
	    	cardImage.setImageResource(poppedCard.getImage());

			cardImage.setScaleType(ImageView.ScaleType.FIT_XY);

            int w_px = (int) (80f * getResources().getDisplayMetrics().density + 0.5f);
            int h_px = (int) (120f * getResources().getDisplayMetrics().density + 0.5f);

			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w_px, h_px);
			cardImage.setLayoutParams(lp);

	    	((RelativeLayout)(hands.get(splitHand).getChildAt(0))).addView(cardImage);

			dealCard(hand);
            handler.postDelayed(new Runnable() {
                public void run() {
                    updatePlayerSumlbl(0);
                    playerSumViews.get(0).setVisibility(RelativeLayout.VISIBLE);
                }
            }, waittime);
            checkPlayerHand(hand);

            dealCard(splitHand);
            handler.postDelayed(new Runnable() {
                public void run() {
                    updatePlayerSumlbl(1);
                    playerSumViews.get(1).setVisibility(RelativeLayout.VISIBLE);
                }
            }, waittime);
			checkPlayerHand(splitHand);

            if(poppedCard.getValue() == 1){
				for ( byte b=0 ; b < player.howManyHands() ; b++ )
					player.getHand(b).setPlaying(false);
			}

            /*
            else{
                //waittime += 500;
                handler.postDelayed(new Runnable() {
                    public void run() {
                        playerOverViews.get(currentPlayerIndex).setBackgroundResource(R.drawable.layout_shape);
                    }
                }, waittime);
            }
            */
		}
	}

	void disableButtons()
	{	
		for (final Integer entry : buttons.keySet())
		{
            if(entry != R.id.checkMyData) {
                ((Button) findViewById(entry)).setVisibility(Button.INVISIBLE);
                ((Button) findViewById(entry)).setOnClickListener(null);
            }
		}
	}

	void act()
	{
        faceUpCard(dealerHand, faceDownCard);
        updateDealerSumlbl(dealerHand.getSoftBJValue(), dealerHand.getHardBJValue());
        waittime += 300;
        if (!(player.howManyHands() == 1 && currentPlayerHand.hasBJ())) {
            if (settings.stand17soft)
                while (dealerHand.getHardBJValue() < 17) {
                    if (dealerHand.getSoftBJValue() >= 17) break;    // stand on soft 17
                    dealCard(dealerHand, true);
                    handler.postDelayed(new Runnable() {
                        byte soft = dealerHand.getSoftBJValue();
                        byte hard = dealerHand.getHardBJValue();

                        public void run() {
                            updateDealerSumlbl(soft, hard);
                        }
                    }, waittime);
                }
            else
                while (dealerHand.getHardBJValue() < 17) {
                    dealCard(dealerHand, true);
                    handler.postDelayed(new Runnable() {
                        byte soft = dealerHand.getSoftBJValue();
                        byte hard = dealerHand.getHardBJValue();

                        public void run() {
                            updateDealerSumlbl(soft, hard);
                        }
                    }, waittime);
                }
        }

		dealerHand.checkIfHasBJ();
		dealerHand.checkIfBusted();

        if (player.tookInsurance()) {
            insurancePayout();
        }
        payout();
        updatePlayerCashlbl();

        handler.postDelayed(new Runnable() {
            public void run() {
                newGame();
            }
        }, waittime);

	}

    private ArrayList<ImageView> betImages;
	void setBet(LinearLayout playerBet, int betNum) {

        betImages=new ArrayList<ImageView>();
        while(betNum > 0 && betImages.size() < 5) {
            ImageView betImage = new ImageView(this);
            if (betNum >= 500) {
                betImage.setImageResource(R.drawable.m500);
                betNum -= 500;
            } else if (betNum >= 100) {
                betImage.setImageResource(R.drawable.m100);
                betNum -= 100;
            } else if (betNum >= 50) {
                betImage.setImageResource(R.drawable.m50);
                betNum -= 50;
            } else if (betNum >= 10) {
                betImage.setImageResource(R.drawable.m10);
                betNum -= 10;
            } else {
                // todo
                betImage.setImageResource(R.drawable.m1000);
                betNum -= 5;
            }
            betImage.setScaleType(ImageView.ScaleType.FIT_XY);
            betImages.add(betImage);
        }


        int anim_start_px = playerBet.getHeight();
        int anim_end_px = (int) (5f * getResources().getDisplayMetrics().density + 0.5f) ;
        int bet_px = (int) (25f * getResources().getDisplayMetrics().density + 0.5f);
        int bet_area_px = (int) (60f * getResources().getDisplayMetrics().density + 0.5f);

        int margin_w_px = 0;
        if(bet_px * betImages.size() > bet_area_px){
            margin_w_px = (int)Math.ceil((double)(bet_px * betImages.size() - bet_area_px)/(betImages.size() - 1));
        }

        for (int i = 0; i < betImages.size(); i++) {
            TranslateAnimation translate = new TranslateAnimation(i * anim_end_px, 0, anim_start_px, 0);
            translate.setDuration(300);
            translate.setStartOffset(50 * i);
            betImages.get(i).startAnimation(translate);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(bet_px, bet_px);
            if(i > 0) {
                lp.setMargins(-margin_w_px, 0, 0, 0);
            }
            betImages.get(i).setLayoutParams(lp);

            playerBet.addView(betImages.get(i));

        }

    }

	void firstDeal()
	{
		//BJHand hand=player.getHand((byte)0); //the player only has one hand at the beginning of the round
		dealCard(currentPlayerHand);
		dealCard(dealerHand, true);
		dealCard(currentPlayerHand);
        dealCard(dealerHand, true, true);

		currentPlayerHand.checkIfHasBJ();

		updatePlayerSumlbl(0);
        updateDealerSumlbl(dealerHand.getSoftBJValue(), dealerHand.getHardBJValue());

		handler.postDelayed(new Runnable() {
            public void run() {
                dealerSumView.setVisibility(RelativeLayout.VISIBLE);
            }
        }, waittime);
		handler.postDelayed(new Runnable() {
            public void run() {
                currentPlayerSumView.setVisibility(RelativeLayout.VISIBLE);
            }
        }, waittime);

        if (isInsurable()) {
            beginInsurance();
        } else {
            checkPlayerHand(currentPlayerHand);
            if (!currentPlayerHand.isPlaying()) {
                getNextAction=true;
                // getNextAction(Action.DEAL);
                /*
                handler.postDelayed(new Runnable() {
                    public void run() {
                        waittime = 0;
                        act(true);
                    }
                }, waittime + 300);
                /*
                byte dealerCardValue = dealerHand.getCard((byte)0).getValue();
                if(dealerCardValue == 1 || dealerCardValue >= 10) {
                    faceUpCard(dealerHand, faceDownCard);
                    dealerHand.checkIfHasBJ();
                }
                payout();
                */
            } else {
                //waittime += 500;
                handler.postDelayed(showActionButton, waittime);
            }
        }
	}

    boolean isInsurable(){
        if ( settings.insurance && dealerHand.getCard((byte)0).getValue()==1 &&
                player.getBalance() >= (float)(0.5*currentPlayerHand.getBet().getValue())) {
            return true;
        }else{
            return false;
        }
    }

    void beginInsurance(){
        insurancingFlg = true;
        handler.postDelayed(new Runnable() {
            public void run() {
                // display insurance buttoms
                findViewById(R.id.insuranceAsk).setVisibility(TextView.VISIBLE);
                findViewById(R.id.insuranceYes).setVisibility(Button.VISIBLE);
                findViewById(R.id.insuranceNo).setVisibility(Button.VISIBLE);
            }
        }, waittime);
    }

    void insurance(){
        float betValue=currentPlayerHand.getBet().getValue();
        float insuranceBetValue = (float)(0.5*betValue); //insurance is a separate bet from the main bet
        player.takeInsurance(insuranceBetValue);
        player.withdraw(insuranceBetValue); //withraw the bet from player's wallet

        findViewById(R.id.insurance).setVisibility(RelativeLayout.VISIBLE);
        LinearLayout insuranceBet = (LinearLayout)findViewById(R.id.insuranceBet);
        setBet(insuranceBet, (int) insuranceBetValue);
        ((TextView)findViewById(R.id.insuranceBetNum)).setText(String.valueOf((int) insuranceBetValue));
        //updatePlayerCashlblForPlaying();
        updatePlayerCashlbl();
        waittime += 300 + (insuranceBet.getChildCount() - 1) * 50;
    }

    void endInsurance(){
        insurancingFlg = false;
        checkPlayerHand(currentPlayerHand);
        if (currentPlayerHand.isPlaying()) {
            handler.postDelayed(showActionButton, waittime);
        }
        findViewById(R.id.insuranceAsk).setVisibility(TextView.INVISIBLE);
        findViewById(R.id.insuranceYes).setVisibility(Button.INVISIBLE);
        findViewById(R.id.insuranceNo).setVisibility(Button.INVISIBLE);
    }

	private final Runnable showActionButton = new Runnable() {
		@Override
		public void run() {
            findViewById(R.id.standButton).setVisibility(Button.VISIBLE);
            findViewById(R.id.hitButton).setVisibility(Button.VISIBLE);
            checksurrenderbutton();
            checkddbutton();
            checksplitbutton();
            /*
            if(dealerHand.getCard((byte)0).getValue() != 1) {
                findViewById(R.id.surrenderButton).setVisibility(Button.VISIBLE);
            }
            if(player.getBalance() >= currentPlayerHand.getBet().getValue()) {
                findViewById(R.id.ddButton).setVisibility(Button.VISIBLE);
            }
			if(currentPlayerHand.splitable(settings.aceResplit) &&
                    player.getBalance() >= currentPlayerHand.getBet().getValue()){
                Button splitButton = (Button) findViewById(R.id.splitButton);
                splitButton.setVisibility(Button.VISIBLE);
			}
			*/
		}
	};


    void checksurrenderbutton(){
        if (player.howManyHands() == 1 &&
                currentPlayerHand.getCardCount() == 2 &&
                dealerHand.getCard((byte)0).getValue() != 1){
            findViewById(R.id.surrenderButton).setVisibility(Button.VISIBLE);
        }else{
            findViewById(R.id.surrenderButton).setVisibility(Button.INVISIBLE);
        }
        /*
        if (player.howManyHands() > 1 ||
                currentPlayerHand.getCardCount() > 2) {
            findViewById(R.id.surrenderButton).setVisibility(Button.INVISIBLE);
        }
        */
    }

    void checkddbutton(){
        if (currentPlayerHand.getCardCount() == 2 &&
                player.getBalance() >= currentPlayerHand.getBet().getValue()){
            findViewById(R.id.ddButton).setVisibility(Button.VISIBLE);
        }else{
            findViewById(R.id.ddButton).setVisibility(Button.INVISIBLE);
        }
        /*
        if (currentPlayerHand.getCardCount() > 2 ||
                player.getBalance() < currentPlayerHand.getBet().getValue()) {
            findViewById(R.id.ddButton).setVisibility(Button.INVISIBLE);
        }
        */
    }

    void checksplitbutton(){
        if(currentPlayerHand.splitable(settings.aceResplit) &&
                player.howManyHands() <= settings.splits &&
                player.getBalance() >= currentPlayerHand.getBet().getValue()){
            findViewById(R.id.splitButton).setVisibility(Button.VISIBLE);
        }else{
            findViewById(R.id.splitButton).setVisibility(Button.INVISIBLE);
        }

        /*
        if (!currentPlayerHand.splitable(settings.aceResplit) ||
                player.howManyHands() > settings.splits ||
                player.getBalance() < currentPlayerHand.getBet().getValue()){
            Button splitButton = (Button) findViewById(R.id.splitButton);
            splitButton.setVisibility(Button.INVISIBLE);
        }
        */
    }

    void dealCard(BJHand hand) {
        dealCard(hand, false, false);
    }
    void dealCard(BJHand hand, boolean playerFlg) {
        dealCard(hand, playerFlg, false);
    }
	void dealCard(BJHand hand, boolean dealerFlg, boolean faceDown)
	{
		if (shoe.cardsLeft()==0)
		{
			shoe.fillDeck();
			shoe.shuffleDeck();
		}

	    Card card=shoe.drawCard();

        if (faceDown) {
            card.flip();
            faceDownCard=card;
        }

        hand.addCard(card);

        ImageView cardImage=new ImageView(this);
	    cardImage.setImageResource(card.getImage());
		cardImage.setScaleType(ImageView.ScaleType.FIT_XY);

        int w_px = (int) (80f * getResources().getDisplayMetrics().density + 0.5f);
        int h_px = (int) (120f * getResources().getDisplayMetrics().density + 0.5f);
        int margin_w_px = (int) (12f * getResources().getDisplayMetrics().density + 0.5f) ;

		if(hand.getCardCount() > 1) {
			//マージンを設定
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(w_px, h_px);
			lp.setMargins(margin_w_px * (hand.getCardCount() - 1), 0, 0, 0);
			cardImage.setLayoutParams(lp);

		}else{
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w_px, h_px);
			cardImage.setLayoutParams(lp);
		}

        int w_anim_start_px = (int) (350f * getResources().getDisplayMetrics().density + 0.5f);
        int h_anim_start_px = (int) (200f * getResources().getDisplayMetrics().density + 0.5f);

		if(!dealerFlg) {
			TranslateAnimation translate = new TranslateAnimation(w_anim_start_px, 0, -h_anim_start_px, 0);
			translate.setDuration(500);
			translate.setStartOffset(waittime);
			cardImage.startAnimation(translate);
		}else{
			TranslateAnimation translate = new TranslateAnimation(w_anim_start_px, 0, 0, 0);
			translate.setDuration(500);
			translate.setStartOffset(waittime);
			cardImage.startAnimation(translate);
		}

        RelativeLayout handView=(RelativeLayout)(hands.get(hand).getChildAt(0));
        handView.addView(cardImage);

        // 再生
        handler.postDelayed(new Runnable() {
            public void run() {
                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                int musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                mSoundPool.play(mCardfall, (float) musicVol, (float) musicVol, 0, 0, 1.0F);
            }
        }, waittime);

        waittime += 500;

    }

    void faceUpCard(BJHand hand, Card card)
    {
        RelativeLayout handView=(RelativeLayout)(hands.get(hand).getChildAt(0));
        handView.removeViewAt(1);
        card.flip();
        ImageView cardImage=new ImageView(this);
        cardImage.setImageResource(card.getImage());

        int w_px = (int) (80f * getResources().getDisplayMetrics().density + 0.5f);
        int h_px = (int) (120f * getResources().getDisplayMetrics().density + 0.5f);
        int margin_w_px = (int) (12f * getResources().getDisplayMetrics().density + 0.5f) ;
        //マージンを設定
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(w_px, h_px);
        lp.setMargins(margin_w_px * (hand.getCardCount() - 1), 0, 0, 0);
        cardImage.setLayoutParams(lp);
        handView.addView(cardImage);

        //再計算
        hand.addCard(null);
    }

	void updatePlayerSumlbl(int handIndex){
		BJHand hand = player.getHand((byte) handIndex);
		byte soft = hand.getSoftBJValue();
		String softString = "";

		if (soft > 0) softString = "/" + String.valueOf(soft);

        RelativeLayout playerSumView = playerSumViews.get(handIndex);
		playerSum = (TextView) playerSumView.getChildAt(1);
		playerSum.setText(String.valueOf(hand.getHardBJValue()) + softString);
	}

	void updateDealerSumlbl(byte soft, byte hard)
	{
		String softString = "";

		if (soft > 0)    softString = "/"+ String.valueOf(soft);

        dealerSum.setText(String.valueOf(hard)  + softString );
	}

	void updatePlayerBetlbl()
    { playerBet.setText(String.valueOf((int)player.getInitBet() ) );}

    void updatePlayerBetlblForPlaying()
    { currentPlayerBetNumView.setText(String.valueOf((int) player.getInitBet()));}

    int countUpNum;
    /*
    void countUpPlayerCash(int beforeCash, int afterCash){
        int loopCnt;
        int countUpCash;
        if((afterCash - beforeCash) < 10){
            countUpCash = 1;
            loopCnt = afterCash - beforeCash;
        }else{
            countUpCash = (afterCash - beforeCash) / 10;
            loopCnt = 10;
        }
        for (int i = 1; i <= loopCnt; i++) {
            if (i == loopCnt) {
                countUpNum = (int) player.getBalance();
            } else {
                countUpNum = beforeCash + (countUpCash * i);
            }
            handler.postDelayed(new Runnable() {
                int updateCash = countUpNum;
                public void run() {
                    playerCash.setText(String.valueOf(updateCash));
                }
            }, waittime + (i * 50));
        }
    }
    */
    void updatePlayerCashlbl()
    {
        int beforeCash = Integer.parseInt(String.valueOf(playerCash.getText()));
        int afterCash = (int) player.getBalance();

        int loopCnt;
        int countCash;
        int sign = 1;
        if(afterCash - beforeCash < 0){
            sign = -1;
        }

        if((afterCash - beforeCash) * sign < 10){
            countCash = sign;
            loopCnt = (afterCash - beforeCash) * sign;
        }else{
            countCash = (afterCash - beforeCash) / 10;
            loopCnt = 10;
        }
        for (int i = 1; i <= loopCnt; i++) {
            if (i == loopCnt) {
                countUpNum = (int) player.getBalance();
            } else {
                countUpNum = beforeCash + (countCash * i);
            }
            handler.postDelayed(new Runnable() {
                int updateCash = countUpNum;
                public void run() {
                    playerCash.setText(String.valueOf(updateCash));
                }
            }, waittime + (i * 50));
        }
    }

    void updatePlayerResultlbl() {
        int animWaittime = waittime + 500;
        int soundWaittime = waittime + 500;
        boolean winSoundFlg = false;
        boolean loseSoundFlg = false;
        boolean pushSoundFlg = false;
        for (global_b = 0; global_b < player.howManyHands(); global_b++){
			switch (player.getHand(global_b).getStatus()) {
    			case PLAYING:break;
                case FINISHED:break;
    			case SURRENDERED:
    			{
                    handler.postDelayed(new Runnable() {
                        byte index = global_b;
                        public void run() {
                            playerResults.get(index).setImageResource(R.drawable.surrender);
                            playerResults.get(index).setVisibility(ImageView.VISIBLE);
                        }
                    }, animWaittime);
                    player.getHand(global_b).setStatus(Status.FINISHED);
                    break;
    			}
				case BLACKJACK:
				{
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            findViewById(R.id.playerBlackjack).setVisibility(ImageView.VISIBLE);
                            ScaleAnimation buttonanim =
                                    new ScaleAnimation(
                                            0.0f,1.0f,0.0f,1.0f,
                                            findViewById(R.id.playerBlackjack).getWidth()/2,
                                            findViewById(R.id.playerBlackjack).getHeight()/2);
                            buttonanim.setDuration(300);
                            findViewById(R.id.playerBlackjack).startAnimation(buttonanim);

                            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            int musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                            mSoundPool.play(mBlackjack, (float) musicVol, (float) musicVol, 0, 0, 1.0F);
                        }
                    },animWaittime);
                    soundWaittime += 1000;
                    player.getHand(global_b).setStatus(Status.FINISHED);
                    break;
				}
				case WON:
    			{
                    handler.postDelayed(new Runnable() {
                        byte index = global_b;

                        public void run() {
                            playerResults.get(index).setImageResource(R.drawable.win);
                            playerResults.get(index).setVisibility(ImageView.VISIBLE);
                        }
                    }, animWaittime);
                    if(!winSoundFlg) {
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                                int musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                                mSoundPool.play(mWin, (float) musicVol, (float) musicVol, 0, 0, 1.0F);
                            }
                        }, soundWaittime);
                        soundWaittime += 800;
                        winSoundFlg = true;
                    }
                    player.getHand(global_b).setStatus(Status.FINISHED);
    				break;
    			}
    			case PUSH:
    			{
                    handler.postDelayed(new Runnable() {
                        byte index = global_b;
                        public void run() {
                            playerResults.get(index).setImageResource(R.drawable.push);
                            playerResults.get(index).setVisibility(ImageView.VISIBLE);
                        }
                    },animWaittime);
                    if(!pushSoundFlg) {
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                                int musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                                mSoundPool.play(mPush, (float) musicVol, (float) musicVol, 0, 0, 1.0F);
                            }
                        },soundWaittime);
                        soundWaittime += 600;
                        pushSoundFlg = true;
                    }
                    player.getHand(global_b).setStatus(Status.FINISHED);
    				break;
    			}
    			case LOST:
    			{
                    handler.postDelayed(new Runnable() {
                        byte index = global_b;
                        public void run() {
                            playerResults.get(index).setImageResource(R.drawable.lose);
                            playerResults.get(index).setVisibility(ImageView.VISIBLE);
                        }
                    },animWaittime);
                    if(!loseSoundFlg) {
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                                int musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                                mSoundPool.play(mLose, (float) musicVol, (float) musicVol, 0, 0, 1.0F);
                            }
                        },soundWaittime);
                        soundWaittime += 900;
                        loseSoundFlg = true;
                    }
                    player.getHand(global_b).setStatus(Status.FINISHED);
					break;
				}
    		}
    	}
        if(animWaittime != soundWaittime){
            waittime += 500;
        }
    }
    
	public void onClick(final View view)
    {
        if(clickable != true){
            return;
        }
        clickable = false;
        waittime = 0;

       	final Action action = buttons.get(view.getId());
        
        if (betting)
        {
        	switch (action) 
        	{
        		case DEAL:
        		{
					if (player.getInitBet()>=settings.tableMin && player.getInitBet()<=settings.tableMax) {
                        editor.putInt("plays", preference.getInt("plays", 0) + 1);
                        editor.commit();
        				betting= false;
                        // dealer view reset
                        RelativeLayout handView = (RelativeLayout) (dealerHandView.getChildAt(0));
                        handView.removeAllViews();
                        dealerSumView.setVisibility(RelativeLayout.INVISIBLE);
                        // player view reset
                        for(byte b = 0; b < 2; b++){
                            ((RelativeLayout)(playerHandViews.get(b).getChildAt(0))).removeAllViews();
                            playerSumViews.get(b).setVisibility(RelativeLayout.INVISIBLE);;
                            ((LinearLayout)(playerBetViews.get(b).getChildAt(0))).removeAllViews();
                            ((LinearLayout)(playerBetViews.get(b).getChildAt(1))).removeAllViews();
                            playerResults.get(b).setVisibility(TextView.INVISIBLE);
                            playerBetNumViews.get(b).setText("");
                        }
                        findViewById(R.id.CenterView).setVisibility(View.GONE);
                        findViewById(R.id.insuranceResult).setVisibility(View.INVISIBLE);
                        findViewById(R.id.playerBlackjack).setVisibility(ImageView.INVISIBLE);
                        findViewById(R.id.insurance).setVisibility(RelativeLayout.INVISIBLE);
                        ((LinearLayout)(findViewById(R.id.insuranceBet))).removeAllViews();
                        initUI();
                        firstDeal();
						findViewById(R.id.betting).setVisibility(RelativeLayout.INVISIBLE);
					}
        			break;
        		}
        		case CLEAR:
        		{
	        		clearBet();
        			break;
        		}
                case ALL:
				{
                    int maxbet;
                    if(player.getBalance() > settings.tableMax){
                        maxbet = (int)(settings.tableMax - currentPlayerHand.getBet().getValue());
                    }else{
                        maxbet=(int)(player.getBalance() - currentPlayerHand.getBet().getValue());
                    }
                    // 10pt単位でしかかけられない
                    maxbet = maxbet - (maxbet % 10);
                    collectBet(maxbet);
					break;
				}
				case REBET:
        		{
        			collectBet(player.getRebet());
        			break;
        		}
				case ONE:
				{
					collectBet(1f);
					break;
				}
				case TEN:
        		{
	        		collectBet(10f);
        			break;
        		}
        		case FIFTY:
	        	{
        			collectBet(50f);
        			break;
        		}
	        	case ONEHUNDRED:
        		{
        			collectBet(100f);
        			break;
	        	}
        		case FIVEHUNDRED:
        		{
        			collectBet(500f);
        			break;
	        	}
				case ONETHOUSAND:
				{
					collectBet(1000f);
					break;
				}
                case BONUS:
                {
                    if (mInterstitialAd.isLoaded()) {
                        mInterstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                beginBonusGame();
                            }
                        });
                        mInterstitialAd.show();
                    } else {
                        beginBonusGame();
                    }
                    break;
                }
            }

            vibrator.vibrate(40);
		}
        
        else
        {
        	switch (action)
        	{
    			case HIT:
    			{
    				dealCard(currentPlayerHand);
					handler.postDelayed(new Runnable() {
                        int NowIndex = currentPlayerIndex;

                        public void run() {
                            updatePlayerSumlbl(NowIndex);
                        }
                    }, waittime);
                    checkPlayerHand(currentPlayerHand);
                    break;
                }
                case STAND: {
                    currentPlayerHand.setPlaying(false);
                    break;
                }
                case DOUBLEDOWN: {
                    editor.putInt("doubles", preference.getInt("doubles", 0) + 1);
                    editor.commit();
    				dd(currentPlayerHand);
					handler.postDelayed(new Runnable() {
                        int NowIndex = currentPlayerIndex;

                        public void run() {
                            updatePlayerSumlbl(NowIndex);
                        }
                    }, waittime);
					break;
                }
                case SPLIT: {
                    editor.putInt("splits", preference.getInt("splits", 0) + 1);
                    split(currentPlayerHand);
                    break;
    			}
                case SURRENDER: {
                    editor.putInt("surrenders", preference.getInt("surrenders", 0) + 1);
                    editor.commit();
                    surrender(currentPlayerHand);
                    break;
                }
                case INSURANCE_YES:{
                    insurance();
                    endInsurance();
                    break;
                }
                case INSURANCE_NO:{
                    endInsurance();
                    break;
                }
        	}
        	
        	vibrator.vibrate(40);

            checksurrenderbutton();
            checkddbutton();
            checksplitbutton();

       	}

        if (getNextAction)	getNextAction(action);

        handler.postDelayed(new Runnable() {
            public void run() {
                clickable = true;
            }
        }, waittime);
    }

    void beginBonusGame(){
        Intent intent = new Intent(this, Bonus.class);
        startActivity(intent);
    }

    void getNextAction(Action action)
    {
    	nextHandtoPlay=(byte)-1;
    	
    	for ( byte b=0 ; b < player.howManyHands() ; b++ )
    		if (player.getHand(b).isPlaying())
    		{
    			nextHandtoPlay=b;
    			break;
    		}
    	
		if (nextHandtoPlay == -1)
    	{
			byte nextHandtoPay=(byte) -1;
    		
    		for ( byte b=0 ; b < player.howManyHands() ; b++ )
        		if (player.getHand(b).toBePayed())
        		{
        			nextHandtoPay=b;
        			break;
        		}
    	
    		if (nextHandtoPay == -1 && !player.tookInsurance())
    		{
                handler.postDelayed(new Runnable() {
					public void run() {
                        playerOverViews.get(currentPlayerIndex).setBackgroundResource(0);
                        disableButtons();
                        newGame();
					}
				}, waittime);

	    	}
    		else {
                if(currentPlayerHand.hasBJ() || currentPlayerHand.has21() ||
                        action == action.SPLIT || action == action.DOUBLEDOWN){
                    waittime+=300;
                }

                handler.postDelayed(new Runnable() {
                    public void run() {
                        waittime = 0;
                        disableButtons();
                        playerOverViews.get(currentPlayerIndex).setBackgroundResource(0);
                        act();
                    }
                }, waittime);
            }
        } else {
            currentPlayerHand = player.getHand(nextHandtoPlay);
            currentPlayerHandView = hands.get(currentPlayerHand);
            currentPlayerSumView = playerSumViews.get(nextHandtoPlay);
            currentPlayerBetView = playerBetViews.get(nextHandtoPlay);
            currentPlayerBetNumView = playerBetNumViews.get(nextHandtoPlay);

            if (currentPlayerIndex != nextHandtoPlay ||
                    action == action.SPLIT){
                checksplitbutton();
                //if(action != action.STAND){
                    //waittime += 500;
                //}
                handler.postDelayed(new Runnable() {
                    public void run() {
                        findViewById(R.id.ddButton).setVisibility(Button.VISIBLE);
                        playerOverViews.get(currentPlayerIndex).setBackgroundResource(0);
                        playerOverViews.get(nextHandtoPlay).setBackgroundResource(R.drawable.layout_shape);
                        currentPlayerIndex = nextHandtoPlay;
                    }
                }, waittime);
			}
    	}
    }

    public void onClickHeader(final View view) {
        // ヘッダー用
        switch (view.getId()) {
            case R.id.competition: {
                Intent intent = new Intent(this, Competition.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return;
            }
            case R.id.result: {
                Intent intent = new Intent(this, Result.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return;
            }
            case R.id.checkMyData: {
                Intent intent = new Intent(this, MyData.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return;
            }
            case R.id.setting: {
                Intent intent = new Intent(this, Setting.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return;
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