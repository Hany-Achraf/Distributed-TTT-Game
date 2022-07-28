/*
 * author: Hany Ashraf
 **/

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private ClientSideConnection csc;
    private boolean isFirstPlayer;

    public Client(String ClientPortNumber) {
        csc = new ClientSideConnection(Integer.parseInt(ClientPortNumber));
        isFirstPlayer = false; // the first joined player will change his status later when notifying from the server
    }

    // Inner class to handle client-side connection with the server-side
    public class ClientSideConnection {
        private Socket socket;
        private BufferedReader dataIn;
        private DataOutputStream dataOut;

        public ClientSideConnection(int ClientPortNumber) {
            System.out.println("\n#######  Welcome to Tic-Tack-Toe Game #######\n");
            try {
                socket = new Socket("localhost", 5050, InetAddress.getByName("localhost"), ClientPortNumber);
                dataIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                dataOut = new DataOutputStream(socket.getOutputStream());

                String receivedMessage, clientInput;
                Scanner keyboard = new Scanner(System.in);

                while (dataIn != null) { // Looping till no longer messages are coming from the server, or player quits.
                    // reading the delivered message from the server.
                    receivedMessage = dataIn.readLine().trim();

                    if (receivedMessage.equals("0")) { // Message 0
                        System.out.println(">>Success connected to server on port " + socket.getLocalPort());
                        dataOut.writeBytes("0" + "\r\n");
                    } else if (receivedMessage.equals("1W")) { // Message 1W (First joined player)
                        System.out.println("***** You are player 'X', you will go first *****\n");
                        isFirstPlayer = true;
                        System.out.println(">>Waiting for other player...");
                        System.out.println(">>Press Q to quit the game.");
                        System.out.println(">>Press C to continue waiting other player.");
                        if ((clientInput = keyboard.nextLine()) != null && clientInput.equalsIgnoreCase("C")) {
                            System.out.println("\n>>Game continue...");
                            dataOut.writeBytes("2R" + "\r\n"); // respond by 2R message
                        } else if (clientInput.equalsIgnoreCase("Q")){
                            System.out.println("\n");
                            dataOut.writeBytes("2Q" + "\r\n"); // respond by 2Q message to quit
                            break;
                        }
                    } else if (receivedMessage.equals("1R")) { // Messgae 1R
                        if (!isFirstPlayer)
                            System.out.println("***** You are player 'O', you will go second *****\n");
                        System.out.println(">>Another player has joined...");
                        System.out.println(">>Press Q to quit the game.");
                        System.out.println(">>Press C to star playing.");
                        if ((clientInput = keyboard.nextLine()) != null && clientInput.equalsIgnoreCase("C"))
                            dataOut.writeBytes("2R" + "\r\n"); // respond by 2R message
                        else if (clientInput.equalsIgnoreCase("Q")){
                            System.out.println("\n");
                            dataOut.writeBytes("2Q" + "\r\n"); // respond by 2R message to quit
                            break;
                        }
                    } else if (receivedMessage.startsWith("3")) { // Handlnig Message from the type 3
                        if (receivedMessage.charAt(10) == 'X') {
                            System.out.println("\n----- Player X has won the game -----\n");
                            printBoardFormatted(receivedMessage.substring(1), isFirstPlayer);
                            if (isFirstPlayer)
                                System.out.println("\n##### Congrats!! You're the winner :) #####\n");
                            else
                                System.out.println("\n##### Sorry, you lose this game :( #####\n");
                            break;
                        } else if (receivedMessage.charAt(10) == 'O') {
                            System.out.println("\n----- Player O has won the game -----\n");
                            if (!isFirstPlayer)
                                System.out.println("\n##### Congrats!! You're the winner :) #####\n");
                            else
                                System.out.println("\n##### Sorry, you lose this game :( #####\n");
                            break;
                        } else if (receivedMessage.charAt(10) == 'D') {
                            printBoardFormatted(receivedMessage.substring(1), isFirstPlayer);
                            System.out.println("\n----- Draw game-----\n");
                            break;
                        } else if (receivedMessage.charAt(10) == 'P') {
                            printBoardFormatted(receivedMessage.substring(1), isFirstPlayer);
                            System.out.println("\n>>Game in progress...");
                            if (receivedMessage.charAt(11) == '0') {
                                System.out.println(">>Your turn to move.");
                                dataOut.writeBytes("4" + (clientInput = keyboard.nextLine()) + "\r\n");
                                if (clientInput.equalsIgnoreCase("Q")) {
                                    System.out.println("\n----- Game Over -----\n");
                                    break;
                                }
                            } else if (receivedMessage.charAt(11) == '1') {
                                System.out.println(">>Invalid move! Please try again..");
                                dataOut.writeBytes("4" + keyboard.nextLine() + "\r\n");
                            } else if (receivedMessage.charAt(11) == '2') {
                                System.out.println(">>Game forfeited by other player.\n");
                                break;
                            }
                        }
                    }
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        // using args to get the port number which the player want to use for his client-side socket
        Client c = new Client(args[0]);
    }

    private void printBoardFormatted(String boardData, boolean isFirstPlayer) {
        String [] board = new String[9];
        for (int i = 0; i < 9; i++)
            board[i] = (boardData.charAt(i) == ' ') ? ("'" + i + "'") : (" " + boardData.charAt(i) + " ");

        System.out.println("\n************************");
        System.out.println(" Tic-Tac-Toe Game Board");
        System.out.println("************************\n");
        System.out.format(" %2s  | %2s | %2s \n", board[0], board[1], board[2]);
        System.out.println("------+-----+----");
        System.out.format(" %2s  | %2s | %2s \n", board[3], board[4], board[5]);
        System.out.println("------+-----+----");
        System.out.format(" %2s  | %2s | %2s \n", board[6], board[7], board[8]);

        System.out.println("\n************* Game Instruction **************" +
                           "\n1. Enter box number that you want to place " + (isFirstPlayer ? "X." : "O.") +
                           "\n2. Enter Q to quit the game." +
                           "\n*********************************************\n");
    }
}
