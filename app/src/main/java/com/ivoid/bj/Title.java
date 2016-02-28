package com.ivoid.bj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bj.R;
import org.json.JSONException;
import org.json.JSONObject;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Title extends FragmentActivity {
    Game game;

    Bitmap bgBmp;

    private final String registUrl = "http://blackjack.uh-oh.jp/users";
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;

    AsyncJsonLoader asyncJsonLoader;
    private DialogFragment alertDialog;

    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        game = (Game) this.getApplication();

        // prepare Preferences
        preference = getSharedPreferences("user_data", MODE_PRIVATE);
        editor = preference.edit();

        setContentView(R.layout.title);
    }

    @Override
    public void onResume() {
        super.onResume();

        // display title only first
        if(game.getUserId().equals("")){
            if ((preference.getString("user_id", "")).equals("")) {
                registUser();
            } else {
                showActionButton();
            }
            setBackgroundImage(R.drawable.bg);
        }else {
            Intent intent = new Intent(this, Playing.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0, 0);
        }
    }

    public void registUser(){
        asyncJsonLoader = new AsyncJsonLoader(this, new AsyncJsonLoader.AsyncCallback() {
            // 実行後
            public boolean postExecute(JSONObject result) {
                try {
                    editor.putString("user_id", result.getString("user_id"));
                    editor.commit();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                showActionButton();
                return true;
            }
            // error
            public boolean postError() {
                showAlertDialog();
                return true;
            }
        }, "Registing");
        String message = "Check internet connection.";
        createAlertDialog("registErrorDialog", message);
        asyncJsonLoader.execute("POST", registUrl);
    }
    
	public void onClickHeader(final View view) {
        game.setUserId(preference.getString("user_id", ""));
        startActivity(game.getNewIntent(view));
        finish();
        overridePendingTransition(0, 0);
    }

    void showActionButton(){
        (findViewById(R.id.message)).setVisibility(Button.VISIBLE);
        (findViewById(R.id.game)).setVisibility(Button.VISIBLE);
        (findViewById(R.id.prize_competition)).setVisibility(Button.VISIBLE);
    }

    // アラートダイアログ作成
    private void createAlertDialog(String dialogType, String message){
        alertDialog = AlertDialogFragment.newInstance(dialogType, message);
    }

    // アラートダイアログ表示
    private void showAlertDialog() {
        if(alertDialog != null &&
                getSupportFragmentManager().findFragmentByTag("alertDialog") == null) {
            alertDialog.show(getSupportFragmentManager(), "alertDialog");
        }
    }

    // アラートダイアログ非表示
    private void dismissAlertDialog() {
        if (alertDialog !=  null) {
            Fragment prev = getSupportFragmentManager().findFragmentByTag("alertDialog");
            if (prev != null) {
                DialogFragment df = (DialogFragment) prev;
                df.dismiss();
            }
        }
        alertDialog = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recycleBackgroundImage();
    }

    public void setBackgroundImage(int resourceId){
        bgBmp = BitmapFactory.decodeResource(getResources(), resourceId);
        ((ImageView)findViewById(R.id.bgImage)).setImageBitmap(bgBmp);
    }

    public void recycleBackgroundImage(){
        if(bgBmp!=null){
            bgBmp.recycle();
            bgBmp = null;
        }
        ((ImageView)findViewById(R.id.bgImage)).setImageBitmap(null);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK){
            moveTaskToBack(true);
            return true;
        }
        return false;
    }

    @Override
    public void onUserLeaveHint() {
        if (asyncJsonLoader != null) {
            // ダイアログを閉じる（2重表示防止）
            asyncJsonLoader.dismissDialog();
            dismissAlertDialog();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}