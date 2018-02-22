package com.google.firebase.codelab.friendlychat;

/**
 * Created by Andreea on 29.12.2017.
 *
 * This class is used to store all the data of a specific food
 */

public class Food {
    private String id;
    private String name;
    private Float prot;
    private Float carb;
    private Float cal;
    private Float fat;
    private String allergy = "";
    private String photo;

    private Float quantity;

    public Food() {}

    // when you need just the food's data
    public Food(String name, Float cal, Float carb, Float prot, Float fat) {
        this.name = name;
        this.prot = prot;
        this.carb = carb;
        this.cal = cal;
        this.fat = fat;
    }

    // for Foods, when you have to display the food's data together with its picture
    public Food(String name,  Float cal, Float carb, Float prot, Float fat, String allergy, String photo) {
        this.name = name;
        this.prot = prot;
        this.carb = carb;
        this.cal = cal;
        this.fat = fat;
        this.allergy = allergy;
        this.photo = photo;
    }

    // when you need just the food's data, with its quantity
    public Food(String name, Float quantity, Float cal, Float carb, Float prot, Float fat) {
        this.name = name;
        this.prot = prot;
        this.carb = carb;
        this.cal = cal;
        this.fat = fat;
        this.quantity = quantity;
    }


    // seters and getters for the food's data
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Float getCarb() { return carb; }
    public void setCarb(Float carb) { this.carb = carb; }
    public Float getProt() { return prot; }
    public void setProt(Float prot) { this.prot = prot; }
    public Float getCal() { return cal; }
    public void setCal(Float cal) { this.cal = cal; }
    public Float getFat() { return fat; }
    public void setFat(Float fat) { this.fat = fat; }


    public String getPhoto() {
        return photo;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public Float getQuantity() {
        return quantity;
    }
    public void setQuantity(Float quantity) {
        this.quantity = quantity;
    }

    public void setAllergy(String allergies) { this.allergy = allergies; }
    public String getAllergy() { return allergy; }


}
