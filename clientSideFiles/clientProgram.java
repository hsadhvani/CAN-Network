/*
 * clientProgram.java
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
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Client Program to take Input from client
 * 
 * @author Harsh Hiro Sadhvani
 * 
 */
public class clientProgram {
	Socket connection = null;
	static Socket requestSocket;
	static ObjectOutputStream out;
	static ObjectInputStream in;
	static ObjectOutputStream out1;
	static ObjectInputStream in1;
	static Peer myPeer;
	static Scanner sc = new Scanner(System.in);
	static ServerSocket providerSocket;
	static boolean joined = false;
	static String input = "localhost";
	int task = 0;
	private static ObjectInput input2;
	private static ObjectOutput output;
	static int conport = 12346;

	/*
	 * main function
	 */
	public static void main(String args[]) throws UnknownHostException,
			IOException, ClassNotFoundException {

		// setting port and bootstrap address
		if (args.length != 0) {
			input = args[0];
			conport = Integer.parseInt(args[1]);
		}
		String message = "";
		// setting up connection with bootstrap
		requestSocket = new Socket(input, conport);
		System.out.println("Connected to bootstrap server " + input
				+ " on port " + conport);

		out = new ObjectOutputStream(requestSocket.getOutputStream());
		in = new ObjectInputStream(requestSocket.getInputStream());
		System.out.println((String) in.readObject());
		// if user joins
		while (!message.startsWith("join")) {

			message = sc.nextLine();
			if (!message.startsWith("join"))
				System.out
						.println("You need to join the network before you do anything.");
		}

		sendMessage(message);
		String check = (String) in.readObject();

		String[] checkArr = check.split("%");
		// for first peer information is set
		if (checkArr[0].equals("first")) {
			myPeer = new Peer(checkArr[1], checkArr[2], 0, 0, 10, 10);
			myPeer.port = 12355;
			// writing to serializable object
			writePeer();

			showView();
			myPeer = readPeer();
			joined = true;
		} else {
			// coordinates for other peer
			double x, y;
			x = (double) (9 * Math.random());
			y = (double) (9 * Math.random());
			// socket connection to first peer
			Socket requestSocket1 = new Socket(checkArr[2].substring(1),
					Integer.parseInt(checkArr[4]));
			ObjectOutputStream out2 = new ObjectOutputStream(
					requestSocket1.getOutputStream());
			ObjectInputStream in2 = new ObjectInputStream(
					requestSocket1.getInputStream());
			// information first peer that you want to join
			out2.writeObject("join%" + checkArr[1] + "%" + checkArr[3] + "%"
					+ x + "%" + y + "%" + String.valueOf(getAvailablePort()));
			// storing updated information
			Peer me = (Peer) in2.readObject();

		//	System.out.println(me.toString());

			myPeer = me;
			writePeer();
			showView();
		}

		// running code to allow other actions
		new clientProgram().dowork();
	}

