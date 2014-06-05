package net.mcdermotsoft.androtweet;

/**
 * Created by Robert McDermot on 4/10/14.
 */
public class User
{
	private String username, imageUrl;
	private long id;

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getImageUrl()
	{
		return imageUrl;
	}

	public void setImageUrl(String imageUrl)
	{
		this.imageUrl = imageUrl;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}
}
