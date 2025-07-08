package com.mycompany.app.repository;

import com.mycompany.app.model.UserBehavior;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface UserBehaviorRepository extends MongoRepository<UserBehavior, String> {
    List<UserBehavior> findByUserId(String userId);

    List<UserBehavior> findByUserIdAndTimestampBetween(String userId, Instant start, Instant end);

    List<UserBehavior> findBySessionId(String sessionId);

    List<UserBehavior> findByUserIdAndActionOrderByTimestampDesc(String userId, String action);
}
