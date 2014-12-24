package project4;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

import javax.net.SocketFactory;

public class UdpClient {

    private short sourcePort = 0b0101010101010101;
    private short length;
    private short checksum;
    private short[] header;
    private int shortBitMask = 0x0000FFFF;
    private long sourceIPAddress;
    private long destinationIPAddress;

    public static void main(String[] args) {
	Driver d = new Driver();
	d.initialize();
    }

    public UdpClient(long sourceIP, long destIP) {
	sourceIPAddress = sourceIP;
	destinationIPAddress = destIP;
    }

    private long generateChecksum(short[] data, long sIp, long dIp) {
	long sum = 0;
	short[] fullSum = new short[(data.length + 6)];
	for (int i = 0; i < data.length; ++i) {
	    fullSum[i] = data[i];
	}

	fullSum[data.length + 1] |= sIp;
	fullSum[data.length] |= (sIp >>> 16);
	fullSum[data.length + 3] |= dIp;
	fullSum[data.length + 2] |= (dIp >>> 16);
	// length calc twice
	fullSum[data.length + 4] |= data[2];
	fullSum[data.length + 5] |= 17;// It will never not be udp

	for (int i = 0; i < fullSum.length; ++i) {
	    sum += ((fullSum[i]) & shortBitMask);

	    if ((sum & 0xffff0000) > 0) {
		// carry occurred
		sum &= shortBitMask;
		sum++;
	    }

	}
	return ~(sum & shortBitMask);
    }

    public short[] generatePacket(short[] input, short destinationPort) {
	length = (short) (4 + input.length);
	short[] packet = new short[length];
	packet[0] |= sourcePort;
	packet[1] |= destinationPort;
	packet[2] |= (2 * length);
	packet[3] = 0;
	for (int i = 0; i < input.length; ++i) {
	    packet[i + 4] = input[i];
	}
	packet[3] = (short) generateChecksum(packet, sourceIPAddress,
		destinationIPAddress);
	return packet;
    }

    public byte[] thisBytes(short[] data) {
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

class IPV4Client {

    private byte version = 0b00000100;
    private byte tOS = 0b00000000; // do not implement as per instructions
    private byte timeToLive = 50;
    private byte protocol = 0x11;
    private byte internetHeaderLength = 5;
    private int totalLength = 0;
    private int identification = 0;
    // Flags and fragment offset combined.Flags = 010, No fragmentation
    private int flagsFragsOffset = 0b0100000000000000;
    private int headerChecksum = 0;
    private int shortBitMask = 0x0000FFFF;
    // 127.0.0.1
    public long sourceIPAddress = 0b01111111000000000000000000000001;
    // 76.91.123.97
    public long destinationIPAddress = 0b01001100010110110111101101100001;

    public short[] createPacket(short[] data) {
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

    public byte[] thisBytes(short[] data) {
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

class Driver {

    public void initialize() {
	IPV4Client ipv = new IPV4Client();
	UdpClient udp = new UdpClient(ipv.sourceIPAddress,
		ipv.destinationIPAddress);
	Random r = new Random();
	short[] temp = { (short) 0xDEAD, (short) 0xBEEF };

	byte[] sendThis = ipv.thisBytes(ipv.createPacket(temp));

	try {

	    Socket socket = SocketFactory.getDefault().createSocket(
		    "76.91.123.97", 22222);

	    if (socket.isConnected()) {
		System.out.println("Connection to server made");
	    }

	    OutputStream outputStream = socket.getOutputStream();
	    InputStream br = socket.getInputStream();
	    outputStream.write(sendThis);
		int firstHalf = br.read();
		firstHalf &= 0xFF;
		int secondHalf = br.read();
		secondHalf &= 0xFF;
		int newPort = firstHalf;
		System.out.print("Unprocessed port is: " + Integer.toHexString(firstHalf) + " ");
		System.out.println(Integer.toHexString(secondHalf));
		
		newPort <<= 8;
		newPort |= secondHalf;
		newPort &= 0xFFFF;
		System.out.println("Port as processed is: " + Integer.toHexString(newPort));
		long roundTrip = 0;
	    for (int i = 0; i < 10; ++i) {

		short[] s = new short[(int) Math.pow(2, i)];
		for (int j = 0; j < s.length; ++j) {
		    s[j] = (short) r.nextInt();
		}
		short[] pureUdp = udp.generatePacket(s, (short) newPort);
		byte[] toSend = ipv.thisBytes(ipv.createPacket(pureUdp));
		//System.out.print("The port in the package is: "+ Integer.toHexString(toSend[22]));

		//System.out.println(Integer.toHexString(toSend[23]) );
		Long startTime = System.currentTimeMillis();
		outputStream.write(toSend);
		//Read 4 byte response& print it
		System.out.print(Integer.toHexString(br.read()));
		Long endTime = System.currentTimeMillis();
		roundTrip += (endTime - startTime);
		System.out.print(Integer.toHexString(br.read()));
		System.out.print(Integer.toHexString(br.read()));
		System.out.println(Integer.toHexString(br.read()));
		System.out.println("Took: " + (endTime-startTime) + "ms");
		while(socket.isConnected()){
		    System.out.println(br.read());
		}
	    }
	    System.out.println("Average time = "  + (roundTrip/10) + "ms");

	} catch (Exception e) {
	    // Connection failed, abort without grace
	    e.printStackTrace();
	}

    }

    public void printPacket(byte[] sendIt) {
	for (int i = 0; i < sendIt.length; ++i) {
	    String temp = Integer.toBinaryString(sendIt[i]);

	    if (i % 4 == 0) {
		System.out.println();
	    }
	    while (temp.length() < 8) {
		temp += ("0" + temp);
	    }
	    System.out.print(temp.substring(temp.length() - 8) + " ");

	}
    }
}
