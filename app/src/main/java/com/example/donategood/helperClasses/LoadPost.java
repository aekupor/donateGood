package com.example.donategood.helperClasses;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.donategood.R;
import com.example.donategood.models.Charity;
import com.example.donategood.models.Offering;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.io.Serializable;

public class LoadPost {

    //set charity name and image from an offering
    public void setCharity(Offering offering, Context context, TextView tvCharity, ImageView ivCharityProfile) {
        if (offering.getCharity() != null) {
            try {
                Charity charity = offering.getCharity().fetchIfNeeded();
                tvCharity.setText(charity.getTitle());
                ivCharityProfile.setImageDrawable(null);
                ParseFile charityImage = offering.getCharity().getImage();
                if (charityImage != null) {
                    Glide.with(context)
                            .load(charityImage.getUrl())
                            .into(ivCharityProfile);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            tvCharity.setText("No charity currently");
        }
    }

    //set charity name and image from a charity
    public void setCharityWithCharity(Charity charity, Context context, TextView tvCharity, ImageView ivCharityProfile) {
            tvCharity.setText(charity.getTitle());
            ivCharityProfile.setImageDrawable(null);
            ParseFile charityImage = charity.getImage();
            if (charityImage != null) {
                Glide.with(context)
                        .load(charityImage.getUrl())
                        .into(ivCharityProfile);
            }
    }

    //sets image for an offering from a ParseFile into specified ImageView
    public void setPostImage(ParseFile image, Context context, ImageView ivOfferingPhoto) {
        ivOfferingPhoto.setImageDrawable(null);
        if (image != null) {
            Glide.with(context)
                    .load(image.getUrl())
                    .into(ivOfferingPhoto);
        }
    }

    //sets the title, price, and selling user name of an offering from the specified offering
    public void setTitlePriceUser(Offering offering, TextView tvTitle, TextView tvPrice, TextView tvUser) {
        tvTitle.setText(offering.getTitle());
        tvPrice.setText("$" + Integer.toString(offering.getPrice()));
        try {
            tvUser.setText(offering.getUser().fetchIfNeeded().getUsername());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    //sets the name and photo of a user from the specified ParseUser
    public void setUser(ParseUser user, Context context, TextView tvTitle, ImageView ivPhoto) {
        try {
            tvTitle.setText(user.fetchIfNeeded().getUsername());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Serializable profileImage;

        if (user.getParseFile("profileImage") != null) {
            profileImage = user.getParseFile("profileImage").getUrl();
        } else {
            profileImage = R.drawable.ic_baseline_person_outline_24;
        }

        Glide.with(context)
                .load(profileImage)
                .circleCrop()
                .into(ivPhoto);
    }

    //sets the name and photo of a user from their name and url of their FB picture
    public void setUserFromFB(String name, String url, Context context, TextView tvTitle, ImageView ivPhoto) {
        tvTitle.setText(name);
        Glide.with(context)
                .load(url)
                .circleCrop()
                .into(ivPhoto);
    }
}
