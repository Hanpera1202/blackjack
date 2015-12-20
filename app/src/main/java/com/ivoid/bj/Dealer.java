package com.ivoid.bj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.bj.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


/*
 * I'm God.
 */

/*TODO
 * Pause in between dealCard()s in act()
 * Show amount in pot
 *  
 */

public class Dealer extends FragmentActivity implements OnClickListener
{
    Game game;

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
    private TextView playerProgress;
    private TextView playerCoin;
    private TextView playerLevel;
    private TextView playerMaxBet;

	private Map<Integer, Action> buttons;
	private Map<BJHand, RelativeLayout> hands;

	private final Handler handler = new Handler();

	private byte currentPlayerIndex;
    private byte nextHandtoPlay;

    private SharedPreferences preference;
    private SharedPreferences.Editor editor;

    private boolean insurancingFlg;
    private boolean clickable;
    private int waittime;
    private byte global_b;

    BonusCDTimer bonusCountDownTimer = null;
    FreeChipsCDTimer freeChipsCountDownTimer = null;

    private DialogFragment alertDialog;
    private ProgressBar bar;

    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        game = (Game) this.getApplication();

        //プリファレンスの準備
        preference = getSharedPreferences("user_data", MODE_PRIVATE);
        editor = preference.edit();

        settings = new GameSettings();
		shoe = new Deck( (byte)settings.decks,
						 (byte)settings.burns, 
						 (byte)settings.shuffleTimes 
					   ); 
		dealerHand = new BJHand("Dealer");

        player = new Player(getApplicationContext(), "God");

        currentPlayerHand = player.getHand((byte) 0);
        betting = true;
        clickable = true;

        setContentView(R.layout.playing);
        setViews();

