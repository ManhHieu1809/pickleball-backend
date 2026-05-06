package com.pickleball.application.services;

import com.pickleball.application.dtos.LeaderboardPageDTO;
import com.pickleball.application.usecases.GetGlobalLeaderboardUseCase;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardApplicationService {

    private final GetGlobalLeaderboardUseCase getGlobalLeaderboardUseCase;

    public LeaderboardApplicationService(GetGlobalLeaderboardUseCase getGlobalLeaderboardUseCase) {
        this.getGlobalLeaderboardUseCase = getGlobalLeaderboardUseCase;
    }

    public LeaderboardPageDTO getGlobalLeaderboard(int page, int size) {
        return getGlobalLeaderboardUseCase.execute(page, size);
    }
}

