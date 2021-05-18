package com.leader.api.data.user;

import com.leader.api.util.Util;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, ObjectId> {

    User findByUsername(String username);

    User findByPhone(String phone);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    default void insertUser(String username, String password, String phone, int saltLength) {
        User user = new User();
        user.username = username;
        user.phone = phone;
        String salt = Util.createRandomSalt(saltLength);
        user.password = Util.SHA1(password + salt);
        user.salt = salt;
        insert(user);
    }

    default boolean validateUser(String username, String password) {
        User user = findByUsername(username);
        String salt = user.salt;
        String processedPassword = Util.SHA1(password + salt);
        return user.password.equals(processedPassword);
    }

    default void updatePasswordByPhone(String phone, String password, int saltLength) {
        User user = findByPhone(phone);
        String salt = Util.createRandomSalt(saltLength);
        user.password = Util.SHA1(password + salt);
        user.salt = salt;
        save(user);
    }

    default ObjectId getIdByUsername(String username) {
        User user = findByUsername(username);
        return user.id;
    }
}
