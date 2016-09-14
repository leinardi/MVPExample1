
package com.leinardi.mvp.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.leinardi.mvp.provider.RepositoryContract;

public class Repository implements Parcelable {
    private static final String TAG = Repository.class.getSimpleName();
    @SerializedName("id")
    @Expose
    private Integer idGitHub;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("owner")
    @Expose
    private Owner owner;
    @SerializedName("html_url")
    @Expose
    private String htmlUrl;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("fork")
    @Expose
    private Boolean fork;

    public Repository(Integer idGitHub, String name, Owner owner, String htmlUrl, String description, Boolean fork) {
        this.idGitHub = idGitHub;
        this.name = name;
        this.owner = owner;
        this.htmlUrl = htmlUrl;
        this.description = description;
        this.fork = fork;
    }

    public Repository(Cursor cursor) {
        try {
            if (!cursor.isClosed()) {
                Owner newOwner = new Owner(
                        cursor.getString(cursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_LOGIN)),
                        cursor.getInt(cursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_ID_GITHUB)),
                        cursor.getString(cursor.getColumnIndex(RepositoryContract.OwnerEntry.COLUMN_HTML_URL))
                );

                idGitHub = cursor.getInt(cursor.getColumnIndex(RepositoryContract.RepositoryEntry.COLUMN_ID_GITHUB));
                name = cursor.getString(cursor.getColumnIndex(RepositoryContract.RepositoryEntry.COLUMN_NAME));
                owner = newOwner;
                htmlUrl = cursor.getString(cursor.getColumnIndex(RepositoryContract.RepositoryEntry.COLUMN_HTML_URL));
                description = cursor.getString(cursor.getColumnIndex(RepositoryContract.RepositoryEntry.COLUMN_DESCRIPTION));
                fork = cursor.getShort(cursor.getColumnIndex(RepositoryContract.RepositoryEntry.COLUMN_FORK)) != 0;
            }
        } catch (Exception e) {
            Log.d(TAG, "Error while trying to get repositories from database");
        }
    }

    public Integer getIdGitHub() {
        return idGitHub;
    }

    public String getName() {
        return name;
    }

    public Owner getOwner() {
        return owner;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getFork() {
        return fork;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.idGitHub);
        dest.writeString(this.name);
        dest.writeParcelable(this.owner, flags);
        dest.writeString(this.htmlUrl);
        dest.writeString(this.description);
        dest.writeValue(this.fork);
    }

    public Repository() {
    }

    protected Repository(Parcel in) {
        this.idGitHub = (Integer) in.readValue(Integer.class.getClassLoader());
        this.name = in.readString();
        this.owner = in.readParcelable(Owner.class.getClassLoader());
        this.htmlUrl = in.readString();
        this.description = in.readString();
        this.fork = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Parcelable.Creator<Repository> CREATOR = new Parcelable.Creator<Repository>() {
        @Override
        public Repository createFromParcel(Parcel source) {
            return new Repository(source);
        }

        @Override
        public Repository[] newArray(int size) {
            return new Repository[size];
        }
    };
}
