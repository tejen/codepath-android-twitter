package org.tejen.codepathandroid.twitter.fragments;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;

import cz.msebera.android.httpclient.Header;

/**
 * Created by tejen on 7/6/17.
 */

public class HomeTimelineFragment extends TweetsListFragment {

    @Override
    void finishTimelineFetch(long max_id, final boolean fetchMore) {
        client.getHomeTimeline(max_id, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                replaceAdapterWith(response, fetchMore);
            }

            public void onFailure(Throwable e) {
                Log.d("DEBUG", "Fetch timeline error: " + e.toString());
            }

        });
    }

}
