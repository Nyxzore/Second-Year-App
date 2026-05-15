package com.example.gon.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private final String id;
    private final String name;

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Category fromJson(JSONObject obj) {
        return new Category(String.valueOf(obj.opt("id")), obj.optString("name", ""));
    }

    public static List<Category> listFromJsonArray(JSONArray array) throws JSONException {
        List<Category> list = new ArrayList<>();
        if (array == null) {
            return list;
        }
        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.getJSONObject(i)));
        }
        return list;
    }

    /** Parses the {@code categories} field from a goal/habit JSON object (array or missing). */
    public static List<Category> listFromItemJson(JSONObject item) {
        if (item == null || !item.has("categories")) {
            return new ArrayList<>();
        }
        Object raw = item.opt("categories");
        if (raw == null || raw == JSONObject.NULL) {
            return new ArrayList<>();
        }
        try {
            if (raw instanceof JSONArray) {
                return listFromJsonArray((JSONArray) raw);
            }
            if (raw instanceof String) {
                String trimmed = ((String) raw).trim();
                if (trimmed.isEmpty()) {
                    return new ArrayList<>();
                }
                return listFromJsonArray(new JSONArray(trimmed));
            }
        } catch (JSONException e) {
            Log.e("GON_CAT", "listFromItemJson", e);
        }
        return new ArrayList<>();
    }

    public static String joinIds(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < categories.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(categories.get(i).getId());
        }
        return sb.toString();
    }

    public static String joinNames(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < categories.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(categories.get(i).getName());
        }
        return sb.toString();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
