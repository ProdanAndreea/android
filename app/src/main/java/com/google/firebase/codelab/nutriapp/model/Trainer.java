package com.google.firebase.codelab.nutriapp.model;

/**
 * Created by Andreea on 03.01.2018.
 */
// https://medium.com/@kevalpatel2106/digesting-singleton-design-pattern-in-java-5d434f4f322
public class Trainer {

    private String id;
    private String name;

    private static Trainer trainerInstance;

    private Trainer() {
        //Prevent form the reflection api.
        if (trainerInstance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static Trainer getInstance(){
        if (trainerInstance == null){ //if there is no instance available... create new one
            trainerInstance = new Trainer();
        }
        return trainerInstance;
    }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }


}
