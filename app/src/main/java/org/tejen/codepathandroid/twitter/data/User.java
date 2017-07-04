package org.tejen.codepathandroid.twitter.data;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;
import org.tejen.codepathandroid.twitter.TwitterApp;

import cz.msebera.android.httpclient.Header;

/**
 * Created by tejen on 6/6/17.
 */

@Parcel
public class User {

    // list the attributes
    private String name;
    private long uid;
    private String screenName;
    private String profileImageUrl;
    private boolean isVerified;

    public String getName(){ return name; }
    public long getUid(){ return uid; }
    public String getScreenName(){ return screenName; }
    public String getProfileImageUrl(){ return profileImageUrl; }
    public boolean isVerified(){ return isVerified; }

    // deserialize JSON
    public static User fromJSON(JSONObject jsonObject) throws JSONException {
        User user = new User();

        // extract and fill the values
        user.name = jsonObject.getString("name");
        user.uid = jsonObject.getLong("id");
        user.screenName = jsonObject.getString("screen_name");
        user.profileImageUrl = jsonObject.getString("profile_image_url");
        user.isVerified = jsonObject.getBoolean("verified");

        return user;
    }

    public interface UserCallbackInterface {
        void onUserAvailable(User currentUser);
    }

    public static void getCurrentUser(final UserCallbackInterface handler) {
        TwitterClient client = TwitterApp.getRestClient();
        client.verifyCredentials(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    handler.onUserAvailable(User.fromJSON(response));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

}
