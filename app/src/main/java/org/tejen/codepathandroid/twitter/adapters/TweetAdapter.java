package org.tejen.codepathandroid.twitter.adapters;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

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
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View tweetView = inflater.inflate(R.layout.item_tweet, parent, false);
        ViewHolder viewHolder = new ViewHolder(tweetView);
        return viewHolder;
    }

    // bind the values based on the position of the element

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // get the data according to position
        Tweet tweet = mTweets.get(position);

        // populate the views according to this data
        holder.tvUsername.setText(tweet.user.name);
        holder.tvUserScreenname.setText("@" + tweet.user.screenName);
        holder.tvBody.setText(tweet.body);
        holder.tvAge.setText(tweet.getRelativeTimeAgo());

        Glide.with(context).load(tweet.user.profileImageUrl).into(holder.ivProfileImage);

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

        public ViewHolder(View itemView) {
            super(itemView);

            // perform findViewById lookups
            ivProfileImage = (ImageView) itemView.findViewById(R.id.ivProfileImage);
            tvUsername = (TextView) itemView.findViewById(R.id.tvUserName);
            tvUserScreenname = (TextView) itemView.findViewById(R.id.tvUserScreenname);
            tvBody = (TextView) itemView.findViewById(R.id.tvBody);
            tvAge = (TextView) itemView.findViewById(R.id.tvAge);
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

}
