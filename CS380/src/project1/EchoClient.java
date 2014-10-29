package project1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.net.SocketFactory;

public final class EchoClient {

    public static void main(String[] args) throws Exception {
	try (Socket socket = SocketFactory.getDefault().createSocket(
		"localhost", 22222)) {
	    Scanner sc = new Scanner(System.in);
	    PrintStream out = new PrintStream(socket.getOutputStream());
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    socket.getInputStream()));
	    while (true) {
		System.out.print("Client> ");
		String sendMessage = sc.nextLine();

		out.println(sendMessage);

		System.out.println("Server> " + br.readLine());
	    }
	}
    }
}
