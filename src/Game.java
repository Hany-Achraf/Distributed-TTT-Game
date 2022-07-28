/*
 * author: Hany Ashraf
 **/

import java.lang.*;

public class Game {
    public int playerMove = 1; // to determine who should play in this turn, s (x -> 1, O -> -1)
    private String [] board = new String[9];

    public Game() {
        for (int i = 0; i < 9; i++)
            board[i] = "";
    }

    // check and submit player move
    public boolean submitMove(int placeNumber) {
        if (placeNumber < 0 || placeNumber > 8)
            return false;

        if (!this.board[placeNumber].isEmpty())
            return false;

        this.board[placeNumber] = this.playerMove == 1 ? "X" : "O";
        this.playerMove = -this.playerMove;
        return true;
    }

    // helps during producing Message 3 before sending it from the server to the client
    // Sample Output: X OX O   X
    public String printState() {
        String output = "";
        for (int i = 0; i < 9; i++)
            output += this.board[i].isEmpty() ? " " : this.board[i];

        return output;
    }


    public String checkWin() {
        final int [][] lines = {
                {0, 1, 2},
                {3, 4, 5},
                {6, 7, 8},
                {0, 3, 6},
                {1, 4, 7},
                {2, 5, 8},
                {0, 4, 8},
                {2, 4, 6}
        };

        for (int i = 0; i < lines.length; i++) {
            final int [] line = lines[i];
            if (!board[line[0]].isEmpty() && board[line[0]]  == board[line[1]] && board[line[0]]  == board[line[2]]) {
                return board[line[0]]; // return X or O, depending on who is the winner
            }
        }

        for (String sqr: board)
            if (sqr.isEmpty())
                return "0"; // There is a room for more moves

        return "D"; // Draw game
    }

}
