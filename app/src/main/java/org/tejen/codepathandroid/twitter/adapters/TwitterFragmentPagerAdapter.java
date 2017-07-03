package org.tejen.codepathandroid.twitter.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.tejen.codepathandroid.twitter.fragments.TimelineFragment;

/**
 * Created by tejen on 7/2/17.
 */

public class TwitterFragmentPagerAdapter extends FragmentPagerAdapter {
    final int PAGE_COUNT = 4;
    private String tabTitles[] = new String[] { "Tab1", "Tab2", "Tab3", "Tab4" };
    private Context context;

    public TwitterFragmentPagerAdapter (FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        return TimelineFragment.newInstance(position + 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return null;
    }
}
