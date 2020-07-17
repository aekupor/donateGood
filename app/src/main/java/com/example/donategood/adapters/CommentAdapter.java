package com.example.donategood.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.donategood.R;
import com.example.donategood.helperClasses.LoadPost;
import com.example.donategood.models.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    public static final String TAG = "CommentAdapter";

    private Context context;
    private List<Comment> comments;

    public CommentAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        comments.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Comment> list) {
        comments.addAll(list);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvUsername;
        TextView tvDescription;
        ImageView ivProfileImage;
        RatingBar rbRating;

        LoadPost loadPost;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUsername = itemView.findViewById(R.id.tvUsernameComment);
            tvDescription = itemView.findViewById(R.id.tvTextComment);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImageComment);
            rbRating = itemView.findViewById(R.id.tvRatingComment);

            loadPost = new LoadPost();

            itemView.setOnClickListener(this);
        }

        public void bind(Comment comment) {
            loadPost.setUser(comment.getByUser(), context, tvUsername, ivProfileImage);
            tvDescription.setText(comment.getText());
            rbRating.setNumStars(comment.getRating());
        }

        public void onClick(View v) {
            // gets item position
            int position = getAdapterPosition();
            // make sure the position is valid, i.e. actually exists in the view
            if (position != RecyclerView.NO_POSITION) {
                // get the post at the position
                Comment comment = comments.get(position);

                Log.i(TAG, "comment clicked: " + comment.getByUser().getUsername());
            }
        }
    }
}