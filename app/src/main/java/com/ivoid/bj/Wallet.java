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
	
	public Wallet(Context context, float initCurrency)
	{
		preference = context.getSharedPreferences("user_data", context.MODE_PRIVATE);
		editor = preference.edit();
		editor.putFloat("Balance", initCurrency);
		editor.commit();
	}
	
	public void deposit(float amount) 
	{
		editor.putFloat("Balance", preference.getFloat("Balance", 0f) + amount);
		editor.commit();

	}
	
	public void withdraw(float amount)
	{
		editor.putFloat("Balance", preference.getFloat("Balance", 0f) - amount);
		editor.commit();
	}
	
	//Accessors
	public float getBalance()
	{ return preference.getFloat("Balance", 0f); }
}