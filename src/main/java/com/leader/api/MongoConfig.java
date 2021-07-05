package com.leader.api;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import java.util.Collections;

@Configuration
@EnableMongoRepositories
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${mongo.host}")
    private String host;

    @Value("${mongo.port}")
    private int port;

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
        builder.applyToClusterSettings(settings -> {
            settings.hosts(Collections.singletonList(new ServerAddress(host, port)));
        });
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
}
