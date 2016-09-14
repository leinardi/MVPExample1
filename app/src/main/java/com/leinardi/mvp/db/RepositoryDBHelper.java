package com.leinardi.mvp.db;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import com.leinardi.mvp.model.Owner;
import com.leinardi.mvp.model.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.leinardi.mvp.provider.RepositoryContract.ConfigEntry;
import static com.leinardi.mvp.provider.RepositoryContract.OwnerEntry;
import static com.leinardi.mvp.provider.RepositoryContract.RepositoryEntry;

/**
 * Created by leinardi on 18/07/16.
 */

public class RepositoryDBHelper extends SQLiteOpenHelper {
    private static final String TAG = RepositoryDBHelper.class.getSimpleName();

    /**
     * The name of the database on the device.
     */
    private static final String DATABASE_NAME = "repositoriesDatabase";

    /**
     * Defines the database version. This variable must be incremented in order for onUpdate to
     * be called when necessary.
     */
    private static final int DATABASE_VERSION = 1;

    private static RepositoryDBHelper sInstance;

    public static synchronized RepositoryDBHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new RepositoryDBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private RepositoryDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Called when the database connection is being configured.
    // Configure database config for things like foreign key support, write-ahead logging, etc.
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // Called when the database is created for the FIRST time.
    // If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
    @Override
    public void onCreate(SQLiteDatabase db) {
        addOwnerTable(db);
        addRepositoryTable(db);
        addConfigTable(db);
        addDefaultConfigValues(db);
    }

    /**
     * Inserts the owner table into the database.
     *
     * @param db The SQLiteDatabase the table is being inserted into.
     */
    private void addOwnerTable(SQLiteDatabase db) {
        String CREATE_OWNERS_TABLE = "CREATE TABLE " + OwnerEntry.TABLE_NAME +
                "(" +
                OwnerEntry.COLUMN_ID_GITHUB + " INTEGER PRIMARY KEY," +
                OwnerEntry.COLUMN_LOGIN + " TEXT," +
                OwnerEntry.COLUMN_HTML_URL + " TEXT" +
                ")";

        db.execSQL(CREATE_OWNERS_TABLE);
    }

    /**
     * Inserts the repository table into the database.
     *
     * @param db The SQLiteDatabase the table is being inserted into.
     */
    private void addRepositoryTable(SQLiteDatabase db) {
        String CREATE_REPOSITORIES_TABLE = "CREATE TABLE " + RepositoryEntry.TABLE_NAME +
                "(" +
                RepositoryEntry._ID + " INTEGER PRIMARY KEY," + // Define a primary key
                RepositoryEntry.COLUMN_OWNER_ID_FK + " INTEGER REFERENCES " + OwnerEntry.TABLE_NAME + "(" + OwnerEntry.COLUMN_ID_GITHUB + ")," + // Define a foreign key
                RepositoryEntry.COLUMN_ID_GITHUB + " INTEGER NOT NULL," +
                RepositoryEntry.COLUMN_NAME + " TEXT," +
                RepositoryEntry.COLUMN_HTML_URL + " TEXT," +
                RepositoryEntry.COLUMN_DESCRIPTION + " TEXT," +
                RepositoryEntry.COLUMN_FORK + " SHORT" +
                ")";
        db.execSQL(CREATE_REPOSITORIES_TABLE);
    }

    /**
     * Inserts the config table into the database.
     *
     * @param db The SQLiteDatabase the table is being inserted into.
     */
    private void addConfigTable(SQLiteDatabase db) {
        String CREATE_OWNERS_TABLE = "CREATE TABLE " + ConfigEntry.TABLE_NAME +
                "(" +
                ConfigEntry._ID + " INTEGER PRIMARY KEY," + // Define a primary key
                ConfigEntry.COLUMN_NAME + " TEXT," +
                ConfigEntry.COLUMN_VALUE + " TEXT" +
                ")";

        db.execSQL(CREATE_OWNERS_TABLE);
    }

    /**
     * Inserts the default config values into the table.
     *
     * @param db The SQLiteDatabase the table is being inserted into.
     */
    private void addDefaultConfigValues(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(ConfigEntry.COLUMN_NAME, ConfigEntry.DEFAULT_PAGE_NAME);
        values.put(ConfigEntry.COLUMN_VALUE, ConfigEntry.DEFAULT_PAGE_VALUE);
        db.insert(ConfigEntry.TABLE_NAME, null, values);
    }

