package com.google.firebase.codelab.friendlychat;

/**
 * Created by Andreea on 03.01.2018.
 */

public class Client {

    private String id;
    private String name;
    private String activity;
    private String scope;
    private String sex;
    private String weight;
    private String allergies;


    private Float cal = 0f;
    private Float carb = 0f;
    private Float prot = 0f;
    private Float fat = 0f;

    public Client() {}

    public Client(String name, String activity, String scope, String sex, String weight, String allergies) {
        this.name = name;
        this.activity = activity;
        this.scope = scope;
        this.sex = sex;
        this.weight = weight;
        this.allergies = allergies;
    }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }

    public void setId(String id) { this.id = id; }
    public String getId() { return id; }

    public void setActivity(String activity) { this.activity = activity; }
    public String getActivity() { return activity; }
    public void setScope(String scope) { this.scope = scope; }
    public String getScope() { return scope; }
    public void setSex(String sex) { this.sex = sex; }
    public String getSex() { return sex; }
    public void setWeight(String weight) { this.weight = weight; }
    public String getWeight() { return weight; }

    public void setCal(Float cal) { this.cal = cal; }
    public Float getCal() { return cal; }

    public void setCarb(Float carb) { this.carb = carb; }
    public Float getCarb() { return carb; }

    public void setProt(Float prot) { this.prot = prot; }
    public Float getProt() { return prot; }

    public void setFat(Float fat) { this.fat = fat; }
    public Float getFat() { return fat; }

    public void setAllergies(String allergies) { this.allergies = allergies; }
    public String getAllergies() { return allergies; }


    public void calculateData() {
        if (activity.equals("sedentary")) {
            cal = 31 * Float.parseFloat(weight);
        } else if (activity.equals("recreational")) {
            cal = 35 * Float.parseFloat(weight);
        } else if (activity.equals("athlete")) {
            cal = 40 * Float.parseFloat(weight);
        } else if (activity.equals("powerlifter")) {
            cal = 60 * Float.parseFloat(weight);
        }

        if (scope.equals("lose")) {
            cal -= 250;
        } else if (scope.equals("gain")) {
            cal += 250;
        }

        prot = Float.parseFloat(weight) * 2;
        fat = (cal * 0.25f) / 9;

        carb = cal - ((prot * 4) + (cal * 0.25f));
        carb /= 4;
    }
}
