package project1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Logger;

import javax.net.ServerSocketFactory;

public final class EchoServer {

    public static void main(String[] args) throws Exception {
	ServerSocket serverSocket = ServerSocketFactory.getDefault()
		.createServerSocket(22222);
	while (true) {
	    try (Socket socket = serverSocket.accept()) {
		System.out.println("Client connected: "
			+ socket.getInetAddress());
		
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		while (socket.isConnected()) {
		    try{
			
			String returnThis = br.readLine();
			PrintStream out = new PrintStream(socket.getOutputStream());
			out.println(returnThis);
		    }catch(SocketException e){
			System.out.println("The connection was interrupted");
			break;
		    }
		}
	    }
	}
    }
}