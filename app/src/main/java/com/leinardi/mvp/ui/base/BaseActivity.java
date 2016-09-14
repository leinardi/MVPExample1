package com.leinardi.mvp.ui.base;


import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;

import com.leinardi.mvp.R;
import com.leinardi.mvp.ui.dialog.ErrorAlertDialogFragment;

/**
 * Created by leinardi on 18/07/16.
 */

public abstract class BaseActivity extends AppCompatActivity implements BaseView {


    @Override
    public void showError(String message) {
        showErrorDialogFragment(message);
    }

    protected void showErrorDialogFragment(String message) {
        DialogFragment newFragment = ErrorAlertDialogFragment.newInstance(
                getString(R.string.error_alert_dialog_title), message);
        newFragment.show(getSupportFragmentManager(), ErrorAlertDialogFragment.class.getSimpleName());
    }
}
