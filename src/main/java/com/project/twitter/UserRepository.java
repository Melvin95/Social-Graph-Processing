package com.project.twitter;


import org.springframework.data.neo4j.repository.GraphRepository;

public interface UserRepository extends GraphRepository<User> {

    User findUserByScreenName(String screenName);

    User findUserBytweetId(Long tweetId);
    
    User findUserById(Long id);
}

