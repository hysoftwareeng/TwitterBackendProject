package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import model.Follower;
import model.Tweet;
import model.TwitterUser;

@Component
public class TwitterDao implements ITwitterDao {
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private NamedParameterJdbcTemplate jdbcTemplate;
	
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	@Override
	public List<Tweet> getUserTweets(int userId) {		
		String query = "SELECT people.id, people.handle, messages.content "
				+ "FROM messages INNER JOIN people ON "
				+ "messages.person_id = people.id WHERE people.id=:userId";
		
		Map<String,Object> params = new HashMap<String,Object>();
	    params.put("userId", userId);
	    List<Tweet> tweets = jdbcTemplate.query(query, params, new TweetMapper());
	    return tweets;
	}
	
	@Override
	public List<Tweet> getFilteredUserTweets(int userId, String searchParam) {
		String query = "SELECT people.id, people.handle, messages.content "
				+ "FROM messages INNER JOIN people ON "
				+ "messages.person_id = people.id WHERE people.id=:userId "
				+ "AND LOWER(messages.content) LIKE :searchParam";
		
		Map<String,Object> params = new HashMap<String,Object>();
	    params.put("userId", userId);
	    params.put("searchParam", "%" + searchParam + "%");
	    List<Tweet> tweets = jdbcTemplate.query(query, params, new TweetMapper());
	    return tweets;
	}
	
	@Override
	public List<Tweet> getFollowingTweets(int userId) {
		String query = "SELECT people.id, people.handle, messages.content "
				+ "FROM followers INNER JOIN people "
				+ "ON followers.person_id = people.id "
				+ "INNER JOIN messages "
				+ "ON followers.person_id = messages.person_id "
				+ "WHERE followers.follower_person_id=:userId";
		
		Map<String,Object> params = new HashMap<String,Object>();
	    params.put("userId", userId);
	    List<Tweet> tweets = jdbcTemplate.query(query, params, new TweetMapper());
	    return tweets;
	}

	@Override
	public List<Tweet> getFilteredFollowingTweets(int userId, String searchParam) {
		String query = "SELECT people.id, people.handle, messages.content "
				+ "FROM followers INNER JOIN people "
				+ "ON followers.person_id = people.id "
				+ "INNER JOIN messages "
				+ "ON followers.person_id = messages.person_id "
				+ "WHERE followers.follower_person_id=:userId "
				+ "AND LOWER(messages.content) LIKE :searchParam";
		
		Map<String,Object> params = new HashMap<String,Object>();
	    params.put("userId", userId);
	    params.put("searchParam", "%" + searchParam + "%");
	    List<Tweet> tweets = jdbcTemplate.query(query, params, new TweetMapper());
	    return tweets;
	}
	
	@Override
	public List<Follower> getFollowers(int userId) {
		String query = "SELECT handle, name, follower_person_id AS followerId "
				+ "FROM followers INNER JOIN people ON "
				+ "followers.follower_person_id = people.id "
				+ "WHERE followers.person_id=:userId";
		Map<String,Object> params = new HashMap<String,Object>();
	    params.put("userId", userId);
		
		List<Follower> followers = jdbcTemplate.query(query, params, new FollowerMapper());
		return followers;
	}
	
	@Override
	public List<Follower> getFollowing(int userId) {
		String query = "SELECT handle, name, person_id AS followerId "
				+ "FROM followers INNER JOIN people ON "
				+ "followers.person_id = people.id "
				+ "WHERE followers.follower_person_id=:userId";

		
		Map<String,Object> params = new HashMap<String,Object>();
	    params.put("userId", userId);
	    
	    List<Follower> following = jdbcTemplate.query(query, params, new FollowerMapper());
		return following;

	}
	
