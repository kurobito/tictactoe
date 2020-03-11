import axios from "axios";

const API_URL = "http://localhost:8080";

class GameDataService {
  retrieveGameBoard() {
    return axios.get(`${API_URL}/game`);
  }

  newMove(move) {
    return axios.post(`${API_URL}/game`, move);
  }

  newGame() {
    return axios.get(`${API_URL}/game/new`);
  }

  getGameRecord() {
    return axios.get(`${API_URL}/game/record`);
  }
}

export default new GameDataService();
