package com.leader.api.data;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${mongo.username}")
    private String username;

    @Value("${mongo.authentication-database}")
    private String authenticationDatabase;

    @Value("${mongo.password}")
    private String password;

    @Value("${mongo.repository-database}")
    private String repositoryDatabase;

    public MongoClient mongoClient() {
        return MongoClients.create(MongoClientSettings.builder()
                .credential(MongoCredential.createCredential(
                        username,
                        authenticationDatabase,
                        password.toCharArray()
                ))
                .build()
        );
    }

    @Override
    protected String getDatabaseName() {
        return repositoryDatabase;
    }

    @Override
    public MongoTemplate mongoTemplate(MongoDatabaseFactory databaseFactory, MappingMongoConverter converter) {
        return new MongoTemplate(mongoClient(), getDatabaseName());
    }
}
