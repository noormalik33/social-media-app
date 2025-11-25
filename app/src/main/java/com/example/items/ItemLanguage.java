package com.example.items;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ItemLanguage implements Serializable {

    @SerializedName("language_id")
    private String language_id;

    @SerializedName("language_name")
    private String language_name;

    @SerializedName("language_image")
    private String language_image;

    @SerializedName("language_image_thumb")
    private String language_image_thumb;

    @SerializedName("is_selected")
    private String is_selected;

    public String getLanguage_id() {
        return language_id;
    }

    public String getLanguage_name() {
        return language_name;
    }

    public String getLanguage_image() {
        return language_image;
    }

    public String getLanguage_image_thumb() {
        return language_image_thumb;
    }

    public String getIs_selected() {
        return is_selected;
    }

    public void setSelected(String isSelected) {
        this.is_selected = isSelected;
    }
}
