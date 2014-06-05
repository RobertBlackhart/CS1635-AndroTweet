package net.mcdermotsoft.androtweet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.koushikdutta.ion.Ion;

import java.util.List;

import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Created by Robert McDermot on 4/8/14.
 */
public class AccountAdapter extends ArrayAdapter<User>
{
	List<User> users;
	Context context;

	public AccountAdapter(Context context, int resource, List<User> users)
	{
		super(context, resource, users);
		this.users = users;
		this.context = context;
	}

	@Override
	public View getView(final int position, final View convertView, ViewGroup parent)
	{
		View v;
		if(convertView != null)
			v = convertView;
		else
			v = LayoutInflater.from(context).inflate(R.layout.account_row, null);

		TextView userName = (TextView) v.findViewById(R.id.userName);
		ImageView userImage = (ImageView) v.findViewById(R.id.userImage);

		userName.setText(users.get(position).getUsername());
		Ion.with(userImage).placeholder(R.drawable.egg).load(users.get(position).getImageUrl());

		v.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				User user = users.get(position);
				Gson gson = new Gson();
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(AddAccountActivity.TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(AddAccountActivity.TWITTER_CONSUMER_SECRET);
				TwitterFactory factory = new TwitterFactory(builder.build());
				AddAccountActivity.twitter = factory.getInstance(gson.fromJson(prefs.getString("accessToken-" + user.getId(), ""), AccessToken.class));
				SharedPreferences.Editor edit = prefs.edit();
				edit.putString("loggedIn",gson.toJson(gson.fromJson(prefs.getString("accessToken-" + user.getId(), ""),AccessToken.class)));
				edit.apply();
				context.startActivity(new Intent(context, TabHandler.class));
			}
		});
		return v;
	}
}
