package com.bosphere.filelogger;

public final class FLGameConfig {
   private String gameId;
    private Long startTime;
    private String gameType;

    public FLGameConfig(String gameId, String gameType, Long startTime) {
        this.gameId = gameId;
        this.startTime = startTime;
        this.gameType = gameType;
    }

    public String getGameId() {
        return gameId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public String getGameType() {
        return gameType;
    }
}
