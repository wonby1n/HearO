package com.ssafy.hearo.domain.user.repository;

import com.ssafy.hearo.domain.user.entity.User;
import com.ssafy.hearo.domain.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRole role);

    /**
     * 상담원 역할을 가진 모든 사용자 ID 조회
     */
    @Query("SELECT u.id FROM User u WHERE u.role IN ('USER', 'VETERAN')")
    Set<Long> findAllCounselorIds();

    /**
     * 주어진 ID 목록에 해당하는 상담원 조회
     */
    @Query("SELECT u FROM User u WHERE u.id IN :ids AND u.role IN ('USER', 'VETERAN')")
    List<User> findCounselorsByIds(Set<Long> ids);
}
