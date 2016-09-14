package com.leinardi.mvp.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.leinardi.mvp.R;

/**
 * Created by leinardi on 18/07/16.
 */

public class OpenInBrowserAlertDialogFragment extends DialogFragment {
    public static final String TAG = OpenInBrowserAlertDialogFragment.class.getSimpleName();
    private static final String KEY_REPO_URL = "repo_url";
    private static final String KEY_OWNER_URL = "owner_url";

    public static OpenInBrowserAlertDialogFragment newInstance(String repoUrl, String ownerUrl) {
        OpenInBrowserAlertDialogFragment frag = new OpenInBrowserAlertDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_REPO_URL, repoUrl);
        args.putString(KEY_OWNER_URL, ownerUrl);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String repoUrl = getArguments().getString(KEY_REPO_URL);
        final String ownerUrl = getArguments().getString(KEY_OWNER_URL);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.open_in_browser_title)
                .setMessage(R.string.open_in_browser_message)
                .setPositiveButton(R.string.open_repo_page, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openUrlInDefaultBrowser(repoUrl);
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.open_owner_page, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        openUrlInDefaultBrowser(ownerUrl);
                        dismiss();
                    }
                })
                .setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                })
                .create();
    }

    private void openUrlInDefaultBrowser(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }
}