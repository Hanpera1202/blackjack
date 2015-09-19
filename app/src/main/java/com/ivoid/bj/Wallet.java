package com.ivoid.bj;

import android.content.Context;
import android.content.SharedPreferences;

public class Wallet
{
	private float balance;
	private Context mContext;
	private SharedPreferences preference;
	private SharedPreferences.Editor editor;

	public Wallet(Context context)
	{
		//balance=0f;
		mContext = context;
		preference = context.getSharedPreferences("user_data", context.MODE_PRIVATE);
		editor = preference.edit();
		balance = preference.getFloat("Balance", 0f);
	}
	
	public Wallet(Context context, float initCurrency)
	{
		balance=initCurrency;
		mContext = context;
		preference = context.getSharedPreferences("user_data", context.MODE_PRIVATE);
		editor = preference.edit();
		editor.putFloat("Balance", balance);
		editor.commit();
	}
	
	public void deposit(float amount) 
	{
		balance+=amount;
		editor.putFloat("Balance", balance);
		editor.commit();

	}
	
	public void withdraw(float amount)
	{
		balance-=amount;
		editor.putFloat("Balance", balance);
		editor.commit();
	}
	
	//Accessors
	public float getBalance()
	{ return balance; }
}