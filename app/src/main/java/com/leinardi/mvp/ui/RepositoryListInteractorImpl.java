package com.leinardi.mvp.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;
import com.leinardi.mvp.BuildConfig;
import com.leinardi.mvp.MvpExample1App;
import com.leinardi.mvp.model.Owner;
import com.leinardi.mvp.model.Repository;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import static com.leinardi.mvp.provider.RepositoryContract.ConfigEntry;
import static com.leinardi.mvp.provider.RepositoryContract.OwnerEntry;
import static com.leinardi.mvp.provider.RepositoryContract.RepositoryEntry;

/**
 * Created by leinardi on 18/07/16.
 */

public class RepositoryListInteractorImpl implements RepositoryListInteractor {
    private static final String CONFIG_PAGE_SELECTION_CLAUSE = ConfigEntry.COLUMN_NAME + " = ?";
    private static final String[] CONFIG_PAGE_SELECTION_ARGS = new String[]{ConfigEntry.DEFAULT_PAGE_NAME};
    private static final String TAG = RepositoryListInteractorImpl.class.getSimpleName();
    private static final String URL_QUERY = "/users/square/repos?page=%s&per_page=%d";

    private boolean mIsCanceled;

    private MvpExample1App mApp = MvpExample1App.getInstance();
    private GetRepositoryListTask mGetRepositoryListTask;

    @Override
    public int getStartingPage() {
        return getConfigPageValue();
    }

    private int getConfigPageValue() {
        String stringValue = ConfigEntry.DEFAULT_PAGE_VALUE;
        Cursor configCursor = mApp.getContentResolver().query(
                ConfigEntry.CONTENT_URI,
                null,
                CONFIG_PAGE_SELECTION_CLAUSE,
                CONFIG_PAGE_SELECTION_ARGS,
                null
        );

        if (configCursor != null) {
            if (configCursor.moveToFirst()) {
                stringValue = configCursor.getString(configCursor.getColumnIndex(ConfigEntry.COLUMN_VALUE));
            }
            configCursor.close();
        } else {
            Log.e(TAG, "Unable to find the config " + ConfigEntry.DEFAULT_PAGE_NAME);
        }
        return Integer.parseInt(stringValue);
    }

    @Override
    public void setStartingPage(int startingPage) {
        updateConfigPageValue(startingPage);
    }

    private void updateConfigPageValue(int startingPage) {
        ContentValues updatedConfigContentValues = new ContentValues();
        updatedConfigContentValues.put(ConfigEntry.COLUMN_NAME, ConfigEntry.DEFAULT_PAGE_NAME);
        updatedConfigContentValues.put(ConfigEntry.COLUMN_VALUE, startingPage);
        mApp.getContentResolver().update(
                ConfigEntry.CONTENT_URI,
                updatedConfigContentValues,
                CONFIG_PAGE_SELECTION_CLAUSE,
                CONFIG_PAGE_SELECTION_ARGS
        );
    }

    @Override
    public void deleteAllData() {
        deleteAllRecords();
    }

    private void deleteAllRecords() {
        // Delete repositories
        mApp.getContentResolver().delete(
                RepositoryEntry.CONTENT_URI,
                null,
                null
        );

        // Delete owners
        mApp.getContentResolver().delete(
                OwnerEntry.CONTENT_URI,
                null,
                null
        );

        setStartingPage(Integer.parseInt(ConfigEntry.DEFAULT_PAGE_VALUE));
    }

    @Override
    public void loadRepositoryListPage(int page, RepositoryListListener listener) {
        reset();
        mGetRepositoryListTask = new GetRepositoryListTask(listener);
        mGetRepositoryListTask.execute(page);
    }

