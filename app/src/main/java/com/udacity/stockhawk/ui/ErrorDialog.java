package com.udacity.stockhawk.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.udacity.stockhawk.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ErrorDialog extends DialogFragment {

    private static final String ERROR_TEXT = "error text";
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error_text)
    TextView textView;

    public static ErrorDialog newInstance(String error) {
        Bundle args = new Bundle();
        args.putString(ERROR_TEXT, error);
        ErrorDialog fragment = new ErrorDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String error = this.getArguments().getString(ERROR_TEXT);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams")
        View custom = inflater.inflate(R.layout.error_stock_dialog, null);
        ButterKnife.bind(this, custom);
        textView.setText(error);
        builder.setView(custom)
         .setMessage(getString(R.string.error_dialog_title))
         .setIcon(android.R.drawable.stat_notify_error)
         .setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });
        return builder.create();
    }
}
