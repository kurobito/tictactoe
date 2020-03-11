package com.tictactoe.tictactoerestapi.game;

public enum GameStatus {
    WIN(210),
    LOSE(211),
    TIE(212),
    ONGOING(0);
    private final int statusCode;

    GameStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
