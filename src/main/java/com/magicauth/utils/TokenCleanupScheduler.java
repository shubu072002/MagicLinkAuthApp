package com.magicauth.utils;

import com.magicauth.repository.MagicTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {
    private final MagicTokenRepository repository;

    @Scheduled(cron = "0 */30 * * * *")
    public void cleanup() {
        repository.deleteExpiredAndUsedTokens(LocalDateTime.now());
    }
}
