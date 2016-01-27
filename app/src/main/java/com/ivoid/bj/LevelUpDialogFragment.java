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

public class LevelUpDialogFragment extends DialogFragment {

    private GameSettings settings;
    private Player player;
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;
    private int getBonusCoin;

    public static LevelUpDialogFragment newInstance() {
        LevelUpDialogFragment frag = new LevelUpDialogFragment();
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
        dialog.setContentView(R.layout.level_up);

        player = new Player(getActivity(), "God");
        settings = new GameSettings();

        preference = getActivity().getSharedPreferences("user_data", getActivity().MODE_PRIVATE);
        editor = preference.edit();

        if(player.isLevelUp()) {
            player.levelUp();
            getBonusCoin = settings.levels.get(player.getLevel()).getCoinCnt;
            editor.putInt("gotBonusCoin", getBonusCoin);
        }

        ((TextView) dialog.findViewById(R.id.newLevel)).setText("You are now level " + player.getLevel() + ".");
        ((TextView) dialog.findViewById(R.id.newMaxBet)).setText(player.getMaxBet() + " pt");
        ((TextView) dialog.findViewById(R.id.loginBonus)).setText(String.valueOf(getBonusCoin));

        editor.commit();

        // OK ボタンのリスナ
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ((Dealer)getActivity()).onResume();
            }
        });

        return dialog;
    }
}