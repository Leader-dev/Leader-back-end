package com.leader.api;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import org.springframework.beans.factory.annotation.Value;
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

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        builder.credential(
            MongoCredential.createCredential(
                    username,
                    authenticationDatabase,
                    password.toCharArray()
            )
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
