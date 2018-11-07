package com.project;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Stack;
import javax.inject.Inject;

import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.social.RateLimitExceededException;
import org.springframework.social.twitter.api.Twitter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.project.twitter.Company;
import com.project.twitter.CompanyRepository;
import com.project.twitter.User;
import com.project.twitter.UserRepository;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

/*Controller for stats page and influences page*/
@Controller
@EnableNeo4jRepositories
public class QueryController {
	
	private CompanyRepository companyRepository;
	
	private UserRepository userRepository;
	
	private Twitter twitter;
	
	private static StanfordCoreNLP pipeline;

	@Inject
	public QueryController(Twitter twitter, CompanyRepository companyRepository, UserRepository userRepository) {
		this.companyRepository = companyRepository;
		this.twitter = twitter;
		pipeline = new StanfordCoreNLP("application.properties");
		this.userRepository = userRepository;
	}
	
	
	/*Does sentiment analysis on tweets from database. 
	 *Initially was using a text file to transfer data between this project and the SentimentAnalysis program 
	 *because Stanford API wasn't working with Spring. 
	 *Just recently found out that the Stanford API must be added as a dependency in the pom.xml and not manually added as external JAR
	 *Stanford API is also available in the Maven repository. Silly us for doubting Spring's power.
	 *
	 *Stores the sentiment value as a property on the User node
	 */
	@RequestMapping(value = "/sentiment")
	public String SentimentAnalysis() throws InterruptedException, FileNotFoundException, UnsupportedEncodingException {
		
   	 	Company taxify = companyRepository.findCompanyByCompanyName("Taxify");
   	 	Company uber = companyRepository.findCompanyByCompanyName("Uber");
		        
	 	Set<User> taxifyUsers = taxify.getUsers();
	    Set<User> uberUsers = uber.getUsers();
			
		   for(User n:taxifyUsers) {
			   if(!n.isSentimateDone()) {		//Only analyse if tweet hasn't been analysed yet
				   try {
						 String text = twitter.timelineOperations().getStatus(n.getId()).getText();
						 int sentiment = findSentiment(text);
						 n.setSentiment(sentiment);
						 n.analysed();
						 userRepository.save(n);
					   }
					   catch(Exception e) {}
			   }
		   }
		   
	       for(User m:uberUsers) {
	    	   if(!m.isSentimateDone()) {
				   try {
						 String text = twitter.timelineOperations().getStatus(m.getId()).getText();
						 int sentiment = findSentiment(text);
						 m.setSentiment(sentiment);
						 m.analysed();
						 userRepository.save(m);
					   }
					   catch(Exception e) {}
	    	   }
		   }
		return "crew/index";	
	}
	
	//Renders stats page of sentiment analysis and RT distribution
	@RequestMapping(value="/annual-sales-and-website-traffic")
	public String stats(Model model) throws InterruptedException {
		
	   Company taxify = companyRepository.findCompanyByCompanyName("Taxify");
	   Company uber = companyRepository.findCompanyByCompanyName("Uber");
	        
       Set<User> taxifyUsers = taxify.getUsers();
       Set<User> uberUsers = uber.getUsers();

		   //Number of RTs for tweets about taxify and uber
	       int tCount = 0;	
	       int uCount = 0;
	       
	       //Sentiment totals
		   int positiveTax = 0;
		   int negativeTax = 0;
		   int positiveUber = 0;
		   int negativeUber = 0;
		   
	   for(User n:taxifyUsers) {
		   tCount += n.getRetweetCount();
		   try {
			   if(n.getSentiment()>=2)
				   positiveTax++;
			   else 
				   negativeTax++;
		   }
		   catch(Exception e) {}
	   }
	          
       for(User m:uberUsers) {
    	   uCount += m.getRetweetCount();
		   try {
			   if(m.getSentiment()>=2)
				   positiveUber++;
			   else 
				   negativeUber++;
		   }
		   catch(Exception e) {}
	   }



	       //Percentage of tweets that were negative, simply for for webpage, will add this to the model
	       double taxifyPercentage = Math.round((negativeTax/(double)(negativeTax+positiveTax))*100);
	       String t = taxifyPercentage+" ";
	       double uberPercentage =Math.round((negativeUber/(double)(negativeUber+positiveUber))*100);
	       String u = uberPercentage +" ";
	       
	       model.addAttribute("tCount", tCount); //total number of RTs on taxify
	       model.addAttribute("uCount", uCount); //total number of RTs on uber
	       model.addAttribute("t",t);
	       model.addAttribute("u",u);
	       model.addAttribute("tNegative",negativeTax);
	       model.addAttribute("tPositive",positiveTax);
	       model.addAttribute("uNegative",negativeUber);
	       model.addAttribute("uPositive",positiveUber);
	       
		return "crew/annual-sales-and-website-traffic";
	}
	
	//Returns the sentiment score of a specific tweet
	public static int findSentiment(String tweet) {

		int mainSentiment = 0;
		if (tweet != null && tweet.length() > 0) {
			int longest = 0;
			Annotation annotation = pipeline.process(tweet);
			for (CoreMap sentence : annotation
					.get(CoreAnnotations.SentencesAnnotation.class)) {
				Tree tree = sentence
						.get(SentimentAnnotatedTree.class);
				int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
				String partText = sentence.toString();
				if (partText.length() > longest) {
					mainSentiment = sentiment;
					longest = partText.length();
				}

			}
		}
		return mainSentiment;
	}
	
	//Renders influnces page, that is, the users with the most followers
	@RequestMapping(value="/influences")
	public String influenceStats(Model model) {
		
		//Top 5 for Uber, that is top 5 with most followers
		Company uber = companyRepository.findCompanyByCompanyName("Uber");
		ArrayList<User> uberTop = uber.getTop();
		
		//Top 5 for Taxify
		Company taxify = companyRepository.findCompanyByCompanyName("Taxify");
		ArrayList<User> taxifyTop = taxify.getTop();
		
		//Array into model, will use thymeleaf to get each value in HTML page
		model.addAttribute("taxifyTop",taxifyTop);
		model.addAttribute("uberTop",uberTop);
		
		return "crew/annual-sales";
	}
		
}
