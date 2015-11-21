package com.ivoid.bj;

import android.content.Context;
import android.content.SharedPreferences;

public class CoinWallet
{
	private SharedPreferences preference;
	private SharedPreferences.Editor editor;

	public CoinWallet(Context context)
	{
		preference = context.getSharedPreferences("user_data", context.MODE_PRIVATE);
		editor = preference.edit();
	}
	
	public void deposit(float amount) 
	{
		editor.putFloat("CoinBalance", preference.getFloat("CoinBalance", 0f) + amount);
		editor.commit();

	}
	
	public void withdraw(float amount)
	{
		editor.putFloat("CoinBalance", preference.getFloat("CoinBalance", 0f) - amount);
		editor.commit();
	}
	
	//Accessors
	public float getBalance()
	{ return preference.getFloat("CoinBalance", 0f); }
}