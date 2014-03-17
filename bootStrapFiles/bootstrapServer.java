/*
 * bootStrapServer.java
 *
 * Version: 1.0
 *
 * Revisions:
 *     Monday, March 17, 2014
 */

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

/**
 * Bootstrap server of CAN network
 * 
 * @author Harsh Hiro Sadhvani
 * 
 */
public class bootstrapServer extends Thread {
	static Socket connection;
	static Socket myConnection;
	static ServerSocket providerSocket;
	static ObjectOutputStream out;
	static ObjectInputStream in;
	static boolean empty = true;
	static int joinedpeer = 0;
	static InetAddress first = null;
	static String peerOne;
	static int firstPort = 12355;
	static int conport = 12347;

	bootstrapServer(Socket c) throws IOException {
		// setting the accepted connection
		myConnection = c;
	}

	/*
	 * main function
	 */
	public static void main(String args[]) throws IOException,
			ClassNotFoundException {
		// setting port number
		if (args.length != 0)
			conport = Integer.parseInt(args[0]);
		dowork();

	}

	/*
	 * function to set up connection
	 */
	public static void dowork() throws IOException {
		providerSocket = new ServerSocket(conport);
		System.out.println("Bootstrap server is waiting..");
		// allowing multiple connections with the help of threads
		while (true) {

			connection = providerSocket.accept();
			new bootstrapServer(connection).start();

		}
	}

	/*
	 * run function for each connection
	 */
	public void run() {
		try {
			// getting user input
			out = new ObjectOutputStream(myConnection.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			out.writeObject("connected to bootstrap server");
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			// receiving input
			in = new ObjectInputStream(myConnection.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		String message = "";
		try {
			// Receiving message from client
			message = (String) in.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		if (!message.startsWith("leave")) {
			// message = "";

			if (message.startsWith("join")) {
				// setting first peer information
				if (empty) {
					++joinedpeer;
					peerOne = myConnection.getInetAddress().toString();
					try {
						// letting first peer know it has joined
						out.writeObject("first%" + joinedpeer + "%" + peerOne);

						first = myConnection.getInetAddress();

					} catch (IOException e) {
						e.printStackTrace();
					}
					empty = false;
				} 
				// for other peers
				else {
					++joinedpeer;
					try {
						// letting other peers knwo it has joined
						out.writeObject("nofirst%" + joinedpeer + "%"
								+ first.toString() + "%"
								+ myConnection.getInetAddress().toString()
								+ "%" + firstPort);
						// out.writeObject(first);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				System.out.println(joinedpeer+" peer joined.");
			}
		}
	}

}