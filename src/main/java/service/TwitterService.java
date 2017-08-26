package service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dao.ITwitterDao;
import dao.TwitterDao;
import model.Follower;
import model.Tweet;
import model.TwitterUser;

@Service("cityService")
@Transactional
public class TwitterService {
	
	@Autowired
	ITwitterDao database; 
	
	public List<Tweet> getTweets(int userId) {
    	List<Tweet> allTweets = new ArrayList<Tweet>();
    	List<Tweet> selfTweets = database.getUserTweets(userId);
    	List<Tweet> followersTweets = database.getFollowingTweets(userId);
    	
    	allTweets.addAll(selfTweets);
    	allTweets.addAll(followersTweets);
		return allTweets;
	}
	
	public List<Tweet> getFilteredTweets(int userId, String searchParam) {
    	List<Tweet> allTweets = new ArrayList<Tweet>();
    	List<Tweet> selfTweets = database.getFilteredUserTweets(userId, searchParam);
    	List<Tweet> followersTweets = database.getFilteredFollowingTweets(userId, searchParam);
    	
    	allTweets.addAll(selfTweets);
    	allTweets.addAll(followersTweets);
		return allTweets;
	}	
	
	public List<Follower> getFollowers(int userId){
		List<Follower> followers = database.getFollowers(userId);
		return followers;
	}
	
	public List<Follower> getFollowing(int userId){
		List<Follower> followers = database.getFollowing(userId);
		return followers;
	}
	
	public Integer findFollower(int userId, int followerId){
		Integer id = database.findFollower(userId, followerId);
		return id;
	}
	
	public Integer getUserId(String followerHandle){
		Integer id = database.getUserId(followerHandle);
		return id;
	}

	public Integer followUser(int userId, int followerId) {
		return database.followUser(userId, followerId);		
	}
	
	public Integer unFollowUser(int userId, int followerId) {
		return database.unFollowUser(userId, followerId);		
	}
	
	public List<TwitterUser> getAllUsersAndMostPopularFollowers() {
		List<TwitterUser> usersAndMostPopularFollowers = new ArrayList<TwitterUser>();
		Map<TwitterUser, List<Follower>> map = database.getAllUsersAndMostPopularFollowers();
		for (Map.Entry<TwitterUser, List<Follower>> entry : map.entrySet()) {
		    TwitterUser user = entry.getKey();
		    List<Follower> popularFollowers = entry.getValue();
		    user.setMostPopularFollower(popularFollowers);
		    usersAndMostPopularFollowers.add(user);
		}
		return usersAndMostPopularFollowers;		
	}	
}
