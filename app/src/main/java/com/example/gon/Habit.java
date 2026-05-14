package com.example.gon;

public class Habit {
    private final String name;
    private final String description;
    private final String id;
    private final boolean completedToday;

    public Habit(String name, String description, String id, boolean completedToday) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.completedToday = completedToday;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public boolean isCompletedToday() {
        return completedToday;
    }

    public String toString(){
        return "ID: " + this.id + " Name: " + this.name + " Description: " + this.description + " Is completed?: " + String.valueOf(completedToday);
    }
}