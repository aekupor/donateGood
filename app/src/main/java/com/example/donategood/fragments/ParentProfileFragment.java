package com.example.donategood.fragments;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.donategood.R;
import com.example.donategood.adapters.SmallOfferingAdapter;
import com.example.donategood.helperClasses.LoadPost;
import com.example.donategood.helperClasses.Query;
import com.example.donategood.models.Offering;

import java.util.ArrayList;
import java.util.List;


public class ParentProfileFragment {

    public static final String TAG = "ParentProfileFragment";

    private LoadPost loadPost;
    private Query query;

    private TextView tvName;
    private ImageView ivProfileImage;
    private TextView tvMoneyRaised;
    private RecyclerView rvOfferings;
    private SmallOfferingAdapter adapter;
    private List<Offering> selectedOfferings;
    private TextView tvBoughtTitle;
    private TextView tvSoldTitle;
    private TextView tvSellingTitle;
    private ProgressBar pb;
    private Button btnChat;
    private RatingBar ratingBar;

    protected void initializeVariables(View view, Context context) {
        tvName = view.findViewById(R.id.tvOtherProfileProfileName);
        ivProfileImage = view.findViewById(R.id.ivOtherProfileProfileImage);
        tvMoneyRaised = view.findViewById(R.id.tvOtherUserMoneyRaised);
        rvOfferings = view.findViewById(R.id.rvOtherUserSelling);
        tvBoughtTitle = view.findViewById(R.id.tvOtherBoughtTitle);
        tvSellingTitle = view.findViewById(R.id.tvOtherSellingTitle);
        tvSoldTitle = view.findViewById(R.id.tvOtherSoldTitle);
        pb = (ProgressBar) view.findViewById(R.id.pbOtherProfileLoading);
        btnChat = view.findViewById(R.id.btnChat);
        ratingBar = (RatingBar) view.findViewById(R.id.rbOtherUserProfile);

        selectedOfferings = new ArrayList<>();
        adapter = new SmallOfferingAdapter(context, selectedOfferings);

        rvOfferings.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        rvOfferings.setLayoutManager(linearLayoutManager);

        loadPost = new LoadPost();
        query = new Query();
    }
}