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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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
    private int mCash;
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
    private TextView playerCoinNum;
    private TextView playerMaxBet;

    private RelativeLayout playerCoin;

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
    private boolean showBonusDialogFlag = false;

    FreeChipsCDTimer freeChipsCountDownTimer = null;

    private DialogFragment alertDialog;
    private DialogFragment LevelUpDialog;
    private DialogFragment BonusDialog;
    private ConfirmDialogFragment ConfirmDialog;
    private ConfirmAdDialogFragment ConfirmAdDialog;
    private HintDialogFragment hintDialog;
    private ProgressBar bar;

    private boolean backendFlag = false;

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
                setText(settings.necessaryPoint + "pt");

    }

    @Override
    public void onResume()
    {
        super.onResume();

        // reset backendFlag
        backendFlag = false;
        // reset animation start time
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
        mCash = mSoundPool.load(getApplicationContext(), R.raw.cash, 1);
        mPush = mSoundPool.load(getApplicationContext(), R.raw.push, 1);
        mLose = mSoundPool.load(getApplicationContext(), R.raw.lose, 1);
        mWin = mSoundPool.load(getApplicationContext(), R.raw.win, 1);
        mBlackjack = mSoundPool.load(getApplicationContext(), R.raw.blackjack, 1);
        mInsuranceWin = mSoundPool.load(getApplicationContext(), R.raw.insurance_win, 1);
        mInsuranceLose = mSoundPool.load(getApplicationContext(), R.raw.insurance_lose, 1);

        setPlayerData();
        if (!betting){
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

    public void setPlayerData(){
        game.setHeaderData(player, (RelativeLayout) findViewById(R.id.header));
        playerMaxBet.setText("MAX " + player.getMaxBet() + "pt");
        setPlayerCoin(RelativeLayout.VISIBLE);
        setPlayerProgress(player.getBalance());
        if (betting){
            if (preference.getInt("gotBonusPoint", 0) > 0) {
                player.deposit(preference.getInt("gotBonusPoint", 0));
                editor.putInt("gotBonusPoint", 0);
                editor.commit();
                updatePlayerCashlbl();
                mSoundPool.play(mCash, game.getSoundVol(), game.getSoundVol(), 0, 0, 1.0F);
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
            execInitUIWhenBetting(0);
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
        playerCoinNum=(TextView)findViewById(R.id.coinNum);
        playerCoin=(RelativeLayout)findViewById(R.id.playerCoin);
        playerMaxBet=(TextView)findViewById(R.id.playerMaxBet);
    }

    void execInitUIWhenBetting(int waittime){
        if(backendFlag || !betting){
            return;
        }

        if(player.getBalance() >= 10.0f) {
            disableFreeChipsButton();
        }

        if (showBonusDialogFlag) {
            createBonusDialog("coin");
            handler.postDelayed(new Runnable() {
                public void run() {
                    showBonusDialog();
                    showBonusDialogFlag = false;
                }
            }, waittime);
        }else if (player.isLevelUp()) {
            createLevelUpDialog();
            handler.postDelayed(new Runnable() {
                public void run() {
                    showLevelUpDialog();
                }
            }, waittime);
        }else if(getTimeLeftOfLoginBonus() == 0L){
            createBonusDialog("login");
            handler.postDelayed(new Runnable() {
                public void run() {
                    showBonusDialog();
                }
            }, waittime);
        }else{
            if(player.getBalance() < 10) {
                handler.postDelayed(new Runnable() {
                    public void run() {
                        showFreeChipsButton();
                    }
                }, waittime);
            }
            if (buttons == null) {
                findViewById(R.id.betting).setVisibility(LinearLayout.VISIBLE);
                setPlayerCoin(RelativeLayout.VISIBLE);
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

	void payTransaction ( int betValue, float ratio )
	{
		player.deposit(betValue);
		
		//calculate reward based on hand's performance in this bj round (indicated by ratio)
		int reward = (int)(betValue * ratio);
		
		player.deposit(reward);
    }

    void payout ()
	{
		byte dealerValue=dealerHand.getBJValue(); // the dealer's hand's value
		
		for ( BJHand hand: player.getHands() )
		{	
			if ( !hand.toBePayed() )	continue; //if the hand still needs to be payed

			byte handValue = hand.getBJValue(); //the hand's hard value
			int betValue = hand.getBet().getValue(); //the hand's bet

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

        int insuranceBetValue = player.getInsuranceBetValue();

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
            waittime += 300;
        }
        else
        {
            player.setData("insurance_loses");
            handler.postDelayed(new Runnable() {
                public void run() {
                    ((ImageView) findViewById(R.id.insuranceResult)).setImageResource(R.drawable.lose);
                    findViewById(R.id.insuranceResult).setVisibility(ImageView.VISIBLE);
                    mSoundPool.play(mInsuranceLose, game.getSoundVol(), game.getSoundVol(), 0, 0, 1.0F);
                }
            }, waittime);
            waittime += 150;
        }
    }

	void newGame()
	{
        dealerHand.update();
		player.update();
		currentPlayerHand=player.getHand((byte) 0);
		betting=true;
		getNextAction=false;
        waittime = 0;
        // update playerBet to 0
        updatePlayerBetlbl();
        execInitUIWhenBetting(500);
	}

	void clearBet()
	{
		BJHand hand = player.getHand((byte) 0);
		hand.clearBet(); //remove the bet from the table
        updatePlayerBetlbl();
	}
	
	void collectBet(int stake)
	{
        int betValue=currentPlayerHand.getBet().getValue();
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
            int betValue = hand.getBet().getValue();
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
			
			int betValue = hand.getBet().getValue();
			
			hand.incrementBet(betValue);
			
			player.withdraw(betValue);

			updatePlayerCashlbl();

            dealCard(hand);

            setBet((LinearLayout) currentPlayerBetView.getChildAt(1), betValue);
			updatePlayerBetlblForPlaying();

			hand.setPlaying(false);

            handler.postDelayed(new Runnable() {
                int NowIndex = currentPlayerIndex;

                public void run() {
                    updatePlayerSumlbl(NowIndex);
                }
            }, waittime);

            checkPlayerHand(hand);
		}
	}
	
	void split(BJHand hand)
	{
		if (hand.splitable(settings.aceResplit) && player.howManyHands()<(settings.splits+1))
		{
            player.setData("splits");
            //remove the second card from the first hand and make a new hand with it
			int betValue=hand.getBet().getValue();
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

            // dp × density + 0.5f（四捨五入)
            int w_px = (int) (getResources().getInteger(R.integer.dp320_80) * getResources().getDisplayMetrics().density + 0.5f);
            int h_px = (int) (getResources().getInteger(R.integer.dp320_120) * getResources().getDisplayMetrics().density + 0.5f);

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
        setPlayerCoin(RelativeLayout.INVISIBLE);
	}

	void act()
	{
        faceUpCard(dealerHand, faceDownCard);
        updateDealerSumlbl(dealerHand.getSoftBJValue(), dealerHand.getHardBJValue());
        waittime += 150;
        if (!(player.howManyHands() == 1 && currentPlayerHand.hasBJ())) {
            while (dealerHand.getHardBJValue() < 17) {
                // stand on soft 17
                if (settings.stand17soft && dealerHand.getSoftBJValue() >= 17) break;
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
            if (betNum >= 1000) {
                betImage.setImageResource(R.drawable.m1000);
                betNum -= 1000;
            } else if (betNum >= 500) {
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
                betImage.setImageResource(R.drawable.m5);
                betNum -= 5;
            }
            betImage.setScaleType(ImageView.ScaleType.FIT_XY);
            betImages.add(betImage);
        }


        int anim_start_y_px = playerBet.getHeight();
        int bet_px = (int) (getResources().getInteger(R.integer.dp320_25) * getResources().getDisplayMetrics().density + 0.5f);
        int bet_area_px = (int) (getResources().getInteger(R.integer.dp320_60) * getResources().getDisplayMetrics().density + 0.5f);

        int margin_w_px = 0;
        if(bet_px * betImages.size() > bet_area_px){
            margin_w_px = (int)Math.ceil((double)(bet_px * betImages.size() - bet_area_px)/(betImages.size() - 1));
        }

        for (int i = 0; i < betImages.size(); i++) {
            TranslateAnimation translate = new TranslateAnimation(0, 0, anim_start_y_px, 0);
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
                handler.postDelayed(new Runnable() {
                    public void run() {
                        checkActionButton();
                    }
                }, waittime);
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
        int betValue=currentPlayerHand.getBet().getValue();
        int insuranceBetValue = (int)(0.5*betValue); //insurance is a separate bet from the main bet
        player.takeInsurance(insuranceBetValue);
        player.withdraw(insuranceBetValue); //withraw the bet from player's wallet

        findViewById(R.id.insurance).setVisibility(RelativeLayout.VISIBLE);
        LinearLayout insuranceBet = (LinearLayout) findViewById(R.id.insuranceBet);
        setBet(insuranceBet, insuranceBetValue);
        ((TextView) findViewById(R.id.insuranceBetNum)).setText(String.valueOf(insuranceBetValue));
        updatePlayerCashlbl();
        waittime += 300 + (insuranceBet.getChildCount() - 1) * 50;
    }

    void endInsurance()
    {
        insurancingFlg = false;
        checkPlayerHand(currentPlayerHand);
        if (currentPlayerHand.isPlaying()) {
            handler.postDelayed(new Runnable() {
                public void run() {
                    checkActionButton();
                }
            }, waittime);
        }
        findViewById(R.id.insuranceAsk).setVisibility(TextView.INVISIBLE);
        findViewById(R.id.insuranceYes).setVisibility(Button.INVISIBLE);
        findViewById(R.id.insuranceNo).setVisibility(Button.INVISIBLE);
    }

    void checkActionButton()
    {
        checkBasicActionButton();
        checkSpecialActionButton();
    }

    void checkBasicActionButton()
    {
        if (currentPlayerHand.isPlaying()){
            findViewById(R.id.standButton).setVisibility(Button.VISIBLE);
            findViewById(R.id.hitButton).setVisibility(Button.VISIBLE);
            setPlayerCoin(RelativeLayout.VISIBLE);
        }else{
            findViewById(R.id.standButton).setVisibility(Button.INVISIBLE);
            findViewById(R.id.hitButton).setVisibility(Button.INVISIBLE);
            setPlayerCoin(RelativeLayout.INVISIBLE);
        }
    }

    void checkSpecialActionButton()
    {
        checksurrenderbutton();
        checkddbutton();
        checksplitbutton();
    };

    void checksurrenderbutton()
    {
        if (currentPlayerHand.isPlaying() &&
                player.howManyHands() == 1 &&
                currentPlayerHand.getCardCount() == 2 &&
                dealerHand.getCard((byte)0).getValue() != 1){
            findViewById(R.id.surrenderButton).setVisibility(Button.VISIBLE);
        }else{
            findViewById(R.id.surrenderButton).setVisibility(Button.INVISIBLE);
        }
    }

    void checkddbutton()
    {
        if (currentPlayerHand.isPlaying() &&
                currentPlayerHand.getCardCount() == 2 &&
                !currentPlayerHand.hasBJ() &&
                player.getBalance() >= currentPlayerHand.getBet().getValue()){
            findViewById(R.id.ddButton).setVisibility(Button.VISIBLE);
        }else{
            findViewById(R.id.ddButton).setVisibility(Button.INVISIBLE);
        }
    }

    void checksplitbutton()
    {
        if(currentPlayerHand.isPlaying() &&
                currentPlayerHand.splitable(settings.aceResplit) &&
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

        int w_px = (int) (getResources().getInteger(R.integer.dp320_80) * getResources().getDisplayMetrics().density + 0.5f);
        int h_px = (int) (getResources().getInteger(R.integer.dp320_120) * getResources().getDisplayMetrics().density + 0.5f);
        int margin_w_px = (int) (getResources().getInteger(R.integer.dp320_12) * getResources().getDisplayMetrics().density + 0.5f) ;

		if(hand.getCardCount() > 1) {
			//マージンを設定
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(w_px, h_px);
			lp.setMargins(margin_w_px * (hand.getCardCount() - 1), 0, 0, 0);
			cardImage.setLayoutParams(lp);

		}else{
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w_px, h_px);
			cardImage.setLayoutParams(lp);
		}

        int w_anim_start_px = (int) (getResources().getInteger(R.integer.dp320_350) * getResources().getDisplayMetrics().density + 0.5f);
        int h_anim_start_px = (int) (getResources().getInteger(R.integer.dp320_200) * getResources().getDisplayMetrics().density + 0.5f);

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

        int w_px = (int) (getResources().getInteger(R.integer.dp320_80) * getResources().getDisplayMetrics().density + 0.5f);
        int h_px = (int) (getResources().getInteger(R.integer.dp320_120) * getResources().getDisplayMetrics().density + 0.5f);
        int margin_w_px = (int) (getResources().getInteger(R.integer.dp320_12) * getResources().getDisplayMetrics().density + 0.5f) ;
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
    { currentPlayerBetNumView.setText(String.valueOf(player.getInitBet()));}

    void updatePlayerCashlbl()
    { updatePlayerlbl(playerCash, player.getBalance());}

    void updatePlayerCoinlbl()
    { updatePlayerlbl(playerCoinNum, player.getCoinBalance());}

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
            }, waittime + (i * 15));
        }
    }


    private void setPlayerProgress(int updateCash){
        ((TextView)findViewById(R.id.applyPossibleNum)).setText(String.valueOf(updateCash / settings.necessaryPoint));
        bar.setProgress(updateCash % settings.necessaryPoint);
    }

    private void setPlayerCoin(int visible) {
        playerCoin.setVisibility(visible);
        if(visible == RelativeLayout.VISIBLE){
            playerCoinNum.setText(String.valueOf(player.getCoinBalance()));
            if (betting) {
                ((TextView) findViewById(R.id.hintButton)).setText("+");
            }else{
                ((TextView) findViewById(R.id.hintButton)).setText("?");
            }
        }
    }

    void updatePlayerResultlbl() {
        int animWaittime = waittime + 300;
        int soundWaittime = waittime + 300;
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
                    }, waittime);
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
            waittime += 300;
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
                        player.experience(1);
                        game.setHeaderNextLevel(player,(RelativeLayout) findViewById(R.id.header));
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
						findViewById(R.id.betting).setVisibility(LinearLayout.INVISIBLE);
                        setPlayerCoin(RelativeLayout.INVISIBLE);
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
                        maxbet = player.getMaxBet() - currentPlayerHand.getBet().getValue();
                    }else{
                        maxbet= player.getBalance() - currentPlayerHand.getBet().getValue();
                    }
                    maxbet = maxbet - (maxbet % (int)settings.tableMin);
                    collectBet(maxbet);
					break;
				}
				case REBET:
        		{
        			collectBet(currentPlayerHand.getBet().getValue());
        			break;
        		}
				case TEN:
        		{
	        		collectBet(10);
        			break;
        		}
        		case FIFTY:
	        	{
        			collectBet(50);
        			break;
        		}
	        	case ONEHUNDRED:
        		{
        			collectBet(100);
        			break;
	        	}
        		case FIVEHUNDRED:
        		{
        			collectBet(500);
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
                    checkSpecialActionButton();
                    break;
                }
                case STAND: {
                    currentPlayerHand.setPlaying(false);
                    break;
                }
                case DOUBLEDOWN: {
                    dd(currentPlayerHand);
                    checkActionButton();
					break;
                }
                case SPLIT: {
                    split(currentPlayerHand);
                    checkActionButton();
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
        	}
        	
        	vibrator.vibrate(40);

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

            if (currentPlayerIndex != nextHandtoPlay || action == action.SPLIT){
                handler.postDelayed(new Runnable() {
                    public void run() {
                        checkActionButton();
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
            freeChips.setAnimation(null);
            freeChips.setOnClickListener(null);
            freeChipsTimer.setVisibility(View.VISIBLE);
            // カウントダウンする
            freeChipsCountDownTimer = new FreeChipsCDTimer(timeLeft, 500);
            freeChipsCountDownTimer.start();
        } else {
            enableFreeChipsButton();
        }
    }

    void enableFreeChipsButton()
    {
        Button freeChips = (Button) findViewById(R.id.freeChips);
        LinearLayout freeChipsTimer = (LinearLayout) findViewById(R.id.freeChipsTimer);
        AlphaAnimation buttonanim = new AlphaAnimation(1, 0.0f);
        buttonanim.setDuration(800);
        buttonanim.setRepeatCount(Animation.INFINITE);
        buttonanim.setRepeatMode(Animation.REVERSE);
        freeChips.startAnimation(buttonanim);
        freeChips.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onClickFreeChips(v);
            }
        });
        freeChipsTimer.setVisibility(View.INVISIBLE);
    }

    void disableFreeChipsButton()
    {
        Button freeChips = (Button) findViewById(R.id.freeChips);
        LinearLayout freeChipsTimer = (LinearLayout) findViewById(R.id.freeChipsTimer);

        // 以前にタイマーを起動していればリセット
        if (freeChipsCountDownTimer != null){
            freeChipsCountDownTimer.cancel();
            freeChipsCountDownTimer = null;
        }

        freeChips.setAnimation(null);
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
            enableFreeChipsButton();
        }
    }

    public Long getTimeLeftOfLoginBonus(){
        Long loginBonusGetTime = preference.getLong("loginBonusGetTime", 0);
        Long needTime = settings.loginBonusSeconds * 1000 - (System.currentTimeMillis() - loginBonusGetTime);
        if(needTime < 0){
            return 0L;
        }else{
            return needTime;
        }
    }

    public void onClickCoin(final View view){
        waittime = 0;
        if(betting){
            createConfirmAdDialog();
            showConfirmAdDialog();
        }else{
            if(player.getCoinBalance() > 0) {
                String message = "Do you use a Hint Coin?";
                createConfirmDialog("confirmHintByCoinDialog", message);
                showConfirmDialog();
            }else{
                createAlertDialog("default", "You don't have a Hint Coin.");
                showAlertDialog();
            }
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
                    showBonusDialogFlag = true;
                }
            });
            game.mMovieAd.show();
        } else {
            createAlertDialog("nowLoadingAdDialog", "Now loading ad.\nPlease Wait a moment.");
            showAlertDialog();
        }
    }

    public void getHint(){
        Card card = shoe.checkNextCard();
        createHintDialog(card.getImage());
        showHintDialog();
        player.withdrawCoin(1);
        updatePlayerCoinlbl();
    }

    public void onClickFreeChipsByCoin(final View view){
        if(player.getCoinBalance() > 0) {
            String message = "Are you sure you want to consume a Hint Coin?";
            createConfirmDialog("confirmFreeChipsByCoinDialog", message);
            showConfirmDialog();
        }else{
            createAlertDialog("noHintCoinDialog", "You don't have a Hint Coin.");
            showAlertDialog();
        }
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

    public void beginFreeChipsByCoin(){
        player.withdrawCoin(1);
        updatePlayerCoinlbl();
        editor.putLong("freeChipsGetTime", 0);
        editor.commit();
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


    // アラートダイアログ作成
    private void createAlertDialog(String dialogType, String message){
        alertDialog = AlertDialogFragment.newInstance(dialogType, message);
    }

    // show alert dialog
    private void showAlertDialog() {
        if (alertDialog != null &&
                getSupportFragmentManager().findFragmentByTag("alertDialog") == null) {
            alertDialog.show(getSupportFragmentManager(), "alertDialog");
        }
    }

    // create level up dialog
    private void createLevelUpDialog(){
        LevelUpDialog = LevelUpDialogFragment.newInstance();
    }

    // show alert dialog
    private void showLevelUpDialog() {
        if (LevelUpDialog != null &&
                getSupportFragmentManager().findFragmentByTag("levelUpDialog") == null) {
            LevelUpDialog.show(getSupportFragmentManager(), "levelUpDialog");
        }
    }

    // hide alert level up dialog
    private void dismissLevelUpDialog() {
        if (LevelUpDialog != null) {
            Fragment prev = getSupportFragmentManager().findFragmentByTag("levelUpDialog");
            if (prev != null) {
                DialogFragment df = (DialogFragment) prev;
                df.dismiss();
            }
        }
        LevelUpDialog = null;
    }

    // create bonus dialog
    private void createBonusDialog(String type){
        BonusDialog = BonusDialogFragment.newInstance(type);
    }

    // show alert dialog
    private void showBonusDialog() {
        if (BonusDialog != null &&
                getSupportFragmentManager().findFragmentByTag("bonusDialog") == null) {
            BonusDialog.show(getSupportFragmentManager(), "bonusDialog");
        }
    }

    // hide alert bonus dialog
    private void dismissBonusDialog() {
        if (BonusDialog !=  null) {
            Fragment prev = getSupportFragmentManager().findFragmentByTag("bonusDialog");
            if (prev != null) {
                DialogFragment df = (DialogFragment) prev;
                df.dismiss();
            }
        }
        BonusDialog = null;
    }

    // create confirm dialog
    private void createConfirmDialog(String dialogType, String message){
        ConfirmDialog = ConfirmDialogFragment.newInstance(dialogType, message);
    }

    // show confirm dialog
    private void showConfirmDialog() {
        if (ConfirmDialog != null &&
                getSupportFragmentManager().findFragmentByTag("confirmDialog") == null) {
            ConfirmDialog.show(getSupportFragmentManager(), "confirmDialog");
        }
    }

    // create confirm ad dialog
    private void createConfirmAdDialog(){
        ConfirmAdDialog = ConfirmAdDialogFragment.newInstance();
    }

    // show confirm ad dialog
    private void showConfirmAdDialog() {
        if (ConfirmAdDialog != null &&
                getSupportFragmentManager().findFragmentByTag("confirmAdDialog") == null) {
            ConfirmAdDialog.show(getSupportFragmentManager(), "confirmAdDialog");
        }
    }

    // create hint dialog
    private void createHintDialog(Integer cardId){
        hintDialog = HintDialogFragment.newInstance(cardId);
    }

    // show hint dialog
    private void showHintDialog() {
        if (hintDialog != null &&
                getSupportFragmentManager().findFragmentByTag("hintDialog") == null) {
            hintDialog.show(getSupportFragmentManager(), "hintDialog");
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            backendFlag = true;
            moveTaskToBack(true);
            return true;
        }
        return false;
    }

    @Override
    public void onUserLeaveHint() {
        dismissLevelUpDialog();
        dismissBonusDialog();
        backendFlag = true;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}