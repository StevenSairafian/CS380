package project3_5;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

import javax.net.SocketFactory;


public class Ipv6Client {
    
    byte version = 0x6;
    byte trafficClass = 0;//do not implement
    byte nextHeader = 0x11;
    short payloadLength = 0;
    short hopLimit = 0x14;
    short[] sourceAddress = new short[8];
    short[] destAddress =new short[8];
    int flowLabel = 0;
    
    public static void main(String[] args) {
   	Ipv6Client ipv = new Ipv6Client();
   	ipv.commence();
       }
    
    public void commence() {
	try (Socket socket = SocketFactory.getDefault().createSocket(
		"76.91.123.97", 22222)) {

	    if (socket.isConnected()) {
		System.out.println("Connection to server made");
	    }

	    OutputStream outputStream = socket.getOutputStream();
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    socket.getInputStream()));
	    

	    // Send 10 packets with x amount of random data
	    sourceAddress = new short[] {0,0,0,0,0,-1,0x7F00,1};
	    destAddress = new short[] {0,0,0,0,0,-1, 0x4C5B,0X7B61};
	 //   initialize(sourceAddress,destAddress); 

	    for (int i = 0; i < 10; ++i) {
		byte[] toSend = thisBytes(buildPackage(createData()));
		//printPacket(toSend);
		outputStream.write(toSend);
		System.out.println("Server> " + br.readLine());
	    }

	} catch (Exception e) {
	    // Connection failed, abort without grace
	    e.printStackTrace();
	}

    }
    
    public void initialize(short[] send, short[] receive){
	for(int i = 0; i < sourceAddress.length; ++i){
	    sourceAddress[i] |= send[i];
	    destAddress[i] |= receive[i];
	}
	
    }
    
    public short[] buildPackage(short[] data) throws Exception{
	payloadLength = (short)(data.length);
	// Break things if length is invalid
	if (payloadLength > 32767 || payloadLength < 0) {
	     Exception e = new Exception();
	     e.printStackTrace();
	}
	
	short[] payload = new short[data.length + 20];
	
	payload[0] |= version;
	payload[0] <<= 12;
	short tempShort = trafficClass;
	tempShort <<= 4;
	payload[0] |= tempShort;
	int tempInt = flowLabel;
	tempInt >>>= 16;
	payload[0] |= tempInt;
	payload[1] |= flowLabel;
	payload[2] |= ((payloadLength) <<1); //to make it length in bytes
	payload[3] |= nextHeader;
	payload[3] <<= 8;
	payload[3] |= hopLimit;
	for(int i = 4; i < 12; ++i){
	    payload[i] |= sourceAddress[i -4];
	}
	for(int i = 12; i < 20; ++i){
	    payload[i] |= destAddress[i-12];
	}
	for(int i = 0; i < data.length; ++i){
	    payload[i + 20] = data[i];
	}
	
	return payload;
	
    }
    
    private short[] createData() {
	Random r = new Random();
	short[] data = new short[r.nextInt(32748)];
	

	for (int i = 0; i < data.length; ++i) {
	    data[i] = (short) (r.nextInt(Short.MAX_VALUE) & 0xFFFF);
	}

	return data;
    }
    
    private byte[] thisBytes(short[] data) {
	int j = 0;
	byte[] result = new byte[(data.length << 1)];

	for (int i = 0; i < data.length; ++i) {
	    result[j + 1] |= (data[i] & 0xFF);
	    data[i] >>>= 8;
	    result[j] |= (data[i] & 0xFF);
	    j += 2;
	}
	return result;
    }
    
    public void printPacket(byte[] sendIt){
	for(int i = 0; i < sendIt.length; ++i){
	    String temp = Integer.toBinaryString(sendIt[i]);

	    if(i%8 ==0){
		System.out.println();
	    }
	    while(temp.length()<8){
		temp += ("0" + temp);
	    }
	    System.out.print(temp.substring(temp.length() - 8) + " ");

	}
    }
    
}
