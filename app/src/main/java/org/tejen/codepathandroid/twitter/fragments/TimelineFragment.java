package org.tejen.codepathandroid.twitter.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tejen.codepathandroid.twitter.R;
import org.tejen.codepathandroid.twitter.TwitterApp;
import org.tejen.codepathandroid.twitter.activities.MainActivity;
import org.tejen.codepathandroid.twitter.adapters.TweetAdapter;
import org.tejen.codepathandroid.twitter.data.Tweet;
import org.tejen.codepathandroid.twitter.data.TwitterClient;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineFragment extends Fragment implements MainActivity.TweetUpdateListener {
    public static final String ARG_PAGE = "ARG_PAGE";

    private TwitterClient client;
    TweetAdapter tweetAdapter;
    ArrayList<Tweet> tweets;
    RecyclerView rvTweets;
    SwipeRefreshLayout swipeContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        client = TwitterApp.getRestClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) view;

        rvTweets = (RecyclerView) view.findViewById(R.id.rvTweet);
        tweets = new ArrayList<>();
        tweetAdapter = new TweetAdapter(tweets);
        rvTweets.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvTweets.setAdapter(tweetAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvTweets.getContext(),
                new LinearLayoutManager(getActivity()).getOrientation());
        rvTweets.addItemDecoration(dividerItemDecoration);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(0);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.twitter_blue);

        populateTimeline();
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((MainActivity) context).registerDataUpdateListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ((MainActivity) getActivity()).unregisterDataUpdateListener(this);
    }

    private void populateTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.d("TwitterClient", response.toString());
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
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
                Log.d("TwitterClient", errorResponse.toString());
                throwable.printStackTrace();
            }
        });
    }

    @Override
    public void onComposedNewTweet(Tweet newTweet) {
        tweets.add(0, newTweet);
        tweetAdapter.notifyItemInserted(0);
        rvTweets.getLayoutManager().scrollToPosition(0);
        Toast.makeText(getActivity(), "Posted New Tweet!", Toast.LENGTH_SHORT).show();
    }

    public void fetchTimelineAsync(int page) {
        // Send the network request to fetch the updated data
        // `client` here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                // Remember to CLEAR OUT old items before appending in the new ones
                // ...the data has come back, add new items to your adapter...
                try {
                    tweetAdapter.clear();
                    tweetAdapter.addAll(Tweet.multipleFromJSON(response));
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            public void onFailure(Throwable e) {
                Log.d("DEBUG", "Fetch timeline error: " + e.toString());
            }

        });
    }
}
