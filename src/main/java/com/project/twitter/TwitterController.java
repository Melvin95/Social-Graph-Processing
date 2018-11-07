package com.project.twitter;

import java.util.List;

import javax.inject.Inject;

import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.social.RateLimitExceededException;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.twitter.api.SearchResults;
import org.springframework.social.twitter.api.Tweet;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/*Twitter crawler, back-end stuff, meant to run just to populate the database*/
@Controller
@RequestMapping("/search") 
@EnableNeo4jRepositories
public class TwitterController {

	private Twitter twitter;
	
	private ConnectionRepository connectionRepository;
	
	public List<Tweet> tweetList;	

	private UserRepository userRepository;
	
	private CompanyRepository companyRepository;
	
	@Inject
	public TwitterController(Twitter twitter, ConnectionRepository connectionRepository, UserRepository userRepository, CompanyRepository companyRepository) {
		this.twitter = twitter;
		this.connectionRepository = connectionRepository;
		this.userRepository = userRepository;
		this.companyRepository = companyRepository;
	}

	/*Crawls twitter and adds data to neo4j*/
    @RequestMapping(method=RequestMethod.GET)
    public String searchTwitter(Model model) throws InterruptedException{
    	//Check if this app has been authorized
        if (connectionRepository.findPrimaryConnection(Twitter.class) == null) {
           return "redirect:/connect/twitter";
        }
        
        
        SearchResults searchResults = twitter.searchOperations().search("-RT Uber");   //Searches for tweets
        tweetList = searchResults.getTweets();										   //Stores result of search in tweetList

    	Company company = companyRepository.findCompanyByCompanyName("Uber");
    	
    	//Get each tweet data
            for(Tweet tweet:tweetList) {
            	Long id = tweet.getId();
            	String screenName = tweet.getUser().getScreenName();
             	User user = new User(id,screenName);
             	user.setFollowerCount(tweet.getUser().getFollowersCount());
            	userRepository.save(user);
            	
            	company.tweetedBy(user);		//User tweeted about company, make the link
            	
            	//Get the RTs of this tweet
                List<Tweet> rtUserList = twitter.timelineOperations().getRetweets(user.getId());
                user = userRepository.findUserByScreenName(user.getName()); 
                
                try {
                    if(rtUserList!=null) {
                    	user.setRetweetCount(rtUserList.size());
                        for(Tweet rUser:rtUserList) {
                        	User user1 = new User(rUser.getUser().getId(),rUser.getUser().getScreenName());
                        	user1.setFollowerCount(rUser.getUser().getFollowersCount());
                        	user1.setRetweetCount(0);
                        	userRepository.save(user1);
                        	if(!user.retweets.contains(user1))
                        		user.retweetedBy(user1);
                        }
                    }
                }
                //If the rate-limit is exceeded, don't want to just crash, save the RTs data we got thus far
                catch(RateLimitExceededException e) {
                	userRepository.save(user);
                	companyRepository.save(company);
                }
                //Save this user to neo4j
            	userRepository.save(user);
            }
        companyRepository.save(company);
        
        //Just to output the tweets to a webpage
        model.addAttribute("tweetList",tweetList);
        return "search";
    } 
    
}
