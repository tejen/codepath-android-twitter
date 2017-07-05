package org.tejen.codepathandroid.twitter.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.parceler.Parcels;
import org.tejen.codepathandroid.twitter.R;
import org.tejen.codepathandroid.twitter.adapters.TwitterFragmentPagerAdapter;
import org.tejen.codepathandroid.twitter.data.Tweet;
import org.tejen.codepathandroid.twitter.data.User;
import org.tejen.codepathandroid.twitter.fragments.SimpleNavigationDrawerFragment;

import java.util.ArrayList;

/**
 * Created by tejen on 7/2/17.
 */

public class MainActivity extends AppCompatActivity {

    public void startBrowserActivity(String mediaUrl) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mediaUrl));
        startActivity(browserIntent);
    }

    public interface TweetUpdateListener {
        void onComposedNewTweet(Tweet newTweet);
    }

    private final int COMPOSE_REQUEST_CODE = 10;
    private final int COMPOSE_RESULT_CODE = 20;

    private ArrayList<TweetUpdateListener> mListeners;

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private TabLayout tabLayout;

    private ImageView ivProfileImage;
    private TextView tvUserName;
    private TextView tvUserScreenname;

    private int[] tabIconsSelected = {
            R.drawable.ic_vector_home,
            R.drawable.ic_vector_search,
            R.drawable.ic_vector_notifications,
            R.drawable.ic_vector_messages
    };

    private int[] tabIconsUnselected = {
            R.drawable.ic_vector_home_stroke,
            R.drawable.ic_vector_search_stroke,
            R.drawable.ic_vector_notifications_stroke,
            R.drawable.ic_vector_messages_stroke
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_twitter_logo_vector);
        getSupportActionBar().setTitle("Home");
        getSupportActionBar().setDisplayUseLogoEnabled(false);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = setupDrawerToggle();

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer.addDrawerListener(drawerToggle);

        // Find our drawer view
        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        View headerLayout = nvDrawer.getHeaderView(0);
        ivProfileImage = (ImageView) headerLayout.findViewById(R.id.ivProfileImage);
        tvUserName = (TextView) headerLayout.findViewById(R.id.tvUserName);
        tvUserScreenname = (TextView) headerLayout.findViewById(R.id.tvUserScreenname);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ivProfileImage.setClipToOutline(true);
        }
        User.getCurrentUser(new User.UserCallbackInterface() {
            @Override
            public void onUserAvailable(User currentUser) {
                Glide.with(getBaseContext()).load(currentUser.getProfileImageUrl()).into(ivProfileImage);
                tvUserName.setText(currentUser.getName());
                tvUserScreenname.setText("@"+currentUser.getScreenName());
            }
        });
        tvUserScreenname.setText("test screenname");

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new TwitterFragmentPagerAdapter(getSupportFragmentManager(),
                MainActivity.this));

        // Give the TabLayout the ViewPager
        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.getTabAt(0).setIcon(tabIconsSelected[0]);
        for (int i = 1; i < tabIconsUnselected.length; i++) {
            tabLayout.getTabAt(i).setIcon(tabIconsUnselected[i]);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabLayout.getTabAt(tab.getPosition()).setIcon(tabIconsSelected[tab.getPosition()]);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tabLayout.getTabAt(tab.getPosition()).setIcon(tabIconsUnselected[tab.getPosition()]);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mListeners = new ArrayList<>();
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        // NOTE: Make sure you pass in a valid toolbar reference.  ActionBarDrawToggle() does not require it
        // and will not render the hamburger icon without it.
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE 1: Make sure to override the method with only a single `Bundle` argument
    // Note 2: Make sure you implement the correct `onPostCreate(Bundle savedInstanceState)` method.
    // There are 2 signatures and only `onPostCreate(Bundle state)` shows the hamburger icon.
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass = SimpleNavigationDrawerFragment.class;

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        setTitle(menuItem.getTitle());
        // Close the navigation drawer
        mDrawer.closeDrawers();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        // The action bar home/up action should open or close the drawer.
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                mDrawer.openDrawer(GravityCompat.START);
//                return true;
//        }

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public synchronized void registerDataUpdateListener(TweetUpdateListener listener) {
        mListeners.add(listener);
    }

    public synchronized void unregisterDataUpdateListener(TweetUpdateListener listener) {
        mListeners.remove(listener);
    }

    public synchronized void dataUpdated(Tweet newTweet) {
        for (TweetUpdateListener listener : mListeners) {
            listener.onComposedNewTweet(newTweet);
        }
    }

    public synchronized void startReplyActivity(Tweet newTweet) {
        Intent i = new Intent(MainActivity.this, ComposeActivity.class);
        i.putExtra(Tweet.class.getName(), Parcels.wrap(newTweet));
        startActivityForResult(i, COMPOSE_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        Drawable drawable = menu.findItem(R.id.miSearch).getIcon();
        drawable = DrawableCompat.wrap(drawable);
        drawable.mutate();
        DrawableCompat.setTint(drawable, ContextCompat.getColor(this,R.color.twitter_blue));
        menu.findItem(R.id.miSearch).setIcon(drawable);

        return true;
    }

    public void onSearchAction(MenuItem mi) {
//        Intent i = new Intent(MainActivity.this, ComposeActivity.class);
//        startActivityForResult(i, COMPOSE_REQUEST_CODE);
    }

    public void onComposeAction(View v) {
        Intent i = new Intent(MainActivity.this, ComposeActivity.class);
        startActivityForResult(i, COMPOSE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == COMPOSE_RESULT_CODE && requestCode == COMPOSE_REQUEST_CODE) {
            Tweet newTweet = (Tweet) data.getParcelableExtra("newTweet");
            dataUpdated(newTweet);
        }
    }
}
