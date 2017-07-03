package org.tejen.codepathandroid.twitter.data;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by tejen on 6/6/17.
 */

public class Tweet implements Parcelable {

    // list out the attributes
    private String body;
    private long uid; // database ID for the tweet
    private User user;
    private Date createdAt;

    private long retweetCount;
    private boolean retweeted;

    private long favoriteCount;
    private boolean favorited;

    private Tweet(Parcel in) {
        body = in.readString();
        uid= in.readLong();
        user = in.readParcelable(getClass().getClassLoader());
        createdAt = new Date(in.readLong());
        retweetCount = in.readLong();
        retweeted = in.readInt() == 1;
        favoriteCount = in.readLong();
        favorited = in.readInt() == 1;
    }

    public Tweet() {

    }

    public User getUser() {
        return user;
    }

    public String getBody() {
        return body;
    }

    // deserialize JSON
    public static Tweet fromJSON(JSONObject jsonObject) throws JSONException{
        Tweet tweet = new Tweet();

        // extract the values from JSON
        tweet.body = jsonObject.getString("text");
        tweet.uid = jsonObject.getLong("id");
        try {
            tweet.createdAt = Tweet.parseTwitterDate(jsonObject.getString("created_at"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
        tweet.retweetCount = jsonObject.getLong("retweet_count");
        tweet.retweeted = jsonObject.getBoolean("retweeted");
        tweet.favoriteCount = jsonObject.getLong("favorite_count");
        tweet.favorited= jsonObject.getBoolean("favorited");

        return tweet;
    }

    public static ArrayList<Tweet> multipleFromJSON(JSONArray jsonArray) throws JSONException{
        ArrayList<Tweet> tweets = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            Tweet tweet = new Tweet();
            JSONObject obj = (JSONObject) jsonArray.get(i);

            // extract the values from JSON
            tweet.body = obj.getString("text");
            tweet.uid = obj.getLong("id");
            try {
                tweet.createdAt = Tweet.parseTwitterDate(obj.getString("created_at"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tweet.user = User.fromJSON(obj.getJSONObject("user"));

            tweets.add(tweet);
        }

        return tweets;
    }

    public static Date parseTwitterDate(String date) throws ParseException
    {
        final String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);
        return sf.parse(date);
    }

    public String getRelativeTimeAgo() {
        Date system_date = createdAt;
        Date user_date = new Date();
        double diff = Math.floor((user_date.getTime() - system_date.getTime()) / 1000);
        if (diff <= 1) {return "just now";}
        if (diff < 20) {return diff + "s";}
        if (diff < 40) {return "30s";}
        if (diff < 60) {return "45s";}
        if (diff <= 90) {return "1m";}
        if (diff <= 3540) {return Math.round(diff / 60) + "m";}
        if (diff <= 5400) {return "1h";}
        if (diff <= 86400) {return Math.round(diff / 3600) + "h";}
        if (diff <= 129600) {return "1d";}
        if (diff < 604800) {return Math.round(diff / 86400) + "d";}
        if (diff <= 777600) {return "1w";}
        return Math.round(diff / 604800) + "w";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(body);
        out.writeLong(uid);
        out.writeParcelable(user, flags);
        out.writeLong(createdAt.getTime());
        out.writeLong(retweetCount);
        out.writeInt(favorited ? 1 : 0);
        out.writeLong(favoriteCount);
        out.writeLong(retweeted ? 1 : 0);
    }

    public static final Parcelable.Creator<Tweet> CREATOR
            = new Parcelable.Creator<Tweet>() {

        @Override
        public Tweet createFromParcel(Parcel in) {
            return new Tweet(in);
        }

        @Override
        public Tweet[] newArray(int size) {
            return new Tweet[size];
        }
    };

    public long getId() {
        return uid;
    }
}
