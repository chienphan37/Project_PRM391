package com.example.androidproject.activity;

import android.app.Dialog;
import android.view.View;

import com.example.androidproject.R;

public class BottomSheetDialog extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {

    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View contentView = View.inflate(getContext(), R.layout.popup, null);
        dialog.setContentView(contentView);
    }
}
