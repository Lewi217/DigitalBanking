package com.mycompany.app.repository;

import com.mycompany.app.model.ProductRecommendation;
import com.mycompany.app.model.RecommendationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ProductRecommendationRepository extends MongoRepository<ProductRecommendation, String> {

    List<ProductRecommendation> findByUserId(String userId);
    List<ProductRecommendation> findByUserIdAndStatus(String userId, RecommendationStatus status);
    List<ProductRecommendation> findByUserIdAndStatusOrderByScoreDesc(String userId, RecommendationStatus status);
    List<ProductRecommendation> findByValidUntilBeforeAndStatus(Instant now, RecommendationStatus status);
    List<ProductRecommendation> findByProductType(String productType);
}
