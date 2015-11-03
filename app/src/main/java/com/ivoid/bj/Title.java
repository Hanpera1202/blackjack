package com.ivoid.bj;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.bj.R;

import org.json.JSONException;
import org.json.JSONObject;


public class Title extends Activity implements OnClickListener
{

    private final String registUrl = "http://blackjack.uh-oh.jp/user/regist/%s";

    private ProgressDialog progressDialog;

    private SharedPreferences preference;
    private SharedPreferences.Editor editor;

    @Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

        Log.d("Activity", "Title->onCreate");

        progressDialog = new ProgressDialog(this);
        //プリファレンスの準備
        preference = getSharedPreferences("user_data", MODE_PRIVATE);
        editor = preference.edit();

        setContentView(R.layout.title);

        AsyncJsonLoader asyncJsonLoader = new AsyncJsonLoader(new AsyncJsonLoader.AsyncCallback() {
            // 実行前
            public void preExecute() {
                showLoading();
            }
            // 実行後
            public void postExecute(JSONObject result) {
                removeProgressDialog();
                if (result == null) {
                    return;
                }
                try {
                    String user_id= result.getString("user_id");
                    Log.d("user_id", user_id);
                    //user_idをセット
                    editor.putString("user_id", user_id);
                    editor.commit();

                    showActionButton();
                } catch (JSONException e) {
                    e.printStackTrace();
                    showLoadError(); // エラーメッセージを表示
                }
            }
            // 実行中
            public void progressUpdate(int progress) {
            }

            // キャンセル
            public void cancel() {
            }
        });
        // 処理を実行
        if (preference.getString("user_id", "") == "") {
            asyncJsonLoader.execute(String.format(registUrl, "test_device_id"));
        }else{
            showActionButton();
        }
        (findViewById(R.id.play)).setOnClickListener(this);
        (findViewById(R.id.apply)).setOnClickListener(this);
        (findViewById(R.id.result)).setOnClickListener(this);
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

    // ロード中ダイアログ表示
    private void showLoading() {
        progressDialog.setMessage("Registing");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    // エラーメッセージ表示
    private void showLoadError() {
        Toast toast = Toast.makeText(this, "I could not get the data.", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    // ロード中ダイアログ非表示
    private void removeProgressDialog() {
        progressDialog.dismiss();
    }

    void showActionButton(){

    }
}