package net.mcdermotsoft.androtweet;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class Tab_Mentions extends Fragment
{
	ListView timeline;
	PullToRefreshLayout pullToRefreshLayout;
	boolean taskDone = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.tab_timeline, container, false);

		// Now find the PullToRefreshLayout to setup
		pullToRefreshLayout = (PullToRefreshLayout) rootView.findViewById(R.id.ptr_layout);

		// Now setup the PullToRefreshLayout
		ActionBarPullToRefresh.from(getActivity())
				// Mark All Children as pullable
				.allChildrenArePullable()
						// Set a OnRefreshListener
				.listener(new OnRefreshListener()
				{
					@Override
					public void onRefreshStarted(View view)
					{
						new TimelineTask(getActivity(),pullToRefreshLayout,timeline).execute(1, TabsPagerAdapter.Tab.MENTIONS_TIMELINE.ordinal());
					}


				})
		// Finally commit the setup to our PullToRefreshLayout
		.setup(pullToRefreshLayout);

		timeline = (ListView) rootView.findViewById(R.id.timeline);
		timeline.setOnScrollListener(new EndlessScrollListener()
		{
			@Override
			public void onLoadMore(int page, int totalItemsCount)
			{
				new TimelineTask(getActivity(),pullToRefreshLayout,timeline).execute(page+1, TabsPagerAdapter.Tab.MENTIONS_TIMELINE.ordinal());
			}
		});
		new TimelineTask(getActivity(),pullToRefreshLayout,timeline).execute(1, TabsPagerAdapter.Tab.MENTIONS_TIMELINE.ordinal());
		taskDone = true;

		return rootView;
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if(!taskDone)
			new TimelineTask(getActivity(),pullToRefreshLayout,timeline).execute(1, TabsPagerAdapter.Tab.MENTIONS_TIMELINE.ordinal());
	}
}