package com.leinardi.mvp.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.leinardi.mvp.db.RepositoryDBHelper;

import static com.leinardi.mvp.provider.RepositoryContract.CONTENT_AUTHORITY;
import static com.leinardi.mvp.provider.RepositoryContract.ConfigEntry;
import static com.leinardi.mvp.provider.RepositoryContract.OwnerEntry;
import static com.leinardi.mvp.provider.RepositoryContract.PATH_CONFIG;
import static com.leinardi.mvp.provider.RepositoryContract.PATH_OWNER;
import static com.leinardi.mvp.provider.RepositoryContract.PATH_REPOSITORY;
import static com.leinardi.mvp.provider.RepositoryContract.RepositoryEntry;

/**
 * Created by leinardi on 18/07/16.
 */

public class RepositoryProvider extends ContentProvider {
    // Use an int for each URI we will run, this represents the different queries
    private static final int OWNER = 100;
    private static final int OWNER_ID = 101;
    private static final int REPOSITORY = 200;
    private static final int REPOSITORY_ID = 201;
    private static final int CONFIG = 300;
    private static final int CONFIG_ID = 301;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private RepositoryDBHelper mOpenHelper;

    /**
     * Builds a UriMatcher that is used to determine witch database request is being made.
     */
    public static UriMatcher buildUriMatcher() {
        String content = CONTENT_AUTHORITY;

        // All paths to the UriMatcher have a corresponding code to return
        // when a match is found (the ints above).
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(content, PATH_CONFIG, CONFIG);
        matcher.addURI(content, PATH_CONFIG + "/#", CONFIG_ID);
        matcher.addURI(content, PATH_OWNER, OWNER);
        matcher.addURI(content, PATH_OWNER + "/#", OWNER_ID);
        matcher.addURI(content, PATH_REPOSITORY, REPOSITORY);
        matcher.addURI(content, PATH_REPOSITORY + "/#", REPOSITORY_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = RepositoryDBHelper.getInstance(getContext());
        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case OWNER:
                return OwnerEntry.CONTENT_TYPE;
            case OWNER_ID:
                return OwnerEntry.CONTENT_ITEM_TYPE;
            case REPOSITORY:
                return RepositoryEntry.CONTENT_TYPE;
            case REPOSITORY_ID:
                return RepositoryEntry.CONTENT_ITEM_TYPE;
            case CONFIG:
                return ConfigEntry.CONTENT_TYPE;
            case CONFIG_ID:
                return ConfigEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case OWNER:
                retCursor = db.query(
                        OwnerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case OWNER_ID:
                long _id = ContentUris.parseId(uri);
                retCursor = db.query(
                        OwnerEntry.TABLE_NAME,
                        projection,
                        OwnerEntry.COLUMN_ID_GITHUB + " = ?",
                        new String[]{String.valueOf(_id)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case REPOSITORY:
                // SELECT * FROM REPOSITORIES
                // LEFT OUTER JOIN OWNERS
                // ON REPOSITORIES.RepositoryContract.RepositoryEntry.COLUMN_OWNER_ID_FK = OWNERS.OwnerEntry.COLUMN_ID
                String REPOSITORIES_SELECT_QUERY =
                        String.format("SELECT * FROM %s LEFT OUTER JOIN %s ON %s.%s = %s.%s",
                                RepositoryEntry.TABLE_NAME,
                                OwnerEntry.TABLE_NAME,
                                RepositoryEntry.TABLE_NAME, RepositoryEntry.COLUMN_OWNER_ID_FK,
                                OwnerEntry.TABLE_NAME, OwnerEntry.COLUMN_ID_GITHUB);

                retCursor = db.rawQuery(REPOSITORIES_SELECT_QUERY, selectionArgs);
                break;
            case REPOSITORY_ID:
                _id = ContentUris.parseId(uri);
                retCursor = db.query(
                        RepositoryEntry.TABLE_NAME,
                        projection,
                        RepositoryEntry._ID + " = ?",
                        new String[]{String.valueOf(_id)},
                        null,
                        null,
                        sortOrder
                );
                break;
            case CONFIG:
                retCursor = db.query(
                        ConfigEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CONFIG_ID:
                _id = ContentUris.parseId(uri);
                retCursor = db.query(
                        ConfigEntry.TABLE_NAME,
                        projection,
                        ConfigEntry._ID + " = ?",
                        new String[]{String.valueOf(_id)},
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set the notification URI for the cursor to the one passed into the function. This
        // causes the cursor to register a content observer to watch for changes that happen to
        // this URI and any of it's descendants. By descendants, we mean any URI that begins
        // with this path.
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long _id;
        Uri returnUri;

        switch (sUriMatcher.match(uri)) {
            case OWNER:
                _id = db.insert(OwnerEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = OwnerEntry.buildOwnerUri(_id);
                } else {
                    throw new UnsupportedOperationException("Unable to insert rows into: " + uri);
                }
                break;
            case REPOSITORY:
                _id = db.insert(RepositoryEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = RepositoryEntry.buildRepositoryUri(_id);
                } else {
                    throw new UnsupportedOperationException("Unable to insert rows into: " + uri);
                }
                break;
            case CONFIG:
                _id = db.insert(ConfigEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = ConfigEntry.buildConfigUri(_id);
                } else {
                    throw new UnsupportedOperationException("Unable to insert rows into: " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Use this on the URI passed into the function to notify any observers that the uri has
        // changed.
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rows; // Number of rows effected

        switch (sUriMatcher.match(uri)) {
            case OWNER:
                rows = db.delete(OwnerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REPOSITORY:
                rows = db.delete(RepositoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CONFIG:
                rows = db.delete(ConfigEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because null could delete all rows:
        if (selection == null || rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rows;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rows;

        switch (sUriMatcher.match(uri)) {
            case OWNER:
                rows = db.update(OwnerEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case REPOSITORY:
                rows = db.update(RepositoryEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CONFIG:
                rows = db.update(ConfigEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rows != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rows;
    }
}