package com.pickleball.application.services;

import com.pickleball.application.usecases.matchmaking.ProcessMatchmakingQueueUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MatchmakingQueueScheduler {

    private final ProcessMatchmakingQueueUseCase processMatchmakingQueueUseCase;

    @Scheduled(fixedDelay = 60000)
    public void runMatchmakingProcess() {
        log.debug("Starting scheduled Matchmaking Queue Processor...");
        try {
            processMatchmakingQueueUseCase.execute();
            log.debug("Finished Matchmaking Queue Processor.");
        } catch (Exception e) {
            log.error("Error processing matchmaking queue: {}", e.getMessage(), e);
        }
    }
}

