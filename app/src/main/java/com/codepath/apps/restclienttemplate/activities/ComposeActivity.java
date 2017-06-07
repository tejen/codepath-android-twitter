package com.codepath.apps.restclienttemplate.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.models.User;

import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class ComposeActivity extends AppCompatActivity {

    TextView tvUsername;
    ImageView ivUserphoto;
    EditText etBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);

        tvUsername = (TextView) findViewById(R.id.tvUsername);
        ivUserphoto = (ImageView) findViewById(R.id.ivUserphoto);
        etBody = (EditText) findViewById(R.id.etBody);

        User.getCurrentUser(new User.UserCallbackInterface() {
            @Override
            public void onUserAvailable(User currentUser) {
                tvUsername.setText(currentUser.name);
                Glide.with(getContext()).load(currentUser.profileImageUrl).into(ivUserphoto);
            }
        });
    }
}
