package com.example.gon.Entities;

import java.util.ArrayList;
import java.util.List;

public class Habit {
    private final String name;
    private final String description;
    private final String id;
    private boolean completed_today;
    private List<Category> categories = new ArrayList<>();

    public Habit(String name, String description, String id, boolean completed_today) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.completed_today = completed_today;
    }

    public String get_name() { return name; }
    public String get_description() { return description; }
    public String get_id() { return id; }
    public boolean is_completed_today() { return completed_today; }

    public void set_completed_today(boolean completed_today) {
        this.completed_today = completed_today;
    }

    public List<Category> get_categories() {
        return categories;
    }

    public void set_categories(List<Category> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Habit{id='" + id + "', name='" + name + "', completed=" + completed_today + "}";
    }
}
