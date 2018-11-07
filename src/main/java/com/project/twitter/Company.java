package com.project.twitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/*Represents the company node in the database*/

@NodeEntity
public class Company {

	@GraphId 
	private Long id;
	
	private String companyName;
	
	//Stores the users who tweeted about this company
	@Relationship(type="TWEETED ABOUT",direction="INCOMING")
	public Set<User> twAbout = new HashSet<>();
	
	//Makes the relationship between the user and a company
	public void tweetedBy(User user) {
		twAbout.add(user);
	}
	
	public Company() {
		
	}
	
	public Company(String companyName) {
		this.companyName = companyName;
	}
	
	public String getName() {
		return companyName;
	}
	
	public Set<User> getUsers(){
		return twAbout;
	}
	
	/*Basic analysis, gets top influences of a company on twitter by ranking the users based on follower count, returns the top 5*/
	public ArrayList<User> getTop(){
		ArrayList<User> sorted = new ArrayList<User>();
		for(User u:twAbout) {
			sorted.add(u);
		}
		Collections.sort(sorted);
		
		ArrayList<User> top = new ArrayList<User>();
		int index = sorted.size()-1;
		while(top.size()!=5) {
			if(top.isEmpty())
				top.add(sorted.get(index));
			else {
				boolean br = false;
				for(User t:top) {
					if( t.getName().equals(sorted.get(index).getName()) ) {
						br = true;
						break;
					}	
				}
				if(br==false)
					top.add(sorted.get(index));
			}
			index--;
		}
		return top;
	}

}
