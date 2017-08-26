package dao;

import java.util.List;
import java.util.Map;

import model.Follower;
import model.Tweet;
import model.TwitterUser;


public interface ITwitterDao {
	
	public List<Tweet> getUserTweets(int userId);
	public List<Tweet> getFollowingTweets(int userId);
	
	public List<Tweet> getFilteredUserTweets(int userId, String searchParam);
	public List<Tweet> getFilteredFollowingTweets(int userId, String searchParam);	

	public List<Follower> getFollowers(int userId);
	public List<Follower> getFollowing(int userId);
	
	public Integer findFollower(int userId, int followerId);
	public Integer getUserId(String handle);
	
	public Integer followUser(int userId, int followerId);
	public Integer unFollowUser(int userId, int followerId);
	
	public Map <TwitterUser, List<Follower>> getAllUsersAndMostPopularFollowers();

}
