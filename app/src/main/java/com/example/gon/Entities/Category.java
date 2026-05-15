package com.example.gon.Entities;

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

    public static Category from_json(JSONObject obj) {
        return new Category(String.valueOf(obj.opt("id")), obj.optString("name", ""));
    }

    public static List<Category> list_from_json_array(JSONArray array) throws JSONException {
        List<Category> list = new ArrayList<>();
        if (array == null) {
            return list;
        }
        for (int i = 0; i < array.length(); i++) {
            list.add(from_json(array.getJSONObject(i)));
        }
        return list;
    }

    public static List<Category> list_from_item_json(JSONObject item) {
        if (item == null || !item.has("categories")) {
            return new ArrayList<>();
        }
        Object raw = item.opt("categories");
        if (raw == null || raw == JSONObject.NULL) {
            return new ArrayList<>();
        }
        try {
            if (raw instanceof JSONArray) {
                return list_from_json_array((JSONArray) raw);
            }
            if (raw instanceof String) {
                String trimmed = ((String) raw).trim();
                if (trimmed.isEmpty()) {
                    return new ArrayList<>();
                }
                return list_from_json_array(new JSONArray(trimmed));
            }
        } catch (JSONException e) {
            Log.e("GON_CAT", "list_from_item_json", e);
        }
        return new ArrayList<>();
    }

    public static String join_ids(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < categories.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(categories.get(i).get_id());
        }
        return sb.toString();
    }

    public static String join_names(List<Category> categories) {
        if (categories == null || categories.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < categories.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(categories.get(i).get_name());
        }
        return sb.toString();
    }

    public String get_id() {
        return id;
    }

    public String get_name() {
        return name;
    }
}