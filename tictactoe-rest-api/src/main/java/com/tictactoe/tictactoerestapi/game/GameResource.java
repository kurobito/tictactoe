package com.tictactoe.tictactoerestapi.game;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:8081"})
@RestController
public class GameResource {

    @Autowired
    private GameService gameService;


    @GetMapping("/game")
    public int[][] getBoard() {
        return gameService.getBoard();
    }

    @PostMapping("/game")
    @ResponseBody
    public int newPlayerMove(@RequestBody Move move) {
        GameStatus gameStatus = gameService.save(move);
        if (gameStatus != GameStatus.ONGOING) {
            gameService.updateRecord(gameStatus);
        }
        return gameStatus.getStatusCode();
    }

    @GetMapping("/game/record")
    public GameRecord getRecord() {
        return gameService.getGameRecord();
    }

    @GetMapping("/game/new")
    @ResponseBody
    public ResponseEntity getNewBoard() {
        gameService.clearBoard();
        return new ResponseEntity(HttpStatus.OK);
    }
}
