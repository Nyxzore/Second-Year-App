package com.example.gon;

public class Goal {
    private String description, title, due_date;

    public Goal() {
        this.description = "What exactly is my goal?";
        this.title = "My Goal";
        this.due_date = "2024-06-21";
    }

    public Goal(String title, String description, String due_date) {
        this.title = title;
        this.description = description;
        this.due_date = due_date;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDueDate() { return due_date; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(String dueDate) { this.due_date = dueDate; }

    public String toString() {
        return "Goal{" + "title='" + title + '\'' + '}';
    }
}
