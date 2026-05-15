package com.example.gon;

import java.util.ArrayList;
import java.util.List;

public class Habit {
    private final String name;
    private final String description;
    private final String id;
    private boolean completedToday;
    private List<Category> categories = new ArrayList<>();

    public Habit(String name, String description, String id, boolean completedToday) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.completedToday = completedToday;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getId() { return id; }
    public boolean isCompletedToday() { return completedToday; }

    public void setCompletedToday(boolean completedToday) {
        this.completedToday = completedToday;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Habit{id='" + id + "', name='" + name + "', completed=" + completedToday + "}";
    }
}