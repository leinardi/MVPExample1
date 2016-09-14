package com.leinardi.mvp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

import com.leinardi.mvp.provider.RepositoryProvider;

import java.util.Map;
import java.util.Set;

import static com.leinardi.mvp.provider.RepositoryContract.CONTENT_AUTHORITY;
import static com.leinardi.mvp.provider.RepositoryContract.ConfigEntry;
import static com.leinardi.mvp.provider.RepositoryContract.OwnerEntry;
import static com.leinardi.mvp.provider.RepositoryContract.RepositoryEntry;

/**
 * Created by leinardi on 15/07/16.
 */

public class RepositoryProviderTest extends ProviderTestCase2<RepositoryProvider> {

    private static final int TEST_OWNER_ID_GITHUB = 27901;
    private static final String TEST_OWNER_LOGIN = "square";
    private static final String TEST_OWNER_HTML_URL = "https://owner.html.url";
    private static final String TEST_UPDATE_OWNER_LOGIN = "leinardi";
    private static final int TEST_REPOSITORY_ID_GITHUB = 2399148;
    private static final String TEST_REPOSITORY_NAME = "absurdity";
    private static final int TEST_REPOSITORY_OWNER = TEST_OWNER_ID_GITHUB;
    private static final String TEST_REPOSITORY_HTML_URL = "https://repo.html.url";
    private static final String TEST_REPOSITORY_DESCRIPTION = "Repo description";
    private static final int TEST_REPOSITORY_FORK = 1;
    private static final String TEST_UPDATE_REPOSITORY_NAME = "kitchentimer";
    private static final String TEST_CONFIG_NAME = "page";
    private static final String TEST_CONFIG_VALUE = "1";
    private static final String TEST_UPDATE_CONFIG_VALUE = "2";

    public RepositoryProviderTest() {
        super(RepositoryProvider.class, CONTENT_AUTHORITY);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testDeleteAllRecords();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        testDeleteAllRecords();
    }

