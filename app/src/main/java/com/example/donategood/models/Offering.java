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
    public static final String KEY_CHARITY = "charity";
    public static final String KEY_TAGS = "tags";
    public static final String KEY_BOUGHT = "isBought";
    public static final String KEY_BOUGHT_BY = "boughtBy";
    public static final String KEY_RATING = "rating";
    public static final String KEY_QUANTITY_LEFT = "quantityLeft";
    public static final String KEY_BOUGHT_BY_ARRAY = "boughtByArray";
    public static final String KEY_IMAGES_ARRAY = "images";
    public static final String KEY_HAS_MULTIPLE_IMAGES = "hasMultipleImages";
    public static final String KEY_DESCRIPTION = "description";

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

    public Charity getCharity() {
        return (Charity) get(KEY_CHARITY);
    }

    public void setCharity(Charity charity) {
        put(KEY_CHARITY, charity);
    }

    public ArrayList<String> getTags() {
        return (ArrayList<String>) get(KEY_TAGS);
    }

    public void setTags(ArrayList<String> tags) {
        put(KEY_TAGS, tags);
    }

    public boolean getIsBought() {
        return getBoolean(KEY_BOUGHT);
    }

    public void setIsBought(Boolean bought) {
        put(KEY_BOUGHT, bought);
    }

    public ParseUser getBoughtBy() {
        return getParseUser(KEY_BOUGHT_BY);
    }

    public void setBoughtBy(ParseUser user) {
        put(KEY_BOUGHT_BY, user);
    }

    public Integer getRating() {
        return getInt(KEY_RATING);
    }

    public void setRating(Integer rating) {
        put(KEY_RATING, rating);
    }

    public Integer getQuantityLeft() {
        return getInt(KEY_QUANTITY_LEFT);
    }

    public void setQuantityLeft(Integer quantityLeft) {
        put(KEY_QUANTITY_LEFT, quantityLeft);
    }

    public ArrayList<Object> getBoughtByArray() {
        return (ArrayList<Object>) get(KEY_BOUGHT_BY_ARRAY);
    }

    public void addToBoughtByArray(ParseUser user) {
        ArrayList<ParseUser> users = new ArrayList<ParseUser>();
        ArrayList<Object> boughtAlready = getBoughtByArray();
        if (boughtAlready == null) {
            users.add(user);
            put(KEY_BOUGHT_BY_ARRAY, users);
        } else {
            boughtAlready.add(user);
            put(KEY_BOUGHT_BY_ARRAY, boughtAlready);
        }
    }

    public void removeFromBoughtByArray(ParseUser user) {
        ArrayList<Object> boughtAlready = getBoughtByArray();

        if (getBoughtByArray().size() == 1) {
            ArrayList<Object> emptyList = new ArrayList<>();
            put(KEY_BOUGHT_BY_ARRAY, emptyList);
            return;
        }

        for (Object boughtObject : boughtAlready) {
            ParseUser boughtUser = (ParseUser) boughtObject;
            if (boughtUser.getObjectId().equals(user.getObjectId())) {
                boughtAlready.remove(boughtObject);
                put(KEY_BOUGHT_BY_ARRAY, boughtAlready);
                return;
            }
        }
    }

    public ArrayList<ParseFile> getImagesArray() {
        return (ArrayList<ParseFile>) get(KEY_IMAGES_ARRAY);
    }

    public void setImagesArray(ArrayList<ParseFile> images) {
        put(KEY_IMAGES_ARRAY, images);
    }

    public Boolean hasMultipleImages() {
        return getBoolean(KEY_HAS_MULTIPLE_IMAGES);
    }

    public void setHasMultipleImages(Boolean hasMultipleImages) {
        put(KEY_HAS_MULTIPLE_IMAGES, hasMultipleImages);
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }
}