    // Called when the database needs to be upgraded.
    // This method will only be called if a database already exists on disk with the same DATABASE_NAME,
    // but the DATABASE_VERSION is different than the version of the database that exists on disk.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            db.execSQL("DROP TABLE IF EXISTS " + RepositoryEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + OwnerEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + ConfigEntry.TABLE_NAME);
            onCreate(db);
        }
    }

    // Insert a repository into the database
    public long addRepository(Repository repository) {
        // Create and/or open the database for writing
        SQLiteDatabase db = getWritableDatabase();

        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        db.beginTransaction();
        long repositoryId = -1;
        try {
            // The owner might already exist in the database (i.e. the same owner created multiple repositories).
            long ownerId = addOrUpdateOwner(repository.getOwner());

            ContentValues values = new ContentValues();
            values.put(RepositoryEntry.COLUMN_OWNER_ID_FK, ownerId);
            values.put(RepositoryEntry.COLUMN_ID_GITHUB, repository.getIdGitHub());
            values.put(RepositoryEntry.COLUMN_NAME, repository.getName());
            values.put(RepositoryEntry.COLUMN_HTML_URL, repository.getHtmlUrl());
            values.put(RepositoryEntry.COLUMN_DESCRIPTION, repository.getDescription());
            values.put(RepositoryEntry.COLUMN_FORK, repository.getFork());

            // Notice how we haven't specified the primary key. SQLite auto increments the primary key column.
            repositoryId = db.insertOrThrow(RepositoryEntry.TABLE_NAME, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add repository to database");
        } finally {
            db.endTransaction();
        }
        return repositoryId;
    }

    // Insert or update a owner in the database
    // Since SQLite doesn't support "upsert" we need to fall back on an attempt to UPDATE (in case the
    // owner already exists) optionally followed by an INSERT (in case the owner does not already exist).
    // Unfortunately, there is a bug with the insertOnConflict method
    // (https://code.google.com/p/android/issues/detail?id=13045) so we need to fall back to the more
    // verbose option of querying for the owner's primary key if we did an update.
    public long addOrUpdateOwner(Owner owner) {
        // The database connection is cached so it's not expensive to call getWriteableDatabase() multiple times.
        SQLiteDatabase db = getWritableDatabase();
        long ownerId = -1;

        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(OwnerEntry.COLUMN_ID_GITHUB, owner.getIdGitHub());
            values.put(OwnerEntry.COLUMN_LOGIN, owner.getLogin());
            values.put(OwnerEntry.COLUMN_HTML_URL, owner.getHtmlUrl());

            // First try to update the owner in case the owner already exists in the database
            // This assumes ownerNames are unique
            int rows = db.update(OwnerEntry.TABLE_NAME, values, OwnerEntry.COLUMN_ID_GITHUB + "= ?", new String[]{owner.getLogin()});

            // Check if update succeeded
            if (rows == 1) {
                // Get the primary key of the owner we just updated
                String ownersSelectQuery = String.format("SELECT %s FROM %s WHERE %s = ?",
                        OwnerEntry.COLUMN_ID_GITHUB, OwnerEntry.TABLE_NAME, OwnerEntry.COLUMN_ID_GITHUB);
                Cursor cursor = db.rawQuery(ownersSelectQuery, new String[]{String.valueOf(owner.getLogin())});
                try {
                    if (cursor.moveToFirst()) {
                        ownerId = cursor.getInt(0);
                        db.setTransactionSuccessful();
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } else {
                // owner with this ownerName did not already exist, so insert new owner
                ownerId = db.insertOrThrow(OwnerEntry.TABLE_NAME, null, values);
                db.setTransactionSuccessful();
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to add or update owner");
        } finally {
            db.endTransaction();
        }
        return ownerId;
    }

    // Get all repositories in the database
    public List<Repository> getAllRepositories() {
        List<Repository> repositories = new ArrayList<>();

        // SELECT * FROM REPOSITORIES
        // LEFT OUTER JOIN OWNERS
        // ON REPOSITORIES.RepositoryContract.RepositoryEntry.COLUMN_OWNER_ID_FK = OWNERS.OwnerEntry.COLUMN_ID
        String REPOSITORIES_SELECT_QUERY =
                String.format("SELECT * FROM %s LEFT OUTER JOIN %s ON %s.%s = %s.%s",
                        RepositoryEntry.TABLE_NAME,
                        OwnerEntry.TABLE_NAME,
                        RepositoryEntry.TABLE_NAME, RepositoryEntry.COLUMN_OWNER_ID_FK,
                        OwnerEntry.TABLE_NAME, OwnerEntry.COLUMN_ID_GITHUB);

        // "getReadableDatabase()" and "getWriteableDatabase()" return the same object (except under low
        // disk space scenarios)
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(REPOSITORIES_SELECT_QUERY, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    Owner newOwner = new Owner(
                            cursor.getString(cursor.getColumnIndex(OwnerEntry.COLUMN_LOGIN)),
                            cursor.getInt(cursor.getColumnIndex(OwnerEntry.COLUMN_ID_GITHUB)),
                            cursor.getString(cursor.getColumnIndex(OwnerEntry.COLUMN_HTML_URL))
                    );

                    Repository newRepository = new Repository(
                            cursor.getInt(cursor.getColumnIndex(RepositoryEntry.COLUMN_ID_GITHUB)),
                            cursor.getString(cursor.getColumnIndex(RepositoryEntry.COLUMN_NAME)),
                            newOwner,
                            cursor.getString(cursor.getColumnIndex(RepositoryEntry.COLUMN_HTML_URL)),
                            cursor.getString(cursor.getColumnIndex(RepositoryEntry.COLUMN_DESCRIPTION)),
                            cursor.getShort(cursor.getColumnIndex(RepositoryEntry.COLUMN_FORK)) != 0
                    );
                    repositories.add(newRepository);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get repositories from database");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return repositories;
    }

    // Update the owner's HTML url
    public int updateHtmlUrl(Owner owner) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(OwnerEntry.COLUMN_HTML_URL, owner.getHtmlUrl());

        // Updating HTML url for owner with that login
        return db.update(OwnerEntry.TABLE_NAME, values, OwnerEntry.COLUMN_ID_GITHUB + " = ?",
                new String[]{String.valueOf(owner.getLogin())});
    }

    // Delete all repositories and owners in the database
    public void deleteAllRepositorysAndOwners() {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            // Order of deletions is important when foreign key relationships exist.
            db.delete(RepositoryEntry.TABLE_NAME, null, null);
            db.delete(OwnerEntry.TABLE_NAME, null, null);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to delete all repositories and owners");
        } finally {
            db.endTransaction();
        }
    }
}