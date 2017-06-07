package com.codepath.apps.restclienttemplate.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.helpers.TwitterApp;
import com.codepath.apps.restclienttemplate.helpers.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class ComposeActivity extends AppCompatActivity {

    TextView tvUsername;
    ImageView ivUserphoto;
    EditText etBody;
    Button buttonCompose;

    private TwitterClient client;
    private final int COMPOSE_RESULT_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        getSupportActionBar().setTitle("Compose");

        client = TwitterApp.getRestClient();

        tvUsername = (TextView) findViewById(R.id.tvUsername);
        ivUserphoto = (ImageView) findViewById(R.id.ivUserphoto);
        etBody = (EditText) findViewById(R.id.etBody);
        buttonCompose = (Button) findViewById(R.id.buttonCompose);

        User.getCurrentUser(new User.UserCallbackInterface() {
            @Override
            public void onUserAvailable(User currentUser) {
                tvUsername.setText(currentUser.name);
                Glide.with(getContext()).load(currentUser.profileImageUrl).into(ivUserphoto);
            }
        });

        buttonCompose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                client.postTweet(etBody.getText().toString(), new JsonHttpResponseHandler()  {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Tweet newTweet = null;
                        try {
                            newTweet = Tweet.fromJSON(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Intent i = new Intent();
                        i.putExtra("newTweet", newTweet);
                        setResult(COMPOSE_RESULT_CODE, i);
                        finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        Log.d("DEBUG", errorResponse.toString());

                    }
                });
            }
        });
    }
}
