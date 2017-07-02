package org.tejen.codepathandroid.twitter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tejen.codepathandroid.twitter.TwitterApp;
import org.tejen.codepathandroid.twitter.adapters.TweetAdapter;
import org.tejen.codepathandroid.twitter.data.Tweet;
import org.tejen.codepathandroid.twitter.data.TwitterClient;
import org.tejen.codepathandroid.twitter.views.DividerItemDecoration;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity {

    private TwitterClient client;
    TweetAdapter tweetAdapter;
    ArrayList<Tweet> tweets;
    RecyclerView rvTweets;

    private final int COMPOSE_REQUEST_CODE = 10;
    private final int COMPOSE_RESULT_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.tejen.codepathandroid.twitter.R.layout.activity_timeline);

//        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME);

        client = TwitterApp.getRestClient();

        rvTweets = (RecyclerView) findViewById(org.tejen.codepathandroid.twitter.R.id.rvTweet); // find the RecyclerView
        tweets = new ArrayList<>(); // init the arraylist (data source)
        tweetAdapter = new TweetAdapter(tweets); // construct the adapter from this datasource
        rvTweets.setLayoutManager(new LinearLayoutManager(this)); // RecyclerView setup (layout manager, use adapter)
        rvTweets.setAdapter(tweetAdapter); // set the adapter

        rvTweets.addItemDecoration(new DividerItemDecoration(this));

        populateTimeline();
        Log.d("MyApp", "I am here");
        Log.wtf("here a tag", "there a tag");
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(org.tejen.codepathandroid.twitter.R.menu.menu_main, menu);
//
//        // set onClick listener for Compose button in menu
//        menu.findItem(org.tejen.codepathandroid.twitter.R.id.action_compose).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                Intent i = new Intent(TimelineActivity.this, ComposeActivity.class);
//                startActivityForResult(i, COMPOSE_REQUEST_CODE);
//                return true;
//            }
//        });
//
//        return true;
//    }

    private void populateTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TwitterClient", response.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
//                Log.d("TwitterClient", response.toString());
                // iterate through array
                // for each, deserialize JSON object and convert to Tweet object

                for (int i = 0; i < response.length(); i++) {
                    try {
                        Tweet tweet = Tweet.fromJSON(response.getJSONObject(i));
                        tweets.add(tweet);
                        tweetAdapter.notifyItemInserted(tweets.size());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d("TwitterClient", responseString);
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == COMPOSE_RESULT_CODE && requestCode == COMPOSE_REQUEST_CODE) {
            Tweet newTweet = (Tweet) data.getParcelableExtra("newTweet");
            tweets.add(0, newTweet);
            tweetAdapter.notifyItemInserted(0);
            rvTweets.getLayoutManager().scrollToPosition(0);
            Toast.makeText(this, "Posted New Tweet!", Toast.LENGTH_SHORT).show();
        }
    }
}
