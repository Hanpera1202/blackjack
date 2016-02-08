package com.ivoid.bj;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bj.R;

public class HintDialogFragment extends DialogFragment {

    public static HintDialogFragment newInstance(Integer cardId) {
        HintDialogFragment frag = new HintDialogFragment();
        Bundle args = new Bundle();
        args.putInt("cardId", cardId);
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
        dialog.setContentView(R.layout.hint);

        ImageView cardImage  = (ImageView)dialog.findViewById(R.id.card);
        cardImage.setImageResource(getArguments().getInt("cardId"));

        // OK ボタンのリスナ
        dialog.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return dialog;
    }
}