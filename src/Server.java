/*
 * author: Hany Ashraf
**/

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private ServerSocket sc;
    private int numClients;
    private ServerSideConnection client1;
    private ServerSideConnection client2;
    private Game game;

    public Server() {
        game = new Game();
        numClients = 0;
        try {
            sc = new ServerSocket(5050);
        } catch (IOException e) {
            System.out.println(e.fillInStackTrace());
        }
    }

    public void acceptConnections() {
        try {
            System.out.println("Waiting for connections on port 5050...");
            while (numClients < 2) { // Accepting exactly two connections or players
                Socket s = sc.accept();
                numClients++;

                System.out.println("\n>>Client connected to server at port #" + s.getPort());

                ServerSideConnection ssc = new ServerSideConnection(s, numClients);
                if (numClients == 1)
                    client1 = ssc;
                else
                    client2 = ssc;

                // Starting Server Socket Connection thread for each joinded player
                Thread t = new Thread(ssc);
                t.start();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    // Inner class to handle server-side connection with the client-side
    private class ServerSideConnection implements Runnable {
        private Socket socket;
        private BufferedReader dataIn;
        private DataOutputStream dataOut;
        private int playerID;

        public ServerSideConnection(Socket s, int id) {
            socket = s;
            playerID = id;
            try {
                dataIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                dataOut = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // exchanging message 0 between server and client
                dataOut.writeBytes("0" + "\r\n");
                System.out.println("\nSent to player " + playerID + "\nMessage: 0");
                System.out.println("\nReceived from player " + playerID + "\nMessage: " + dataIn.readLine().trim());

                String clientInput;
                if (playerID == 1) { // Sending 1W message and receiving 2R or 2Q from the first joined player.
                    dataOut.writeBytes("1W" + "\r\n");
                    System.out.println("\nSent to player 1\nMessage: 1W");
                    System.out.println("\nReceived from player 1\nMessage: " + (clientInput = dataIn.readLine().trim()));
                    if (clientInput.equals("2Q"))
                        return;
                    while (client2 == null) {} // Waiting another player to join the game
                } else { // Sending 1R and receiving 2R or 2Q messages from both client, to start the game
                    client1.dataOut.writeBytes("1R" + "\r\n");
                    System.out.println("\nSent to player 1\nMessage: 1R");
                    System.out.println("\nReceived from player 1\nMessage: " + (clientInput = client1.dataIn.readLine().trim()));
                    if (clientInput.equals("2Q"))
                        return;

                    dataOut.writeBytes("1R" + "\r\n");
                    System.out.println("\nSent to player 2\nMessage: 1R");
                    System.out.println("\nReceived from player 2\nMessage: " + (clientInput = dataIn.readLine().trim()));
                    if (clientInput.equals("2Q"))
                        return;
                }

                String serverMessage3, playerMessage4, result;
                while (game.checkWin().equals("0")) { // Looping till any player wins, draw game, or someone quits
                    if (game.playerMove == 1) { // Sending messages from type 3 and receiving type 4 messages from the player 1
                        serverMessage3 = "3" + game.printState() + "P0";
                        client1.dataOut.writeBytes(serverMessage3 + "\r\n");
                        System.out.println("\nSent to Player 1" + "\nMessage: " + serverMessage3);

                        if ((playerMessage4 = client1.dataIn.readLine().trim()) != null) {
                            System.out.println("\n>>Player 1 move\nReceived from Player 1" + "\nMessage: " + playerMessage4);
                            if (playerMessage4.charAt(1) == 'q') { // in case the player wants to quit
                                // replacing the last character in the message with '2'
                                serverMessage3 = serverMessage3.replace('0', '2');

                                // Sending the modified message to player 2 to notify him about quiting the game
                                client2.dataOut.writeBytes(serverMessage3);
                                System.out.println("\nSent to Player 2" + "\nMessage: " + serverMessage3);
                                break;
                            }

                            // In case, player 1 makes an invalid move
                            if (!(game.submitMove(Integer.parseInt(playerMessage4.charAt(1) + "")))) {
                                // replacing the last character in the message with '1'
                                serverMessage3 = serverMessage3.replace('0', '1');
                                client1.dataOut.writeBytes(serverMessage3);
                                System.out.println("\nSent to Player 1"+ "\nMessage: " + serverMessage3);
                            }
                        }
                    } else { // Sending messages from type 3 and receiving type 4 messages from the player 2
                        serverMessage3 = "3" + game.printState() + "P0";
                        client2.dataOut.writeBytes(serverMessage3 + "\r\n");
                        System.out.println("\nSent to Player 2" + "\nMessage: " + serverMessage3);

                        if ((playerMessage4 = client2.dataIn.readLine().trim()) != null) {
                            System.out.println("\n>>Player 2 move\nReceived from Player 2" + "\nMessage: " + playerMessage4);
                            if (playerMessage4.equals("4q")) {
                                serverMessage3 = serverMessage3.replace('0', '2');

                                // Sending the modified message to player 1 to notify him about quiting the game
                                client1.dataOut.writeBytes(serverMessage3);
                                System.out.println("Sent to Player 1" + "\nMessage: " + serverMessage3);
                                break;
                            }

                            // In case, player 2 makes an invalid move
                            if (!(game.submitMove(Integer.parseInt(playerMessage4.charAt(1) + "")))) {
                                serverMessage3 = serverMessage3.replace('0', '1');
                                client2.dataOut.writeBytes(serverMessage3);
                                System.out.println("\nSent to Player 2" + "\nMessage: " + serverMessage3);
                            }
                        }
                    }
                }

                result = game.checkWin();

                if (!result.equals("0")) { // Making sure that the loop wasn't broken by quiting it by any player.
                    if (result.equalsIgnoreCase("D"))
                        System.out.println("\n>>Sent draw");
                    serverMessage3 = "3" + game.printState() + result + "0";
                    client1.dataOut.writeBytes(serverMessage3 + "\r\n");
                    System.out.println("\nSent to Player 1" + "\nMessage: " + serverMessage3);

                    client2.dataOut.writeBytes(serverMessage3 + "\r\n");
                    System.out.println("\nSent to Player 2" + "\nMessage: " + serverMessage3);
                }

                System.out.println("\n>>Exit play game");

                // closing the opened sockets.
                client1.socket.close();
                client2.socket.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Server s = new Server();
        s.acceptConnections();
    }
}
