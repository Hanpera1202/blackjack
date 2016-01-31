package com.ivoid.bj;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;

public class AlertDialogFragment extends DialogFragment {

    public static AlertDialogFragment newInstance(String message) {
        AlertDialogFragment frag = new AlertDialogFragment();
        Bundle args = new Bundle();
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = getArguments().getString("message");

        return new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        switch(getTag()) {
                            case "applyCompletedDialog":
                                ((Competition) getActivity()).showAd();
                                break;
                            case "compRegistMailDialog":
                                getActivity().finish();
                                break;
                        }
                    }
                })
                .create();
    }
}