    public void testDeleteAllRecords() {
        // Delete repositories
        mContext.getContentResolver().delete(
                RepositoryEntry.CONTENT_URI,
                null,
                null
        );

        // Delete owners
        mContext.getContentResolver().delete(
                OwnerEntry.CONTENT_URI,
                null,
                null
        );

        // Delete configs
        mContext.getContentResolver().delete(
                ConfigEntry.CONTENT_URI,
                null,
                null
        );

        // Ensure repositories were deleted
        Cursor cursor = mContext.getContentResolver().query(
                RepositoryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        // Ensure owners were deleted
        cursor = mContext.getContentResolver().query(
                OwnerEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        // Ensure configs were deleted
        cursor = mContext.getContentResolver().query(
                ConfigEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    public void testGetType() {
        // content_authority = "content://com.leinardi.mvpexample1.provider/:

        //-- OWNER --//
        // content_authority + owner
        String type = mContext.getContentResolver().getType(OwnerEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.leinardi.mvpexample1.provider/owner
        assertEquals(OwnerEntry.CONTENT_TYPE, type);

        //-- OWNER_ID --//
        // content_authority + owner/id
        type = mContext.getContentResolver().getType(OwnerEntry.buildOwnerUri(0));
        // vnd.android.cursor.item/com.leinardi.mvpexample1.provider/owner
        assertEquals(OwnerEntry.CONTENT_ITEM_TYPE, type);

        //-- REPOSITORY --//
        type = mContext.getContentResolver().getType(RepositoryEntry.CONTENT_URI);
        assertEquals(RepositoryEntry.CONTENT_TYPE, type);

        //-- REPOSITORY_ID --//
        type = mContext.getContentResolver().getType(RepositoryEntry.buildRepositoryUri(0));
        assertEquals(RepositoryEntry.CONTENT_ITEM_TYPE, type);

        //-- CONFIG --//
        type = mContext.getContentResolver().getType(ConfigEntry.CONTENT_URI);
        assertEquals(ConfigEntry.CONTENT_TYPE, type);

        //-- CONFIG_ID --//
        type = mContext.getContentResolver().getType(ConfigEntry.buildConfigUri(0));
        assertEquals(ConfigEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testInsertReadOwner() {
        ContentValues ownerContentValues = getOwnerContentValues();
        Uri ownerInsertUri = mContext.getContentResolver().insert(OwnerEntry.CONTENT_URI, ownerContentValues);
        long ownerRowId = ContentUris.parseId(ownerInsertUri);

        // Verify we inserted a row
        assertTrue(ownerRowId > 0);

        // Query for all rows and validate cursor
        Cursor ownerCursor = mContext.getContentResolver().query(
                OwnerEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        validateCursor(ownerCursor, ownerContentValues);
        ownerCursor.close();

        // Query for specific row and validate cursor
        ownerCursor = mContext.getContentResolver().query(
                OwnerEntry.buildOwnerUri(ownerRowId),
                null,
                null,
                null,
                null
        );
        validateCursor(ownerCursor, ownerContentValues);
        ownerCursor.close();
    }

    public void testInsertReadRepository() {
        // We first insert a Owner
        // No need to verify this, we already have a test for inserting owner
        ContentValues ownerContentValues = getOwnerContentValues();
        Uri ownerInsertUri = mContext.getContentResolver().insert(OwnerEntry.CONTENT_URI, ownerContentValues);
        long ownerRowId = ContentUris.parseId(ownerInsertUri);

        // Verify we got a row back
        assertTrue(ownerRowId > 0);

        ContentValues repositoryContentValues = getRepositoryContentValues();
        Uri repositoryInsertUri = mContext.getContentResolver().insert(RepositoryEntry.CONTENT_URI, repositoryContentValues);
        long repositoryRowId = ContentUris.parseId(repositoryInsertUri);

        // Verify we got a row back
        assertTrue(repositoryRowId > 0);

        // Query for all and validate
        Cursor repositoryCursor = mContext.getContentResolver().query(
                RepositoryEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        validateCursor(repositoryCursor, repositoryContentValues);
        repositoryCursor.close();

        repositoryCursor = mContext.getContentResolver().query(
                RepositoryEntry.buildRepositoryUri(repositoryRowId),
                null,
                null,
                null,
                null
        );
        validateCursor(repositoryCursor, repositoryContentValues);
        repositoryCursor.close();
    }

    public void testInsertReadConfig() {
        ContentValues configContentValues = getConfigContentValues();
        Uri configInsertUri = mContext.getContentResolver().insert(ConfigEntry.CONTENT_URI, configContentValues);
        long configRowId = ContentUris.parseId(configInsertUri);

        // Verify we inserted a row
        assertTrue(configRowId > 0);

        // Query for all rows and validate cursor
        Cursor configCursor = mContext.getContentResolver().query(
                ConfigEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        validateCursor(configCursor, configContentValues);
        configCursor.close();

        // Query for specific row and validate cursor
        configCursor = mContext.getContentResolver().query(
                ConfigEntry.buildConfigUri(configRowId),
                null,
                null,
                null,
                null
        );
        validateCursor(configCursor, configContentValues);
        configCursor.close();
    }

    public void testUpdateOwner() {
        // Insert the owner first.
        // No need to verify, we have a test for that.
        ContentValues ownerContentValues = getOwnerContentValues();
        Uri ownerInsertUri = mContext.getContentResolver().insert(OwnerEntry.CONTENT_URI, ownerContentValues);
        long ownerRowId = ContentUris.parseId(ownerInsertUri);

        // UpdateValues
        ContentValues updatedOwnerContentValues = new ContentValues(ownerContentValues);
        updatedOwnerContentValues.put(OwnerEntry.COLUMN_ID_GITHUB, ownerRowId);
        updatedOwnerContentValues.put(OwnerEntry.COLUMN_LOGIN, TEST_UPDATE_OWNER_LOGIN);
        mContext.getContentResolver().update(
                OwnerEntry.CONTENT_URI,
                updatedOwnerContentValues,
                OwnerEntry.COLUMN_ID_GITHUB + " = ?",
                new String[]{String.valueOf(ownerRowId)}
        );

        // Query for that specific row and verify it
        Cursor ownerCursor = mContext.getContentResolver().query(
                OwnerEntry.buildOwnerUri(ownerRowId),
                null,
                null,
                null,
                null
        );
        validateCursor(ownerCursor, updatedOwnerContentValues);
        ownerCursor.close();
    }

    public void testUpdateRepository() {
        // We first insert a Owner
        // No need to verify this, we already have a test for inserting owner
        ContentValues ownerContentValues = getOwnerContentValues();
        mContext.getContentResolver().insert(OwnerEntry.CONTENT_URI, ownerContentValues);

        ContentValues repositoryContentValues = getRepositoryContentValues();
        Uri repositoryInsertUri = mContext.getContentResolver().insert(RepositoryEntry.CONTENT_URI, repositoryContentValues);
        long repositoryRowId = ContentUris.parseId(repositoryInsertUri);

        // Update
        ContentValues updatedRepositoryContentValues = new ContentValues(repositoryContentValues);
        updatedRepositoryContentValues.put(RepositoryEntry._ID, repositoryRowId);
        updatedRepositoryContentValues.put(RepositoryEntry.COLUMN_NAME, TEST_UPDATE_REPOSITORY_NAME);
        mContext.getContentResolver().update(
                RepositoryEntry.CONTENT_URI,
                updatedRepositoryContentValues,
                RepositoryEntry._ID + " = ?",
                new String[]{String.valueOf(repositoryRowId)}
        );

        Cursor repositoryCursor = mContext.getContentResolver().query(
                RepositoryEntry.buildRepositoryUri(repositoryRowId),
                null,
                null,
                null,
                null
        );
        validateCursor(repositoryCursor, updatedRepositoryContentValues);
        repositoryCursor.close();
    }

    public void testUpdateConfig() {
        // Insert the config first.
        // No need to verify, we have a test for that.
        ContentValues configContentValues = getConfigContentValues();
        Uri configInsertUri = mContext.getContentResolver().insert(ConfigEntry.CONTENT_URI, configContentValues);
        long configRowId = ContentUris.parseId(configInsertUri);


        // UpdateValues
        ContentValues updatedConfigContentValues = new ContentValues(configContentValues);
        updatedConfigContentValues.put(RepositoryEntry._ID, configRowId);
        updatedConfigContentValues.put(ConfigEntry.COLUMN_NAME, TEST_CONFIG_NAME);
        updatedConfigContentValues.put(ConfigEntry.COLUMN_VALUE, TEST_UPDATE_CONFIG_VALUE);
        mContext.getContentResolver().update(
                ConfigEntry.CONTENT_URI,
                updatedConfigContentValues,
                ConfigEntry._ID + " = ?",
                new String[]{String.valueOf(configRowId)}
        );

        // Query for that specific row and verify it
        Cursor configCursor = mContext.getContentResolver().query(
                ConfigEntry.buildConfigUri(configRowId),
                null,
                null,
                null,
                null
        );
        validateCursor(configCursor, updatedConfigContentValues);
        configCursor.close();
    }

    private ContentValues getOwnerContentValues() {
        ContentValues values = new ContentValues();
        values.put(OwnerEntry.COLUMN_ID_GITHUB, TEST_OWNER_ID_GITHUB);
        values.put(OwnerEntry.COLUMN_LOGIN, TEST_OWNER_LOGIN);
        values.put(OwnerEntry.COLUMN_HTML_URL, TEST_OWNER_HTML_URL);
        return values;
    }

    private ContentValues getRepositoryContentValues() {
        ContentValues values = new ContentValues();
        values.put(RepositoryEntry.COLUMN_ID_GITHUB, TEST_REPOSITORY_ID_GITHUB);
        values.put(RepositoryEntry.COLUMN_OWNER_ID_FK, TEST_REPOSITORY_OWNER);
        values.put(RepositoryEntry.COLUMN_NAME, TEST_REPOSITORY_NAME);
        values.put(RepositoryEntry.COLUMN_HTML_URL, TEST_REPOSITORY_HTML_URL);
        values.put(RepositoryEntry.COLUMN_DESCRIPTION, TEST_REPOSITORY_DESCRIPTION);
        values.put(RepositoryEntry.COLUMN_FORK, TEST_REPOSITORY_FORK);
        return values;
    }

    private ContentValues getConfigContentValues() {
        ContentValues values = new ContentValues();
        values.put(ConfigEntry.COLUMN_NAME, TEST_CONFIG_NAME);
        values.put(ConfigEntry.COLUMN_VALUE, TEST_CONFIG_VALUE);
        return values;
    }

    private void validateCursor(Cursor valueCursor, ContentValues expectedValues) {
        assertTrue(valueCursor.moveToFirst());

        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse(idx == -1);
            switch (valueCursor.getType(idx)) {
                case Cursor.FIELD_TYPE_FLOAT:
                    assertEquals(entry.getValue(), valueCursor.getDouble(idx));
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    assertEquals(Integer.parseInt(entry.getValue().toString()), valueCursor.getInt(idx));
                    break;
                case Cursor.FIELD_TYPE_STRING:
                    assertEquals(entry.getValue(), valueCursor.getString(idx));
                    break;
                default:
                    assertEquals(entry.getValue().toString(), valueCursor.getString(idx));
                    break;
            }
        }
        valueCursor.close();
    }
}