	public void dowork() throws ClassNotFoundException, IOException {

		String message = "";
		while (true) {
			System.out.println("What would you like to do?");
			// taking message from user
			message = sc.nextLine();
			// System.out.println(message);

			// for insert
			if (message.startsWith("insert")) {
				String m[] = message.split(" ");
				// calculation X and Y using hash function
				int X = charAtOdd(m[1]) % 10;
				int Y = charAtEven(m[1]) % 10;
				System.out.println("To insert at " + X + "," + Y);

				try {
					myPeer = readPeer();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				myPeer = readPeer();
				// inserted on that peer if X and Y are in that peer

				System.out.println();
				if (X >= myPeer.startX && Y >= myPeer.startY && X < myPeer.x
						&& Y < myPeer.y) {
					myPeer.files.add(m[1]);
					try {
						System.out.println("Inserted on Peer " + myPeer.name);
						writePeer();
						//showView();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {

					boolean inNeighbor = false;
					int IDn = 0;
					// checking in which neighbor it can be found
					for (int i = 0; i < myPeer.neighbor.size(); i++) {
						if (X >= myPeer.neighbor.get(i).startX
								&& Y >= myPeer.neighbor.get(i).startY
								&& X < myPeer.neighbor.get(i).x
								&& Y < myPeer.neighbor.get(i).y) {
							inNeighbor = true;
							IDn = i;
							break;
						}
					}
					if (inNeighbor) {

						Socket insertSocket = new Socket(
								myPeer.neighbor.get(IDn).ip.substring(1),
								myPeer.neighbor.get(IDn).port);
						ObjectOutputStream outInsert = new ObjectOutputStream(
								insertSocket.getOutputStream());
						ObjectInputStream inInsert = new ObjectInputStream(
								insertSocket.getInputStream());
						// sending insert message to closest peer
						outInsert.writeObject("insert%" + m[1] + "%Peer "
								+ myPeer.name + "-> ");
						String receiveInsert = "";
						receiveInsert = (String) inInsert.readObject();
						// displaying path

						System.out.println("Inserted at Peer "
								+ myPeer.neighbor.get(IDn).name);
						System.out.println("And path is : "
								+ receiveInsert);

					} else {

						// finding close neighbor where it can insert
						int closeIndex = greedyIndexNeighbor(myPeer, X, Y);
						try {
							// connecting to closest neighbor
							Socket insertSocket = new Socket(
									myPeer.neighbor.get(closeIndex).ip
											.substring(1),
									myPeer.neighbor.get(closeIndex).port);
							ObjectOutputStream outInsert = new ObjectOutputStream(
									insertSocket.getOutputStream());
							ObjectInputStream inInsert = new ObjectInputStream(
									insertSocket.getInputStream());
							// sending insert message to closest peer
							outInsert.writeObject("insert%" + m[1] + "%Peer "
									+ myPeer.name + "-> ");
							String receiveInsert = "";
							receiveInsert = (String) inInsert.readObject();
							// displaying path
							System.out.println("Inserted at "
									+ receiveInsert.substring(receiveInsert
											.lastIndexOf("Peer"))
									+ " and path is:");
							System.out.println("Peer " + myPeer.name + " -> "
									+ receiveInsert);

						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			} else if (message.startsWith("search")) {
				String m[] = message.split(" ");

				try {
					myPeer = readPeer();
				} catch (ClassNotFoundException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// calculating X and Y cords using Hash function
				int X = charAtOdd(m[1]) % 10;
				int Y = charAtEven(m[1]) % 10;
				System.out.println("search at " + X + " , " + Y);
				// first check if X and Y are in that peer
				if (X >= myPeer.startX && Y >= myPeer.startY && X < myPeer.x
						&& Y < myPeer.y) {
					// check if file is found
					if (myPeer.files.contains(m[1]))
						System.out.println("File found on Peer " + myPeer.name);
					else
						System.out.println("Cannot find file");
				}
				// now checking in it's neighbors and then neighbors' neighbors
				else {
					try {
						myPeer = readPeer();
					} catch (ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					boolean inNeighbor = false;
					int IDn = 0;
					// checking in which neighbor it can be found
					for (int i = 0; i < myPeer.neighbor.size(); i++) {
						if (X >= myPeer.neighbor.get(i).startX
								&& Y >= myPeer.neighbor.get(i).startY
								&& X < myPeer.neighbor.get(i).x
								&& Y < myPeer.neighbor.get(i).y) {
							inNeighbor = true;
							IDn = i;
							break;
						}
					}
					// if X and Y are in the neighbor
					if (inNeighbor) {

						Socket searchSocket;
						try {
							// contacting the neighbor where it could find the
							// file
							searchSocket = new Socket(
									myPeer.neighbor.get(IDn).ip.substring(1),
									myPeer.neighbor.get(IDn).port);
							ObjectOutputStream outSearch;

							outSearch = new ObjectOutputStream(
									searchSocket.getOutputStream());

							ObjectInputStream inSearch = new ObjectInputStream(
									searchSocket.getInputStream());
							// /informing the neighbors what it wants to search
							outSearch.writeObject("search%" + m[1] + "%");
							String si = "";
							try {
								si = (String) inSearch.readObject();
							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							// System.out.println(si);

							// checking if the file was found and the path
							if (si.contains("yes")) {
								si = si.substring(si.lastIndexOf('%') + 1);
								si = si.replace("yes", " ");
								System.out.println("Found on Peer "+myPeer.neighbor.get(IDn).name);
								System.out.println("Path is :  Peer "+myPeer.name+ " -> "+ si);
							} else
								System.out.println("Cannot find file");
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					// if not in neighbors then check neighbors' neighbors
					else {
						// find the closest neighbor where it can find
						double neighborDist[] = new double[myPeer.neighbor
								.size()];

						for (int i = 0; i < myPeer.neighbor.size(); i++) {
							neighborDist[i] = linedist(
									X,
									Y,
									((myPeer.neighbor.get(i).x + myPeer.neighbor
											.get(i).startX) / 2),
									((myPeer.neighbor.get(i).y + myPeer.neighbor
											.get(i).startY) / 2));
						}
						int minID = 0;
						for (int i = 0; i < myPeer.neighbor.size(); i++) {
							if (neighborDist[i] < neighborDist[minID])
								minID = i;
						}
						// look for minID neighbor

						// System.out.println("Will look at "
						// + myPeer.neighbor.get(minID).name);

						// set up connection with nearest neighbor
						Socket searchSocket;
						try {
							searchSocket = new Socket(
									myPeer.neighbor.get(minID).ip.substring(1),
									myPeer.neighbor.get(minID).port);

							ObjectOutputStream outSearch = new ObjectOutputStream(
									searchSocket.getOutputStream());
							ObjectInputStream inSearch = new ObjectInputStream(
									searchSocket.getInputStream());

							// informing neighbor what it wants to search
							outSearch.writeObject("search%" + m[1] + "%");
							String si = (String) inSearch.readObject();
							// System.out.println(si);

							// if file is found then shows the path or says file
							// was not found

							if (si.contains("yes")) {
								si = si.substring(si.lastIndexOf('%') + 1);
								si = si.replace("yes", " ");
								System.out.println("Found on "+si.substring(si.lastIndexOf("Peer")));
								
								System.out.println("Path is : Peer "
										+ myPeer.name + " -> " + si);
							} else
								System.out.println("Cannot find file");

						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			}

			else if (message.equals("view")) {
				try {
					// displaying the data of the peer
					myPeer = readPeer();
					showView();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (message.startsWith("leave")) {
				System.out.println("Sorry you cannot leave this CAN Network.");
			} else {

				System.out.println("Invalid Entry");
			}

			try {
				writePeer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/*
	 * hash function for chat at odd
	 */
	public static int charAtOdd(String s) {
		int sum = 0;

		for (int i = 0; i < s.length(); i++) {
			if (i % 2 != 0)
				sum += (int) s.charAt(i);
		}
		return sum;

	}

	/*
	 * Display peer information
	 */
	public static void showView() throws ClassNotFoundException, IOException {
		myPeer = readPeer();
		System.out.println("Peer identifier : Peer " + myPeer.name);
		System.out.println("Peer IP : " + myPeer.ip);
		System.out.println("Peer Port : " + myPeer.port);

		System.out.println("Coordinates : " + myPeer.startX + ","
				+ myPeer.startY + " to " + myPeer.x + "," + myPeer.y);
		if (myPeer.neighbor.isEmpty())
			System.out.println("No neighbors");
		else {
			System.out.println("Neighbors : ");
			for (int i = 0; i < myPeer.neighbor.size(); i++) {
				System.out.println(" Peer " + myPeer.neighbor.get(i).name);

				System.out.println("Coordinates : "
						+ myPeer.neighbor.get(i).startX + ","
						+ myPeer.neighbor.get(i).startY + " to "
						+ myPeer.neighbor.get(i).x + ","
						+ myPeer.neighbor.get(i).y);
			}
			System.out.println();
		}
		if (myPeer.files.isEmpty())
			System.out.println("No files in the peer");
		else {
			System.out.println("Files :");
			for (int i = 0; i < myPeer.files.size(); i++) {
				System.out.print(myPeer.files.get(i) + " ");
			}
		}
		System.out.println();

	}

	/*
	 * hash function for chat at even
	 */
	public static int charAtEven(String s) {
		int sum = 0;

		for (int i = 0; i < s.length(); i++) {
			if (i % 2 == 0)
				sum += (int) s.charAt(i);
		}
		return sum;

	}

	/*
	 * function to send message to bootstrap
	 */
	static void sendMessage(String msg) {
		try {
			out.writeObject(msg);
			out.flush();
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	/*
	 * function to calculate distance of a line
	 */
	static double linedist(double x1, double y1, double x2, double y2) {
		double ans = Math.abs(Math.sqrt(Math.pow((x2 - x1), 2)
				+ Math.pow((y2 - y1), 2)));
		return ans;
	}

	/*
	 * function to check if the cords are of a square
	 */
	boolean checkSquare(double startx, double starty, double X, double Y) {
		if (linedist(startx, starty, X, starty) == linedist(startx, starty,
				startx, Y))
			return true;
		return false;
	}

	/*
	 * function to write peer info
	 */
	public static void writePeer() throws IOException {

		FileOutputStream fout = new FileOutputStream("peerInfo.ser");
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(myPeer);
		oos.close();

	}

	/*
	 * function to peer info
	 */
	public static Peer readPeer() throws ClassNotFoundException, IOException {

		InputStream file = new FileInputStream("peerInfo.ser");
		InputStream buffer = new BufferedInputStream(file);
		ObjectInputStream input1 = new ObjectInputStream(buffer);
		Peer ret = (Peer) input1.readObject();
		return ret;

	}

	/*
	 * function to find a random available port
	 */
	private static int getAvailablePort() throws IOException {
		int port = 0;
		do {
			port = 1000 + (int) (Math.random() * 4000);
			;
		} while (!isPortAvailable(port));

		return port;
	}

	/*
	 * function to check if port is available
	 */
	private static boolean isPortAvailable(int port) throws IOException {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
			return true;
		} catch (final IOException e) {
		} finally {
			if (ss != null) {
				ss.close();
			}
		}

		return false;
	}

	/*
	 * finding nearest neighbor for given cords
	 */
	static int greedyIndexNeighbor(Peer testPeer, int X, int Y)
			throws ClassNotFoundException, IOException {
		myPeer = readPeer();
		double minDist = linedist(
				X,
				Y,
				((myPeer.neighbor.get(0).x + myPeer.neighbor.get(0).startX) / 2),
				(myPeer.neighbor.get(0).x + myPeer.neighbor.get(0).startY) / 2);
		int minID = 0;
		for (int i = 0; i < myPeer.neighbor.size(); i++) {
			if (linedist(
					X,
					Y,
					((myPeer.neighbor.get(i).x + myPeer.neighbor.get(i).startX) / 2),
					(myPeer.neighbor.get(i).x + myPeer.neighbor.get(i).startY) / 2) < minDist) {
				minID = i;
			}
		}
		return minID;
	}
}
