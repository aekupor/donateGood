package com.example.donategood.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.donategood.adapters.OfferingAdapter;
import com.example.donategood.adapters.SmallOfferingAdapter;
import com.example.donategood.helperClasses.LoadPost;
import com.example.donategood.helperClasses.Query;
import com.example.donategood.R;
import com.example.donategood.helperClasses.Recommend;
import com.example.donategood.models.Offering;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailFragment extends Fragment {

    public static final String TAG = "DetailFragment";

    private String offeringId;
    private Query query;
    private Offering offering;
    private LoadPost loadPost;

    private TextView tvTitle;
    private TextView tvPrice;
    private TextView tvUser;
    private TextView tvCharity;
    private ImageView ivCharityImage;
    private ImageView ivOfferingPhoto;
    private Button btnPurchase;
    private TextView tvTags;

    private RecyclerView rvRecommendedOfferings;
    private SmallOfferingAdapter adapter;
    private List<Offering> reccomendedOfferings;

    private ShareButton shareButton;
    private ShareLinkContent content;

    public DetailFragment() {
        // Required empty public constructor
    }

    public static DetailFragment newInstance(String offeringId) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString("offeringId", offeringId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        offeringId = getArguments().getString("offeringId", "");
        Log.i(TAG, "post id: " + offeringId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvTitle = view.findViewById(R.id.tvDetailTitle);
        tvPrice = view.findViewById(R.id.tvDetailPrice);
        tvCharity = view.findViewById(R.id.tvDetailCharity);
        tvUser = view.findViewById(R.id.tvDetailUser);
        ivCharityImage = view.findViewById(R.id.ivDetailCharityImage);
        btnPurchase = view.findViewById(R.id.btnPurchase);
        ivOfferingPhoto = view.findViewById(R.id.ivDetailOfferingPhoto);
        shareButton = (ShareButton)view.findViewById(R.id.fbShareButtonDetail);
        tvTags = view.findViewById(R.id.tvDetailTagList);
        rvRecommendedOfferings = view.findViewById(R.id.rvRecommendOfferings);

        reccomendedOfferings = new ArrayList<>();
        adapter = new SmallOfferingAdapter(getContext(), reccomendedOfferings);

        rvRecommendedOfferings.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvRecommendedOfferings.setLayoutManager(linearLayoutManager);

        tvCharity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCharity();
            }
        });

        ivCharityImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToCharity();
            }
        });

        tvUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToUser();
            }
        });

        btnPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                purchaseItem();
            }
        });

        loadPost = new LoadPost();

        query = new Query();
        query.queryOfferingById(offeringId, new FindCallback<Offering>() {
            @Override
            public void done(List<Offering> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting post", e);
                    return;
                }
                offering = objects.get(0);
                Log.i(TAG, "got offering with title: " + offering.getTitle());

                loadPost.setTitlePriceUser(offering, tvTitle, tvPrice, tvUser);
                loadPost.setCharity(offering, getContext(), tvCharity, ivCharityImage);
                loadPost.setPostImage(offering.getImage(), getContext(), ivOfferingPhoto);
                loadPost.setTags(offering.getTags(), tvTags);

                setShareButton();
                queryRecommendedPosts();
            }
        });
    }

    private void queryRecommendedPosts() {
        final Recommend recommend = new Recommend();
        final Map<Offering, Integer>[] pointValues = new Map[]{new HashMap<>()};

        query.queryAllPostsWithoutPage(new FindCallback<Offering>() {
               @Override
               public void done(List<Offering> offerings, ParseException e) {
                   if (e != null) {
                       Log.e(TAG, "Issue with getting offerings", e);
                       return;
                   }
                   for (Offering otherOffering : offerings) {
                       if (otherOffering.getObjectId().equals(offering.getObjectId())) {
                           //if offering is the same, do not include as recommended offering
                           continue;
                       }

                       Integer pointValue = recommend.getPointValue(offering, otherOffering);
                       pointValues[0].put(otherOffering, pointValue);
                   }
                   final Map<Offering, Integer>[] sortedPointValues = new Map[]{new HashMap<>()};
                   sortedPointValues[0] = recommend.sortMapByPoints(pointValues[0]);
                   Log.i(TAG, "sorted point values list: " + sortedPointValues[0].toString());

                   adapter.clear();
                   reccomendedOfferings.clear();
                   reccomendedOfferings.addAll(sortedPointValues[0].keySet());
                   adapter.notifyDataSetChanged();
               }
           });
    }

    private void setShareButton() {
        content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(offering.getImage().getUrl()))
                .build();

        shareButton.setShareContent(content);

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "share button clicked");
                ShareDialog.show(DetailFragment.this, content);
            }
        });
    }

    private void goToCharity() {
        Log.i(TAG, "go to charity");

        //go to charity fragment
        final FragmentManager fragmentManager = ((AppCompatActivity)getContext()).getSupportFragmentManager();
        Fragment fragment = CharityFragment.newInstance(offering.getCharity().getTitle());
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
    }

    private void goToUser() {
        Log.i(TAG, "go to user");

        //go to user fragment
        final FragmentManager fragmentManager = ((AppCompatActivity)getContext()).getSupportFragmentManager();
        Fragment fragment = OtherUserProfileFragment.newInstance((String) tvUser.getText());
        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
    }

    private void purchaseItem() {
        Log.i(TAG, "purchase item");
        offering.setIsBought(true);
        offering.setBoughtBy(ParseUser.getCurrentUser());
        offering.saveInBackground();
    }
}