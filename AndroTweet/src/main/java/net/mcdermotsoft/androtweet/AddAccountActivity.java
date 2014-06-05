package net.mcdermotsoft.androtweet;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

public class AddAccountActivity extends Activity
{
	// Constants
	static String TWITTER_CONSUMER_KEY = "urLS9ysjyOtcAv6NM42b7vyCS";
	static String TWITTER_CONSUMER_SECRET = "9L3hbDpX5reOdF16SZ5iq9M5x0hq5xEyqW0nxzoYWH6AcXXjpE";

	static final String TWITTER_CALLBACK_URL = "x-androtweet-oauth-twitter://callback";

	// Twitter oauth urls
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";

	// Twitter
	public static Twitter twitter;
	private static RequestToken requestToken;
	private ArrayList<AccessToken> accessTokens = new ArrayList<AccessToken>();

	SharedPreferences prefs;
	ListView accountsList;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_account);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		final Gson gson = new Gson();

		if(prefs.contains("accessTokens"))
		{
			Type listType = new TypeToken<ArrayList<AccessToken>>(){}.getType();
			accessTokens = gson.fromJson(prefs.getString("accessTokens", ""), listType);
		}

		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
		builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
		TwitterFactory factory = new TwitterFactory(builder.build());
		if(prefs.contains("loggedIn"))
		{
			twitter = factory.getInstance(gson.fromJson(prefs.getString("loggedIn", ""), AccessToken.class));
			startActivity(new Intent(AddAccountActivity.this, TabHandler.class));
			finish();
		}
		else
		{
			twitter = factory.getInstance();

			if(prefs.contains("users"))
			{
				Type listType = new TypeToken<ArrayList<net.mcdermotsoft.androtweet.User>>(){}.getType();
				ArrayList<net.mcdermotsoft.androtweet.User> users = gson.fromJson(prefs.getString("users", ""), listType);
				accountsList = (ListView) findViewById(R.id.accounts);
				accountsList.setAdapter(new AccountAdapter(AddAccountActivity.this, 1, users));
			}
		}

		Button addAccount = (Button) findViewById(R.id.addAccount);
		addAccount.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View arg0)
			{
				new RequestTokenTask().execute();
			}
		});
	}

	private class OAuthAccessTokenTask extends AsyncTask<Uri, Void, Void>
	{
		@Override
		protected Void doInBackground(Uri... params)
		{
			try
			{
				Log.d("AndroTweet","uri: " + params[0].toString());
				String verifier = params[0].getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
				AccessToken accessToken = twitter.getOAuthAccessToken(requestToken,verifier);
				if(accessToken != null && !accessTokens.contains(accessToken))
				{
					accessTokens.add(accessToken);
					Gson gson = new Gson();
					Editor edit = prefs.edit();
					edit.putString("accessTokens",gson.toJson(accessTokens));
					edit.putString("accessToken-"+accessToken.getUserId(),gson.toJson(accessToken));
					edit.apply();
				}
				new GetUsersTask().execute(accessTokens);
			}
			catch(TwitterException e)
			{
				Log.e(AddAccountActivity.class.getName(), "TwitterError: " + e.getErrorMessage());
			}
			catch(Exception e)
			{
				e.printStackTrace();
				Log.e(AddAccountActivity.class.getName(), "Error: " + e.getMessage());
			}

			return null;
		}
	}

	private class RequestTokenTask extends AsyncTask<Void,Void,Void>
	{
		@Override
		protected Void doInBackground(Void... params)
		{
			try
			{
				requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
			}
			catch(Exception e)
			{
				Log.d("AndroTweet","Already have requestToken");
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
				TwitterFactory factory = new TwitterFactory(builder.build());
				twitter = factory.getInstance();
				try
				{
					requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
				}
				catch(TwitterException e1)
				{
					e1.printStackTrace();
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void v)
		{
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if(requestToken != null)
		{
			Uri uri = getIntent().getData();
			if(uri != null)
				new OAuthAccessTokenTask().execute(uri);
		}
	}

	private class GetUsersTask extends AsyncTask<ArrayList<AccessToken>,Void,ArrayList<User>>
	{
		@Override
		protected ArrayList<User> doInBackground(ArrayList<AccessToken>... params)
		{
			try
			{
				ArrayList<User> users = new ArrayList<User>();

				for(AccessToken accessToken : params[0])
				{
					if(accessToken != null)
						users.add(twitter.users().showUser(accessToken.getUserId()));
				}

				return users;
			}
			catch(TwitterException e)
			{
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(ArrayList<User> users)
		{
			final Gson gson = new Gson();
			ArrayList<net.mcdermotsoft.androtweet.User> saveUsers = new ArrayList<net.mcdermotsoft.androtweet.User>();
			for(User user : users)
			{
				net.mcdermotsoft.androtweet.User saveUser = new net.mcdermotsoft.androtweet.User();
				saveUser.setUsername(user.getScreenName());
				saveUser.setImageUrl(user.getProfileImageURL());
				saveUser.setId(user.getId());
				saveUsers.add(saveUser);
			}
			prefs.edit().putString("users",gson.toJson(saveUsers)).apply();

			accountsList = (ListView) findViewById(R.id.accounts);
			accountsList.setAdapter(new AccountAdapter(AddAccountActivity.this,1,saveUsers));
		}
	}
}