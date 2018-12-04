package com.bosphere.filelogger;

public final class FLGameConfig {
   private String gameId;
    private Long startTime;

    public FLGameConfig(String gameId, Long startTime) {
        this.gameId = gameId;
        this.startTime = startTime;
    }

    public String getGameId() {
        return gameId;
    }

    private void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public Long getStartTime() {
        return startTime;
    }

    private void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
}
