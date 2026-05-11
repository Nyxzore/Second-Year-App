package com.example.gon;

import android.database.DatabaseErrorHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/*
    This is the generic goal object equipped fully with accesser and mutator methods
 */
public class Goal {
    private String description, title, due_date, id;
    //if new attributes are added make sure this is reflected in the goals table as well as ln63 of GoalList.java

    public Goal() {
        this.description = "What exactly is my goal?";
        this.title = "My Goal";
        this.due_date = "2026-06-21";
    }

    //Generic instantiation would look like Goal("Finish Docs", "Finish writing documentation for this project", "2027-02-03")
    public Goal(String title, String description, String due_date, String id) {
        this.title = title;
        this.description = description;
        this.due_date = due_date;
        this.id = id;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Date getDueDateAsDate() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.parse(this.due_date);
    }
    public String getDueDate() {
        return this.due_date;
    }
    public String getId() { return id; }

    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDueDate(String dueDate) { this.due_date = dueDate; }

    public String toString() {return null; }
}
