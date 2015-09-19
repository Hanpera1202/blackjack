package com.ivoid.bj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.bj.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
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
	private Vibrator vibrator;
    private SoundPool mSoundPool;
    private int mCardfall;
    private int mLose;
    private int mWin;
    private int mBlackjack;

	private GameSettings settings; 
	private Deck shoe;
	private BJHand dealerHand;
	private TextView dealerSum;

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
	private ArrayList<TextView> playerResults;

	private TextView playerBet;
	private TextView playerSum;
	private TextView playerCash;

	private Map<Integer, Action> buttons;
	private Map<BJHand, RelativeLayout> hands;

	private final Handler handler = new Handler();

	private byte currentPlayerIndex;
    private byte nextHandtoPlay;
	private float beforeInitBet;

    private SharedPreferences preference;
    private SharedPreferences.Editor editor;

    AdView mAdView;
    AdRequest adRequest;
    InterstitialAd mInterstitialAd;

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
        currentPlayerHand = player.getHand((byte)0);
        betting = true;
        clickable = true;

        setContentView(R.layout.playing);
        setViews();
        initUI();

        // display ad
        mAdView = (AdView) findViewById(R.id.adView);
        mAdView.loadAd(adRequest);

    }

    @Override
    protected void onResume(){
        super.onResume();

        //インタースティシャル広告の読み込み
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.inters_ad_unit_id));
        requestNewInterstitial();

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
        mLose = mSoundPool.load(getApplicationContext(), R.raw.lose, 1);
        mWin = mSoundPool.load(getApplicationContext(), R.raw.win, 1);
        mBlackjack = mSoundPool.load(getApplicationContext(), R.raw.blackjack, 1);

        if(preference.getFloat("gotBonusPoints", 0f) >= 10.0f){
            player.deposit(preference.getFloat("gotBonusPoints", 0f));
            editor.putFloat("gotBonusPoints", 0f);
            editor.commit();
            Button bonus = (Button) findViewById(R.id.bonus);
            bonus.setAnimation(null);
            bonus.setVisibility(Button.INVISIBLE);
            beforeInitBet = 0f;
            initUI();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // リリース
        mSoundPool.release();
    }

    private void setViews(){

        // dealerHandZoneを最前面へ
        findViewById(R.id.dealerInfo).bringToFront();

        // getBonusを最前面へ
        findViewById(R.id.bonus).bringToFront();

        // playerInfoを最前面へ
        findViewById(R.id.playerInfo1).bringToFront();
        findViewById(R.id.playerInfo2).bringToFront();

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

        playerResults = new ArrayList<TextView>();
        playerResults.add((TextView) findViewById(R.id.playerResult1));
        playerResults.add((TextView) findViewById(R.id.playerResult2));
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
        playerCash=(TextView)findViewById(R.id.playerCash);

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
			buttons.put(R.id.checkMyData, Action.CHECK);

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
            }

            editor.putFloat("Balance", player.getBalance());
            editor.commit();

            collectBet(beforeInitBet);

		}
		else
		{
			getNextAction=true;
            player.withdraw(currentPlayerHand.getBet().getValue());
			
			buttons.put(R.id.standButton, Action.STAND);
			buttons.put(R.id.hitButton, Action.HIT);
			buttons.put(R.id.ddButton, Action.DOUBLEDOWN);
			buttons.put(R.id.splitButton, Action.SPLIT);
            buttons.put(R.id.checkMyData, Action.CHECK);

			hands=new HashMap<BJHand, RelativeLayout>();

			hands.put(player.getHand((byte) 0), playerHandViews.get(0));
			hands.put(dealerHand, dealerHandView);

			currentPlayerIndex = 0;
			currentPlayerHandView=playerHandViews.get(currentPlayerIndex);
            currentPlayerSumView=playerSumViews.get(currentPlayerIndex);
			currentPlayerBetView=playerBetViews.get(currentPlayerIndex);
			currentPlayerBetNumView=playerBetNumViews.get(currentPlayerIndex);

			playerSum=(TextView) currentPlayerSumView.getChildAt(1);
			dealerSum=(TextView) dealerSumView.getChildAt(1);

            setBet((LinearLayout) currentPlayerBetView.getChildAt(0), (int) player.getInitBet());
			updatePlayerBetlblForPlaying();
            updatePlayerCashlblForPlaying();

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
		
		updatePlayerCashlblForPlaying();
	}
	
	void setPlaying(BJHand hand, boolean b)
	{
        hand.setPlaying(b); }
	
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
					hand.setStatus(Status.DRAW);
                    editor.putInt("draws", preference.getInt("draws", 0) + 1);
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
				hand.setStatus(Status.DRAW);
                editor.putInt("draws", preference.getInt("draws", 0) + 1);
                editor.commit();
			}

		}
        updatePlayerResultlbl();

        handler.postDelayed(new Runnable() {
            public void run() {
                newGame();
            }
        }, waittime);
	}

	void newGame()
	{

        findViewById(R.id.betting).setVisibility(LinearLayout.VISIBLE);
        dealerHand.update();
		player.update();
		currentPlayerHand=player.getHand((byte)0);
		betting=true;
		getNextAction=false;
		//setContentView(R.layout.playing);
        updatePlayerBetlbl();
        initUI();
	}
		
	void clearBet()
	{
		BJHand hand = player.getHand((byte)0);
		float betValue=hand.getBet().getValue();
        hand.clearBet(); //remove the bet from the table
        //player.deposit(betValue); //put bet back into player's wallet
        updatePlayerBetlbl();
        updatePlayerCashlbl();
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
        updatePlayerCashlbl();
        //player.withdraw(stake); //take the bet out of player's wallet
    }
	
	void checkPlayerHand(BJHand hand)
	{
		hand.checkIfBusted();
		hand.checkIfHasBJ();
        hand.checkIfHas21();

		if(hand.didBust()) // bust animation
		{
			hand.setToBePayed(false);
			hand.setPlaying(false);
			hand.setStatus(Status.LOST);

            updatePlayerResultlbl();
		} 
		else if(hand.hasBJ() || hand.has21()) {
            hand.setPlaying(false);
            waittime += 500;
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

			updatePlayerCashlblForPlaying();

            dealCard(hand, true);

            setBet((LinearLayout) currentPlayerBetView.getChildAt(1), (int) beforeInitBet);
			updatePlayerBetlblForPlaying();

			hand.setPlaying(false);

            checkPlayerHand(hand);
		}
	}
	
	void surrender(BJHand hand)
	{
		if (hand.getCardCount()==2)
		{
			float betValue = hand.getBet().getValue();
			hand.update(); // clear hand
			payTransaction(betValue, settings.surrenderPay); //player gets half his bet back via negative ratio
			hand.setStatus(Status.SURRENDERED);
			hand.setToBePayed(false);
			hand.setPlaying(false);

			updatePlayerResultlbl();
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
            updatePlayerCashlblForPlaying();

            hands.put(splitHand, playerHandViews.get(1));
			findViewById(R.id.playerInfo2).setVisibility(View.VISIBLE);
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

            // マージンを再セット
            ViewGroup.MarginLayoutParams handMlp =
                    (ViewGroup.MarginLayoutParams)hands.get(hand).getChildAt(0).getLayoutParams();
            handMlp.leftMargin = (int) (41f * getResources().getDisplayMetrics().density + 0.5f);
            hands.get(hand).getChildAt(0).setLayoutParams(handMlp);
            ViewGroup.MarginLayoutParams splitHandMlp =
                    (ViewGroup.MarginLayoutParams)hands.get(splitHand).getChildAt(0).getLayoutParams();
            splitHandMlp.leftMargin = (int) (41f * getResources().getDisplayMetrics().density + 0.5f);
            hands.get(splitHand).getChildAt(0).setLayoutParams(splitHandMlp);
			
			dealCard(hand, true);
            handler.postDelayed(new Runnable() {
                public void run() {
                    updatePlayerSumlbl(0);
                    playerSumViews.get(0).setVisibility(RelativeLayout.VISIBLE);
                }
            }, waittime);
            checkPlayerHand(hand);

            dealCard(splitHand, true);
            handler.postDelayed(new Runnable() {
                public void run() {
                    updatePlayerSumlbl(1);
                    playerSumViews.get(1).setVisibility(RelativeLayout.VISIBLE);
                }
            }, waittime);
			checkPlayerHand(splitHand);

            playerSum=(TextView) currentPlayerSumView.getChildAt(1);

			currentPlayerHand=player.getHand((byte)(player.howManyHands()-2));

            if(poppedCard.getValue() == 1){
				for ( byte b=0 ; b < player.howManyHands() ; b++ )
					player.getHand(b).setPlaying(false);
			}else{
                waittime += 500;
                handler.postDelayed(new Runnable() {
                    public void run() {
                        playerOverViews.get(currentPlayerIndex).setBackgroundResource(R.drawable.layout_shape);
                    }
                }, waittime);
            }
		}
	}

	void disableButtons()
	{	
		for (final Integer entry : buttons.keySet())
		{
            if(entry != R.id.checkMyData) {
                ((Button) findViewById(entry)).setVisibility(ImageView.GONE);
                ((Button) findViewById(entry)).setOnClickListener(null);
            }
		}
	}
	
	void act()
	{
        int startWaittime = waittime;
        RelativeLayout handView = (RelativeLayout)dealerHandView.getChildAt(0);
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)handView.getLayoutParams();
        int startMargin = mlp.leftMargin;
        if (settings.stand17soft)
			while ( dealerHand.getHardBJValue() < 17)
			{
				if (dealerHand.getSoftBJValue() >= 17)    break;    // stand on soft 17
				dealCard(dealerHand, false);
				handler.postDelayed(new Runnable() {
					byte soft = dealerHand.getSoftBJValue();
					byte hard = dealerHand.getHardBJValue();
					public void run() {
						updateDealerSumlbl(soft, hard);
					}
				}, waittime);
			}
		else
			while ( dealerHand.getHardBJValue() < 17 ) {
				dealCard(dealerHand, false);
				handler.postDelayed(new Runnable() {
					byte soft = dealerHand.getSoftBJValue();
					byte hard = dealerHand.getHardBJValue();
					public void run() {
						updateDealerSumlbl(soft, hard);
					}
				}, waittime);
			}

        int endMargin = mlp.leftMargin;
        int relocation_start_x = startMargin - endMargin;
        int duration;
        if( endMargin == 0) {
            duration = 7 * 70;
        }else{
            duration = (dealerHand.getCardCount() - 1) * 70;
        }
        TranslateAnimation translate = new TranslateAnimation(relocation_start_x, 0, 0, 0);
        translate.setDuration(duration);
        translate.setStartOffset(startWaittime + 500);
        handView.startAnimation(translate);

		dealerHand.checkIfHasBJ();
		dealerHand.checkIfBusted();

        payout();
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
            }
            betImage.setScaleType(ImageView.ScaleType.FIT_XY);
            betImages.add(betImage);
        }

        int anim_start_px = (int) (200f * getResources().getDisplayMetrics().density + 0.5f) ;
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
		dealCard(currentPlayerHand, true);
		dealCard(dealerHand, false);
		dealCard(currentPlayerHand, true);

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

		if (currentPlayerHand.hasBJ()){
			currentPlayerHand.setPlaying(false);
			byte dealerCardValue = dealerHand.getCard((byte)0).getValue();
			if(dealerCardValue == 1 || dealerCardValue >= 10) {
				dealCard(dealerHand, false);
				dealerHand.checkIfHasBJ();
			}
            payout();
		}else {
            waittime += 500;
			handler.postDelayed(showActionButton, waittime);
		}
	}

	private final Runnable showActionButton = new Runnable() {
		@Override
		public void run() {
			findViewById(R.id.standButton).setVisibility(Button.VISIBLE);
			findViewById(R.id.hitButton).setVisibility(Button.VISIBLE);
            if(player.getBalance() >= currentPlayerHand.getBet().getValue()) {
                findViewById(R.id.ddButton).setVisibility(Button.VISIBLE);
            }
			if(currentPlayerHand.splitable(settings.aceResplit) &&
                    player.getBalance() >= currentPlayerHand.getBet().getValue()){
				findViewById(R.id.splitButton).setVisibility(Button.VISIBLE);
			}
		}
	};

	void dealCard(BJHand hand, boolean playerFlg)
	{
		if (shoe.cardsLeft()==0)
		{
			shoe.fillDeck();
			shoe.shuffleDeck();
		}

	    Card card=shoe.drawCard();
	    
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
			if(playerFlg) {
				//if(hand.getCardCount() < 8) {
					//lp.setMargins(margin_w_px-w_px, 0, 0, margin_h_px * (hand.getCardCount() - 1));
                    lp.setMargins(margin_w_px * (hand.getCardCount() - 1), 0, 0, 0);
				//}else{
				//	lp.setMargins(-w_px, 0, 0, 0);
				//}
			}else{
				lp.setMargins(margin_w_px * (hand.getCardCount() - 1), 0, 0, 0);
			}
			cardImage.setLayoutParams(lp);

		}else{
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w_px, h_px);
			cardImage.setLayoutParams(lp);
		}

        int w_anim_start_px = (int) (350f * getResources().getDisplayMetrics().density + 0.5f);
        int h_anim_start_px = (int) (200f * getResources().getDisplayMetrics().density + 0.5f);

		if(playerFlg) {
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
/*
        int startMargin = (int) (41f * getResources().getDisplayMetrics().density + 0.5f);
        int setMargin = startMargin-(margin_w_px/2)*(hand.getCardCount() - 1);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)handView.getLayoutParams();
        if(setMargin > 0) {
            lp.setMargins(setMargin, 0, 0, 0);
        }else{
            lp.setMargins(0, 0, 0, 0);
        }
        handView.setLayoutParams(lp);
*/
        // 再生
        handler.postDelayed(new Runnable() {
            public void run() {
                AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                int musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                mSoundPool.play(mCardfall, (float) musicVol, (float) musicVol, 0, 0, 1.0F);
            }
        }, waittime);
/*
        if(setMargin > -margin_w_px/2) {
            int relocation_start_x = margin_w_px/2;
            if(setMargin < 0) {
                relocation_start_x = (margin_w_px/2)+setMargin;
            }
            if(playerFlg) {
                TranslateAnimation translate = new TranslateAnimation(relocation_start_x, 0, 0, 0);
                translate.setDuration(500);
                translate.setStartOffset(waittime);
                handView.startAnimation(translate);
            }else{
                //animationを上書きセット
                TranslateAnimation translate = new TranslateAnimation(relocation_start_x, 0, 0, 0);
                translate.setDuration(500);
                translate.setStartOffset(waittime);
                handView.startAnimation(translate);
            }
        }
*/

        waittime += 500;

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams)handView.getLayoutParams();

        if(hand.getCardCount() == 1) {
            mlp.leftMargin = (int) (41f * getResources().getDisplayMetrics().density + 0.5f);
            handView.setLayoutParams(mlp);
        }else if(mlp.leftMargin > 0){
            int relocation_start_x;
            if(mlp.leftMargin > margin_w_px/2) {
                relocation_start_x = margin_w_px/2;
                mlp.leftMargin -= margin_w_px/2;
            }else{
                relocation_start_x = mlp.leftMargin;
                mlp.leftMargin = 0;
            }
            handView.setLayoutParams(mlp);
            if(playerFlg) {
                TranslateAnimation translate = new TranslateAnimation(relocation_start_x, 0, 0, 0);
                translate.setDuration(100);
                translate.setStartOffset(waittime);
                handView.startAnimation(translate);
            }
        }


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

        dealerSum.setText ( String.valueOf( hard )  + softString );
	}

	void updatePlayerBetlbl()
    { playerBet.setText(String.valueOf((int)player.getInitBet() ) );}

    void updatePlayerBetlblForPlaying()
    { currentPlayerBetNumView.setText( String.valueOf( (int)player.getInitBet() ) );}

    void updatePlayerCashlbl()
    {
        float betValue=currentPlayerHand.getBet().getValue();
        playerCash.setText( String.valueOf( (int)(player.getBalance() - betValue )) );
    }

    void updatePlayerCashlblForPlaying()
    {
        playerCash.setText(String.valueOf( (int)player.getBalance() ) );
    }

    void updatePlayerResultlbl() {
        int startwaittime = waittime;
        for (global_b = 0; global_b < player.howManyHands(); global_b++){
			switch (player.getHand(global_b).getStatus()) {
    			case PLAYING:	break;
                case FINISHED:	break;
    			case SURRENDERED:
    			{
                    if(startwaittime == waittime) {
                        waittime += 500;
                    }
                    handler.postDelayed(new Runnable() {
                        byte index = global_b;
                        public void run() {
                            playerResults.get(index).setText("SURRENDERED!");
                            playerResults.get(index).setVisibility(TextView.VISIBLE);
                        }
                    }, waittime);
                    waittime += 1000;
                    player.getHand(global_b).setStatus(Status.FINISHED);
                    break;
    			}
				case BLACKJACK:
				{
                    if(startwaittime == waittime) {
                        waittime += 500;
                    }
                    handler.postDelayed(new Runnable() {
                        byte index = global_b;
                        public void run() {
                            playerResults.get(index).setText("BLACK JACK!!");
                            playerResults.get(index).setTextColor(Color.parseColor("#FFFFF800"));
                            playerResults.get(index).setVisibility(TextView.VISIBLE);
                            // 再生
                            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            int musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                            mSoundPool.play(mBlackjack, (float) musicVol, (float) musicVol, 0, 0, 1.0F);
                        }
                    },waittime);
                    waittime+=1000;
                    player.getHand(global_b).setStatus(Status.FINISHED);
                    break;
				}
				case WON:
    			{
                    if(startwaittime == waittime) {
                        waittime += 500;
                    }
                    handler.postDelayed(new Runnable() {
                        byte index = global_b;
                        public void run() {
					        playerResults.get(index).setText("WIN!");
                            playerResults.get(index).setTextColor(Color.parseColor("#FFFFEF00"));
                            playerResults.get(index).setVisibility(TextView.VISIBLE);
                            // 再生
                            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            int musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                            mSoundPool.play(mWin, (float) musicVol, (float) musicVol, 0, 0, 1.0F);
                        }
                    },waittime);
                    waittime+=1000;
                    player.getHand(global_b).setStatus(Status.FINISHED);
    				break;
    			}
    			case DRAW:
    			{
                    if(startwaittime == waittime) {
                        waittime += 500;
                    }
                    handler.postDelayed(new Runnable() {
                        byte index = global_b;
                        public void run() {
                            playerResults.get(index).setText("DRAW!");
                            playerResults.get(index).setTextColor(Color.parseColor("#FFFFFFFF"));
                            playerResults.get(index).setVisibility(TextView.VISIBLE);
                        }
                    },waittime);
                    waittime+=1000;
                    player.getHand(global_b).setStatus(Status.FINISHED);
    				break;
    			}
    			case LOST:
    			{
                    if(startwaittime == waittime) {
                        waittime += 500;
                    }
                    handler.postDelayed(new Runnable() {
                        byte index = global_b;
                        public void run() {
					        playerResults.get(index).setText("LOSE!");
                            playerResults.get(index).setTextColor(Color.parseColor("#FF00E1FF"));
                            playerResults.get(index).setVisibility(TextView.VISIBLE);
                            // 再生
                            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                            int musicVol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                            mSoundPool.play(mLose, (float) musicVol, (float) musicVol, 0, 0, 1.0F);
                        }
                    },waittime);
                    waittime+=1000;
                    player.getHand(global_b).setStatus(Status.FINISHED);
					break;
				}
    		}
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
                        //setContentView(R.layout.playing);
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
                        }
                        findViewById(R.id.playerInfo2).setVisibility(View.GONE);
                        findViewById(R.id.CenterView).setVisibility(View.GONE);

                        initUI();
                        firstDeal();
						findViewById(R.id.betting).setVisibility(RelativeLayout.INVISIBLE);
						beforeInitBet = player.getInitBet();
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
                    int maxbet=(int)(player.getBalance() - currentPlayerHand.getBet().getValue());
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
                case CHECK:
                {
                    // 遷移先のActivityを指定して、Intentを作成する
                    Intent intent = new Intent( this,MyData.class );
                    // 遷移先のアクティビティを起動させる
                    startActivity(intent);
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
    				dealCard(currentPlayerHand, true);
					handler.postDelayed(new Runnable() {
                        int NowIndex = currentPlayerIndex;
                        public void run() {
                            updatePlayerSumlbl(NowIndex);
                        }
                    }, waittime);
    				checkPlayerHand(currentPlayerHand);
					break;
    			}
    			case STAND:
    			{	
    				currentPlayerHand.setPlaying(false);
	    			break;
				}
				case DOUBLEDOWN:
    			{
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
    			case SURRENDER: {
                    editor.putInt("surrenders", preference.getInt("surrenders", 0) + 1);
                    editor.commit();
					surrender(currentPlayerHand);
					break;
				}
    			case SPLIT: {
                    editor.putInt("splits", preference.getInt("splits", 0) + 1);
                    split(currentPlayerHand);
                    break;
    			}
                case CHECK:
                {
                    // 遷移先のActivityを指定して、Intentを作成する
                    Intent intent = new Intent( this,MyData.class );
                    // 遷移先のアクティビティを起動させる
                    startActivity(intent);
                    break;
                }
        	}
        	
        	vibrator.vibrate(40);

            checkddbutton();
            checksplitbutton();

        	if (getNextAction)	getNextAction(action);
       	}

        handler.postDelayed(new Runnable() {
            public void run() {
                clickable = true;
            }
        }, waittime);
    }

    void beginBonusGame(){
        // 遷移先のActivityを指定して、Intentを作成する
        Intent intent = new Intent(this, Bonus.class);
        // 遷移先のアクティビティを起動させる
        startActivity(intent);
    }

    void checkddbutton(){
        if (currentPlayerHand.getCardCount() > 2 ||
                player.getBalance() < currentPlayerHand.getBet().getValue()) {
            findViewById(R.id.ddButton).setVisibility(Button.INVISIBLE);
        }
    }

    void checksplitbutton(){
        if (!currentPlayerHand.splitable(settings.aceResplit) ||
                player.howManyHands() > settings.splits){
            findViewById(R.id.splitButton).setVisibility(Button.GONE);
        }
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
    	
    		if (nextHandtoPay == -1)
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

                handler.postDelayed(new Runnable() {
                    public void run() {
                        disableButtons();
                        playerOverViews.get(currentPlayerIndex).setBackgroundResource(0);
                    }
                }, waittime);
                act();
            }
        } else {
            currentPlayerHand = player.getHand(nextHandtoPlay);
            currentPlayerHandView = hands.get(currentPlayerHand);
            currentPlayerSumView = playerSumViews.get(nextHandtoPlay);
            currentPlayerBetView = playerBetViews.get(nextHandtoPlay);
            currentPlayerBetNumView = playerBetNumViews.get(nextHandtoPlay);
            playerSum = (TextView) currentPlayerSumView.getChildAt(1);

            if (currentPlayerIndex != nextHandtoPlay){
                checksplitbutton();
                if(action != action.STAND){
                    //waittime += 500;
                }
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
}