package com.leinardi.mvp.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import com.leinardi.mvp.BuildConfig;

/**
 * Created by leinardi on 18/07/16.
 */
public class RepositoryContract {
    /**
     * The Content Authority is a name for the entire content provider, similar to the relationship
     * between a domain name and its website. A convenient string to use for content authority is
     * the package name for the app, since it is guaranteed to be unique on the device.
     */
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    /**
     * The content authority is used to create the base of all URIs which apps will use to
     * contact this content provider.
     */
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * A list of possible paths that will be appended to the base URI for each of the different
     * tables.
     */
    public static final String PATH_REPOSITORY = "repository";
    public static final String PATH_OWNER = "owner";
    public static final String PATH_CONFIG = "config";

    /**
     * Create one class for each table that handles all information regarding the table schema and
     * the URIs related to it.
     */
    public static final class RepositoryEntry implements BaseColumns {
        // Content URI represents the base location for the table
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_REPOSITORY).build();

        // These are special type prefixes that specify if a URI returns a list or a specific item
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_URI + "/" + PATH_REPOSITORY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_URI + "/" + PATH_REPOSITORY;

        // Define the table schema
        public static final String TABLE_NAME = "repositoryTable";
        public static final String COLUMN_OWNER_ID_FK = "ownerId";
        public static final String COLUMN_ID_GITHUB = "idGitHub";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_HTML_URL = "htmlUrl";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_FORK = "fork";

        // Define a function to build a URI to find a specific repository by it's identifier
        public static Uri buildRepositoryUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class OwnerEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_OWNER).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_URI + "/" + PATH_OWNER;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_URI + "/" + PATH_OWNER;

        public static final String TABLE_NAME = "ownerTable";
        public static final String COLUMN_ID_GITHUB = "idGitHubOwner";
        public static final String COLUMN_LOGIN = "login";
        public static final String COLUMN_HTML_URL = "htmlUrlOwner";

        public static Uri buildOwnerUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class ConfigEntry implements BaseColumns {
        public static final String DEFAULT_PAGE_NAME = "page";
        public static final String DEFAULT_PAGE_VALUE = "1";
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONFIG).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_URI + "/" + PATH_CONFIG;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_URI + "/" + PATH_CONFIG;

        public static final String TABLE_NAME = "configTable";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_VALUE = "VALUE";

        public static Uri buildConfigUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
