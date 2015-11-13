package com.ivoid.bj;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.bj.BuildConfig;
import com.bj.R;
import com.growthpush.GrowthPush;
import com.growthpush.handler.BaseReceiveHandler;
import com.growthpush.model.Environment;

import org.json.JSONException;
import org.json.JSONObject;

public class Title extends Activity implements OnClickListener
{
    private final String registUrl = "http://blackjack.uh-oh.jp/user/regist/%s";
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;

    private String device_id;
    private String user_id;

    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        //プリファレンスの準備
        preference = getSharedPreferences("user_data", MODE_PRIVATE);
        editor = preference.edit();

        //GrowthPush.getInstance().initialize(getApplicationContext(), 9252, "eVm8r9Ma42ihP1JRFz1Pa3onKoSxlC57").register("191839645621");
        GrowthPush.getInstance().initialize(getApplicationContext(), 9252, "eVm8r9Ma42ihP1JRFz1Pa3onKoSxlC57", BuildConfig.DEBUG ? Environment.development : Environment.production, true).register("191839645621");

        GrowthPush.getInstance().setReceiveHandler(new BaseReceiveHandler() {
            @Override
            public void onReceive(Context context, Intent intent) {
                super.onReceive(context, intent);
                if (preference.getBoolean("push_on", true)) {
                    showAlert(context, intent);
                    addNotification(context, intent);
                }
            }
        });

        GrowthPush.getInstance().trackEvent("Launch");
        GrowthPush.getInstance().setDeviceTags();

        user_id = preference.getString("user_id", "");

        setContentView(R.layout.title);

        (findViewById(R.id.play)).setOnClickListener(this);
        (findViewById(R.id.apply)).setOnClickListener(this);
        (findViewById(R.id.result)).setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(user_id.equals("")){
            registUser();
        }else{
            showActionButton();
        }
    }

    public void registUser(){
        AsyncJsonLoader asyncJsonLoader = new AsyncJsonLoader(this, new AsyncJsonLoader.AsyncCallback() {
            // 実行後
            public boolean postExecute(JSONObject result) {
                try {
                    user_id = result.getString("user_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                //user_idをセット
                editor.putString("user_id", user_id);
                editor.commit();
                showActionButton();
                return true;
            }
        }, null);
        asyncJsonLoader.execute(String.format(registUrl, "test"));
    }
    
	public void onClick(final View view)
    {
        switch (view.getId()) {
            case R.id.play: {
                // 遷移先のActivityを指定して、Intentを作成する
                Intent intent = new Intent(this, Dealer.class);
                // 遷移先のアクティビティを起動させる
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.apply: {
                // 遷移先のActivityを指定して、Intentを作成する
                Intent intent = new Intent(this, Competition.class);
                // 遷移先のアクティビティを起動させる
                startActivity(intent);
                overridePendingTransition(0, 0);
                break;
            }
            case R.id.result: {
                // 遷移先のActivityを指定して、Intentを作成する
                //Intent intent = new Intent( this,Result.class );
                // 遷移先のアクティビティを起動させる
                //startActivity(intent);
                break;
            }
        }
    }

    void showActionButton(){
        (findViewById(R.id.play)).setVisibility(Button.VISIBLE);
        (findViewById(R.id.apply)).setVisibility(Button.VISIBLE);
        (findViewById(R.id.result)).setVisibility(Button.VISIBLE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return false;
    }
}