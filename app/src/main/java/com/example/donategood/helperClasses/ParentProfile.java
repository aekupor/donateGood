package com.example.donategood.helperClasses;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.donategood.R;
import com.example.donategood.adapters.NotificationAdapter;
import com.example.donategood.adapters.SmallOfferingAdapter;
import com.example.donategood.fragments.AnalyticsFragment;
import com.example.donategood.models.Charity;
import com.example.donategood.models.Notification;
import com.example.donategood.models.Offering;
import com.google.android.material.tabs.TabLayout;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    public ProgressBar pb;
    public RatingBar ratingBar;
    public ImageView ivLevelIcon;
    public ImageView ivCharityIcon;
    public ImageView ivFollow;
    public Boolean following;
    public TextView tvBio;

    public void initializeVariables(View view, final Context context, final String queryType) {
        profileType = queryType;

        //find items on view
        tvName = view.findViewById(R.id.tvProfileProfileName);
        ivProfileImage = view.findViewById(R.id.ivProfileProfileImage);
        tvMoneyRaised = view.findViewById(R.id.tvProfileMoneyRaised);
        rvOfferings = view.findViewById(R.id.rvProfileOfferings);
        pb = (ProgressBar) view.findViewById(R.id.pbProfileLoading);

        //initialize adapter and recycler view
        selectedOfferings = new ArrayList<>();
        adapter = new SmallOfferingAdapter(context, selectedOfferings);

        rvOfferings.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        rvOfferings.setLayoutManager(linearLayoutManager);

        loadPost = new LoadPost();
        query = new Query();

        //initialize tab layout
        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tabLayout);

        tabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                Log.i(TAG, "tab selected at position: " + position);
                if (profileType != KEY_CHARITY) {
                    if (position == 0) {
                        queryPosts(KEY_BOUGHT);
                    } else if (position == 1) {
                        queryPosts(KEY_SOLD);
                    } else if (position == 2) {
                        queryPosts(KEY_SELLING);
                    } else {
                        getNotifications(context);
                    }
                } else {
                    if (position == 0) {
                        queryPosts(KEY_SOLD);
                    } else {
                        queryPosts(KEY_SELLING);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //only user profile and other user profile has a rating bar, level icons, and bio
        if (profileType != KEY_CHARITY) {
            ratingBar = (RatingBar) view.findViewById(R.id.rbProfile);
            ivLevelIcon = view.findViewById(R.id.ivLevelIcon);
            ivCharityIcon = view.findViewById(R.id.ivCharityIcon);
            tvBio = view.findViewById(R.id.tvBio);
        }

        //only the current user has a "notifications" tab
        if (profileType == KEY_CURRENT_USER) {
            initializeNotifications(view, context);
        } else {
            //only other users and charities have a "follow" option
            initializeFollow(view, context);
        }
    }

    //initialize variables relating to follow
    //ProfileFragment does not have follow since user is current signed in use
    private void initializeFollow(View view, final Context context) {
        following = false;
        ivFollow = view.findViewById(R.id.ivFollow);
        checkIfFollowing();
        ivFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "follow clicked");
                if (following) {
                    //if user is already following, onClick unfollows
                    if (profileType == KEY_CHARITY) {
                        ParseUser.getCurrentUser().getRelation("followingCharity").remove(charity);
                    } else {
                        ParseUser.getCurrentUser().getRelation("following").remove(user);
                    }
                    ParseUser.getCurrentUser().saveInBackground();
                    ivFollow.setImageResource(R.drawable.ic_baseline_person_add_24);
                    Toast.makeText(context, "Unfollowed", Toast.LENGTH_SHORT).show();
                    following = false;
                } else {
                    //if user is not already following, onClick follows
                    if (profileType == KEY_CHARITY) {
                        ParseUser.getCurrentUser().getRelation("followingCharity").add(charity);
                    } else {
                        ParseUser.getCurrentUser().getRelation("following").add(user);
                    }
                    ParseUser.getCurrentUser().saveInBackground();
                    ivFollow.setImageResource(R.drawable.ic_baseline_person_add_disabled_24);
                    Toast.makeText(context, "Following", Toast.LENGTH_SHORT).show();
                    following = true;
                }
            }
        });
    }

    //check is current user is already following this user/charity
    public void checkIfFollowing() {
        pb.setVisibility(ProgressBar.VISIBLE);
        if (profileType == KEY_CHARITY) {
            //check if following charity
            ParseUser.getCurrentUser().getRelation("followingCharity").getQuery().findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e != null) {
                        return;
                    }
                    for (ParseObject followingObject : objects) {
                        Charity followingCharity = (Charity) followingObject;
                        if (followingCharity != null && charity != null) {
                            if (followingCharity.getObjectId().equals(charity.getObjectId())) {
                                setIsFollowing();
                                return;
                            }
                        }
                    }
                }
            });
        } else {
            //check if following user
            ParseUser.getCurrentUser().getRelation("following").getQuery().findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e != null) {
                        return;
                    }
                    for (ParseObject followingObject : objects) {
                        ParseUser followingUser = (ParseUser) followingObject;
                        if (followingUser.getObjectId().equals(user.getObjectId())) {
                            setIsFollowing();
                            return;
                        }
                    }
                }
            });
        }
    }

    //if user is following that charity/user, set appropiate variables
    private void setIsFollowing() {
        following = true;
        ivFollow.setImageResource(R.drawable.ic_baseline_person_add_disabled_24);
    }

    //initialize notifications and related variables
    public void initializeNotifications(View view, final Context context) {
        rvNotifications = view.findViewById(R.id.rvNotifications);
        pendingNotifications = view.findViewById(R.id.layoutNotification);
        tvPendingNotificationsTitle = view.findViewById(R.id.tvWaitingNotificationsTitle);

        //make notifications invisible until user clicks on "notification" tab
        tvPendingNotificationsTitle.setVisibility(View.INVISIBLE);
        pendingNotifications.setVisibility(View.INVISIBLE);

        //initialize adapter and recycler view
        notifications = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(context, notifications);

        rvNotifications.setAdapter(notificationAdapter);
        LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(context);
        rvNotifications.setLayoutManager(linearLayoutManager2);
    }

    //call correct query depending on queryType
    public void queryPosts(String queryType) {
        pb.setVisibility(ProgressBar.VISIBLE);

        if (profileType == KEY_CHARITY) {
            Boolean selling;
            if (queryType == KEY_SELLING) {
                selling = false;
            } else {
                selling = true;
            }

            query.setCharityPosts(selling, this);
        } else {
            if (profileType == KEY_CURRENT_USER) {
                //if is current user, hide the notification tab since not on that tab
                hideNotificationsTab();
            }
            query.queryPosts(queryType, this);
            if (user.get("bio") != null) {
                //set bio if user has one
                tvBio.setText(user.get("bio").toString());
            }
        }
    }

    //make notifications tab invisible
    public void hideNotificationsTab() {
        rvOfferings.setVisibility(View.VISIBLE);
        rvNotifications.setVisibility(View.INVISIBLE);
        notificationAdapter.clear();
        tvPendingNotificationsTitle.setVisibility(View.INVISIBLE);
        pendingNotifications.setVisibility(View.INVISIBLE);
    }

    //set information for current or other user profile
    public void queryInfo(Context context, View view) {
        loadPost.setUser(user, context, tvName, ivProfileImage);
        query.queryMoneyRaised(this, context);
        queryPosts(KEY_BOUGHT);
        query.setUserRating(user, ratingBar);
        if (profileType != KEY_CURRENT_USER) {
            initializeFollow(view, context);
        }
    }

    //set information for charity
    public void queryCharityInfo(Context context, View view) {
        pb.setVisibility(View.VISIBLE);
        loadPost.setCharityWithCharity(charity, context, tvName, ivProfileImage);
        query.findCharityMoneyRaised(charity, tvMoneyRaised, pb);
        initializeFollow(view, context);
        queryPosts(KEY_SOLD);
    }

    public void setUser(ParseUser parseUser) {
        user = parseUser;
    }

    public void setCharity(Charity currentCharity) {
        charity = currentCharity;
    }

    //query and set notifications for current user
    public void getNotifications(final Context context) {
        Log.i(TAG, "notification button clicked");

        //clear adapter and set notifications tab as visible
        adapter.clear();
        notifications.clear();
        rvOfferings.setVisibility(View.INVISIBLE);
        rvNotifications.setVisibility(View.VISIBLE);
        tvPendingNotificationsTitle.setVisibility(View.VISIBLE);
        pendingNotifications.setVisibility(View.VISIBLE);
        pendingNotifications.removeAllViews();

        //query notifications for offerings that the current user is selling
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

        //query notifications for offering that the current user is attempting to buy
        query.queryNotificationsForBuyer(ParseUser.getCurrentUser(), new FindCallback<Notification>() {
            @Override
            public void done(List<Notification> objects, ParseException e) {
                if (e != null) {
                    return;
                }

                if (objects != null) {
                    for (Notification notification : objects) {
                        Log.i(TAG, "found notification for title for post: " + notification.getKeyOffering().getTitle());

                        //if user hasn't seen the notification yet, then display
                        if (!notification.getUserSeen()) {
                            TextView textView = new TextView(context);
                            if (!notification.getUserActed()) {
                                //notification is still pending on seller approval
                                textView.setText("Still waiting on seller to approval your purchase of " + notification.getKeyOffering().getTitle() + ".");
                            } else if (notification.getKeyApproved()) {
                                //attempt to buy has been approved by seller
                                textView.setText("You have been approved to buy " + notification.getKeyOffering().getTitle() + ".");
                                notification.setUserSeen(true);
                                notification.saveInBackground();
                            } else {
                                //attempt to buy has been denied by seller
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

    //make string of analytics to pass into AnalyticsFragment
    public String getAnalytics() {
        HashMap<String, Integer> moneyByCharity;
        if (profileType == KEY_CHARITY) {
            moneyByCharity = query.getMoneyRaisedForCharityByPerson();
        } else {
            moneyByCharity = query.getMoneyRaisedForPersonByCharity();
        }
        String analytics = "";
        for (Map.Entry mapElement : moneyByCharity.entrySet()) {
            //set analytics string to be equal to the items of moneyByCharity
            analytics += mapElement.toString() + "; ";
        }
        return analytics;
    }

    //open AnalyticsFragment
    public void openAnalyticsDialog(Fragment currentFragment, FragmentManager fmManager) {
        FragmentManager fm = fmManager;
        AnalyticsFragment fragment;
        if (profileType == KEY_CHARITY) {
            fragment = (AnalyticsFragment) AnalyticsFragment.newInstance(getAnalytics(), true);
        } else {
            fragment = (AnalyticsFragment) AnalyticsFragment.newInstance(getAnalytics(), false);
        }
        // SETS the target fragment for use later when sending results
        fragment.setTargetFragment(currentFragment, 200);
        fragment.show(fm, "fragment_analytics");
    }
}