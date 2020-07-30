package com.example.donategood.helperClasses;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.donategood.R;
import com.example.donategood.models.Charity;
import com.example.donategood.models.Offering;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MoneyRaised {

    //determines amount of money raised for a charity
    public void findCharityMoneyRaised(final Charity charity, final TextView tvMoney, final ProgressBar pb, final Query query) {
        pb.setVisibility(View.VISIBLE);
        final Integer[] moneyRaised = {0};
        final HashMap<ParseUser, Integer> moneyRaisedByPerson = new HashMap<>();
        query.queryAllPosts(new FindCallback<Offering>() {
            @Override
            public void done(List<Offering> objects, ParseException e) {
                for (Offering offering : objects) {
                    if (offering.getCharity().getObjectId().equals(charity.getObjectId())) {
                        ArrayList<Object> boughtUsers = offering.getBoughtByArray();
                        if (boughtUsers != null && !boughtUsers.isEmpty()) {
                            moneyRaised[0] += boughtUsers.size() * offering.getPrice();

                            //fill HashMap with users
                            for (Object boughtObject : boughtUsers) {
                                ParseUser boughtUser = (ParseUser) boughtObject;
                                //add each user who bought an item for that charity
                                if (moneyRaisedByPerson.containsKey(boughtUser)) {
                                    moneyRaisedByPerson.put(boughtUser, moneyRaisedByPerson.get(boughtUser) + offering.getPrice());
                                } else {
                                    moneyRaisedByPerson.put(boughtUser, offering.getPrice());
                                }
                            }
                            //add sellers who raised money for that charity
                            if (moneyRaisedByPerson.containsKey(offering.getUser())) {
                                moneyRaisedByPerson.put(offering.getUser(), moneyRaisedByPerson.get(offering.getUser()) + offering.getPrice() * boughtUsers.size());
                            } else {
                                moneyRaisedByPerson.put(offering.getUser(), offering.getPrice());
                            }
                        }
                    }
                }
                tvMoney.setText("$" + moneyRaised[0].toString());

                //make map that has only one entry by user (add up all the prices)
                HashMap<String, Integer> consolidateMapByUser = new HashMap<>();
                for (Map.Entry mapElement : moneyRaisedByPerson.entrySet()) {
                    ParseUser key = (ParseUser) mapElement.getKey();
                    ParseUser user = null;
                    try {
                        user = key.fetchIfNeeded();
                    } catch (ParseException e2) {
                        e2.printStackTrace();
                    }
                    int value = (int)mapElement.getValue();

                    if (consolidateMapByUser.containsKey(user.getUsername())) {
                        consolidateMapByUser.put(user.getUsername(), consolidateMapByUser.get(user.getUsername()) + value);
                    } else {
                        consolidateMapByUser.put(user.getUsername(), value);
                    }
                }

                HashMap<String, Integer> sortedMap = query.sortMapByPointsByUser(consolidateMapByUser);
                query.moneyRaisedForCharityByPerson = sortedMap;
                pb.setVisibility(View.INVISIBLE);
            }
        });
    }

    //find money raised by a specified user
    public void queryMoneyRaised(final ParentProfile parentProfile, final Context context, final Query query) {
        parentProfile.pb.setVisibility(View.VISIBLE);

        final Integer[] moneyRaised = {0};
        final Integer[] moneySold = {0};

        final HashMap<Charity, Integer> moneyRaisedByCharity = new HashMap<>();

        query.queryAllPosts(new FindCallback<Offering>() {
            @Override
            public void done(List<Offering> objects, ParseException e) {
                for (Offering offering : objects) {
                    ArrayList<Object> boughtUsers = offering.getBoughtByArray();
                    if (boughtUsers != null && !boughtUsers.isEmpty()) {
                        for (Object object : boughtUsers) {
                            ParseUser user = (ParseUser) object;
                            if (user.getObjectId().equals(parentProfile.user.getObjectId())) {
                                //if user bought the offering, add its price to the total money raised
                                moneyRaised[0] += offering.getPrice();
                                Charity charity = null;
                                try {
                                    charity = offering.getCharity().fetchIfNeeded();
                                } catch (ParseException ex) {
                                    ex.printStackTrace();
                                }
                                if (moneyRaisedByCharity.containsKey(charity)) {
                                    moneyRaisedByCharity.put(charity, moneyRaisedByCharity.get(charity) + offering.getPrice());
                                } else {
                                    moneyRaisedByCharity.put(charity, offering.getPrice());
                                }
                            }
                        }
                        if (offering.getUser().getObjectId().equals(parentProfile.user.getObjectId())) {
                            //if user sold the offering, add its price * quantity sold to the total money sold
                            moneySold[0] += offering.getPrice() * boughtUsers.size();
                            Charity charity = offering.getCharity();
                            if (moneyRaisedByCharity.containsKey(charity)) {
                                moneyRaisedByCharity.put(charity, moneyRaisedByCharity.get(charity) + offering.getPrice() * boughtUsers.size());
                            } else {
                                moneyRaisedByCharity.put(charity, offering.getPrice() * boughtUsers.size());
                            }
                        }
                    }
                }
                //find and set total money raised
                Integer totalMoney = moneyRaised[0] + moneySold[0];
                parentProfile.tvMoneyRaised.setText("$" + totalMoney.toString());

                //determine the level of the user based on the amount of money raised + sold
                int iconImage = -1;
                if (totalMoney < 100) {
                    iconImage = R.drawable.level_one;
                } else if (totalMoney < 200) {
                    iconImage = R.drawable.level_two;
                } else if (totalMoney < 300) {
                    iconImage = R.drawable.level_three;
                } else if (totalMoney < 400) {
                    iconImage = R.drawable.level_four;
                } else if (totalMoney < 500) {
                    iconImage = R.drawable.level_five;
                } else {
                    iconImage = R.drawable.crown;
                }

                //set icon based on level
                if (totalMoney < 25) {
                    parentProfile.ivLevelIcon.setVisibility(View.INVISIBLE);
                } else {
                    Glide.with(context)
                            .load(iconImage)
                            .circleCrop()
                            .into(parentProfile.ivLevelIcon);
                }

                //sort map
                HashMap<Charity, Integer> sortedMap = query.sortMapByPoints(moneyRaisedByCharity);
                query.sortedMapMoneyRaisedByCharity = sortedMap;

                //make map that has only one entry by charity (add up all the prices)
                HashMap<String, Integer> consolidateMapByCharity = new HashMap<>();
                for (Map.Entry mapElement : query.sortedMapMoneyRaisedByCharity.entrySet()) {
                    Charity key = (Charity) mapElement.getKey();
                    Charity charity = null;
                    try {
                        charity = key.fetchIfNeeded();
                    } catch (ParseException e2) {
                        e2.printStackTrace();
                    }
                    int value = (int) mapElement.getValue();

                    if (consolidateMapByCharity.containsKey(charity.getTitle())) {
                        consolidateMapByCharity.put(charity.getTitle(), consolidateMapByCharity.get(charity.getTitle()) + value);
                    } else {
                        consolidateMapByCharity.put(charity.getTitle(), value);
                    }
                    query.moneyRaisedForPersonByCharity = consolidateMapByCharity;
                }

                Set<String> charities = consolidateMapByCharity.keySet();
                for (String charity : charities) {
                    query.findCharity(charity, new FindCallback<Charity>() {
                        @Override
                        public void done(List<Charity> objects, ParseException e) {
                            //set charity icon with profile image of charity with most money raised
                            Glide.with(context)
                                    .load(objects.get(0).getImage().getUrl())
                                    .into(parentProfile.ivCharityIcon);
                            parentProfile.pb.setVisibility(View.INVISIBLE);
                            return;
                        }
                    });
                    return;
                }
            }
        });
    }
}
