package project3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import javax.net.SocketFactory;

public class IPV4Client {

    private static byte version = 0b00000100;
    private byte tOS = 0b00000000; // do not implement as per instructions
    private byte timeToLive = 50;
    private byte protocol = 0b00000110;
    private byte internetHeaderLength = 5;
    private int totalLength = 0;
    private int identification = 0;
    // Flags and fragment offset combined.Flags = 010, No fragmentation
    private int flagsFragsOffset = 0b0100000000000000;
    private int headerChecksum = 0;
    private int shortBitMask = 0x0000FFFF;
    // 127.0.0.1
    private long sourceIPAddress = 0b01111111000000000000000000000001;
    // 76.91.123.97
    private long destinationIPAddress = 0b01001100010110110111101101100001;

    public static void main(String[] args) {
	IPV4Client ipv = new IPV4Client();
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
	    for (int i = 0; i < 10; ++i) {
		byte[] toSend = thisBytes(createPacket(createData(53)));
		outputStream.write(toSend);
		System.out.println("Server> " + br.readLine());
	    }

	} catch (Exception e) {
	    // Connection failed, abort without grace
	    e.printStackTrace();
	}

    }

    private short[] createPacket(short[] data) {
	// Calculate total length in shorts
	totalLength = data.length + (internetHeaderLength * 2);
	// Break things if length is invalid
	if (totalLength > 32767 || totalLength < 0) {
	    return null;
	}

	short temp = 0;
	long tempLong = 0;
	short[] checkSum = new short[totalLength];

	// Store version, then shift left by 12 to push the 4 version bits to
	// the front
	checkSum[0] = version;
	checkSum[0] <<= 12;
	// Get the internetHeaderLength bits into the correct spot
	temp = internetHeaderLength;
	temp <<= 8;

	checkSum[0] |= temp;
	checkSum[0] |= tOS;
	// Multiply total length by 2 to get length in bytes
	totalLength *= 2;
	checkSum[1] |= totalLength;

	checkSum[2] |= identification;
	checkSum[3] |= flagsFragsOffset;

	checkSum[4] |= (timeToLive & 0xFF);
	checkSum[4] <<= 8;
	checkSum[4] |= (protocol & 0xFF);

	checkSum[5] |= headerChecksum;
	// Note 7 is given a value first. This spares the use of a temporary
	// variable
	checkSum[7] |= sourceIPAddress;
	checkSum[6] |= (sourceIPAddress >>> 16);
	// Note 9 is given a value first
	checkSum[9] |= destinationIPAddress;
	checkSum[8] |= (destinationIPAddress >>> 16);
	// Store the passed data in slots 10-x of the array. Starts at 10 to
	// avoid overwriting the header
	for (int i = 0; i < data.length; ++i) {
	    checkSum[i + 10] = data[i];
	}

	long checksum = generateChecksum(checkSum);
	checkSum[5] |= checksum;

	return checkSum;

    }

    private short[] createData(int length) {
	short[] data = new short[length];
	Random r = new Random();

	for (int i = 0; i < data.length; ++i) {
	    data[i] = (short) (r.nextInt(Short.MAX_VALUE) & 0xFFFF);
	}

	return data;
    }

    private long generateChecksum(short[] data) {
	long sum = 0;

	for (int i = 0; i < 10; ++i) {
	    sum += ((data[i]) & shortBitMask);

	    if ((sum & 0xffff0000) > 0) {
		// carry occurred
		sum &= shortBitMask;
		sum++;
	    }

	}
	return ~(sum & shortBitMask);
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

}
