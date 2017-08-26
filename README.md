Mini Twitter Backend REST API

1. Login
All users are authenticated in memory, and has their handle names as username and password. 
eg. username = batman, password = batman
The current spring security setup uses sessions, if you want to test using a different user, please clear session/cookies first
or open a new incognito browser window.


2. Endpoints
/tweets -- all tweets by user and users that he/she follows
/tweets?search= --all tweets by user and users that he/she follows, filtered by word after search. 
                  eg./tweets?search=
				  
/followers --lists users that follow the user
/following --lists the users the current user follows

/startfollow?handle= --starts following the user with the handle name given
/unfollow?handle= --unfollow the user with the handle given

/mostpopular --lists all users along with their most popular follower(s) if they are tied
"# TwitterBackendProject" 
"# TwitterBackendProject" 
