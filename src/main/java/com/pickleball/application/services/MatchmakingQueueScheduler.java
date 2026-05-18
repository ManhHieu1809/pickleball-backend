package com.pickleball.application.services;

import com.pickleball.application.usecases.matchmaking.ProcessMatchmakingQueueUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchmakingQueueScheduler {

    private static final Logger log = LoggerFactory.getLogger(MatchmakingQueueScheduler.class);

    private final ProcessMatchmakingQueueUseCase processMatchmakingQueueUseCase;

    @Scheduled(fixedDelay = 20000)
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