    @Override
    public boolean isRepositoryContentProviderEmpty() {
        Cursor cursor = mApp.getContentResolver().query(RepositoryEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        boolean isEmpty = cursor == null || cursor.getCount() == 0;
        if (cursor != null) {
            cursor.close();
        }
        return isEmpty;
    }


    @Override
    public void cancel() {
        if(mGetRepositoryListTask != null && !mGetRepositoryListTask.isCancelled()){
            mGetRepositoryListTask.cancel(true);
        }
        mIsCanceled = true;
    }

    @Override
    public void reset() {
        if(mGetRepositoryListTask != null && !mGetRepositoryListTask.isCancelled()){
            mGetRepositoryListTask.cancel(true);
        }
        mIsCanceled = false;
    }

    // Find a better solution that doesn't leak
    private class GetRepositoryListTask extends AsyncTask<Integer, Void, Message> {
        private RepositoryListListener listener;

        GetRepositoryListTask(RepositoryListListener listener) {
            this.listener = listener;
        }

        protected Message doInBackground(Integer... pages) {
            Message message = new Message();
            int page = pages[0];
            message.arg1 = page;

            Repository[] repositories = getRepositories(message, page);

            if (message.obj == null && repositories.length > 0) {
                for (Repository repository : repositories) {
                    insertOrUpdateOwner(repository.getOwner());
                    insertRepository(repository);
                }
                setStartingPage(page);
            }

            return message;
        }

        private void insertOrUpdateOwner(Owner owner) {
            ContentValues values = new ContentValues();
            values.put(OwnerEntry.COLUMN_ID_GITHUB, owner.getIdGitHub());
            values.put(OwnerEntry.COLUMN_LOGIN, owner.getLogin());
            values.put(OwnerEntry.COLUMN_HTML_URL, owner.getHtmlUrl());

            if (!checkIfOwnerAlreadyExists(owner.getIdGitHub())) {
                // Insert owner
                mApp.getContentResolver().insert(OwnerEntry.CONTENT_URI, values);
            } else {
                // Update owner
                mApp.getContentResolver().update(
                        OwnerEntry.CONTENT_URI,
                        values,
                        OwnerEntry.COLUMN_ID_GITHUB + " = ?",
                        new String[]{String.valueOf(owner.getIdGitHub())}
                );
            }
        }

        private void insertRepository(Repository repository) {
            ContentValues values = new ContentValues();
            values.put(RepositoryEntry.COLUMN_ID_GITHUB, repository.getIdGitHub());
            values.put(RepositoryEntry.COLUMN_OWNER_ID_FK, repository.getOwner().getIdGitHub());
            values.put(RepositoryEntry.COLUMN_NAME, repository.getName());
            values.put(RepositoryEntry.COLUMN_HTML_URL, repository.getHtmlUrl());
            values.put(RepositoryEntry.COLUMN_DESCRIPTION, repository.getDescription());
            values.put(RepositoryEntry.COLUMN_FORK, repository.getFork());
            mApp.getContentResolver().insert(RepositoryEntry.CONTENT_URI, values);
        }

        private Repository[] getRepositories(Message message, int page) {
            URL url;
            HttpURLConnection urlConnection = null;
            Repository[] repositories = null;
            try {
                url = new URL(BuildConfig.API_URL + String.format(Locale.US, URL_QUERY, page, BuildConfig.REPO_PER_PAGE));

                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader isw = new InputStreamReader(in);

                Gson gson = new Gson();
                repositories = gson.fromJson(isw, Repository[].class);
            } catch (Exception e) {
                // TODO remove Pokemon exception handling
                Log.e(TAG, "An error occurred while retrieving the JSON data", e);
                message.obj = e.getMessage();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return repositories;
        }

        protected void onPostExecute(Message message) {
            String error = ((String) message.obj);
            if (error == null) {
                if (!mIsCanceled) {
                    listener.onSuccess();
                }
            } else {
                if (!mIsCanceled) {
                    listener.onFailure(error);
                }
            }
        }
    }

    private boolean checkIfOwnerAlreadyExists(int id) {
        Cursor cursor = mApp.getContentResolver().query(
                OwnerEntry.buildOwnerUri(id),
                null,
                null,
                null,
                null
        );

        if (cursor == null) {
            return false;
        } else {
            boolean exists = cursor.getCount() != 0;
            cursor.close();
            return exists;
        }
    }
}
