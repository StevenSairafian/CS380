package homeWork1;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Logger;
import javax.net.SocketFactory;

public final class Client {

    public static void main(String[] args) throws Exception {
        try (Socket socket = SocketFactory.getDefault().createSocket("login.unx.csupomona.edu", 23)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println(br.readLine());
        }
    }
}
