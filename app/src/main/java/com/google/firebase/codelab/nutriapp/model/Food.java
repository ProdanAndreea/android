package com.google.firebase.codelab.nutriapp.model;

/**
 * Created by Andreea on 29.12.2017.
 *
 * This class is used to store all the data of a specific food
 */
// https://jlordiales.me/2012/12/13/the-builder-pattern-in-practice/
public class Food {
    private String id;
    private  String name;
    private  Float prot;
    private  Float carb;
    private  Float cal;
    private  Float fat;
    private  String allergy;
    private  String photo;
    private  Float quantity;

    public Food() {}

    private Food(Builder builder) {
        this.name = builder.name;
        this.cal = builder.cal;
        this.carb = builder.carb;
        this.prot = builder.prot;
        this.fat = builder.fat;
        this.photo = builder.photo;
        this.quantity = builder.quantity;
        this.allergy = builder.allergy;
    }

    // seters and getters for the food's data
    public String getId() {
        return id;
    }
    public void setId(String id) { this.id = id;}
    public String getName() { return name; }
    public Float getCarb() { return carb; }
    public Float getProt() { return prot; }
    public Float getCal() { return cal; }
    public Float getFat() { return fat; }
    public String getPhoto() {
        return photo;
    }
    public Float getQuantity() {
        return quantity;
    }
    public String getAllergy() { return allergy; }

    public void setName(String name) { this.name = name; }

    public static class Builder {
        private String id;
        private String name;
        private Float prot;
        private Float carb;
        private Float cal;
        private Float fat;
        private String allergy = "";
        private String photo;
        private Float quantity;

        public Builder(String name, Float cal, Float carb, Float prot, Float fat) {
            this.name = name;
            this.cal = cal;
            this.carb = carb;
            this.prot = prot;
            this.fat = fat;
        }

        public Builder() {}

        public Builder setAllergy(String allergy) {
            this.allergy = allergy;
            return this;
        }

        public Builder setPhoto(String photo) {
            this.photo = photo;
            return this;
        }

        public Builder setQuantity(Float quantity) {
            this.quantity = quantity;
            return this;
        }

        public Food build() {
            return new Food(this);
        }
    }

}