        bar = (ProgressBar)findViewById(R.id.progressBar);
        bar.setMax(settings.necessaryPoint);
        ((TextView)findViewById(R.id.necessaryPoint)).
                setText("/" + settings.necessaryPoint + "pt");

    }

    @Override
    public void onResume()
    {
        super.onResume();

        // アニメーションスタート時間のリセット
        waittime = 0;

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

        playerLevel.setText(String.valueOf(player.getLevel()));
        playerMaxBet.setText("MAX " + player.getMaxBet() + "pt");
        playerCash.setText(String.valueOf((int) player.getBalance()));
        playerCoin.setText(String.valueOf((int) player.getCoinBalance()));
        setPlayerProgress((int) player.getBalance());
        if (betting){
            if (preference.getFloat("gotBonusPoint", 0f) > 0f) {
                player.deposit(preference.getFloat("gotBonusPoint", 0f));
                editor.putFloat("gotBonusPoint", 0f);
                editor.commit();
                updatePlayerCashlbl();
            }
            if (preference.getInt("gotBonusCoin", 0) > 0) {
                player.depositCoin(preference.getInt("gotBonusCoin", 0));
                editor.putInt("gotBonusCoin", 0);
                editor.commit();
                updatePlayerCoinlbl();
            }
            if(player.getBalance() < player.getInitBet()) {
                clearBet();
            }
            loginBonusCountDown();
            checkExecInitUI(0);
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

    @Override
    public void onPause()
    {
        super.onPause();
        mSoundPool.release();
    }

    private void setViews()
    {
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
        playerProgress=(TextView)findViewById(R.id.playerProgress);
        playerCoin=(TextView)findViewById(R.id.hintButton);
        playerLevel=(TextView)findViewById(R.id.playerLevel);
        playerMaxBet=(TextView)findViewById(R.id.playerMaxBet);
    }

    void checkExecInitUI(int waittime){
        if(player.getBalance() < 10.0f) {
            handler.postDelayed(new Runnable() {
                public void run() {
                    showFreeChipsButton();
                }
            }, waittime);
        }else{
            disableFreeChipsButton();
        }

        if (player.isLevelUp()) {
            handler.postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), LevelUp.class);
                    startActivity(intent);
                }
            }, waittime);
        }else if(getTimeLefOfLoginBonus() == 0L){
            handler.postDelayed(new Runnable() {
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), Bonus.class);
                    intent.putExtra("type", "login");
                    startActivity(intent);
                }
            }, waittime);
        }else{
            if (buttons == null) {
                findViewById(R.id.betting).setVisibility(LinearLayout.VISIBLE);
                findViewById(R.id.hintButton).setVisibility(LinearLayout.VISIBLE);
                initUI();
            }
        }
    }

    private void initUI()
	{
        buttons = new HashMap<Integer, Action>();

        if (betting)
		{
			buttons.put(R.id.clearButton, Action.CLEAR);
			buttons.put(R.id.dealButton, Action.DEAL);

			buttons.put(R.id.bet_10, Action.TEN);
			buttons.put(R.id.bet_50, Action.FIFTY);
			buttons.put(R.id.bet_100, Action.ONEHUNDRED);
			buttons.put(R.id.bet_500, Action.FIVEHUNDRED);
            buttons.put(R.id.bet_all, Action.ALL);
            buttons.put(R.id.rebet, Action.REBET);
            buttons.put(R.id.hintButton, Action.HINTCOIN);

			playerBet=(TextView) findViewById(R.id.playerBet);

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
            buttons.put(R.id.hintButton, Action.HINTCOIN);

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
        player.experience((int) reward);

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
                    player.setData("blackjacks");
				}else{
					payTransaction(betValue, 0f);
					hand.setStatus(Status.PUSH);
                    player.setData("pushs");
                }
			}
			else if ( dealerHand.didBust() || handValue > dealerValue ) {
				payTransaction(betValue, settings.winPay);
				hand.setStatus(Status.WON);
                player.setData("wins");
                if(hand.didDD()){
                    player.setData("doublewins");
                }
			}
			else if( dealerHand.hasBJ() || handValue < dealerValue) {
				hand.setStatus(Status.LOST);
			}
			else {
				payTransaction(betValue, 0f); /* if the hands tie in value, the player simply get his money back */
				hand.setStatus(Status.PUSH);
                player.setData("pushs");
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
            player.setData("insurance_wins");
            handler.postDelayed(new Runnable() {
                public void run() {
                    ((ImageView) findViewById(R.id.insuranceResult)).setImageResource(R.drawable.win);
                    findViewById(R.id.insuranceResult).setVisibility(ImageView.VISIBLE);
                    ScaleAnimation buttonanim =
                            new ScaleAnimation(
                                    0.0f, 1.0f, 0.0f, 1.0f,
                                    findViewById(R.id.insuranceResult).getWidth() / 2,
                                    findViewById(R.id.insuranceResult).getHeight() / 2);
                    buttonanim.setDuration(300);
                    findViewById(R.id.insuranceResult).startAnimation(buttonanim);

                    mSoundPool.play(mInsuranceWin, game.getSoundVol(), game.getSoundVol(), 0, 0, 1.0F);
                }
            }, waittime);
        }
        else
        {
            player.setData("insurance_loses");
            handler.postDelayed(new Runnable() {
                public void run() {
                    ((ImageView)findViewById(R.id.insuranceResult)).setImageResource(R.drawable.lose);
                    findViewById(R.id.insuranceResult).setVisibility(ImageView.VISIBLE);
                    mSoundPool.play(mInsuranceLose, game.getSoundVol(), game.getSoundVol(), 0, 0, 1.0F);
                }
            }, waittime);
        }
    }

	void newGame()
	{
        dealerHand.update();
		player.update();
		currentPlayerHand=player.getHand((byte) 0);
		betting=true;
		getNextAction=false;
        // update playerBet to 0
        updatePlayerBetlbl();
        checkExecInitUI(500);
	}

	void clearBet()
	{
		BJHand hand = player.getHand((byte) 0);
		hand.clearBet(); //remove the bet from the table
        updatePlayerBetlbl();
	}
	
	void collectBet(float stake)
	{
        float betValue=currentPlayerHand.getBet().getValue();
        if( stake > player.getBalance() - betValue ||
                stake + betValue > player.getMaxBet()
                ) {
            return;
		}

		player.getHand((byte) 0).incrementBet(stake);

        updatePlayerBetlbl();
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
            player.setData("surrenders");
            float betValue = hand.getBet().getValue();
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
            player.setData("doubles");

            //check 10/11
			byte handHardBJValue = hand.getHardBJValue(); 
			
			if (handHardBJValue == 10 || handHardBJValue==11)
				if (!settings.dd1011)
					return;
			
			hand.takeDD();
			
			float betValue = hand.getBet().getValue();
			
			hand.incrementBet(betValue);
			
			player.withdraw(betValue);

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
            player.setData("splits");
            //remove the second card from the first hand and make a new hand with it
			float betValue=hand.getBet().getValue();
			Card poppedCard = hand.popCard((byte) 1);
			((RelativeLayout)(currentPlayerHandView.getChildAt(0))).removeViewAt(1);
            playerSumViews.get(0).setVisibility(RelativeLayout.INVISIBLE);

			BJHand splitHand = new BJHand(hand.ownerName, poppedCard, betValue);
            player.addHand(splitHand);
            player.withdraw(betValue);
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
		}
	}

	void disableButtons()
	{	
		for (final Integer entry : buttons.keySet())
		{
            ((Button) findViewById(entry)).setVisibility(Button.INVISIBLE);
            ((Button) findViewById(entry)).setOnClickListener(null);
		}
        buttons = null;
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
	void setBet(LinearLayout playerBet, int betNum)
    {

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
            } else {
                handler.postDelayed(showActionButton, waittime);
            }
        }
	}

    boolean isInsurable()
    {
        if (settings.insurance && dealerHand.getCard((byte)0).getValue()==1 &&
                player.getBalance() >= (float)(0.5*currentPlayerHand.getBet().getValue())) {
            return true;
        }else{
            return false;
        }
    }

    void beginInsurance()
    {
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

    void insurance()
    {
        float betValue=currentPlayerHand.getBet().getValue();
        float insuranceBetValue = (float)(0.5*betValue); //insurance is a separate bet from the main bet
        player.takeInsurance(insuranceBetValue);
        player.withdraw(insuranceBetValue); //withraw the bet from player's wallet

        findViewById(R.id.insurance).setVisibility(RelativeLayout.VISIBLE);
        LinearLayout insuranceBet = (LinearLayout) findViewById(R.id.insuranceBet);
        setBet(insuranceBet, (int) insuranceBetValue);
        ((TextView) findViewById(R.id.insuranceBetNum)).setText(String.valueOf((int) insuranceBetValue));
        updatePlayerCashlbl();
        waittime += 300 + (insuranceBet.getChildCount() - 1) * 50;
    }

    void endInsurance()
    {
        insurancingFlg = false;
        checkPlayerHand(currentPlayerHand);
        if (currentPlayerHand.isPlaying()) {
            handler.postDelayed(showActionButton, waittime);
        }
        findViewById(R.id.insuranceAsk).setVisibility(TextView.INVISIBLE);
        findViewById(R.id.insuranceYes).setVisibility(Button.INVISIBLE);
        findViewById(R.id.insuranceNo).setVisibility(Button.INVISIBLE);
    }

	private final Runnable showActionButton = new Runnable()
    {
		@Override
		public void run() {
            findViewById(R.id.standButton).setVisibility(Button.VISIBLE);
            findViewById(R.id.hitButton).setVisibility(Button.VISIBLE);
            checksurrenderbutton();
            checkddbutton();
            checksplitbutton();
            findViewById(R.id.hintButton).setVisibility(Button.VISIBLE);
		}
	};


    void checksurrenderbutton()
    {
        if (player.howManyHands() == 1 &&
                currentPlayerHand.getCardCount() == 2 &&
                dealerHand.getCard((byte)0).getValue() != 1){
            findViewById(R.id.surrenderButton).setVisibility(Button.VISIBLE);
        }else{
            findViewById(R.id.surrenderButton).setVisibility(Button.INVISIBLE);
        }
    }

    void checkddbutton()
    {
        if (currentPlayerHand.getCardCount() == 2 &&
                player.getBalance() >= currentPlayerHand.getBet().getValue()){
            findViewById(R.id.ddButton).setVisibility(Button.VISIBLE);
        }else{
            findViewById(R.id.ddButton).setVisibility(Button.INVISIBLE);
        }
    }

    void checksplitbutton()
    {
        if(currentPlayerHand.splitable(settings.aceResplit) &&
                player.howManyHands() <= settings.splits &&
                player.getBalance() >= currentPlayerHand.getBet().getValue()){
            findViewById(R.id.splitButton).setVisibility(Button.VISIBLE);
        }else{
            findViewById(R.id.splitButton).setVisibility(Button.INVISIBLE);
        }
    }

    void dealCard(BJHand hand) {
        dealCard(hand, false, false);
    }
    void dealCard(BJHand hand, boolean playerFlg) {
        dealCard(hand, playerFlg, false);
    }
	void dealCard(BJHand hand, boolean dealerFlg, boolean faceDown)
	{
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
                mSoundPool.play(mCardfall, game.getSoundVol(), game.getSoundVol(), 0, 0, 1.0F);
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

	void updatePlayerSumlbl(int handIndex)
    {
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

    void updatePlayerCashlbl()
    { updatePlayerlbl(playerCash, (int)player.getBalance());}

    void updatePlayerCoinlbl()
    { updatePlayerlbl(playerCoin, (int) player.getCoinBalance());}

    int countUpNum;
    TextView updateView;
    void updatePlayerlbl(TextView view, int afterCash)
    {
        updateView = view;
        int beforeCash = Integer.parseInt(String.valueOf(updateView.getText()));

        int loopCnt;
        int countCash;
        int sign = 1;
        if(afterCash - beforeCash < 0){
            sign = -1;
        }

        if((afterCash - beforeCash) * sign < 30){
            countCash = sign;
            loopCnt = (afterCash - beforeCash) * sign;
        }else{
            countCash = (afterCash - beforeCash) / 30;
            loopCnt = 30;
        }
        for (int i = 1; i <= loopCnt; i++) {
            if (i == loopCnt) {
                countUpNum = afterCash;
            } else {
                countUpNum = beforeCash + (countCash * i);
            }
            handler.postDelayed(new Runnable() {
                int updateCash = countUpNum;
                TextView updateTextView = updateView;
                public void run() {
                    updateTextView.setText(String.valueOf(updateCash));
                    if(updateTextView.getId() == R.id.playerCash) {
                        setPlayerProgress(updateCash);
                    }
                }
            }, waittime + (i * 33));
        }
    }


    private void setPlayerProgress(int updateCash){
        if(updateCash > settings.necessaryPoint) {
            playerProgress.setText(String.valueOf(settings.necessaryPoint));
            bar.setProgress(settings.necessaryPoint);
        }else{
            playerProgress.setText(String.valueOf(updateCash));
            bar.setProgress(updateCash);
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

                            mSoundPool.play(mBlackjack, game.getSoundVol(), game.getSoundVol(), 0, 0, 1.0F);
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
                                mSoundPool.play(mWin, game.getSoundVol(), game.getSoundVol(), 0, 0, 1.0F);
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
                                mSoundPool.play(mPush, game.getSoundVol(), game.getSoundVol(), 0, 0, 1.0F);
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
                                mSoundPool.play(mLose, game.getSoundVol(), game.getSoundVol(), 0, 0, 1.0F);
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
					if (player.getInitBet()>=settings.tableMin && player.getInitBet()<=player.getMaxBet()) {
                        player.setData("plays");
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
                        findViewById(R.id.hintButton).setVisibility(RelativeLayout.INVISIBLE);
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
                    if(player.getBalance() > player.getMaxBet()){
                        maxbet = (int)(player.getMaxBet() - currentPlayerHand.getBet().getValue());
                    }else{
                        maxbet=(int)(player.getBalance() - currentPlayerHand.getBet().getValue());
                    }
                    maxbet = maxbet - (maxbet % (int)settings.tableMin);
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
                case HINTCOIN:
                {
                    if(checkCoinBonus()) {
                        String message = "Do you want to get a hint coin to see the video ad?";
                        DialogFragment ConfirmAdDialog = ConfirmDialogFragment.newInstance(message);
                        ConfirmAdDialog.show(getSupportFragmentManager(), "confirmAdDialog");
                    }else{
                        createAlertDialog("The only up to " + settings.coninBonusCount + " times can get in one day.");
                        showAlertDialog();
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
                    split(currentPlayerHand);
                    break;
    			}
                case SURRENDER: {
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
                case HINTCOIN:
                {
                    if(player.getCoinBalance() > 0) {
                        String message = "Do you use a hint coin?";
                        DialogFragment ConfirmDialog = ConfirmDialogFragment.newInstance(message);
                        ConfirmDialog.show(getSupportFragmentManager(), "confirmHintByCoinDialog");
                    }else{
                        createAlertDialog("You don't have a hint coin.");
                        showAlertDialog();
                    }
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

    public Long getTimeLefOfFreeChips(){
        Long freeChipsGetTime = preference.getLong("freeChipsGetTime", 0);
        Long needTime = settings.freeChipsSeconds * 1000 - (System.currentTimeMillis() - freeChipsGetTime);
        if(needTime < 0){
            return 0L;
        }else{
            return needTime;
        }
    }

    void showFreeChipsButton()
    {
        Button freeChips = (Button) findViewById(R.id.freeChips);
        LinearLayout freeChipsTimer = (LinearLayout) findViewById(R.id.freeChipsTimer);

        // 以前にタイマーを起動していればリセット
        if (freeChipsCountDownTimer != null){
            freeChipsCountDownTimer.cancel();
            freeChipsCountDownTimer = null;
        }
        Long timeLeft = getTimeLefOfFreeChips();
        freeChips.setVisibility(Button.VISIBLE);
        if(timeLeft > 0L){
            freeChips.setOnClickListener(null);
            freeChipsTimer.setVisibility(View.VISIBLE);
            // カウントダウンする
            freeChipsCountDownTimer = new FreeChipsCDTimer(timeLeft, 500);
            freeChipsCountDownTimer.start();
        } else {
            freeChips.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onClickFreeChips(v);
                }
            });
        }
    }

    void disableFreeChipsButton()
    {
        Button freeChips = (Button) findViewById(R.id.freeChips);
        LinearLayout freeChipsTimer = (LinearLayout) findViewById(R.id.freeChipsTimer);

        //bonus.setAnimation(null);
        freeChips.setOnClickListener(null);
        freeChips.setVisibility(Button.INVISIBLE);
        freeChipsTimer.setVisibility(View.INVISIBLE);
    }


    public class FreeChipsCDTimer extends CountDownTimer {

        TextView count_txt = (TextView) findViewById(R.id.freeChipsCountDown);

        public FreeChipsCDTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            count_txt.setText((millisUntilFinished / 1000 / 3600) + ":" +
                    String.format("%02d", (millisUntilFinished / 1000 / 60 % 60)) + ":" +
                    String.format("%02d", (millisUntilFinished / 1000 % 60)));
        }

        @Override
        public void onFinish() {
            findViewById(R.id.freeChipsTimer).setVisibility(View.INVISIBLE);
            findViewById(R.id.freeChips).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    onClickFreeChips(v);
                }
            });
        }
    }

    public boolean checkCoinBonus(){
        Integer gotCoinBonusCounts = preference.getInt("gotCoinBonusCounts", 0);
        Log.d("gotCoinBonusCounts", String.valueOf(gotCoinBonusCounts));
        if(gotCoinBonusCounts < settings.coninBonusCount){
            return true;
        }else {
            Long coinBonusGetTime = preference.getLong("coinBonusGetTime", 0);
            Long needTime = 86400 * 1000 - (System.currentTimeMillis() - coinBonusGetTime);
            if (needTime < 0L) {
                editor.putInt("gotCoinBonusCounts", 0);
                return true;
            } else {
                return false;
            }
        }
    }

    public Long getTimeLefOfLoginBonus(){
        Long loginBonusGetTime = preference.getLong("loginBonusGetTime", 0);
        Long needTime = settings.loginBonusSeconds * 1000 - (System.currentTimeMillis() - loginBonusGetTime);
        if(needTime < 0){
            return 0L;
        }else{
            return needTime;
        }
    }

    public void loginBonusCountDown(){

        // 以前にタイマーを起動していればリセット
        if (bonusCountDownTimer != null){
            bonusCountDownTimer.cancel();
            bonusCountDownTimer = null;
        }
        Long timeLeft = getTimeLefOfLoginBonus();
        if(timeLeft == 0L){
            ((TextView)findViewById(R.id.CountDown)).setText("GET BONUS!");
            Intent intent = new Intent(getApplicationContext(), Bonus.class);
            intent.putExtra("type", "login");
            startActivity(intent);
        } else {
            findViewById(R.id.loginBonus).setOnClickListener(null);
            // カウントダウンする
            bonusCountDownTimer = new BonusCDTimer(timeLeft, 500);
            bonusCountDownTimer.start();
        }
    }

    public class BonusCDTimer extends CountDownTimer {

        TextView count_txt = (TextView) findViewById(R.id.CountDown);

        public BonusCDTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            count_txt.setText((millisUntilFinished / 1000 / 3600) + ":" +
                    String.format("%02d", (millisUntilFinished / 1000 / 60 % 60)) + ":" +
                    String.format("%02d", (millisUntilFinished / 1000 % 60)));
        }

        @Override
        public void onFinish() {
            count_txt.setText("GET BONUS!");
            findViewById(R.id.loginBonus).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), Bonus.class);
                    intent.putExtra("type", "login");
                    startActivity(intent);
                }
            });
        }
    }

    public void showMovieAd(){
        if (game.mMovieAd.isLoaded()) {
            game.mMovieAd.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int errorCode) {
                    game.requestNewMovie();
                }

                @Override
                public void onAdClosed() {
                    game.requestNewMovie();
                    Intent intent = new Intent(getApplicationContext(), Bonus.class);
                    intent.putExtra("type", "coin");
                    startActivity(intent);
                }
            });
            game.mMovieAd.show();
        } else {
            createAlertDialog("Now loading ad.\nPlease Wait a moment.");
            showAlertDialog();
        }
    }

    public void getHint(){
        Card card = shoe.checkNextCard();
        if(card.getValue() > 7) {
            createAlertDialog("The next card is greater than 7.");
        }else if(card.getValue() == 7){
            createAlertDialog("The next card is 7.");
        }else{
            createAlertDialog("The next card is less than 7.");
        }
        showAlertDialog();
        player.withdrawCoin(1);
        updatePlayerCoinlbl();
    }

    // アラートダイアログ作成
    private void createAlertDialog(String message){
        alertDialog = AlertDialogFragment.newInstance(message);
    }

    // アラートダイアログ表示
    private void showAlertDialog() {
        if (alertDialog != null) {
            alertDialog.show(getSupportFragmentManager(), "alertDialog");
        }
    }

    public void onClickFreeChipsByCoin(final View view){
        String message = "Are you sure you want to consume a coin?";
        DialogFragment ConfirmAdDialog = ConfirmDialogFragment.newInstance(message);
        ConfirmAdDialog.show(getSupportFragmentManager(), "confirmFreeChipsByCoinDialog");
    }

    public void onClickFreeChips(final View view){
        if (game.mInterstitialAd.isLoaded()) {
            game.mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int errorCode) {
                    game.requestNewInterstitial();
                }
                @Override
                public void onAdClosed() {
                    game.requestNewInterstitial();
                    biginFreeChips();
                }
            });
            game.mInterstitialAd.show();
        } else {
            biginFreeChips();
        }
    }

    public void biginFreeChipsByCoin(){
        player.withdrawCoin(1);
        updatePlayerCoinlbl();
        biginFreeChips();
    }

    public void biginFreeChips(){
        Intent intent = new Intent(this, FreeChips.class);
        startActivity(intent);
        overridePendingTransition(R.anim.in_up, 0);
    }

    public void onClickHeader(final View view) {
        startActivity(game.getNewIntent(view));
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