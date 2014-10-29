package project2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.net.SocketFactory;

public final class ChatClient {

    public static void main(String[] args) throws Exception {

	try (Socket socket = new Socket("76.91.123.97", 22222)) {
	    if (socket.isConnected()) {
		System.out.println("Connection to server made");
	    }
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    socket.getInputStream()));
	    Scanner sc = new Scanner(System.in);
	    PrintStream out;

	    out = new PrintStream(socket.getOutputStream());
	    out.println("9giu5z5w8");

	    Runnable receiver = () -> {
		String received;
		while (true) {
		    try {
			received = br.readLine();
			if (received != null)
			    System.out.println("\nServer> " + received);
		    } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	    };

	    Thread receiveThread = new Thread(receiver);
	    receiveThread.start();
	    while (true) {
		System.out.print("Client> ");
		if (sc.hasNextLine()) {
		    String sendMessage = sc.nextLine();

		    out.println(sendMessage);
		    out.flush();
		}
	    }

	}

    }
}
