package com.uxsino.repository;

import com.uxsino.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<Token, Integer> {
    /**
     * 通过token查找
     *
     * @param token
     * @return
     */
    Token findByToken(String token);

    /**
     * 通过userID查找
     *
     * @param userId
     * @return
     */
    Token findByUserId(String userId);
}
