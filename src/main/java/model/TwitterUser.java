package model;

import java.util.List;

public class TwitterUser {
	private Integer id;
	private String handle;
	private String name;
	private List<Follower> followers;
	private List<Follower> following;
	private List<Tweet> tweets;
	private List<Follower> mostPopularFollower;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getHandle() {
		return handle;
	}
	public void setHandle(String handle) {
		this.handle = handle;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Follower> getFollowers() {
		return followers;
	}
	public void setFollowers(List<Follower> followers) {
		this.followers = followers;
	}
	public List<Follower> getFollowing() {
		return following;
	}
	public void setFollowing(List<Follower> following) {
		this.following = following;
	}
	public List<Tweet> getTweets() {
		return tweets;
	}
	public void setTweets(List<Tweet> tweets) {
		this.tweets = tweets;
	}
	public List<Follower> getMostPopularFollower() {
		return mostPopularFollower;
	}
	public void setMostPopularFollower(List<Follower> mostPopularFollower) {
		this.mostPopularFollower = mostPopularFollower;
	}
	
	//hashCode depend on only user Id
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;  
        return result;
    }
 
    //compare user IDs only
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TwitterUser other = (TwitterUser) obj;
        if (id != other.id)
            return false;
        return true;
    }
	
}