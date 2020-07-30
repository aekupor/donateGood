package com.example.donategood.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.donategood.OnSwipeTouchListener;
import com.example.donategood.R;
import com.example.donategood.adapters.CommentAdapter;
import com.example.donategood.adapters.SmallOfferingAdapter;
import com.example.donategood.helperClasses.CommentLoader;
import com.example.donategood.helperClasses.LoadPost;
import com.example.donategood.helperClasses.NotificationLoader;
import com.example.donategood.helperClasses.Query;
import com.example.donategood.helperClasses.Recommend;
import com.example.donategood.models.Comment;
import com.example.donategood.models.Offering;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class DetailFragment extends Fragment implements ComposeCommentFragment.ComposeCommentDialogListener {

    public static final String TAG = "DetailFragment";

    private String offeringId;
    public Query query;
    public Offering offering;
    private LoadPost loadPost;
    private NotificationLoader notificationLoader;
    private Recommend recommend;
    private CommentLoader commentLoader;

    private TextView tvTitle;
    private TextView tvPrice;
    private TextView tvUser;
    private TextView tvCharity;
    private ImageView ivCharityImage;
    private ImageView ivOfferingPhoto;
    private Button btnPurchase;
    private Button btnComment;
    private TextView tvQuantityLeft;
    public TextView tvCommentTitle;
    private LinearLayout layoutImages;
    private RatingBar ratingBar;
    private TextView tvDescription;
    private Button btnEdit;

    private RecyclerView rvRecommendedOfferings;
    private SmallOfferingAdapter adapter;
    private List<Offering> reccomendedOfferings;
    private RecyclerView rvComments;
    public CommentAdapter commentAdapter;
    public List<Comment> allComments;
    public Integer numComments;

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

        view.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
            @Override
            public void onSwipeRight() {
                Log.i(TAG, "onSwipeRight, going to home fragment");
                final FragmentManager fragmentManager = ((AppCompatActivity)getContext()).getSupportFragmentManager();
                Fragment fragment = new HomeFragment();
                fragmentManager.beginTransaction().setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right).replace(R.id.flContainer, fragment).commit();
            }
        });

        //find all variables
        tvTitle = view.findViewById(R.id.tvDetailTitle);
        tvPrice = view.findViewById(R.id.tvDetailPrice);
        tvCharity = view.findViewById(R.id.tvDetailCharity);
        tvUser = view.findViewById(R.id.tvDetailUser);
        ivCharityImage = view.findViewById(R.id.ivDetailCharityImage);
        btnPurchase = view.findViewById(R.id.btnPurchase);
        ivOfferingPhoto = view.findViewById(R.id.ivDetailOfferingPhoto);
        shareButton = (ShareButton)view.findViewById(R.id.fbShareButtonDetail);
        rvRecommendedOfferings = view.findViewById(R.id.rvRecommendOfferings);
        btnComment = view.findViewById(R.id.btnComment);
        tvQuantityLeft = view.findViewById(R.id.tvQuantityLeft);
        tvCommentTitle = view.findViewById(R.id.tvViewCommentsTitle);
        layoutImages = (LinearLayout) view.findViewById(R.id.linearImages);
        ratingBar = (RatingBar) view.findViewById(R.id.rbDetail);
        rvComments = view.findViewById(R.id.rvComments);
        tvDescription = view.findViewById(R.id.tvDetailDescription);
        btnEdit = view.findViewById(R.id.btnEditOffering);

        numComments = 0;
        loadPost = new LoadPost();
        query = new Query();
        notificationLoader = new NotificationLoader();
        recommend = new Recommend();
        commentLoader = new CommentLoader();

        //set up recycler view and adapter for reccomended offerings
        reccomendedOfferings = new ArrayList<>();
        adapter = new SmallOfferingAdapter(getContext(), reccomendedOfferings);

        rvRecommendedOfferings.setAdapter(adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        rvRecommendedOfferings.setLayoutManager(linearLayoutManager);

        //set up recycler view and adapter for comments
        allComments = new ArrayList<>();
        commentAdapter = new CommentAdapter(getContext(), allComments);

        rvComments.setAdapter(commentAdapter);
        LinearLayoutManager linearLayoutManagerComment = new LinearLayoutManager(getContext());
        rvComments.setLayoutManager(linearLayoutManagerComment);

        //set onClickListeners
        tvCharity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToOtherFragment("charity");
            }
        });

        ivCharityImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToOtherFragment("charity");
            }
        });

        tvUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToOtherFragment("user");
            }
        });

        btnPurchase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                purchaseItem();
            }
        });

        btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "comment button clicked");
                showEditDialog();
            }
        });

        findOffering();
    }

    //finds the offering to display on detail page (offering that the user clicked on)
    private void findOffering() {
        query.findOffering(offeringId, new FindCallback<Offering>() {
            @Override
            public void done(List<Offering> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with getting post", e);
                    return;
                }
                offering = objects.get(0);
                Log.i(TAG, "got offering with title: " + offering.getTitle());

                loadInformation();
            }
        });
    }

    //sets all variables to the appropriate values
    private void loadInformation() {
        loadPost.setTitlePriceUser(offering, tvTitle, tvPrice, tvUser);
        loadPost.setCharity(offering, getContext(), tvCharity, ivCharityImage);

        if (offering.getRating() == 0) {
            ratingBar.setVisibility(View.INVISIBLE);
        } else {
            ratingBar.setNumStars(offering.getRating());
        }

        if (offering.getDescription() == null) {
            tvDescription.setVisibility(View.INVISIBLE);
        } else {
            tvDescription.setText(offering.getDescription());
        }

        tvQuantityLeft.setText("Quantity Left: " + offering.getQuantityLeft().toString());
        if (offering.getQuantityLeft() == 0) {
            btnPurchase.setVisibility(View.INVISIBLE);
        }

        if (offering.getUser().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            //only show edit button if selling user of offering is the current signed in user
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.i(TAG, "edit button clicked");
                    goToOtherFragment("compose");
                }
            });
        } else {
            btnEdit.setVisibility(View.INVISIBLE);
        }

        loadPost.setMultipleImages(offering, getContext(), ivOfferingPhoto, layoutImages);
        setShareButton();
        recommend.queryRecommendedPosts(query, offering, adapter, reccomendedOfferings);
        commentLoader.queryComments(this);
    }

    //initialize FB share button with information about offering
    private void setShareButton() {
        content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(offering.getImage().getUrl()))
                .setQuote("Check out this " + offering.getTitle() + " that I am purchasing on Donate Good!")
                .setShareHashtag(new ShareHashtag.Builder()
                        .setHashtag("#DonateGood")
                        .build())
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

    private void goToOtherFragment(String fragmentName) {
        final FragmentManager fragmentManager = ((AppCompatActivity)getContext()).getSupportFragmentManager();
        Fragment fragment = null;
        if (fragmentName.equals("charity")) {
            //go to charity fragment
            fragment = CharityFragment.newInstance(offering.getCharity().getTitle());
        } else if (fragmentName.equals("user")) {
            //go to other user profile fragment
            Bundle bundle = new Bundle();
            bundle.putParcelable("user", offering.getUser());
            fragment = OtherUserProfileFragment.newInstance(bundle);
        } else if (fragmentName.equals("compose")) {
            // go to compose fragment to edit the offering
            Bundle bundle = new Bundle();
            bundle.putParcelable("offering", offering);
            fragment = ComposeFragment.newInstance(bundle);
        }

        fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).addToBackStack(null).commit();
    }

    private void purchaseItem() {
        Log.i(TAG, "purchase item");

        //open venmo
        Intent implicit = new Intent(Intent.ACTION_VIEW, Uri.parse("venmo://paycharge?txn=pay&recipients="
                + offering.getUser().get("venmoName") + "&amount="
                + offering.getPrice().toString() + "&note=" + offering.getTitle()));

        if (implicit.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(implicit);
        } else {
            Toast.makeText(getContext(), "You must have venmo installed", Toast.LENGTH_SHORT).show();
        }

        //remove one from the quantity left
        Integer quantityLeft = offering.getQuantityLeft() - 1;
        Toast.makeText(getContext(), "Thank you for your purchase!", Toast.LENGTH_SHORT).show();
        tvQuantityLeft.setText("Quantity Left: " + quantityLeft.toString());
        updateQuantityLeft(quantityLeft);

        //creates notification on the "notifications" tab within the app
        notificationLoader.createNotification(offering);
    }

    //called after user has created a comment
    @Override
    public void onFinishEditDialog(String inputText, String rating) {
        Log.i(TAG, "got comment with text: " + inputText + " and rating: " + rating);

        //add comment to adapter
        allComments.add(commentLoader.saveComment(inputText, rating, getContext(), offering));
        commentAdapter.notifyDataSetChanged();
        tvCommentTitle.setVisibility(View.VISIBLE);

        updateOfferingRating(rating);
    }

    //open ComposeCommentFragment
    private void showEditDialog() {
        FragmentManager fm = getFragmentManager();
        ComposeCommentFragment composeCommentFragment = (ComposeCommentFragment) ComposeCommentFragment.newInstance();
        //sets the target fragment for use later when sending results
        composeCommentFragment.setTargetFragment(DetailFragment.this, 300);
        composeCommentFragment.show(fm, "fragment_compose_comment");
    }

    //update rating of offering based on rating from new comment
    private void updateOfferingRating(String rating) {
        Integer offeringRating = offering.getRating();
        if (offeringRating == 0) {
            offering.setRating(Integer.parseInt(rating));
            ratingBar.setVisibility(View.INVISIBLE);
        } else {
            Log.i(TAG, "current rating: " + offeringRating.toString() + "num comments: " + numComments.toString());
            offeringRating = (offeringRating * numComments) + Integer.parseInt(rating);
            numComments++;
            offeringRating = offeringRating / numComments;
            offering.setRating(offeringRating);
            ratingBar.setNumStars(offeringRating);
        }
        offering.saveInBackground();
    }

    //update quantity left of offering
    private void updateQuantityLeft(Integer quantityLeft) {
        if (quantityLeft == 0) {
            //determine if all quantities of the offering has been bought
            offering.setIsBought(true);
            btnPurchase.setVisibility(View.INVISIBLE);
        }
        offering.setBoughtBy(ParseUser.getCurrentUser());
        offering.addToBoughtByArray(ParseUser.getCurrentUser());
        offering.setQuantityLeft(quantityLeft);
        offering.saveInBackground();
    }
}