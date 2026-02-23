package com.example.foodathome;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import java.util.Objects;

public class Ingredient {
    private String id;
    private String name;
    private float price;
    private float unitSize;
    private float amount;


    public Ingredient() { }

    public Ingredient(String name) {
        this.name = name;
        this.price = 0;
        this.unitSize = 0;
        this.amount = 0;
        this.id = "";
    }

    public Ingredient(String name,float price,float unitSize) {
        this.name = name;
        this.price = price;
        this.unitSize = unitSize;
        this.amount = 0;
        this.id = "";
    }

    public void copy(Ingredient ingredient) {
        this.id = ingredient.id;
        this.name = ingredient.name;
        this.price = ingredient.price;
        this.unitSize = ingredient.unitSize;
    }

    @Exclude
    public String getId() {return id;}
    public void setId(String id) {this.id =id;}
    public String getName() { return name; }
    public void setName(String name) { this.name = name;}
    public float getPrice() { return price; }
    public void setPrice(float price) { this.price=price; }

    @Exclude
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
