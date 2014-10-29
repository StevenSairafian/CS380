package homeWork1;


import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;
import javax.net.ServerSocketFactory;

public final class Server {

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(22222);
        while (true) {
            try (Socket socket = serverSocket.accept()) {
                System.out.println("Client connected: " + socket.getInetAddress());
                PrintStream out = new PrintStream(socket.getOutputStream());
                out.println("Hi client, thanks for connecting!");
            }
        }
    }
}
