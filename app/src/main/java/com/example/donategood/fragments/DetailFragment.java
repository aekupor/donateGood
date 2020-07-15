package com.example.donategood.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.donategood.helperClasses.LoadPost;
import com.example.donategood.helperClasses.Query;
import com.example.donategood.R;
import com.example.donategood.models.Offering;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

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
    private Button btnPurchase;

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