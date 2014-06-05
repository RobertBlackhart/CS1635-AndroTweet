package net.mcdermotsoft.androtweet;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.TwitterException;
import twitter4j.User;

public class TweetActivity extends ActionBarActivity
{
	EditText tweetEdit;
	Status status;
	String toString = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tweet_activity);

		if(getIntent().hasExtra("status"))
			status = (Status) getIntent().getSerializableExtra("status");
		if(getIntent().hasExtra("to"))
			toString = getIntent().getStringExtra("to");

		tweetEdit = (EditText) findViewById(R.id.tweetEdit);
		final TextView charLeft = (TextView) findViewById(R.id.charLeft);
		final ImageView send = (ImageView) findViewById(R.id.send);

		if(status != null)
		{
			tweetEdit.append("@" + status.getUser().getScreenName() + " ");
			charLeft.setText(String.valueOf(140-tweetEdit.getText().toString().length()));
		}
		if(toString != null)
		{
			tweetEdit.append("@" + toString + " ");
			charLeft.setText(String.valueOf(140-tweetEdit.getText().toString().length()));
		}

		tweetEdit.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				charLeft.setText(String.valueOf(140-s.length()));
				if(s.length() > 140)
				{
					charLeft.setTextColor(Color.YELLOW);
					send.setEnabled(false);
					send.setImageResource(R.drawable.send_disabled);
				}
				else
				{
					charLeft.setTextColor(Color.WHITE);
					send.setEnabled(true);
					send.setImageResource(R.drawable.send);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			@Override
			public void afterTextChanged(Editable s){}
		});

		send.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				StatusUpdate statusUpdate = new StatusUpdate(tweetEdit.getText().toString());
				if(status != null)
					statusUpdate.setInReplyToStatusId(status.getId());
				new TweetTask().execute(statusUpdate);
			}
		});

		ImageView userImage = (ImageView) findViewById(R.id.userImage);
		new SetImageTask(userImage).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tweet, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		if(id == R.id.hashtag)
		{
			tweetEdit.append("#");

		}
		if(id == R.id.mention)
		{
			tweetEdit.append("@");
		}

		return super.onOptionsItemSelected(item);
	}

	private class TweetTask extends AsyncTask<StatusUpdate,Void,Void>
	{
		ProgressDialog dialog;

		@Override
		protected void onPreExecute()
		{
			dialog = ProgressDialog.show(TweetActivity.this,"","Sending status update...");
		}
		@Override
		protected Void doInBackground(StatusUpdate... params)
		{
			try
			{
				AddAccountActivity.twitter.tweets().updateStatus(params[0]);
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
			dialog.dismiss();
			finish();
		}
	}

	private class SetImageTask extends AsyncTask<Void,Void,User>
	{
		ImageView userImage;

		public SetImageTask(ImageView userImage)
		{
			this.userImage = userImage;
		}
		@Override
		protected User doInBackground(Void... params)
		{
			try
			{
				return AddAccountActivity.twitter.users().showUser(AddAccountActivity.twitter.getId());
			}
			catch(TwitterException e)
			{
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(User user)
		{
			Ion.with(userImage).placeholder(R.drawable.egg).load(user.getProfileImageURL());
		}
	}
}