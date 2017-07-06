package org.tejen.codepathandroid.twitter.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.tejen.codepathandroid.twitter.R;
import org.tejen.codepathandroid.twitter.activities.MainActivity;
import org.tejen.codepathandroid.twitter.data.Tweet;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by tejen on 6/6/17.
 */

public class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

    public interface TweetItemListener {
        public void onReplyButton(Tweet tweet);
    }

    private List<Tweet> mTweets;
    Context context;
    private TweetItemListener listener;
    private static int defaultMediaWidth = 250;

    // pass in the Tweets array in the constructor
    public TweetAdapter(List<Tweet> tweets, TweetItemListener parentListener) {
        mTweets = tweets;
        listener = parentListener;
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
                viewHolder.tvRetweetCount.setText(Tweet.format(mTweets.get(position).getRetweetCount()));
            }
        });

        viewHolder.buttonFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                mTweets.get(position).toggleFavorite(new JsonHttpResponseHandler());
                updateButton(viewHolder.buttonFavorite, mTweets.get(position).isFavorited(), R.drawable.ic_vector_heart_stroke, R.drawable.ic_vector_heart, R.color.twitter_red);
                viewHolder.tvFavoriteCount.setText(Tweet.format(mTweets.get(position).getFavoriteCount()));
            }
        });

        viewHolder.buttonReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onReplyButton(mTweets.get(viewHolder.getAdapterPosition()));
            }
        });

        viewHolder.llMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) context).startBrowserActivity(mTweets.get(viewHolder.getAdapterPosition()).getMediaUrl());
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // get the data according to position
        Tweet tweet = mTweets.get(position);

        // populate the views according to this data
        holder.tvUserName.setText(tweet.getUser().getName());
        holder.tvUserScreenname.setText("@" + tweet.getUser().getScreenName());
        holder.tvBody.setText(tweet.getBody());
        holder.tvAge.setText(tweet.getRelativeTimeAgo());
        holder.tvRetweetCount.setText(Tweet.format(tweet.getRetweetCount()));
        holder.tvFavoriteCount.setText(Tweet.format(tweet.getFavoriteCount()));
        holder.ivVerifiedBadge.setVisibility(tweet.getUser().isVerified() ? View.VISIBLE : View.GONE);
        updateButton(holder.buttonRetweet, tweet.isRetweeted(), R.drawable.ic_vector_retweet_stroke, R.drawable.ic_vector_retweet, R.color.twitter_blue);
        updateButton(holder.buttonFavorite, tweet.isFavorited(), R.drawable.ic_vector_heart_stroke, R.drawable.ic_vector_heart, R.color.twitter_red);

        if(tweet.getRetweetedBy() != null) {
            holder.ivRetweetedIcon.setVisibility(View.VISIBLE);
            holder.tvRetweetedBy.setVisibility(View.VISIBLE);
            holder.tvRetweetedBy.setText(tweet.getRetweetedBy().getName() + " Retweeted");
        } else {
            holder.ivRetweetedIcon.setVisibility(View.GONE);
            holder.tvRetweetedBy.setVisibility(View.GONE);
        }

        if(!tweet.getMediaUrl().isEmpty()) {
            holder.llMedia.setVisibility(View.INVISIBLE);
            try {
                holder.tvMediaUrl.setText((new URL(tweet.getMediaUrl())).getHost());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            tweet.getMediaThumbnail(new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try {
                        String responseString = new String(responseBody, getCharset());
                        JSONObject response = new JSONObject(responseString);
                        if(response.has("result") && !response.getString("result").isEmpty()) {
                            try {
                                AsyncHttpClient client = new AsyncHttpClient();
                                client.get(response.getString("result"), null, new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] fileData) {
                                        Bitmap image = BitmapFactory.decodeByteArray(fileData, 0, fileData.length);
                                        float aspectRatio = image.getWidth() /
                                                (float) image.getHeight();
                                        int width = TweetAdapter.defaultMediaWidth;
                                        Log.i("WIDTH", Integer.toString(width));
                                        int height = Math.round(width / aspectRatio);
                                        image = Bitmap.createScaledBitmap(
                                                image, width, height, false);
                                        holder.ivMedia.setImageDrawable(ImageHelper.getRoundedCornerBitmap(image, 10, context.getResources()));
                                        holder.llMedia.setVisibility(View.VISIBLE);
                                    }
                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            holder.llMedia.setVisibility(View.GONE);
                        }
                    } catch (UnsupportedEncodingException | JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                }
            });
        } else {
            holder.llMedia.setVisibility(View.GONE);
        }

        Glide.with(context).load(tweet.getUser().getProfileImageUrl()).into(holder.ivProfileImage);
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    // create ViewHolder class

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvRetweetedBy;
        public ImageView ivRetweetedIcon;
        public ImageView ivProfileImage;
        public TextView tvUserName;
        public TextView tvUserScreenname;
        public TextView tvBody;
        public TextView tvAge;
        public TextView tvRetweetCount;
        public TextView tvFavoriteCount;
        public ImageButton buttonRetweet;
        public ImageButton buttonFavorite;
        public ImageButton buttonReply;
        public ImageView ivVerifiedBadge;
        public LinearLayout llMedia;
        public ImageView ivMedia;
        public TextView tvMediaUrl;

        public ViewHolder(View itemView) {
            super(itemView);

            // perform findViewById lookups
            tvRetweetedBy = (TextView) itemView.findViewById(R.id.tvRetweetedBy);
            ivRetweetedIcon = (ImageView) itemView.findViewById(R.id.ivRetweetedIcon);
            ivProfileImage = (ImageView) itemView.findViewById(R.id.ivProfileImage);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
            tvUserScreenname = (TextView) itemView.findViewById(R.id.tvUserScreenname);
            tvBody = (TextView) itemView.findViewById(R.id.tvBody);
            tvAge = (TextView) itemView.findViewById(R.id.tvAge);
            tvRetweetCount = (TextView) itemView.findViewById(R.id.tvRetweetCount);
            tvFavoriteCount = (TextView) itemView.findViewById(R.id.tvFavoriteCount);
            buttonRetweet = (ImageButton) itemView.findViewById(R.id.buttonRetweet);
            buttonFavorite = (ImageButton) itemView.findViewById(R.id.buttonFavorite);
            buttonReply = (ImageButton) itemView.findViewById(R.id.buttonReply);
            ivVerifiedBadge = (ImageView) itemView.findViewById(R.id.ivVerifiedBadge);
            llMedia = (LinearLayout) itemView.findViewById(R.id.llMedia);
            ivMedia = (ImageView) itemView.findViewById(R.id.ivMedia);
            tvMediaUrl = (TextView) itemView.findViewById(R.id.tvMediaUrl);

            if(TweetAdapter.defaultMediaWidth == 250) {
                ivMedia.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        if(ivMedia.getWidth() > 0) {
                            TweetAdapter.defaultMediaWidth = ivMedia.getWidth();
                        }
                        ivMedia.getViewTreeObserver().removeOnPreDrawListener(this);
                        return true;
                    }
                });
            }

            // define standard attributes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ivProfileImage.setClipToOutline(true);
                tvBody.setLetterSpacing((float) 0.005);
                tvUserScreenname.setLetterSpacing((float) -0.025);
                tvAge.setLetterSpacing((float) -0.025);
            }
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

