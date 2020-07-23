package com.example.donategood.helperClasses;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.donategood.R;
import com.example.donategood.adapters.NotificationAdapter;
import com.example.donategood.adapters.SmallOfferingAdapter;
import com.example.donategood.models.Charity;
import com.example.donategood.models.Notification;
import com.example.donategood.models.Offering;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class ParentProfile {

    public static final String TAG = "ParentProfile";

    public static final String KEY_BOUGHT = "bought";
    public static final String KEY_SELLING = "selling";
    public static final String KEY_SOLD = "sold";

    public static final String KEY_CURRENT_USER = "currentUser";
    public static final String KEY_OTHER_USER = "otherUser";
    public static final String KEY_CHARITY = "charity";

    public LoadPost loadPost;
    public Query query;
    public ParseUser user;
    public Charity charity;
    public String profileType;

    public TextView tvNotificationsTitle;
    public TextView tvPendingNotificationsTitle;
    public List<Notification> notifications;
    public RecyclerView rvNotifications;
    public NotificationAdapter notificationAdapter;
    public LinearLayout pendingNotifications;

    public TextView tvName;
    public ImageView ivProfileImage;
    public TextView tvMoneyRaised;
    public RecyclerView rvOfferings;
    public SmallOfferingAdapter adapter;
    public List<Offering> selectedOfferings;
    public TextView tvBoughtTitle;
    public TextView tvSoldTitle;
    public TextView tvSellingTitle;
    public ProgressBar pb;
    public RatingBar ratingBar;

    public void initializeVariables(View view, Context context, String queryType) {
        profileType = queryType;

        tvName = view.findViewById(R.id.tvProfileProfileName);
        ivProfileImage = view.findViewById(R.id.ivProfileProfileImage);
        tvMoneyRaised = view.findViewById(R.id.tvProfileMoneyRaised);
        rvOfferings = view.findViewById(R.id.rvProfileOfferings);
        tvSellingTitle = view.findViewById(R.id.tvProfileSellingTitle);
        tvSoldTitle = view.findViewById(R.id.tvProfileSoldTitle);
        pb = (ProgressBar) view.findViewById(R.id.pbProfileLoading);

        if (profileType != KEY_CHARITY) {
            tvBoughtTitle = view.findViewById(R.id.tvProfileBoughtTitle);
            ratingBar = (RatingBar) view.findViewById(R.id.rbProfile);

            tvBoughtTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    queryPosts(KEY_BOUGHT);
                }
            });
        }

        if (profileType == KEY_CURRENT_USER) {
            initializeNotifications(view, context);
        }

        selectedOfferings = new ArrayList<>();
        adapter = new SmallOfferingAdapter(context, selectedOfferings);

        rvOfferings.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        rvOfferings.setLayoutManager(linearLayoutManager);

        loadPost = new LoadPost();
        query = new Query();

        tvSellingTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryPosts(KEY_SELLING);
            }
        });

        tvSoldTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryPosts(KEY_SOLD);
            }
        });
    }

    public void initializeNotifications(View view, final Context context) {
        rvNotifications = view.findViewById(R.id.rvNotifications);
        tvNotificationsTitle = view.findViewById(R.id.tvNotificationsTitle);
        pendingNotifications = view.findViewById(R.id.layoutNotification);
        tvPendingNotificationsTitle = view.findViewById(R.id.tvWaitingNotificationsTitle);

        tvPendingNotificationsTitle.setVisibility(View.INVISIBLE);
        pendingNotifications.setVisibility(View.INVISIBLE);

        notifications = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(context, notifications);

        rvNotifications.setAdapter(notificationAdapter);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(context);
        rvNotifications.setLayoutManager(linearLayoutManager2);

        tvNotificationsTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getNotifications(context);
            }
        });
    }

    public void queryPosts(String queryType) {
        pb.setVisibility(ProgressBar.VISIBLE);

        if (profileType == KEY_CHARITY) {
            Boolean selling;
            if (queryType == KEY_SELLING) {
                selling = true;
                tvSellingTitle.setTypeface(null, Typeface.BOLD);
                tvSoldTitle.setTypeface(null, Typeface.NORMAL);
            } else {
                selling = false;
                tvSellingTitle.setTypeface(null, Typeface.NORMAL);
                tvSoldTitle.setTypeface(null, Typeface.BOLD);
            }

            query.queryPostsByCharity(charity, selling, new FindCallback<Offering>() {
                @Override
                public void done(List<Offering> offerings, ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Issue with getting offerings", e);
                        return;
                    }
                    Log.i(TAG, "Successfully received this number of offerings: " + offerings.size());
                    selectedOfferings.clear();
                    selectedOfferings.addAll(offerings);
                    adapter.notifyDataSetChanged();
                    pb.setVisibility(ProgressBar.INVISIBLE);
                }
            });
        } else {
            if (profileType == KEY_CURRENT_USER) {
                changeVisibility();
            }
            query.setBold(queryType, tvSoldTitle, tvSellingTitle, tvBoughtTitle);
            query.queryPosts(user, queryType, adapter, selectedOfferings, pb);
        }
    }

    public void changeVisibility() {
        rvOfferings.setVisibility(View.VISIBLE);
        rvNotifications.setVisibility(View.INVISIBLE);
        notificationAdapter.clear();
        tvNotificationsTitle.setTypeface(null, Typeface.NORMAL);
        tvPendingNotificationsTitle.setVisibility(View.INVISIBLE);
        pendingNotifications.setVisibility(View.INVISIBLE);
    }

    public void queryInfo(Context context) {
        loadPost.setUser(user, context, tvName, ivProfileImage);

        queryPosts(KEY_BOUGHT);
        query.queryMoneyRaised(user, tvMoneyRaised);
        query.queryUserRating(user, ratingBar);
    }

    public void setUser(ParseUser parseUser) {
        user = parseUser;
    }

    public void setCharity(Charity currentCharity) {
        charity = currentCharity;
    }

    public void getNotifications(final Context context) {
        Log.i(TAG, "notification button clicked");

        tvNotificationsTitle.setTypeface(null, Typeface.BOLD);
        tvSoldTitle.setTypeface(null, Typeface.NORMAL);
        tvSellingTitle.setTypeface(null, Typeface.NORMAL);
        tvBoughtTitle.setTypeface(null, Typeface.NORMAL);

        adapter.clear();
        notifications.clear();
        rvOfferings.setVisibility(View.INVISIBLE);
        rvNotifications.setVisibility(View.VISIBLE);
        tvPendingNotificationsTitle.setVisibility(View.VISIBLE);
        pendingNotifications.setVisibility(View.VISIBLE);
        pendingNotifications.removeAllViews();

        query.queryNotificationsForSeller(new FindCallback<Notification>() {
            @Override
            public void done(List<Notification> objects, ParseException e) {
                if (e != null) {
                    return;
                }

                if (objects != null) {
                    for (Notification notification : objects) {
                        if (notification.getSellingUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                            //notification is for current user to approve
                            Log.i(TAG, "found notification for title for post: " + notification.getKeyOffering().getTitle());
                            notifications.add(notification);
                        }
                    }
                    notificationAdapter.notifyDataSetChanged();
                }
            }
        });

        query.queryNotificationsForBuyer(ParseUser.getCurrentUser(), new FindCallback<Notification>() {
            @Override
            public void done(List<Notification> objects, ParseException e) {
                if (e != null) {
                    return;
                }

                if (objects != null) {
                    for (Notification notification : objects) {
                        Log.i(TAG, "found notification for title for post: " + notification.getKeyOffering().getTitle());

                        if (!notification.getUserSeen()) {
                            TextView textView = new TextView(context);
                            if (!notification.getUserActed()) {
                                textView.setText("Still waiting on seller to approval your purchase of " + notification.getKeyOffering().getTitle() + ".");
                            } else if (notification.getKeyApproved()) {
                                textView.setText("You have been approved to buy " + notification.getKeyOffering().getTitle() + ".");
                                notification.setUserSeen(true);
                                notification.saveInBackground();
                            } else {
                                textView.setText("You have NOT been approved to buy " + notification.getKeyOffering().getTitle() + ".");
                                notification.setUserSeen(true);
                                notification.saveInBackground();
                            }
                            pendingNotifications.addView(textView);
                        }
                    }
                }
            }
        });
    }
}