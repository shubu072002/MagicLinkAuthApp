package com.magicauth.repository;

import com.magicauth.model.MagicToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

public interface MagicTokenRepository extends JpaRepository<MagicToken, Long> {
    Optional<MagicToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM MagicToken t WHERE t.expiresAt < :now OR t.used = true")
    void deleteExpiredAndUsedTokens(LocalDateTime now);

    @Modifying
    @Transactional
    @Query("""
            UPDATE MagicToken t
            SET t.used = true
            WHERE t.user.id = :userId
            AND t.used = false
           """)
    void markAllTokensAsUsed(Long userId);
}
