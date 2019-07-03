package com.ivoid.bj;

import android.content.Context;
import android.content.SharedPreferences;

public class Wallet
{
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;
    private String type;

    public Wallet(Context context, String walletType)
    {
        preference = context.getSharedPreferences("user_data", context.MODE_PRIVATE);
        editor = preference.edit();
        type = walletType;
    }
    
    public void deposit(int amount)
    {
        editor.putInt(type, preference.getInt(type, 0) + amount);
        editor.commit();

    }
    
    public void withdraw(int amount)
    {
        editor.putInt(type, preference.getInt(type, 0) - amount);
        editor.commit();
    }
    
    //Accessors
    public int getBalance()
    { return preference.getInt(type, 0); }
}