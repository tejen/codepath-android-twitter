package org.tejen.codepathandroid.twitter.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.tejen.codepathandroid.twitter.R;
import org.tejen.codepathandroid.twitter.adapters.TwitterFragmentPagerAdapter;

/**
 * Created by tejen on 7/2/17.
 */

public class MainActivity extends AppCompatActivity {
    private final int COMPOSE_REQUEST_CODE = 10;
    private final int COMPOSE_RESULT_CODE = 20;

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

    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_twitter_logo_vector);
        getSupportActionBar().setTitle("Home");
        getSupportActionBar().setDisplayUseLogoEnabled(true);

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

//    public void onComposeAction(MenuItem mi) {
//        Intent i = new Intent(MainActivity.this, ComposeActivity.class);
//        startActivityForResult(i, COMPOSE_REQUEST_CODE);
//    }
}
