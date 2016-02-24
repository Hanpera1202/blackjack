package com.ivoid.bj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bj.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by nakazato on 2015/10/27.
 */
public class ResultDialog extends FragmentActivity implements View.OnClickListener {

    Game game;

    private final String getResultUrl = "http://blackjack.uh-oh.jp/users/%s/results/%s";
    private final String getMailRegistUrl = "http://blackjack.uh-oh.jp/users/%s";

    private String name;
    private String imageUrl;
    private String result;
    private String receiveFlag;

    private DialogFragment registMailDialog;
    private DialogFragment CompRegistMailDialog;

    private SharedPreferences preference;
    private SharedPreferences.Editor editor;

    private String mail_address;

    AsyncJsonLoader asyncJsonLoader;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        game = (Game) this.getApplication();

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //プリファレンスの準備
        preference = getSharedPreferences("user_data", MODE_PRIVATE);
        editor = preference.edit();

        mail_address = preference.getString("mail_address", "");

        Intent i = getIntent();
        String competition_id = i.getStringExtra("competition_id");

        asyncJsonLoader = new AsyncJsonLoader(this, new AsyncJsonLoader.AsyncCallback() {

            // 実行後
            public boolean postExecute(JSONObject apiresult) {
                try {
                    // 各 ATND イベントのタイトルを配列へ格納
                    result = apiresult.getString("result");
                    name = apiresult.getString("name");
                    imageUrl = apiresult.getString("image_url");
                    receiveFlag = apiresult.getString("receive_flag");

                    setView();

                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            // error
            public boolean postError() {
                return false;
            }
        });
        // 処理を実行
        asyncJsonLoader.execute(String.format(getResultUrl, game.getUserId(), competition_id));
    }

    @Override
    public void onResume(){
        super.onResume();
        // Redisplay dialog
        if (asyncJsonLoader.isInProcess()) {
            asyncJsonLoader.showDialog();
        }
    }

    private void setView() {
        if (result.equals("1")) {
            setContentView(R.layout.result_dialog_win);
            if (receiveFlag.equals("1")){
                findViewById(R.id.receive_msg).setVisibility(TextView.VISIBLE);
                findViewById(R.id.positive_button).setVisibility(Button.GONE);
            }else{
                findViewById(R.id.positive_button).setOnClickListener(this);
                findViewById(R.id.receive_msg).setVisibility(TextView.GONE);
                findViewById(R.id.positive_button).setVisibility(Button.VISIBLE);
            }
        }else{
            setContentView(R.layout.result_dialog_lose);
            findViewById(R.id.close_button).setOnClickListener(this);
        }

        ((TextView)findViewById(R.id.name)).setText(name);
        ImageView itemImage = (ImageView) findViewById(R.id.image);
        itemImage.setTag(imageUrl);
        AsyncImageLoader asyncImageLoader = new AsyncImageLoader(itemImage);
        asyncImageLoader.execute(imageUrl);
    }

    public void onClick(final View view)
    {
        switch (view.getId()) {
            case R.id.positive_button: {
                createRegistMailDialog();
                showRegistMailDialog();
                break;
            }
            case R.id.close_button: {
                finish();
                break;
            }
        }
    }

    public void registMailAddress(String inputMailAddress){
        mail_address = inputMailAddress;
        editor.putString("mail_address", mail_address);
        editor.commit();
        asyncJsonLoader = new AsyncJsonLoader(this, new AsyncJsonLoader.AsyncCallback() {
            // 実行後
            public boolean postExecute(JSONObject apiresult) {
                try {
                    result = apiresult.getString("result");
                    showCompRegistMailDialog();
                } catch (JSONException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            // error
            public boolean postError() {
                return false;
            }
        },"Registing");

        createCompRegistMailDialog();
        String url = String.format(getMailRegistUrl, game.getUserId());
        try {
            Crypt crypt = new Crypt(mail_address);
            asyncJsonLoader.execute("POST", url, "mail_address", crypt.encrypt());
        } catch (Exception e) {
            e.printStackTrace();
        }
/*
        try {
            byte[] data = mail_address.getBytes("UTF-8");
            String base64_mail_address = Base64.encodeToString(data, android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP);
            asyncJsonLoader.execute(String.format(getMailRegistUrl, game.getUserId(), base64_mail_address));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        */
    }

    private void createRegistMailDialog() {
        String message;
        if(mail_address == ""){
            message = "Please input your mail address";
        }else{
            message = "Is it correct in the e-mail address is " + mail_address + "?\n" +
                    "If correct, Please press the OK button.\n" +
                    "If wrong, please fix and press the OK button.";
        }
        registMailDialog = RegistMailDialogFragment.newInstance(message, mail_address);
    }

    private void showRegistMailDialog() {
        if(registMailDialog != null) {
            registMailDialog.show(getSupportFragmentManager(), "registMailDialog");
        }
    }

    // アラートダイアログ非表示
    private void dismissRegistMailDialog() {
        if (registMailDialog !=  null) {
            registMailDialog.dismiss();
            Fragment prev = getSupportFragmentManager().findFragmentByTag("registMailDialog");
            if (prev != null) {
                DialogFragment df = (DialogFragment) prev;
                df.dismiss();
            }
        }
        registMailDialog = null;
    }

    // 登録完了ダイアログ作成
    private void createCompRegistMailDialog() {
        String message = "Registration is complete.\n" +
                "Please wait for the mail.";
        CompRegistMailDialog = AlertDialogFragment.newInstance("compRegistMailDialog", message);
    }

    // 登録完了ダイアログ表示
    private void showCompRegistMailDialog() {
        if(CompRegistMailDialog != null &&
                getSupportFragmentManager().findFragmentByTag("alertDialog") == null) {
            CompRegistMailDialog.show(getSupportFragmentManager(), "alertDialog");
        }
    }

    // 登録完了ダイアログ非表示
    private void dismissCompRegistMailDialog() {
        if (CompRegistMailDialog !=  null) {
            Fragment prev = getSupportFragmentManager().findFragmentByTag("alertDialog");
            if (prev != null) {
                DialogFragment df = (DialogFragment) prev;
                df.dismiss();
            }
        }
        CompRegistMailDialog = null;
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
            dismissRegistMailDialog();
            dismissCompRegistMailDialog();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}
