package com.project.twitter;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.springframework.beans.factory.annotation.Autowired;

/*Represents tweets not specifically users as users can have multiple tweets of one or both company*/
@NodeEntity
public class User implements Comparable<User> {

	@GraphId 
	private Long id;
	
	//Unique tweet
	@Index(unique=true)
	private Long tweetId;
	
	//Sentiment of tweet
	private int sentiment;  
	
	//Whether tweet has been analysed
	private boolean sentimentDone = false;
	
	//Stores those users who retweeted this tweet
	@Relationship(type="RETWEETS",direction = "INCOMING")
	public Set<User> retweets = new HashSet<>();
	
	public void retweetedBy(User user) {
		retweets.add(user);
	}
	
	//Twitter handle @
	private String screenName;
	
	private int followerCount;
	
	private int retweetCount;
	
	public User() {
		
	}
	
	public User(Long tweetId, String screenName) {
		this.tweetId = tweetId;
		this.screenName = screenName;
	}
	
	public String getName() {
		return screenName;
	}
	
	public void setRetweetCount(int c) {
		this.retweetCount = c;
	}
	
	public void setFollowerCount(int c) {
		this.followerCount = c;
	}
	
	public int getRetweetCount() {
		return retweetCount;
	}
	
	public int getfollowerCount() {
		return followerCount;
	}
	
	public Long getId() {
		return tweetId;
	}
	
	
	public void setSentiment(int sentiment) {
		this.sentiment = sentiment;
	}
	
	public int getSentiment() {
		return sentiment;
	}
	
	public boolean isSentimateDone() {
		return sentimentDone;
	}
	
	public void analysed() {
		sentimentDone=true;
	}
	@Override
	public int compareTo(User user) {
		if(this.followerCount>user.getfollowerCount())
			return 1;
		else if(this.followerCount<user.getfollowerCount())
			return -1;
		else
			return 0;
	}
	
}
