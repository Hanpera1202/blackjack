package com.ivoid.bj;

import android.content.Context;
import android.content.SharedPreferences;

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
	private int insuranceBetValue = 0; //did the player make an insurance bet?
	private boolean playing; // all players are either playing or not (if not, they are removed from the Game list of players)
	private int rebet=0; // Saved last bet

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
        insuranceBetValue = 0;
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

    void deposit(int amount)
    { wallet.deposit(amount); }
	
    void withdraw(int amount)
    { wallet.withdraw(amount); }

    void depositCoin(int amount)
    { coinWallet.deposit(amount); }

    void withdrawCoin(int amount)
    { coinWallet.withdraw(amount); }

    void addHand( BJHand hand )
    { hands.add(hand); }

    void takeInsurance(int amount)
    {
        insuranceBetValue = amount;
        insurance=true;
    }
	
    //Accessors
    String getName()
    { return name; }
	
    int getBalance()
    { return wallet.getBalance(); }

    int getCoinBalance()
    { return coinWallet.getBalance(); }

    int getInsuranceBetValue()
    { return insuranceBetValue; }

    boolean tookInsurance()
    { return insurance; }

    int getRebet()
    { return rebet; }
	 
    int getInitBet()
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

    int getMaxBet() {
        if (getLevel() > settings.levels.size()) {
            return settings.levels.get(settings.levels.size()).maxBet;
        }else{
            return settings.levels.get(getLevel()).maxBet;
        }
    }

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