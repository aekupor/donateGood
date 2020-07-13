package com.example.donategood;

import com.parse.FindCallback;
import com.parse.ParseQuery;

public class Query {

    protected void queryAllPosts(FindCallback<Offering> callback) {
        Integer displayLimit = 20;
        ParseQuery<Offering> query = ParseQuery.getQuery(Offering.class);
        query.setLimit(displayLimit);
        query.addDescendingOrder(Offering.KEY_CREATED_AT);
        query.findInBackground(callback);
    }
}
