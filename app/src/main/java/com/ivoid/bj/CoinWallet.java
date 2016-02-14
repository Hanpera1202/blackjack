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
	
	public void deposit(int amount)
	{
		editor.putInt("CoinBalance", preference.getInt("CoinBalance", 0) + amount);
		editor.commit();

	}
	
	public void withdraw(int amount)
	{
		editor.putInt("CoinBalance", preference.getInt("CoinBalance", 0) - amount);
		editor.commit();
	}
	
	//Accessors
	public int getBalance()
	{ return preference.getInt("CoinBalance", 0); }
}