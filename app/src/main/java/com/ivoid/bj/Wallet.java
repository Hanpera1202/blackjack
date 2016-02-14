package com.ivoid.bj;

import android.content.Context;
import android.content.SharedPreferences;

public class Wallet
{
	private SharedPreferences preference;
	private SharedPreferences.Editor editor;

	public Wallet(Context context)
	{
		preference = context.getSharedPreferences("user_data", context.MODE_PRIVATE);
		editor = preference.edit();
	}
	
	public void deposit(int amount)
	{
		editor.putInt("Balance", preference.getInt("Balance", 0) + amount);
		editor.commit();

	}
	
	public void withdraw(int amount)
	{
		editor.putInt("Balance", preference.getInt("Balance", 0) - amount);
		editor.commit();
	}
	
	//Accessors
	public int getBalance()
	{ return preference.getInt("Balance", 0); }
}