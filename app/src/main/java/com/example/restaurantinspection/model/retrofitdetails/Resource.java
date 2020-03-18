package com.example.restaurantinspection.model.retrofitdetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Resource {
    @SerializedName("format")
    @Expose
    private String format;

    @SerializedName("url")
    @Expose
    private String url;

    @SerializedName("last_modified")
    @Expose
    private String date_last_modified;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDate_last_modified() {
        return date_last_modified;
    }

    public void setDate_last_modified(String date_last_modified) {
        this.date_last_modified = date_last_modified;
    }

    public Resource(String format, String url, String date_last_modified) {
        this.format = format;
        this.url = url;
        this.date_last_modified = date_last_modified;
    }

    public Resource() {

    }

    @Override
    public String toString() {
        return "Resource{" +
                "format='" + format + '\'' +
                ", url='" + url + '\'' +
                ", date_last_modified='" + date_last_modified + '\'' +
                '}';
    }
}
