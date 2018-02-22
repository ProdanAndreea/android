package com.google.firebase.codelab.friendlychat;

/**
 * Created by Andreea on 03.01.2018.
 */

public class Trainer {

    private String id;
    private String name;

    public Trainer() {}

    public Trainer(String name) {
        this.name = name;
    }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }


}
