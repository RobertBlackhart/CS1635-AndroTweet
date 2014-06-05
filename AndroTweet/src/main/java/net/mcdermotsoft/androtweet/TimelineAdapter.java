package net.mcdermotsoft.androtweet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;

import java.util.List;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.User;
import uk.co.senab.photoview.PhotoView;

/**
 * Created by Robert McDermot on 4/8/14.
 */
public class TimelineAdapter extends ArrayAdapter<Status>
{
	List<Status> statuses;
	Context context;

	public TimelineAdapter(Context context, int resource, List<Status> statuses)
	{
		super(context, resource, statuses);
		this.statuses = statuses;
		this.context = context;
	}

	@Override
	public View getView(final int position, final View convertView, ViewGroup parent)
	{
		View v;
		if(convertView != null)
			v = convertView;
		else
			v = LayoutInflater.from(context).inflate(R.layout.timeline_row, null);

		TextView statusText = (TextView) v.findViewById(R.id.statusText);
		TextView userName = (TextView) v.findViewById(R.id.userName);
		TextView userHandle = (TextView) v.findViewById(R.id.userHandle);
		ImageView userImage = (ImageView) v.findViewById(R.id.userImage);
		TextView retweeter = (TextView) v.findViewById(R.id.retweeter);
		TextView retweetBy = (TextView) v.findViewById(R.id.retweetBy);
		TextView time = (TextView) v.findViewById(R.id.time);
		ImageView imageMedia = (ImageView) v.findViewById(R.id.imageMedia);

		final View optionsLayout = v.findViewById(R.id.optionsLayout);
		TextView reply = (TextView) v.findViewById(R.id.reply);
		TextView profile = (TextView) v.findViewById(R.id.profile);
		TextView retweet = (TextView) v.findViewById(R.id.retweet);
		final TextView favorite = (TextView) v.findViewById(R.id.favorite);

		reply.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(context, TweetActivity.class);
				intent.putExtra("status", statuses.get(position));
				context.startActivity(intent);
			}
		});
		profile.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(context, ProfileActivity.class);
				if(statuses.get(position).isRetweet())
					intent.putExtra("user", statuses.get(position).getRetweetedStatus().getUser());
				else
					intent.putExtra("user", statuses.get(position).getUser());
				context.startActivity(intent);
			}
		});
		retweet.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new RetweetTask().execute(statuses.get(position));
			}
		});
		favorite.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				new FavoriteTask()
				{
					@Override
					protected void onPostExecute(Boolean favorited)
					{
						if(favorited)
							favorite.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.btn_favorite_full_dark, 0, 0);
						else
							favorite.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.btn_favorite_empty_dark, 0, 0);
					}
				}.execute(statuses.get(position));
			}
		});

		if(statuses.get(position).isFavorited())
			favorite.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.btn_favorite_full_dark, 0, 0);
		else
			favorite.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.btn_favorite_empty_dark, 0, 0);

		if(statuses.get(position).isRetweet())
		{
			User user = statuses.get(position).getRetweetedStatus().getUser();
			statusText.setText(statuses.get(position).getRetweetedStatus().getText());
			retweeter.setText(statuses.get(position).getUser().getName());
			userName.setText(user.getName());
			userHandle.setText("@" + user.getScreenName());
			Ion.with(userImage).placeholder(R.drawable.egg).load(user.getProfileImageURL());
			retweeter.setVisibility(View.VISIBLE);
			retweetBy.setVisibility(View.VISIBLE);
		}
		else
		{
			statusText.setText(statuses.get(position).getText());
			userName.setText(statuses.get(position).getUser().getName());
			userHandle.setText("@" + statuses.get(position).getUser().getScreenName());
			Ion.with(userImage).placeholder(R.drawable.egg).load(statuses.get(position).getUser().getProfileImageURL());
			retweeter.setVisibility(View.GONE);
			retweetBy.setVisibility(View.GONE);
		}

		String text = statusText.getText().toString();
		URLEntity imageEntity = null;
		for(URLEntity entity : statuses.get(position).getURLEntities())
		{
			text = text.replace(entity.getText(), entity.getExpandedURL());
			if(entity.getExpandedURL().endsWith(".jpg") || entity.getExpandedURL().endsWith(".png") ||entity.getExpandedURL().endsWith(".gif"))
				imageEntity = entity;
		}
		statusText.setText(text);
		Linkify.addLinks(statusText, Linkify.ALL);

		String imageUrl = null;
		if(statuses.get(position).getMediaEntities().length > 0)
		{
			imageMedia.setVisibility(View.VISIBLE);
			Ion.with(context).load(statuses.get(position).getMediaEntities()[0].getMediaURL()).withBitmap().deepZoom().intoImageView(imageMedia);
			imageUrl = statuses.get(position).getMediaEntities()[0].getMediaURL();
		}
		else if(imageEntity != null)
		{
			imageMedia.setVisibility(View.VISIBLE);
			Ion.with(context).load(imageEntity.getExpandedURL()).withBitmap().deepZoom().intoImageView(imageMedia);
			imageUrl = imageEntity.getExpandedURL();
		}
		else
			imageMedia.setVisibility(View.GONE);

		final String finalImageUrl = imageUrl;
		imageMedia.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				PhotoView photoView = new PhotoView(context);
				photoView.setMaximumScale(16);
				photoView.setScaleType(ImageView.ScaleType.CENTER);
				builder.setView(photoView);

				Ion.with(context).load(finalImageUrl).noCache().withBitmap().deepZoom().intoImageView(photoView);

				builder.setCancelable(true);
				builder.create().show();
			}
		});

		time.setText(DateUtils.getRelativeTimeSpanString(context, statuses.get(position).getCreatedAt().getTime(), false));

		v.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(v.getTag() == null || v.getTag().equals("collapsed"))
				{
					v.setTag("expanded");
					optionsLayout.setVisibility(View.VISIBLE);
				}
				else
				{
					v.setTag("collapsed");
					optionsLayout.setVisibility(View.GONE);
				}
			}
		});
		return v;
	}

	private class FavoriteTask extends AsyncTask<Status, Void, Boolean>
	{
		@Override
		protected Boolean doInBackground(twitter4j.Status... params)
		{
			try
			{
				AddAccountActivity.twitter.favorites().destroyFavorite(params[0].getId());
				return false;
			}
			catch(TwitterException e)
			{
				try
				{
					AddAccountActivity.twitter.favorites().createFavorite(params[0].getId());
				}
				catch(TwitterException e1)
				{
					e1.printStackTrace();
				}
				return true;
			}
		}
	}

	private class RetweetTask extends AsyncTask<Status,Void,Void>
	{
		@Override
		protected Void doInBackground(twitter4j.Status... params)
		{
			try
			{
				AddAccountActivity.twitter.tweets().retweetStatus(params[0].getId());
			}
			catch(TwitterException e)
			{
				e.printStackTrace();
			}

			return null;
		}
	}
}
