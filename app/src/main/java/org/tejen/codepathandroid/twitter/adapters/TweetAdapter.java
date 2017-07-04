package org.tejen.codepathandroid.twitter.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.tejen.codepathandroid.twitter.R;
import org.tejen.codepathandroid.twitter.data.Tweet;

import java.util.List;

/**
 * Created by tejen on 6/6/17.
 */

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

    private List<Tweet> mTweets;
    Context context;

    // pass in the Tweets array in the constructor
    public TweetAdapter(List<Tweet> tweets) {
        mTweets = tweets;
    }

    // for each row, inflate the layout and cache references into ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View tweetView = inflater.inflate(R.layout.item_tweet, parent, false);
        final ViewHolder viewHolder = new ViewHolder(tweetView);

        viewHolder.buttonRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                mTweets.get(position).toggleRetweet(new JsonHttpResponseHandler());
                updateButton(viewHolder.buttonRetweet, mTweets.get(position).isRetweeted(), R.drawable.ic_vector_retweet_stroke, R.drawable.ic_vector_retweet, R.color.twitter_blue);
                viewHolder.tvRetweetCount.setText(Long.toString(mTweets.get(position).getRetweetCount()));
            }
        });

        viewHolder.buttonFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                mTweets.get(position).toggleFavorite(new JsonHttpResponseHandler());
                updateButton(viewHolder.buttonFavorite, mTweets.get(position).isFavorited(), R.drawable.ic_vector_heart_stroke, R.drawable.ic_vector_heart, R.color.twitter_red);
                viewHolder.tvFavoriteCount.setText(Long.toString(mTweets.get(position).getFavoriteCount()));
            }
        });

        return viewHolder;
    }

    private void updateButton(ImageButton b, boolean isActive, int strokeResId, int fillResId, int activeColor) {
        b.setImageResource(isActive ? fillResId : strokeResId);
        b.setColorFilter(ContextCompat.getColor(context, isActive ? activeColor : R.color.twitter_gray));
    }

    // bind the values based on the position of the element
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // get the data according to position
        Tweet tweet = mTweets.get(position);

        // populate the views according to this data
        holder.tvUsername.setText(tweet.getUser().name);
        holder.tvUserScreenname.setText("@" + tweet.getUser().screenName);
        holder.tvBody.setText(tweet.getBody());
        holder.tvAge.setText(tweet.getRelativeTimeAgo());
        holder.tvRetweetCount.setText(Long.toString(tweet.getRetweetCount()));
        holder.tvFavoriteCount.setText(Long.toString(tweet.getFavoriteCount()));
        updateButton(holder.buttonRetweet, tweet.isRetweeted(), R.drawable.ic_vector_retweet_stroke, R.drawable.ic_vector_retweet, R.color.twitter_blue);
        updateButton(holder.buttonFavorite, tweet.isFavorited(), R.drawable.ic_vector_heart_stroke, R.drawable.ic_vector_heart, R.color.twitter_red);

        Glide.with(context).load(tweet.getUser().profileImageUrl).into(holder.ivProfileImage);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.ivProfileImage.setClipToOutline(true);
            holder.tvBody.setLetterSpacing((float) 0.01);
        }
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    // create ViewHolder class

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView ivProfileImage;
        public TextView tvUsername;
        public TextView tvUserScreenname;
        public TextView tvBody;
        public TextView tvAge;
        public TextView tvRetweetCount;
        public TextView tvFavoriteCount;
        public ImageButton buttonRetweet;
        public ImageButton buttonFavorite;

        public ViewHolder(View itemView) {
            super(itemView);

            // perform findViewById lookups
            ivProfileImage = (ImageView) itemView.findViewById(R.id.ivProfileImage);
            tvUsername = (TextView) itemView.findViewById(R.id.tvUserName);
            tvUserScreenname = (TextView) itemView.findViewById(R.id.tvUserScreenname);
            tvBody = (TextView) itemView.findViewById(R.id.tvBody);
            tvAge = (TextView) itemView.findViewById(R.id.tvAge);
            tvRetweetCount = (TextView) itemView.findViewById(R.id.tvRetweetCount);
            tvFavoriteCount = (TextView) itemView.findViewById(R.id.tvFavoriteCount);
            buttonRetweet = (ImageButton) itemView.findViewById(R.id.buttonRetweet);
            buttonFavorite = (ImageButton) itemView.findViewById(R.id.buttonFavorite);
        }
    }

    public void clear() {
        int size = this.mTweets.size();
        this.mTweets.clear();
        notifyItemRangeRemoved(0, size);
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        this.mTweets.addAll(list);
        notifyDataSetChanged();
    }

    public Tweet getAt(int position) {
        return this.mTweets.get(position);
    }

}
