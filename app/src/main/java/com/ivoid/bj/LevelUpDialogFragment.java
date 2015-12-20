package com.ivoid.bj;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.bj.R;

public class LevelUpDialogFragment extends DialogFragment {

    public static LevelUpDialogFragment newInstance(String message) {
        LevelUpDialogFragment frag = new LevelUpDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
/*
        LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.level_up, null);
*/
        Dialog dialog = new Dialog(getActivity());

        // タイトル非表示
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        // フルスクリーン
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

        dialog.setContentView(R.layout.level_up);

        // OK ボタンのリスナ
        dialog.findViewById(R.id.collect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return dialog;
/*
        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .create();
                */
    }
}