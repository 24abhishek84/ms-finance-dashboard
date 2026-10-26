package com.finance.dashboard.repository;

import com.finance.dashboard.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {

    long countByEmailAndSuccessFalseAndAttemptTimeGreaterThan(String email, LocalDateTime since);

    long countByIpAddressAndSuccessFalseAndAttemptTimeGreaterThan(String ipAddress, LocalDateTime since);

    List<LoginAttempt> findByEmailAndAttemptTimeGreaterThanOrderByAttemptTimeDesc(String email, LocalDateTime since);

    List<LoginAttempt> findByIpAddressAndAttemptTimeGreaterThanOrderByAttemptTimeDesc(String ipAddress, LocalDateTime since);
}
