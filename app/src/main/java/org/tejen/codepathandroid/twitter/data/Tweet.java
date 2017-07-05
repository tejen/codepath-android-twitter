package org.tejen.codepathandroid.twitter.data;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;
import org.tejen.codepathandroid.twitter.TwitterApp;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Created by tejen on 6/6/17.
 */

@Parcel
public class Tweet {

    // list out the attributes
    private String body;
    private long uid; // database ID for the tweet
    private User user;
    private Date createdAt;

    private User retweetedBy;

    private long retweetCount;
    private boolean retweeted;

    private long favoriteCount;
    private boolean favorited;

    private String mediaUrl = "";

    public long getId() {
        return uid;
    }
    public User getUser() {
        return user;
    }
    public String getBody() {
        return body;
    }
    public long getRetweetCount() { return retweetCount; }
    public long getFavoriteCount() { return favoriteCount; }
    public boolean isRetweeted() { return retweeted; }
    public boolean isFavorited() { return favorited; }
    public User getRetweetedBy() { return retweetedBy; }
    public String getMediaUrl() { return mediaUrl; }

    public void toggleRetweet(JsonHttpResponseHandler handler) {
        TwitterApp.getRestClient().retweet(uid, retweeted ^= true, handler);
        retweetCount += retweeted ? 1 : -1;
    }
    public void toggleFavorite(JsonHttpResponseHandler handler) {
        TwitterApp.getRestClient().favorite(uid, favorited ^= true, handler);
        favoriteCount += favorited ? 1 : -1;
    }

    // deserialize JSON & extract values from it
    public static Tweet fromJSON(JSONObject jsonObject) throws JSONException, ParseException{
        Tweet tweet = new Tweet();

        tweet.uid = jsonObject.getLong("id");

        if(jsonObject.has("retweeted_status")) {
            // extract all data needed from root tweet
            tweet.retweetedBy = User.fromJSON(jsonObject.getJSONObject("user"));
            // discard root tweet (i.e. replace it with original tweet)
            jsonObject = jsonObject.getJSONObject("retweeted_status");
        }

        tweet.body = jsonObject.getString("text");
        tweet.createdAt = Tweet.parseTwitterDate(jsonObject.getString("created_at"));
        tweet.user = User.fromJSON(jsonObject.getJSONObject("user"));
        tweet.retweeted = jsonObject.getBoolean("retweeted");
        tweet.favorited = jsonObject.getBoolean("favorited");
        tweet.retweetCount = jsonObject.getLong("retweet_count");
        tweet.favoriteCount = jsonObject.getLong("favorite_count");

        if(jsonObject.has("entities")) {
            String shortUrl = null;
            String expandedUrl = null;
            String[] entityTypes = { "urls", "media" }; // we're supporting two entity types. prioritized last to first (app will display media rather than url, if both exist in a single tweet)
            for (String type: entityTypes) {
                if(jsonObject.getJSONObject("entities").has(type)
                        && jsonObject.getJSONObject("entities").getJSONArray(type).length() > 0) {
                    shortUrl = ((JSONObject) jsonObject.getJSONObject("entities").getJSONArray(type).get(0)).getString("url");
                    expandedUrl = ((JSONObject) jsonObject.getJSONObject("entities").getJSONArray(type).get(0)).getString("expanded_url");
                    for (int i = 0; i < jsonObject.getJSONObject("entities").getJSONArray(type).length(); i++) {
                        tweet.body = tweet.body.replace(((JSONObject) jsonObject.getJSONObject("entities").getJSONArray(type).get(i)).getString("url"), "");
                    }
                }
            }
            if(shortUrl != null && expandedUrl != null) {
                tweet.body = tweet.body.replace(" " + shortUrl, "");
                tweet.mediaUrl = expandedUrl;
            }
        }

        return tweet;
    }

    public void getMediaThumbnail(final AsyncHttpResponseHandler handler) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://tejen.net/sub/codepath/twitter/ogimage.php?externalsite=" + mediaUrl, handler);
        // endpoint returns JSONObject with parameter "result" containing an image URL as String
    }

    public static ArrayList<Tweet> multipleFromJSON(JSONArray jsonArray) throws JSONException, ParseException{
        ArrayList<Tweet> tweets = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = (JSONObject) jsonArray.get(i);
            Tweet tweet = fromJSON(obj);
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

    private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
    static {
        suffixes.put(1_000L, "k");
        suffixes.put(1_000_000L, "M");
        suffixes.put(1_000_000_000L, "G");
        suffixes.put(1_000_000_000_000L, "T");
        suffixes.put(1_000_000_000_000_000L, "P");
        suffixes.put(1_000_000_000_000_000_000L, "E");
    }

    public static String format(long value) {
        //Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
        if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
        if (value < 0) return "-" + format(-value);
        if (value < 10000) return NumberFormat.getNumberInstance(Locale.US).format(value); //deal with easy case

        Map.Entry<Long, String> e = suffixes.floorEntry(value);
        Long divideBy = e.getKey();
        String suffix = e.getValue();

        long truncated = value / (divideBy / 10); //the number part of the output times 10
        boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
        return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
    }

}
