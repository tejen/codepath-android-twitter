package org.tejen.codepathandroid.twitter.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;
import org.tejen.codepathandroid.twitter.R;
import org.tejen.codepathandroid.twitter.TwitterApp;
import org.tejen.codepathandroid.twitter.data.Tweet;
import org.tejen.codepathandroid.twitter.data.TwitterClient;
import org.tejen.codepathandroid.twitter.data.User;

import java.text.ParseException;

import cz.msebera.android.httpclient.Header;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class ComposeActivity extends AppCompatActivity {

    ImageView ivProfileImage;
    RequiredEditText etBody;
    TextView tvCharacterCount;
    Button buttonTweet;
    Boolean formEnabled;

    private TwitterClient client;
    private final int COMPOSE_RESULT_CODE = 20;
    private Tweet replyTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        client = TwitterApp.getRestClient();

        ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);
        etBody = (RequiredEditText ) findViewById(R.id.etBody);
        tvCharacterCount = (TextView) findViewById(R.id.tvCharacterCount);
        buttonTweet = (Button) findViewById(R.id.buttonTweet);

        if(getIntent().hasExtra(Tweet.class.getName())) {
            replyTo = (Tweet) getIntent().getParcelableExtra(Tweet.class.getName());
            etBody.setText("@" + replyTo.getUser().getScreenName() + ": ");
            etBody.setSelection(etBody.getText().length());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ivProfileImage.setClipToOutline(true);
        }

        User.getCurrentUser(new User.UserCallbackInterface() {
            @Override
            public void onUserAvailable(User currentUser) {
                Glide.with(getContext()).load(currentUser.getProfileImageUrl()).into(ivProfileImage);
            }
        });

        buttonTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!formEnabled) return;
                setTweetButtonEnabled(false);
                long inReplyTo = -1;
                if(replyTo != null) {
                    inReplyTo = replyTo.getId();
                }
                client.postTweet(etBody.getText().toString(), inReplyTo, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Tweet newTweet = null;
                        try {
                            newTweet = Tweet.fromJSON(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        Intent i = new Intent();
                        i.putExtra("newTweet", Parcels.wrap(newTweet));
                        setResult(COMPOSE_RESULT_CODE, i);
                        finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        Log.d("DEBUG", errorResponse.toString());
                        setTweetButtonEnabled(true);
                    }
                });
            }
        });

        etBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tvCharacterCount.setText(Integer.toString(140 - etBody.length()));
                tvCharacterCount.setTextColor(etBody.length() > 140 ?
                        ResourcesCompat.getColor(getResources(), R.color.twitter_red, null) :
                        ResourcesCompat.getColor(getResources(), R.color.twitter_gray, null));
                setTweetButtonEnabled(etBody.length() > 0 && etBody.length() <= 140);
            }
        });

        setTweetButtonEnabled(false);
    }

    private void setTweetButtonEnabled(boolean enabled) {
        formEnabled = enabled;
        buttonTweet.setEnabled(enabled);
        buttonTweet.setAlpha((float) (enabled ? 1 : 0.6));
    }

    public void onCancelAction(View view) {
        finish();
    }
}