	//Check if user is following the follower with id given
	@Override
	public Integer findFollower(int userId, int followerId) {
		try{
			String query = "SELECT follower_person_id FROM followers WHERE "
					+ "follower_person_id=:userId "
					+ "AND person_id=:followerId";
		    
		    Map<String,Object> params = new HashMap<String,Object>();
		    params.put("followerId", followerId);
		    params.put("userId", userId);
		    Integer id = (Integer) jdbcTemplate.queryForObject(query, params, Integer.class);
			return id;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	@Override
	public Integer getUserId(String handle) {
		try{
			String query = "SELECT id FROM people WHERE handle=:handle";
		    
		    Map<String,Object> params = new HashMap<String,Object>();
		    params.put("handle", handle);
			Integer id = (Integer) jdbcTemplate.queryForObject(query, params, Integer.class);
			return id;
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}
	
	//To let user/userId start following another user with the followerId given
	@Override
	public Integer followUser(int userId, int followerId){
		try{
			String query = "INSERT INTO followers (person_id, follower_person_id)"
					+ " VALUES(:followerId, :userId)";
		    Map<String,Object> params = new HashMap<String,Object>();
		    params.put("userId", userId);
		    params.put("followerId", followerId);
		    int result = (Integer) jdbcTemplate.update(query, params);
		    return result;
		} catch (DataIntegrityViolationException e) {
            return null;
        }
	} 
	
	//To let user/userId unfollow another user with the followerId given
	@Override
	public Integer unFollowUser(int userId, int followerId){
		try{
			String query = "DELETE FROM followers WHERE "
					+ "follower_person_id=:userId "
					+ "AND person_id=:followerId";
			
		    Map<String,Object> params = new HashMap<String,Object>();
		    params.put("userId", userId);
		    params.put("followerId", followerId);
		    int result = (Integer) jdbcTemplate.update(query, params);
		    return result;
		} catch (DataIntegrityViolationException e) {
            return null;
        }
	} 

	@Override
	public Map <TwitterUser, List<Follower>> getAllUsersAndMostPopularFollowers() {
		String query = "SELECT person_id, p2.name AS person_name, p2.handle AS person_handle,"
				+ " follower_person_id, p3.name AS follower_name, p3.handle AS follower_handle, follower_count "
				+ "FROM "
				+ "(SELECT f.person_id, f.follower_person_id, f.follower_count FROM"
				+ 	"(SELECT t1.person_id, MAX(t2.cnt) AS maxCount FROM followers AS t1 "
				+ 		"JOIN ("
				+ 			"SELECT person_id, COUNT(*) AS cnt FROM followers "
				+   		"GROUP BY person_id"
				+ 		") AS t2  ON t1.follower_person_id = t2.person_id	"
				+ 	"GROUP BY t1.person_id) AS followerCnt "
				+ "INNER JOIN "
				+ "("
				+ 	"SELECT t1.person_id, t1.follower_person_id, t2.cnt AS follower_count "
				+ 	"FROM followers AS t1 "
				+ "JOIN "
				+ "(SELECT person_id, COUNT(*) AS cnt	FROM followers "
				+ 	"GROUP BY person_id) AS t2 "
				+ "ON t1.follower_person_id = t2.person_id) AS f "
				+ "ON f.person_id = followerCnt.person_id AND f.follower_count = followerCnt.maxCount) "
				+ "AS countTable "
				+ "INNER JOIN people p2 ON countTable.person_id = p2.id	"
				+ "INNER JOIN people p3 ON countTable.follower_person_id = p3.id "
				+ "ORDER BY person_id, follower_person_id";

		Map<TwitterUser, List<Follower>> result = jdbcTemplate.query(query, new ResultSetExtractor<Map <TwitterUser, List<Follower>>>(){
			 @Override
			    public Map<TwitterUser, List<Follower>> extractData(ResultSet rs) throws SQLException, DataAccessException {
			        HashMap<TwitterUser, List<Follower>> mapRet= new HashMap<TwitterUser, List<Follower>>();
			        while(rs.next()){
			        	TwitterUser user = new TwitterUser();
			        	user.setId(rs.getInt("person_id"));
			        	user.setHandle(rs.getString("person_handle"));
			        	user.setName(rs.getString("person_name"));
			        	
						Follower follower = new Follower();
						follower.setId(rs.getInt("follower_person_id"));
						follower.setHandle(rs.getString("follower_handle"));
						follower.setName(rs.getString("follower_name"));
			        	
			        	if (mapRet.containsKey(user)){
			        		List<Follower> current =  mapRet.get(user);
			        		current.add(follower);
			        		mapRet.put(user, current);
			        	} else {
			        		List<Follower> current = new ArrayList<Follower>();
			        		current.add(follower);
			        		mapRet.put(user, current);
			        	}
			        }
			        return mapRet;
			    }
		});
		
		return result;
	}

	private static class FollowerMapper implements RowMapper<Follower> {
		@Override
		public Follower mapRow(ResultSet rs, int rowNum) throws SQLException {
			Follower user = new Follower();
			user.setId(rs.getInt("followerId"));
			user.setHandle(rs.getString("handle"));
			user.setName(rs.getString("name"));
			return user;
		}
    }
	
	private static class TweetMapper implements RowMapper<Tweet> {
		@Override
		public Tweet mapRow(ResultSet rs, int rowNum) throws SQLException {
			Tweet tweet = new Tweet();
			
			tweet.setUserId(rs.getInt("id"));
			tweet.setHandle(rs.getString("handle"));
			tweet.setContent(rs.getString("content"));
			return tweet;
		}
    }
}
