package com.ivoid.bj;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Button;

import java.util.ArrayList;

class Player
{
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;

    private GameSettings settings;

    private final String name;
	private CoinWallet coinWallet;
    private Wallet wallet;

	private ArrayList<BJHand> hands=new ArrayList<BJHand>();

    private boolean insurance = false; //did the player make an insurance bet?
	private float insuranceBetValue = 0f; //did the player make an insurance bet?
	private boolean playing; // all players are either playing or not (if not, they are removed from the Game list of players)
	private float rebet=0f; // Saved last bet

	Player(Context context, String name)
	{
        preference = context.getSharedPreferences("user_data", context.MODE_PRIVATE);
        editor = preference.edit();

        settings = new GameSettings();

		this.name=name;
		wallet=new Wallet(context);
        coinWallet=new CoinWallet(context);
		playing = true;
		addHand(new BJHand(name));
	}

	void setPlaying(boolean b)
	{ playing = b; }

	void update()
	{
		insurance = false;
        insuranceBetValue = 0f;
        BJHand firstHand = hands.get(0);
        rebet = firstHand.getBet().getValue();
		 
        if (firstHand.didDD()) rebet/=2f;
		 
        clearHands();
        addHand(new BJHand(name));
	}
	 
    void clearHands()
    { hands.clear(); }

    void experience(int exp)
    {
        editor.putInt("experience", preference.getInt("experience", 0) + exp);
        editor.commit();
    }

    void deposit(float amount)
    { wallet.deposit(amount); }
	
    void withdraw(float amount)
    { wallet.withdraw(amount); }

    void depositCoin(float amount)
    { coinWallet.deposit(amount); }

    void withdrawCoin(float amount)
    { coinWallet.withdraw(amount); }

    void addHand( BJHand hand )
    { hands.add(hand); }

    void takeInsurance(float amount)
    {
        insuranceBetValue = amount;
        insurance=true;
    }
	
    //Accessors
    String getName()
    { return name; }
	
    float getBalance()
    { return wallet.getBalance(); }

    float getCoinBalance()
    { return coinWallet.getBalance(); }

    float getInsuranceBetValue()
    { return insuranceBetValue; }

    boolean tookInsurance()
    { return insurance; }

    float getRebet()
    { return rebet; }
	 
    float getInitBet()
    { return hands.get(0).getBet().getValue(); }
	
    boolean getPlaying()
    { return playing; }
	
    byte howManyHands()
    { return (byte) hands.size(); }
	
    ArrayList<BJHand> getHands()
    { return hands; }
	
    BJHand getHand(byte b)
    { return hands.get(b); }

    void setData(String preferenceName)
    {
        editor.putInt(preferenceName, preference.getInt(preferenceName, 0) + 1);
        editor.commit();
    }

    int getMaxBet()
    { return settings.levels.get(getLevel()).maxBet; }

    int getLevel()
    { return preference.getInt("level", 1); }

    boolean isLevelUp(){
        int totalExp;
        if(getLevel() > settings.levels.size()){
            int maxTotalExp = settings.levels.get(settings.levels.size()).totalExp;
            int maxNextExp = settings.levels.get(settings.levels.size()).nextExp;
            int overCnt = getLevel() - settings.levels.size();
            totalExp = maxTotalExp + overCnt * maxNextExp;
        }else{
            totalExp = settings.levels.get(getLevel()).totalExp;
        }
        int experience = preference.getInt("experience", 0);
        if(experience >= totalExp){
            return true;
        }
        return false;
    }

    int getNowExp(){
        int experience = preference.getInt("experience", 0);
        int totalExp;
        int nextExp;
        if(getLevel() > settings.levels.size()){
            int maxTotalExp = settings.levels.get(settings.levels.size()).totalExp;
            int maxNextExp = settings.levels.get(settings.levels.size()).nextExp;
            int overCnt = getLevel() - settings.levels.size();
            totalExp = maxTotalExp + overCnt * maxNextExp;
            nextExp = maxNextExp;
        }else{
            totalExp = settings.levels.get(getLevel()).totalExp;
            nextExp = settings.levels.get(getLevel()).nextExp;
        }
        return experience - (totalExp - nextExp);
    }

    int getNextExp(){
        if(getLevel() > settings.levels.size()){
            return settings.levels.get(settings.levels.size()).nextExp;
        }else{
            return settings.levels.get(getLevel()).nextExp;
        }
    }

    void levelUp(){
        editor.putInt("level", getLevel() + 1);
        editor.commit();
    }

}