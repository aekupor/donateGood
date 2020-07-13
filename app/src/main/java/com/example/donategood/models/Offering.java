package com.example.donategood.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;

@ParseClassName("Offering")
public class Offering extends ParseObject {

    public static final String KEY_TITLE = "title";
    public static final String KEY_PRICE = "price";
    public static final String KEY_USER = "user";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_IMAGES = "images";

    public String getTitle() {
        return getString(KEY_TITLE);
    }

    public void setTitle(String title) {
        put(KEY_TITLE, title);
    }

    public Integer getPrice() {
        return getInt(KEY_PRICE);
    }

    public void setPrice(Integer price) {
        put(KEY_PRICE, price);
    }

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMAGE);
    }

    public void setImage(ParseFile image) {
        put(KEY_IMAGE, image);
    }

    public ArrayList<ParseUser> getImages() {
        return (ArrayList<ParseUser>) get(KEY_IMAGES);
    }
}