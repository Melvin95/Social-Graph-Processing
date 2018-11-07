package com.project.twitter;

import org.springframework.data.neo4j.repository.GraphRepository;

public interface CompanyRepository extends GraphRepository<Company>{
	
	Company findCompanyByCompanyName(String companyName);
	
}

