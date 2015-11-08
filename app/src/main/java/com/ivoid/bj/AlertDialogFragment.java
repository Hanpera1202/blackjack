package com.ivoid.bj;

import android.app.Dialog;
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
                .setPositiveButton("OK", null)
                .create();
    }
}