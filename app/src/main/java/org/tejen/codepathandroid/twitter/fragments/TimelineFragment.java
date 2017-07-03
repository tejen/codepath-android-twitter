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
import org.tejen.codepathandroid.twitter.R;
import org.tejen.codepathandroid.twitter.TwitterApp;
import org.tejen.codepathandroid.twitter.activities.MainActivity;
import org.tejen.codepathandroid.twitter.adapters.TweetAdapter;
import org.tejen.codepathandroid.twitter.data.EndlessRecyclerViewScrollListener;
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
    EndlessRecyclerViewScrollListener scrollListener;

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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rvTweets.setLayoutManager(linearLayoutManager);
        rvTweets.setAdapter(tweetAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rvTweets.getContext(),
                new LinearLayoutManager(getActivity()).getOrientation());
        rvTweets.addItemDecoration(dividerItemDecoration);

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                fetchTimelineAsync(true);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(false);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.twitter_blue);

        fetchTimelineAsync(false);
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

    @Override
    public void onComposedNewTweet(Tweet newTweet) {
        tweets.add(0, newTweet);
        tweetAdapter.notifyItemInserted(0);
        rvTweets.getLayoutManager().scrollToPosition(0);
        Toast.makeText(getActivity(), "Posted New Tweet!", Toast.LENGTH_SHORT).show();
    }

    public void fetchTimelineAsync(final boolean fetchMore) {
        // Send the network request to fetch the updated data
        // `client` here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.

        long max_id;

        if(fetchMore == false) {
            max_id = -1;
        } else {
            max_id = tweetAdapter.getAt(tweetAdapter.getItemCount() - 1).getId();
            max_id--; // Twitter max_id param will otherwise include the tweet matching id in max_id param
        }

        client.getHomeTimeline(max_id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                // Remember to CLEAR OUT old items before appending in the new ones
                // ...the data has come back, add new items to your adapter...
                try {
                    if(fetchMore == false) {
                        tweetAdapter.clear();
                    }
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
