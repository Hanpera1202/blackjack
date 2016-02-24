package com.ivoid.bj;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.bj.R;

public class ConfirmAdDialogFragment extends DialogFragment {

    private Dialog dialog;
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;
    private GameSettings settings;

    private BonusCDTimer bonusCountDownTimer = null;

    public static ConfirmAdDialogFragment newInstance() {
        ConfirmAdDialogFragment frag = new ConfirmAdDialogFragment();
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        dialog = new Dialog(getActivity());

        // タイトル非表示
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // フルスクリーン
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        // キャンセルを無効にする
        this.setCancelable(false);
        dialog.setContentView(R.layout.confirm_ad);

        //プリファレンスの準備
        preference = getActivity().getSharedPreferences("user_data", getActivity().MODE_PRIVATE);
        editor = preference.edit();

        settings = new GameSettings();

        String title;
        String message;
        if(checkCoinBonus()) {
            title = "Get more Hint Coin!";
            message = "Do you want to get a Hint Coin to see the video ad?";
            // OK ボタンのリスナ
            dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    ((Dealer) getActivity()).showMovieAd();
                }
            });
            // CANCEL ボタンのリスナ
            dialog.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    ((Dealer) getActivity()).execInitUIWhenBetting(0, false);
                }
            });
        }else{
            title = "Up to " + settings.coinBonusCount + " times a day!";
            message = "Please wait until tomorrow.";
            // OK ボタンのリスナ
            dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    ((Dealer) getActivity()).execInitUIWhenBetting(0, false);
                }
            });
            dialog.findViewById(R.id.cancel).setVisibility(Button.GONE);
        }

        ((TextView)dialog.findViewById(R.id.title)).setText(title);
        ((TextView)dialog.findViewById(R.id.message)).setText(message);

        loginBonusCountDown();

        return dialog;
    }

    public void loginBonusCountDown(){

        // 以前にタイマーを起動していればリセット
        if (bonusCountDownTimer != null){
            bonusCountDownTimer.cancel();
            bonusCountDownTimer = null;
        }
        Long timeLeft = ((Dealer) getActivity()).getTimeLeftOfLoginBonus();
        if(timeLeft == 0L){
            ((TextView)dialog.findViewById(R.id.CountDown)).setText("Get Now!!");
        } else {
            // カウントダウンする
            bonusCountDownTimer = new BonusCDTimer(timeLeft, 500);
            bonusCountDownTimer.start();
        }
    }

    public class BonusCDTimer extends CountDownTimer {

        TextView count_txt = (TextView) dialog.findViewById(R.id.CountDown);

        public BonusCDTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            count_txt.setText((millisUntilFinished / 1000 / 3600) + ":" +
                    String.format("%02d", (millisUntilFinished / 1000 / 60 % 60)) + ":" +
                    String.format("%02d", (millisUntilFinished / 1000 % 60)));
        }

        @Override
        public void onFinish() {
            count_txt.setText("Get Now!!");
        }
    }

    public boolean checkCoinBonus(){
        Integer gotCoinBonusCount = preference.getInt("gotCoinBonusCount", 0);
        if(gotCoinBonusCount < settings.coinBonusCount){
            return true;
        }else {
            Long coinBonusGetTime = preference.getLong("coinBonusGetTime", 0);
            Long needTime = 86400 * 1000 - (System.currentTimeMillis() - coinBonusGetTime);
            if (needTime < 0L) {
                editor.putInt("gotCoinBonusCount", 0);
                editor.commit();
                return true;
            } else {
                return false;
            }
        }
    }

}