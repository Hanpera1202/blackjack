package com.ivoid.bj;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bj.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class BonusDialogFragment extends DialogFragment {

    private GameSettings settings;
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;

    public static BonusDialogFragment newInstance(String bonusType) {
        BonusDialogFragment frag = new BonusDialogFragment();
        Bundle args = new Bundle();
        args.putString("bonusType", bonusType);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(getActivity());

        // タイトル非表示
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // フルスクリーン
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        // キャンセルを無効にする
        this.setCancelable(false);

        dialog.setContentView(R.layout.bonus);

        settings = new GameSettings();
        preference = getActivity().getSharedPreferences("user_data", getActivity().MODE_PRIVATE);
        editor = preference.edit();

        if(getArguments().getString("bonusType").equals("login")) {
            ((TextView) dialog.findViewById(R.id.bonusMessage)).setText("YOU GOT BONUS!");
            // calculate login bonus points
            Long loginBonusGetTime = preference.getLong("loginBonusGetTime", 0);
            if (loginBonusGetTime == 0L) {
                editor.putInt("gotBonusPoint", settings.startCash);
                ((TextView) dialog.findViewById(R.id.specialBonus)).setText("+" + (int) settings.startCash);
                dialog.findViewById(R.id.specialBonusArea).setVisibility(View.VISIBLE);
            }
            editor.putInt("gotBonusCoin", settings.loginBonusCoins);
            ((TextView) dialog.findViewById(R.id.loginBonus)).setText("+" + settings.loginBonusCoins);

            editor.putLong("loginBonusGetTime", System.currentTimeMillis());

        }else{
            ((TextView) dialog.findViewById(R.id.bonusMessage)).setText("YOU GOT COIN!");
            editor.putInt("gotBonusCoin", 1);
            ((TextView) dialog.findViewById(R.id.loginBonus)).setText("+1");
            editor.putInt("gotCoinBonusCount", preference.getInt("gotCoinBonusCount", 0) + 1);
            try {
                String getTime = new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
                getTime += " 00:00:00";
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                editor.putLong("coinBonusGetTime", sdf.parse(getTime).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        editor.commit();

        // OK ボタンのリスナ
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ((Playing)getActivity()).setPlayerData();
            }
        });

        return dialog;
    }
}