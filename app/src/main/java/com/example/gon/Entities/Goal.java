package com.example.gon.Entities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Goal {
    private String description, title, due_date, id;
    private List<Category> categories = new ArrayList<>();

    public Goal() {
        this.description = "What exactly is my goal?";
        this.title = "My Goal";
        this.due_date = "2026-06-21";
    }

    public Goal(String title, String description, String due_date, String id) {
        this.title = title;
        this.description = description;
        this.due_date = due_date;
        this.id = id;
    }

    public String get_title() { return title; }
    public String get_description() { return description; }
    public Date get_due_date_as_date() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.parse(this.due_date);
    }
    public String get_due_date() {
        return this.due_date;
    }
    public String get_id() { return id; }

    public void set_title(String title) { this.title = title; }
    public void set_description(String description) { this.description = description; }
    public void set_due_date(String due_date) { this.due_date = due_date; }

    public List<Category> get_categories() {
        return categories;
    }

    public void set_categories(List<Category> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
    }

    public String toString() {
        return "ID: " + this.id + " Title: " + this.title + " Description: " + this.description + " Due Date: " + this.due_date;
    }
}
