package controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import model.Follower;
import model.Tweet;
import model.TwitterUser;
import service.TwitterService;
import twitter.IAuthenticationFacade;


@RestController
@SessionAttributes({"userId", "handle"})
public class TwitterController {
	
	@Autowired
    private IAuthenticationFacade authenticationFacade;
	
    @Autowired
    private TwitterService twitterService; 
    
    //Exception handler to handle conflicts/bad requests
    @ExceptionHandler({
        IllegalArgumentException.class
    })
    ResponseEntity <String> handleConflicts(Exception e) {
        return new ResponseEntity <> (e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    
    //Set up session attributes to retrieve ID, handle of current user
    @ModelAttribute("userId")
    public int getUserId() {
    	Authentication authentication = authenticationFacade.getAuthentication();
    	String handle = authentication.getName();
    	return twitterService.getUserId(handle);
    }
    
    @ModelAttribute("handle")
    public String getHandle() {
    	Authentication authentication = authenticationFacade.getAuthentication();
    	String handle = authentication.getName();
    	return handle;
    }
    
    /**
     * An end point to read the message list for the current user 
     * (as identified by their HTTP Basic authentication credentials). 
     * Include messages they have sent and messages sent by users they follow. 
     * Supports a “search=” parameter that can be used to further filter messages based on keyword.
     * 
     * @param searchParam
     * @return
     */
    
    @RequestMapping(value = "/tweets", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Tweet>> getTweets(@RequestParam(value = "search", required = false) String searchParam, @ModelAttribute("userId")int userId) {

    	List<Tweet> tweets = new ArrayList<Tweet>();
    	//if no search parameter is given, retrieve all tweets for user and followers;
    	if (searchParam == null){
    		tweets =  twitterService.getTweets(userId);
    	} 
    	//otherwise filter the tweets using the search parameter
    	else {
    		searchParam = searchParam.toLowerCase();
    		tweets = twitterService.getFilteredTweets(userId, searchParam);
    	}

        if (tweets.isEmpty()) {
            return new ResponseEntity <List<Tweet>> (HttpStatus.NO_CONTENT);
        }
        System.out.println(tweets);
        return new ResponseEntity<List<Tweet>> (tweets, HttpStatus.OK);
    }

    /**This method will return a list of the users that are following the current user.
     * 
     * @param userId Id of the current logged on user, session attribute
     * @return List<Follower>, a list of the users following the current user
     */
    
    @RequestMapping(value = "/followers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Follower>> getFollowers(@ModelAttribute("userId")int userId) {
    	
    	List<Follower> followers = twitterService.getFollowers(userId);

        if (followers.isEmpty()) {
            return new ResponseEntity <List<Follower>> (HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<List<Follower>> (followers, HttpStatus.OK);
    }
    
    /**This method will return a list of the users the current user is following
     * 
     * @param userId Id of the current logged on user, session attribute
     * @return List<Follower>, a list of the users the current session user is following
     */
    
    @RequestMapping(value = "/following", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Follower>> getFollowing(@ModelAttribute("userId")int userId) {
   	
    	List<Follower> following = twitterService.getFollowing(userId);

        if (following.isEmpty()) {
            return new ResponseEntity <List<Follower>> (HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<List<Follower>> (following, HttpStatus.OK);
    }
    
    /**
     * This method allows the logged on session user to start following another user, provide
     * the handle of the user to follow
     * @param followerHandle, handle of the new user to follow
     * @param userId, userId of the current logged on user
     * @param ucBuilder UriComponentsBuilder
     * @return string result message
     */
    
    @RequestMapping(value = "/startfollow", method = RequestMethod.POST)
    public ResponseEntity<String> followUser(@RequestParam(value = "handle", required = true) String followerHandle, @ModelAttribute("userId")int userId, UriComponentsBuilder ucBuilder) {
    	
    	//Check if the the ID of the handle given to follow actually exists
    	Integer followerId = twitterService.getUserId(followerHandle);
    	
    	if (followerId == userId){
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You cannot follow yourself");
    	}
    	
    	if (followerId == null){
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The user you are trying to follow does not exist.");
    	}

    	//Check if already following the user
    	if (twitterService.findFollower(userId, followerId) != null){
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Already following user.");
    	}
    	
    	//Follow user if all checks passed;
    	if (twitterService.followUser(userId, followerId) == null){
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not follow user. Please make sure handle is correct.");
    	}  	

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/following/").buildAndExpand().toUri());
        return new ResponseEntity <String> (headers, HttpStatus.CREATED);
    }
    
    /**
     * This method will allow the logged on session user to unfollow another user, a parameter
     * of the handle of the user to unfollow should be given
     * 
     * @param unfollowHandle, handle of user to unfollow
     * @param userId, ID of current logged on user
     * @param ucBuilder UriComponentsBuilder
     * @return result message
     */
    
    @RequestMapping(value = "/unfollow", method = RequestMethod.DELETE)
    public ResponseEntity<String> unFollowUser(@RequestParam(value = "handle", required = true) String unfollowHandle, @ModelAttribute("userId")int userId, UriComponentsBuilder ucBuilder) {
    	
    	//Check if the the ID of the handle given to unfollow exists
    	Integer unfollowId = twitterService.getUserId(unfollowHandle);
    	  	
    	if (unfollowId == userId){
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You cannot unfollow yourself");
    	}
    	
    	if (unfollowId == null){
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The user you are trying to unfollow does not exist.");
    	}

    	//Check if actually following the user
    	if (twitterService.findFollower(userId, unfollowId) == null){
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("You have already unfollowed this user.");
    	}
    	
    	//unfollow user if all checks passed;
    	if (twitterService.unFollowUser(userId, unfollowId) == null){
    		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Could not unfollow user. Please make sure handle is correct.");
    	}  	

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/following/").buildAndExpand().toUri());
        return new ResponseEntity <String> (headers, HttpStatus.CREATED);
    }
    
    /**
     * This method returns all users with their most popular follower/list of most popular followers 
     * there follower count is all equal.
     * @return List<TwitterUser>, a list of all users along with their most popular followers
     */    

    @RequestMapping(value = "/mostpopular", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TwitterUser>> getAllUsersAndMostPopularFollowers() {
   	
    	List<TwitterUser> allUsersAndMostPopular = twitterService.getAllUsersAndMostPopularFollowers();

        if (allUsersAndMostPopular.isEmpty()) {
            return new ResponseEntity <List<TwitterUser>> (HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<List<TwitterUser>>( allUsersAndMostPopular, HttpStatus.OK);
    }
}
