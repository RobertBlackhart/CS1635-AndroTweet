package net.mcdermotsoft.androtweet;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter
{
	public enum Tab{HOME_TIMELINE,MENTIONS_TIMELINE}

	public TabsPagerAdapter(FragmentManager fm)
	{
		super(fm);
	}

	@Override
	public Fragment getItem(int index)
	{
		switch(index)
		{
			case 0:
				return new Tab_Timeline();
			case 1:
				return new Tab_Mentions();
		}

		return null;
	}

	@Override
	public int getCount()
	{
		// get item count - equal to number of tabs
		return 2;
	}

}