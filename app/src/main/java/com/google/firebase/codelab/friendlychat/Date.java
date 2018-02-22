package com.google.firebase.codelab.friendlychat;

/**
 * Created by Andreea on 02.01.2018.
 */

public class Date {
    private String date;
    private String id;

    public Date() {}

    public Date(String date) {
        this.date = date;
    }

    public void setDate(String date) { this.date = date; }
    public String getDate() { return date; }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}
