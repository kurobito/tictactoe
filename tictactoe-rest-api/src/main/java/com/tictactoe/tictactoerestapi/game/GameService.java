package com.tictactoe.tictactoerestapi.game;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.tictactoe.tictactoerestapi.FirebaseService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class GameService extends FirebaseService {
    private List<Move> playerMoves;
    private List<Move> cpuMoves;
    private GameRecord gameRecord;
    private GameStatus gameStatus;
    private int cpuMovesSize;
    private int playerMovesSize;
    private int[][] gameBoard = new int[3][3];

    public List<Move> getCpuMoves() {
        cpuMoves = new ArrayList<>();
        Query group = db.collectionGroup("cpu_moves");
        ApiFuture<QuerySnapshot> querySnapshot = group.get();
        try {
            for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
                Move move = doc.toObject(Move.class);
                if (move != null) {
                    cpuMoves.add(move);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        cpuMovesSize = cpuMoves.size();
        return cpuMoves;
    }

    public List<Move> getPlayerMoves() {
        playerMoves = new ArrayList<>();
        Query group = db.collectionGroup("player_moves");
        ApiFuture<QuerySnapshot> querySnapshot = group.get();
        try {
            for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
                Move move = doc.toObject(Move.class);
                if (move != null) {
                    playerMoves.add(move);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        playerMovesSize = playerMoves.size();
        return playerMoves;
    }

    public int[][] getBoard() {
        return gameBoard;
    }

    public GameStatus save(Move newMove) {
        getPlayerMoves();
        ApiFuture<WriteResult> future = db.collection("games").document("1")
                .collection("player_moves").document(String.valueOf(playerMovesSize)).set(newMove);
        try {
            System.out.println("Update time : " + future.get().getUpdateTime());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        getPlayerMoves();
        gameStatus = updateBoard();
        if (gameStatus != GameStatus.ONGOING) {
            return gameStatus;
        } else {
            getCpuMoves();
            cpuMove();
            getCpuMoves();
            gameStatus = updateBoard();
        }
        return gameStatus;
    }

    public void clearBoard() {
        CollectionReference playerMovesRef = db.collection("games").document("1").collection("player_moves");
        CollectionReference cpuMovesRef = db.collection("games").document("1").collection("cpu_moves");

        deleteCollection(playerMovesRef, 100);
        deleteCollection(cpuMovesRef, 100);
        cpuMoves = new ArrayList<>();
        playerMoves = new ArrayList<>();

        for (int[] rows : gameBoard) {
            Arrays.fill(rows, 0);
        }
        gameStatus = GameStatus.ONGOING;
    }

    public void updateRecord(GameStatus gameStatus) {
        DocumentReference gamesRef = db.collection("games").document("1");
        switch (gameStatus) {
            case WIN:
                gamesRef.update("wins", FieldValue.increment(1));
                break;
            case LOSE:
                gamesRef.update("losses", FieldValue.increment(1));
                break;
            case TIE:
                gamesRef.update("ties", FieldValue.increment(1));
                break;
        }
    }

    public GameRecord getGameRecord() {
        DocumentReference gamesRef = db.collection("games").document("1");
        ApiFuture<DocumentSnapshot> future = gamesRef.get();
        try {
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                gameRecord = document.toObject(GameRecord.class);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return gameRecord;
    }

    private void cpuMove() {
        int randomX = (int) (Math.random() * 3);
        int randomY = (int) (Math.random() * 3);
        Move newMove = new Move(randomX, randomY);

        while (cpuMoves.contains(newMove) || playerMoves.contains(newMove)) {
            randomX = (int) (Math.random() * 3);
            randomY = (int) (Math.random() * 3);
            newMove = new Move(randomX, randomY);
//            System.out.println("cpuMove: " + newMove);
        }
        System.out.println(playerMoves);
        ApiFuture<WriteResult> future = db.collection("games").document("1")
                .collection("cpu_moves").document(String.valueOf(cpuMovesSize)).set(newMove);
        try {
            System.out.println("Update time : " + future.get().getUpdateTime());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private void deleteCollection(CollectionReference collection, int batchSize) {
        try {
            // retrieve a small batch of documents to avoid out-of-memory errors
            ApiFuture<QuerySnapshot> future = collection.limit(batchSize).get();
            int deleted = 0;
            // future.get() blocks on document retrieval
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                document.getReference().delete();
                ++deleted;
            }
            if (deleted >= batchSize) {
                // retrieve and delete another batch
                deleteCollection(collection, batchSize);
            }
        } catch (Exception e) {
            System.err.println("Error deleting collection : " + e.getMessage());
        }
    }

    private GameStatus updateBoard() {
        if (cpuMoves == null || playerMoves == null) return GameStatus.ONGOING;

        if (!cpuMoves.isEmpty()) {
            for (Move move : cpuMoves) {
                gameBoard[move.getY()][move.getX()] = 1;
            }
        }
        if (!playerMoves.isEmpty()) {
            for (Move move : playerMoves) {
                gameBoard[move.getY()][move.getX()] = 2;
            }
        }
        return gameOver();
    }

    private GameStatus gameOver() {
        // row check
        for (int[] row : gameBoard) {
            int playerMatches = 0;
            int cpuMatches = 0;
            for (int cell : row) {
                if (cell == 1) cpuMatches++;
                if (cell == 2) playerMatches++;
            }
            if (playerMatches == 3) return GameStatus.WIN;
            if (cpuMatches == 3) return GameStatus.LOSE;
        }


        // cross check
        int playerMatches = 0;
        int cpuMatches = 0;
        for (int y = 0; y < gameBoard.length; y++) {
            for (int x = 0; x < gameBoard[y].length; x++) {
                if (x == y) {
                    if (gameBoard[y][x] == 1) cpuMatches++;
                    if (gameBoard[y][x] == 2) playerMatches++;
                }
            }
        }
        if (playerMatches == 3) return GameStatus.WIN;
        if (cpuMatches == 3) return GameStatus.LOSE;

        // cross check
        playerMatches = 0;
        cpuMatches = 0;
        for (int y = 0; y < gameBoard.length; y++) {
            for (int x = 0; x < gameBoard[y].length; x++) {
                if ((x + y) == (gameBoard.length - 1)) {
                    if (gameBoard[y][x] == 1) cpuMatches++;
                    if (gameBoard[y][x] == 2) playerMatches++;
                }
            }
        }
        if (playerMatches == 3) return GameStatus.WIN;
        if (cpuMatches == 3) return GameStatus.LOSE;


        // transpose to get columns
        int[][] transposed = new int[3][3];
        for (int y = 0; y < gameBoard.length; y++) {
            for (int x = 0; x < gameBoard[y].length; x++) {
                transposed[y][x] = gameBoard[x][y];
            }
        }

        // column check
        for (int[] row : transposed) {
            playerMatches = 0;
            cpuMatches = 0;
            for (int cell : row) {
                if (cell == 1) cpuMatches++;
                if (cell == 2) playerMatches++;
            }
            if (playerMatches == 3) return GameStatus.WIN;
            if (cpuMatches == 3) return GameStatus.LOSE;
        }


        if (playerMoves.size() + cpuMoves.size() == 9)
            return GameStatus.TIE;
        else return GameStatus.ONGOING;
    }

}
