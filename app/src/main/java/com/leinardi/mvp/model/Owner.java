
package com.leinardi.mvp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Owner implements Parcelable {
    @SerializedName("login")
    @Expose
    private String login;
    @SerializedName("id")
    @Expose
    private Integer idGitHub;
    @SerializedName("html_url")
    @Expose
    private String htmlUrl;

    public Owner(String login, Integer idGitHub, String htmlUrl) {
        this.login = login;
        this.idGitHub = idGitHub;
        this.htmlUrl = htmlUrl;
    }

    public String getLogin() {
        return login;
    }

    public Integer getIdGitHub() {
        return idGitHub;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.login);
        dest.writeValue(this.idGitHub);
        dest.writeString(this.htmlUrl);
    }

    public Owner() {
    }

    protected Owner(Parcel in) {
        this.login = in.readString();
        this.idGitHub = (Integer) in.readValue(Integer.class.getClassLoader());
        this.htmlUrl = in.readString();
    }

    public static final Parcelable.Creator<Owner> CREATOR = new Parcelable.Creator<Owner>() {
        @Override
        public Owner createFromParcel(Parcel source) {
            return new Owner(source);
        }

        @Override
        public Owner[] newArray(int size) {
            return new Owner[size];
        }
    };
}
