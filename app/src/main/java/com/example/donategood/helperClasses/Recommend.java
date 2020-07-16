package com.example.donategood.helperClasses;

import android.util.Log;

import com.example.donategood.models.Charity;
import com.example.donategood.models.Offering;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Recommend {

    public static final String TAG = "Recommend";

    private Map<Offering, Integer> pointValues;
    private Query query;

    public void getRecommendedOfferings(final Offering mainOffering) {
        pointValues = new HashMap<>();
        query = new Query();
        query.queryAllPostsWithoutPage(new FindCallback<Offering>() {
            @Override
            public void done(List<Offering> offerings, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting offerings", e);
                    return;
                }
                for (Offering offering : offerings) {
                    Log.i(TAG, "Offering: " + offering.getTitle());

                    if (offering.equals(mainOffering)) {
                        //if offering is the same, do not include as recommended offering
                        continue;
                    }

                    Integer pointValue = getPointValue(mainOffering, offering);
                    pointValues.put(offering, pointValue);
                }
                Log.i(TAG, "point values list: " + pointValues.toString());
            }
        });
    }

    public HashMap<Offering, Integer> sortMapByPoints() {
        // Create a list from elements of HashMap
        List<Map.Entry<Offering, Integer> > list = new LinkedList<Map.Entry<Offering, Integer> >(pointValues.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<Offering, Integer> >() {
            public int compare(Map.Entry<Offering, Integer> o1,
                               Map.Entry<Offering, Integer> o2)
            {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<Offering, Integer> temp = new LinkedHashMap<Offering, Integer>();
        for (Map.Entry<Offering, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

    private Integer getPointValue(Offering mainOffering, Offering offering) {
        Integer pointValue = 0;
        pointValue += checkPrice(mainOffering.getPrice(), offering.getPrice());
        pointValue += checkCharity(mainOffering.getCharity(), offering.getCharity());
        pointValue += checkTags(mainOffering.getTags(), offering.getTags());
        pointValue += checkSellingUser(mainOffering.getUser(), offering.getUser());
        return pointValue;
    }

    private Integer checkPrice(Integer mainPrice, Integer otherPrice) {
        Integer priceDifference = Math.abs(mainPrice - otherPrice);
        if (priceDifference <= 5) {
            return 2;
        } else if (priceDifference <= 20) {
            return 1;
        }
        return 0;
    }

    private Integer checkCharity(Charity mainCharity, Charity otherCharity) {
        if (mainCharity.equals(otherCharity)) {
            return 2;
        }
        //TODO: if same type of charity (i.e. environmental, BLM, etc.), add 1 point
        return 0;
    }

    private Integer checkTags(ArrayList<String> mainTags, ArrayList<String> otherTags) {
        Integer points = 0;
        for (String tag : mainTags) {
            if (otherTags.contains(tag)) {
                points++;
            }
        }
        return points;
    }

    private Integer checkSellingUser(ParseUser mainUser, ParseUser otherUser) {
        if (mainUser.equals(otherUser)) {
            return 2;
        }
        return 0;
    }
}