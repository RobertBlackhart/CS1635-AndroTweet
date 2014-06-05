package net.mcdermotsoft.androtweet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;

public class TabHandler extends ActionBarActivity implements ActionBar.TabListener
{
	private ViewPager viewPager;
	private ActionBar actionBar;
	// Tab titles
	private String[] tabs = {"Timeline", "Mentions"};
	SharedPreferences prefs;
	static MenuItem refresh;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		// Initilization
		viewPager = (ViewPager) findViewById(R.id.pager);
		actionBar = getSupportActionBar();
		TabsPagerAdapter mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

		viewPager.setAdapter(mAdapter);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Adding Tabs
		for(String tab_name : tabs)
		{
			actionBar.addTab(actionBar.newTab().setText(tab_name).setTabListener(this));
		}

		/**
		 * on swiping the viewpager make respective tab selected
		 * */
		viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
		{

			@Override
			public void onPageSelected(int position)
			{
				// on changing the page
				// make respected tab selected
				actionBar.setSelectedNavigationItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2)
			{
			}

			@Override
			public void onPageScrollStateChanged(int arg0)
			{
			}
		});
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
	{
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
	{

	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
	{

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.timeline, menu);
		refresh = menu.findItem(R.id.refresh);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		if(id == R.id.tweet)
		{
			startActivity(new Intent(this,TweetActivity.class));
		}
		if(id == R.id.refresh)
		{
			Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem());
			// based on the current position you can then cast the page to the correct
			// class and call the method:
			if(viewPager.getCurrentItem() == 0 && page != null)
			{
				PullToRefreshLayout pullToRefreshLayout = ((Tab_Timeline) page).pullToRefreshLayout;
				ListView listView = ((Tab_Timeline) page).timeline;
				new TimelineTask(this, pullToRefreshLayout, listView).execute(1, TabsPagerAdapter.Tab.HOME_TIMELINE.ordinal());
			}
			if(viewPager.getCurrentItem() == 1 && page != null)
			{
				PullToRefreshLayout pullToRefreshLayout = ((Tab_Mentions) page).pullToRefreshLayout;
				ListView listView = ((Tab_Mentions) page).timeline;
				new TimelineTask(this, pullToRefreshLayout, listView).execute(1, TabsPagerAdapter.Tab.MENTIONS_TIMELINE.ordinal());
			}
		}
		if(id == R.id.accounts)
		{
			prefs.edit().remove("loggedIn").apply();
			startActivity(new Intent(this,AddAccountActivity.class));
			finish();
		}

		return super.onOptionsItemSelected(item);
	}
}