package com.ivoid.bj;

import android.app.Dialog;
import android.content.Intent;
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

public class TicketDialogFragment extends DialogFragment {

    private GameSettings settings;
    private SharedPreferences preference;
    private SharedPreferences.Editor editor;

    private Player player;

    public static TicketDialogFragment newInstance() {
        TicketDialogFragment frag = new TicketDialogFragment();
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

        dialog.setContentView(R.layout.ticket);

        settings = new GameSettings();
        preference = getActivity().getSharedPreferences("user_data", getActivity().MODE_PRIVATE);
        editor = preference.edit();

        player = new Player(getActivity(), "God");

        // calculate login bonus points
        if (player.getPointBalance() >= settings.ticketExchangePoint) {
            player.withdrawPoint(settings.ticketExchangePoint);
            player.depositTicket(1);
            ((TextView) dialog.findViewById(R.id.gotTicket)).setText("+1");
        }

        // APPLY ボタンのリスナ
        dialog.findViewById(R.id.apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Intent newIntent;
                newIntent = new Intent(getActivity(), Competition.class);
                startActivity(newIntent);
                getActivity().overridePendingTransition(0, 0);
            }
        });
        // CLOSE ボタンのリスナ
        dialog.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                ((Playing)getActivity()).setPlayerDataWhenReturnDialog();
            }
        });

        return dialog;
    }
}