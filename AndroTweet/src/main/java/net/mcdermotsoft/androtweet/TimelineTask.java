package net.mcdermotsoft.androtweet;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ListView;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarcompat.PullToRefreshLayout;

public class TimelineTask extends AsyncTask<Integer,Void, ResponseList<Status>>
{
	PullToRefreshLayout pullToRefreshLayout;
	ListView timeline;
	Context context;
	int page;

	public TimelineTask(Context c, PullToRefreshLayout refreshLayout, ListView timelineList)
	{
		context = c;
		pullToRefreshLayout = refreshLayout;
		timeline = timelineList;
	}

	@Override
	protected void onPreExecute()
	{
		if(TabHandler.refresh != null)
			TabHandler.refresh.setActionView(R.layout.actionbar_indeterminite_progress);
	}

	@Override
	protected ResponseList<twitter4j.Status> doInBackground(Integer... params)
	{
		try
		{
			page = params[0];
			if(params[1] == TabsPagerAdapter.Tab.HOME_TIMELINE.ordinal())
				return AddAccountActivity.twitter.timelines().getHomeTimeline(new Paging(params[0]));
			if(params[1] == TabsPagerAdapter.Tab.MENTIONS_TIMELINE.ordinal())
				return AddAccountActivity.twitter.timelines().getMentionsTimeline(new Paging(params[0]));
		}
		catch(TwitterException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onPostExecute(ResponseList<twitter4j.Status> statuses)
	{
		if(TabHandler.refresh != null)
			TabHandler.refresh.setActionView(null);

		pullToRefreshLayout.setRefreshComplete();

		if(statuses != null)
		{
			if(timeline.getAdapter() != null && page > 1) //reset list if we are getting page 1
			{
				TimelineAdapter adapter = (TimelineAdapter) timeline.getAdapter();
				adapter.addAll(statuses);
				adapter.notifyDataSetChanged();
			}
			else
				timeline.setAdapter(new TimelineAdapter(context,1,statuses));
		}
	}
}