package com.mycompany.app.repository;

import com.mycompany.app.model.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FraudRuleRepository extends JpaRepository<FraudRule, Long> {
    List<FraudRule> findByActiveTrue();
    List<FraudRule> findByRuleType(String ruleType);
}
