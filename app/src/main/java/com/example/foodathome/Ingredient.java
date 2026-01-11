package com.example.foodathome;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;

public class Ingredient {
    @Exclude
    private String id;
    private String name;
    private float price;
    private float unitSize;
    @Exclude
    private float amount;


    public Ingredient() { }

    public Ingredient(String name) {
        this.name = name;
        this.price = 0;
        this.unitSize = 0;
        this.amount = 0;
    }

    public Ingredient(String name,float price,float unitSize) {
        this.name = name;
        this.price = price;
        this.unitSize = unitSize;
        this.amount = 0;
    }

    public void copy(Ingredient ingredient) {
        this.id = ingredient.id;
        this.name = ingredient.name;
        this.price = ingredient.price;
        this.unitSize = ingredient.unitSize;
        this.amount = ingredient.amount;
    }
    public String getId() {return id;}
    public void setId(String id) {this.id =id;}
    public String getName() { return name; }
    public float getPrice() { return price; }
    public void setPrice(float price) { this.price=price; }
    public float getAmount() { return  amount; }
    public void setAmount(float amount) { this.amount = amount; }
    public float getUnitSize() { return this.unitSize; }
    public void setUnitSize(float size) { this.unitSize = size; }

    @Override
    public String toString() {
        return String.format("%s, %f, %f",name,price,amount);
    }

    public float calcPrice() {
        if(unitSize != 0)
            return price*amount/unitSize;
        else
            return price*amount;
    }
}
