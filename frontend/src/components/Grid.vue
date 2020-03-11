<template>
  <div>
    <p>
      <span class="wins">Wins: {{ wins }}</span>
      <span class="losses">Losses: {{ losses }}</span>
      <span class="ties">Ties: {{ ties }}</span>
    </p>
    <table class="board">
      <tr v-for="(row, y) in items" :key="y">
        <td @click="playerClick(y, x)" v-for="(col, x) in row" :key="x">
          <p class="cell">{{ setCellValue(items[y][x]) }}</p>
        </td>
      </tr>
    </table>

    <button @click="newGame()" class="btn">New game</button>
  </div>
</template>

<script>
import GameDataService from "../service/GameDataService";

export default {
  name: "GameBoard",
  data() {
    return {
      items: [
        [0, 0, 0],
        [0, 0, 0],
        [0, 0, 0]
      ],
      gameOver: false,
      wins: 0,
      losses: 0,
      ties: 0
    };
  },
  methods: {
    setCellValue(i) {
      switch (i) {
        case 1:
          return "O";
        case 2:
          return "X";
        default:
          return " ";
      }
    },
    playerClick(moveY, moveX) {
      if (!this.gameOver) {
        if (this.items[moveY][moveX] == 0) {
          GameDataService.newMove({ x: moveX, y: moveY })
            .then(response => {
              switch (response.data) {
                case 210:
                  alert("You win");
                  this.gameOver = true;
                  break;
                case 211:
                  alert("You lose");
                  this.gameOver = true;
                  break;
                case 212:
                  alert("It's a tie");
                  this.gameOver = true;
                  break;
              }
              if (this.gameOver) this.getGameRecord();
            })
            .then(() => this.refreshBoard());
        }
      }
    },
    refreshBoard() {
      console.log("called");
      GameDataService.retrieveGameBoard()
        .then(response => {
          this.items = response.data;
        });
    },
    newGame() {
      GameDataService.newGame()
        .then(async response => {
          await response;
        })
        .then(() => {
          this.gameOver = false;
          this.refreshBoard();
        });
    },
    getGameRecord() {
      GameDataService.getGameRecord().then(response => {
        this.wins = response.data.wins;
        this.losses = response.data.losses;
        this.ties = response.data.ties;
      });
    }
  },
  created() {
    this.refreshBoard();
    this.getGameRecord();
  }
};
</script>
<style scoped>
.board {
  margin: 0 auto;
}

.cell {
  padding: 24px;
  width: 48px;
  height: 24px;
  font-size: 24px;
}
table {
  border-collapse: collapse;
}
table,
th,
td {
  border: 1px solid black;
}
.btn {
  padding: 12px;
  margin: 12px auto;
}

p span {
  padding-right: 36px;
}
</style>
