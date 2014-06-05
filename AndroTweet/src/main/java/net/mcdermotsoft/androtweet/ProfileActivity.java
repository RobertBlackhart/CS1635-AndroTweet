package net.mcdermotsoft.androtweet;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

public class ProfileActivity extends ActionBarActivity
{
	User user;
	MenuItem followingMenu, blockingMenu;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile_activity);

		if(getIntent().hasExtra("user"))
			user = (User) getIntent().getSerializableExtra("user");
		else
		{
			Toast.makeText(this, "Could not load profile", Toast.LENGTH_SHORT).show();
			finish();
		}

		getSupportActionBar().setTitle(user.getName());

		ImageView backgroundImage = (ImageView) findViewById(R.id.backgroundImage);
		Ion.with(backgroundImage).placeholder(R.drawable.grey_header_web).load(user.getProfileBannerURL());

		ImageView userImage = (ImageView) findViewById(R.id.userImage);
		Ion.with(userImage).placeholder(R.drawable.egg).load(user.getProfileImageURL());

		TextView realName = (TextView) findViewById(R.id.realName);
		realName.setText(user.getName());

		TextView screenName = (TextView) findViewById(R.id.screenName);
		screenName.setText("@" + user.getScreenName());

		final TextView following = (TextView) findViewById(R.id.following);
		new SetFollowingTextTask()
		{
			@Override
			protected void onPostExecute(Boolean isFollowing)
			{
				if(isFollowing)
					following.setText("is following you");
				else
					following.setText("not following you");
			}
		}.execute();

		TextView aboutText = (TextView) findViewById(R.id.aboutText);
		aboutText.setText(user.getDescription());

		TextView location = (TextView) findViewById(R.id.location);
		location.setText(user.getLocation());

		TextView link = (TextView) findViewById(R.id.link);
		link.setText(user.getURLEntity().getDisplayURL());

		final ListView tweetList = (ListView) findViewById(R.id.tweetList);
		new GetTweetsTask()
		{
			@Override
			protected void onPostExecute(ResponseList<twitter4j.Status> statuses)
			{
				if(statuses != null)
					tweetList.setAdapter(new TimelineAdapter(ProfileActivity.this, 1, statuses));
			}
		}.execute(user);

		TextView numTweets = (TextView) findViewById(R.id.numTweets);
		numTweets.setText(user.getStatusesCount()+"\nTweets");

		TextView numFollowers = (TextView) findViewById(R.id.numFollowers);
		numFollowers.setText(user.getFollowersCount()+"\nFollowers");

		TextView numFollowees = (TextView) findViewById(R.id.numFollowees);
		numFollowees.setText(user.getFriendsCount()+"\nFriends");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile, menu);
		followingMenu = menu.findItem(R.id.follow);
		blockingMenu = menu.findItem(R.id.block);
		new IsFollowingTask()
		{
			@Override
			protected void onPostExecute(Boolean isFollowing)
			{
				if(isFollowing)
					followingMenu.setTitle("Unfollow");
			}
		}.execute();
		new IsBlockingTask()
		{
			@Override
			protected void onPostExecute(Boolean isBlocking)
			{
				if(isBlocking)
					blockingMenu.setTitle("Unblock");
			}
		}.execute();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		if(id == R.id.tweet)
		{
			Intent intent = new Intent(this, TweetActivity.class);
			intent.putExtra("to", user.getScreenName());
			startActivity(intent);
		}
		if(id == R.id.follow)
		{
			new FollowTask().execute();
		}
		if(id == R.id.block)
		{
			new BlockTask().execute();
		}

		return super.onOptionsItemSelected(item);
	}

	private class SetFollowingTextTask extends AsyncTask<Void, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(Void... params)
		{
			try
			{
				long[] ids = AddAccountActivity.twitter.friendsFollowers().getFollowersIDs(user.getId()).getIDs();
				for(long id : ids)
				{
					if(id == AddAccountActivity.twitter.getId())
						return true;
				}
			}
			catch(TwitterException e)
			{
				e.printStackTrace();
			}

			return false;
		}
	}

	private class GetTweetsTask extends AsyncTask<User, Void, ResponseList<Status>>
	{
		@Override
		protected ResponseList<twitter4j.Status> doInBackground(User... params)
		{
			try
			{
				return AddAccountActivity.twitter.timelines().getUserTimeline(params[0].getId());
			}
			catch(TwitterException e)
			{
				e.printStackTrace();
			}

			return null;
		}
	}

	private class IsFollowingTask extends AsyncTask<Void, Void, Boolean>
	{

		@Override
		protected Boolean doInBackground(Void... params)
		{
			try
			{
				return AddAccountActivity.twitter.friendsFollowers().showFriendship(AddAccountActivity.twitter.getId(), user.getId()).isSourceFollowingTarget();
			}
			catch(TwitterException e)
			{
				e.printStackTrace();
			}

			return false;
		}
	}

	private class IsBlockingTask extends AsyncTask<Void, Void, Boolean>
	{

		@Override
		protected Boolean doInBackground(Void... params)
		{
			try
			{
				return AddAccountActivity.twitter.friendsFollowers().showFriendship(AddAccountActivity.twitter.getId(), user.getId()).isSourceBlockingTarget();
			}
			catch(TwitterException e)
			{
				e.printStackTrace();
			}

			return false;
		}
	}

	private class BlockTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			try
			{
				if(blockingMenu.getTitle().equals("Block"))
					AddAccountActivity.twitter.createBlock(user.getId());
				else
					AddAccountActivity.twitter.destroyBlock(user.getId());

			}
			catch(TwitterException e)
			{
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void v)
		{
			if(blockingMenu.getTitle().equals("Block"))
			{
				blockingMenu.setTitle("Unblock");
				Toast.makeText(ProfileActivity.this,"You are now blocking @" + user.getScreenName(),Toast.LENGTH_SHORT).show();
			}
			else
			{
				blockingMenu.setTitle("Block");
				Toast.makeText(ProfileActivity.this,"You are no longer blocking @" + user.getScreenName(),Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class FollowTask extends AsyncTask<Void, Void, Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			try
			{
				if(followingMenu.getTitle().equals("Follow"))
					AddAccountActivity.twitter.createFriendship(user.getId());
				else
					AddAccountActivity.twitter.destroyFriendship(user.getId());
			}
			catch(TwitterException e)
			{
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void v)
		{
			if(followingMenu.getTitle().equals("Follow"))
			{
				followingMenu.setTitle("Unfollow");
				Toast.makeText(ProfileActivity.this,"You are now following @" + user.getScreenName(),Toast.LENGTH_SHORT).show();
			}
			else
			{
				followingMenu.setTitle("Follow");
				Toast.makeText(ProfileActivity.this,"You are no longer following @" + user.getScreenName(),Toast.LENGTH_SHORT).show();
			}
		}
	}
}