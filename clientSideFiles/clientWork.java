/*
 * clientProgram.java
 *
 * Version: 1.0
 *
 * Revisions:
 *     Monday, March 17, 2014
 */

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Client Program to accept connections
 * 
 * @author Harsh Hiro Sadhvani
 */
public class clientWork extends Thread {

	/**
	 * 
	 * Line for storing line information
	 * 
	 */
	class Line {
		double x1, y1, x2, y2;

		Line(double x1, double y1, double x2, double y2) {

			this.x1 = x1;
			this.x2 = x2;
			this.y2 = y2;
			this.y1 = y1;

		}

	}

	static ServerSocket providerSocket;
	static Socket connectionToMe;
	static Socket connection;
	static Peer myPeer;
	private static ObjectInput input;

	clientWork(Socket c) throws IOException, ClassNotFoundException {
		// setting connection for incoming messages
		connectionToMe = c;
	}

	/*
	 * main function
	 */
	public static void main(String args[]) throws IOException,
			ClassNotFoundException {
		System.out.println("Running");
		dowork();
	}

	/*
	 * function to set up connection
	 */

	public static void dowork() throws IOException, ClassNotFoundException {

		myPeer = readPeer();

		providerSocket = new ServerSocket(myPeer.port);

		while (true) {
			//System.out.println("I shall wait now");
			connection = providerSocket.accept();
			// thread for each connection
			new clientWork(connection).start();
		}
	}

	/*
	 * run function for thread
	 */
	public void run() {

		// System.out.println("Someone connected");
		ObjectOutputStream out1;
		try {

			out1 = new ObjectOutputStream(connectionToMe.getOutputStream());

			ObjectInputStream in1 = new ObjectInputStream(
					connectionToMe.getInputStream());
			// input from other peers
			String checker = (String) in1.readObject();
			String checkers[] = checker.split("%");

			// if file is searched for on that peer
			if (checker.startsWith("search")) {
				String fileSearch = checkers[1];
				// System.out.println(fileSearch);
				myPeer = readPeer();
				// calculating cords using hash function
				int X = clientProgram.charAtOdd(fileSearch) % 10;
				int Y = clientProgram.charAtEven(fileSearch) % 10;
			//	System.out.println("search at " + X + " , " + Y);
				// checking if is on that peer
				if (X >= myPeer.startX && Y >= myPeer.startY && X < myPeer.x
						&& Y < myPeer.y) {

					// if found informing the connecting peer
					if (myPeer.files.contains(fileSearch))
						out1.writeObject(checker + "yes Peer " + myPeer.name);
					else
						out1.writeObject("no" + myPeer.name);

				}
				// will look at neighbors of their neighbors
				else {
					try {
						myPeer = readPeer();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					// check if found in the neighbor
					boolean inNeighbor = false;
					int IDn = 0;

					// if found in the neighbors
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
						// System.out.println("Will look at neighbor "
						// + myPeer.neighbor.get(IDn).name);
						
						// connecting to neighbor and seeing if file is found
						Socket searchSocket;
						try {
							searchSocket = new Socket(
									myPeer.neighbor.get(IDn).ip.substring(1),
									myPeer.neighbor.get(IDn).port);
							ObjectOutputStream outSearch;

							outSearch = new ObjectOutputStream(
									searchSocket.getOutputStream());

							ObjectInputStream inSearch = new ObjectInputStream(
									searchSocket.getInputStream());
							// information sent to the neighbor
							outSearch.writeObject(checker + " Peer "
									+ myPeer.name + " -> ");
							String si = "";
							try {
								// information received from neighbor
								si = (String) inSearch.readObject();
							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// System.out.println(si);
							if (si.contains("yes")) {
								out1.writeObject(si);
							} else
								out1.writeObject("no");
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					// checking the closest neighbor if not found in the
					// neighbor
					else {
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
						// finding minimum distance possible
						int minID = 0;
						for (int i = 0; i < myPeer.neighbor.size(); i++) {
							if (neighborDist[i] < neighborDist[minID])
								minID = i;
						}
						// contacting closest neighbor
						Socket searchSocket = new Socket(
								myPeer.neighbor.get(minID).ip.substring(1),
								myPeer.neighbor.get(minID).port);

						ObjectOutputStream outSearch = new ObjectOutputStream(
								searchSocket.getOutputStream());
						ObjectInputStream inSearch = new ObjectInputStream(
								searchSocket.getInputStream());

						// informing closest neighbor
						outSearch.writeObject(checker + " Peer " + myPeer.name
								+ " -> ");
						// information received from the neighbor
						String si = (String) inSearch.readObject();
						if (si.contains("yes")) {
							out1.writeObject(si);
						} else {
							out1.writeObject(si + "no");

						}

					}
				}
			}
			if (checker.startsWith("insert")) {
				// calculating cords to insert at using hash function
				int X = clientProgram.charAtOdd(checkers[1]) % 10;
				int Y = clientProgram.charAtEven(checkers[1]) % 10;
				myPeer = readPeer();
				// if in the peer then insert
				if (X >= myPeer.startX && Y >= myPeer.startY && X < myPeer.x
						&& Y < myPeer.y) {
					myPeer.files.add(checkers[1]);
					out1.writeObject(checkers[2] + " Peer " + myPeer.name + "");
					writePeer();
				//	clientProgram.showView();
				}

				else {

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
						myPeer = readPeer();

						System.out.println("I shall look into "
								+ myPeer.neighbor.get(IDn).name);
						System.out
								.println("Maybe that's where I need to insert");
						// contacting peer it might need to insert at
						Socket insertSocket = new Socket(
								myPeer.neighbor.get(IDn).ip.substring(1),
								myPeer.neighbor.get(IDn).port);
						ObjectOutputStream outInsert = new ObjectOutputStream(
								insertSocket.getOutputStream());
						ObjectInputStream inInsert = new ObjectInputStream(
								insertSocket.getInputStream());
						// sending information to that peer
						outInsert.writeObject("insert%" + checkers[1]
								+ "%Peer " + myPeer.name + " -> ");

						//System.out.println("Inserted");
						// information received
						String receiveInsert = "";
						receiveInsert = (String) inInsert.readObject();
						//System.out.println(receiveInsert);
						// informing peer where the file was inserted
						out1.writeObject(receiveInsert);
					} else {
						// finding closest peer to insert at
						myPeer = readPeer();
						int closeIndex = clientProgram.greedyIndexNeighbor(
								myPeer, X, Y);
					//	System.out.println("I shall look into "
						//		+ myPeer.neighbor.get(closeIndex).name);
						//System.out
							//	.println("Maybe that's where I need to insert");
						// contacting peer it might need to insert at
						Socket insertSocket = new Socket(
								myPeer.neighbor.get(closeIndex).ip.substring(1),
								myPeer.neighbor.get(closeIndex).port);
						ObjectOutputStream outInsert = new ObjectOutputStream(
								insertSocket.getOutputStream());
						ObjectInputStream inInsert = new ObjectInputStream(
								insertSocket.getInputStream());
						// sending information to that peer
						outInsert.writeObject("insert%" + checkers[1]
								+ "%Peer " + myPeer.name + " -> ");

					//	System.out.println("Inserted");
						// information received
						String receiveInsert = "";
						receiveInsert = (String) inInsert.readObject();
						//System.out.println(receiveInsert);
						// informing peer where the file was inserted
						out1.writeObject(receiveInsert);
					}
				}
			}
			// updating cords of the peer
			if (checker.startsWith("Updatec")) {

				myPeer = readPeer();
				for (int i = 0; i < myPeer.neighbor.size(); i++) {
					if (myPeer.neighbor.get(i).name.equals(checkers[1])) {
						myPeer.neighbor.get(i).startX = Double
								.parseDouble(checkers[2]);

						myPeer.neighbor.get(i).startY = Double
								.parseDouble(checkers[3]);

						myPeer.neighbor.get(i).x = Double
								.parseDouble(checkers[4]);

						myPeer.neighbor.get(i).y = Double
								.parseDouble(checkers[5]);
					}

				}
				writePeer();
			}
			// information about neighbor
			if (checker.startsWith("iamneighbor")) {
				// neighbor data is received
				myPeer = readPeer();
				out1.flush();
				out1.writeObject("ok");
				out1.flush();
				//System.out.println("Neighbor being informed ");
				Peer temp = (Peer) in1.readObject();
				//System.out.println("temp object read");
				// neighbor is added on both sides
				temp.neighbor.add(myPeer);
				myPeer.neighbor.add(temp);
				Peer info = temp.neighbor.get(temp.neighbor.size() - 1);
				//System.out.println("Neighbor added to " + info.name);
				// updated peer is sent
				out1.writeObject(temp);

				writePeer();
				//System.out.println("myPeer updated with neighbors");
				//clientProgram.showView();
			}
			// informing when neighbor has to be removed
			if (checker.startsWith("iamnotneighbor")) {
				// myPeer = readPeer();
				String neighborname = checkers[1];
				// neighbor remove position is calculated and removed
				//System.out.println(neighborname + "is not neighbor");
				int delIndex = 0;
				for (int i = 0; i < myPeer.neighbor.size(); i++) {
					if (myPeer.neighbor.get(i).name.equals(neighborname)) {
						delIndex = i;
						break;
					}
				}
				myPeer.neighbor.remove(delIndex);
				writePeer();
				out1.writeObject("ok");
			}

			// when some peer needs to join
			if (checker.startsWith("join")) {
				//System.out.println(checker);
				myPeer = readPeer();

				double x = Double.parseDouble(checkers[3]);
				double y = Double.parseDouble(checkers[4]);
				// checking if it has to join it's zone
				if (x >= myPeer.startX && y >= myPeer.startY && x < myPeer.x
						&& y < myPeer.y) {
					Peer temp = null;
					// if myPeer is square
					if (checkSquare(myPeer.startX, myPeer.startY, myPeer.x,
							myPeer.y)) {
						// if y is less
						if (y < (myPeer.y / 2)) {
							// System.out.println("Square y less " +
							// myPeer.name);

							// calculating cords
							x = myPeer.x / 2;
							y = myPeer.y;
							double startY = myPeer.startY;
							double startX = myPeer.startX;
							temp = new Peer(checkers[1], checkers[2], startX,
									startY, x, y);
							myPeer.startX = myPeer.x / 2;
							writePeer();
						}
						// if y is less

						else {
							// System.out.println("square y more " +
							// myPeer.name);

							// calculating cords
							x = myPeer.x;
							y = myPeer.y;
							double startY = myPeer.startY;
							double startX = myPeer.x / 2;

							myPeer.x = myPeer.x / 2;

							temp = new Peer(checkers[1], checkers[2], startX,
									startY, x, y);
							writePeer();
						}
					}
					// for rectangle

					else {
						// if x is less

						if (x < (myPeer.x / 2)) {
							// System.out.println("no square x less "
							// + myPeer.name);
							x = myPeer.x;
							y = myPeer.y / 2;
							double startY = myPeer.startX;
							double startX = myPeer.startY;
							temp = new Peer(checkers[1], checkers[2], startX,
									startY, x, y);

							myPeer.startY = myPeer.y / 2;
							writePeer();
						}
						// if x is more
						else {
							// System.out.println("no square x more "
							// + myPeer.name);

							x = myPeer.x;
							y = myPeer.y;
							double startX = myPeer.startX;
							double startY = myPeer.y / 2;

							temp = new Peer(checkers[1], checkers[2], startX,
									startY, x, y);

							myPeer.y = myPeer.y / 2;
							writePeer();
						}
					}
					temp.port = (int) (Integer.parseInt(checkers[5]));
					//System.out.println(checkers[5]);
					temp.neighbor.add(myPeer);
					myPeer = readPeer();
					// moving files after zone is taken over
					for (int i = 0; i < myPeer.files.size(); i++) {
						int X = clientProgram.charAtOdd(myPeer.files.get(i)) % 10;
						int Y = clientProgram.charAtEven(myPeer.files.get(i)) % 10;

						if (X >= temp.startX && Y >= temp.startY && X < temp.x
								&& Y < temp.y) {
//							System.out.println(X + " is greater than equal "
//									+ temp.startX + " and " + Y
//									+ " is greater than equal " + temp.startY
//									+ " and " + X + " is less than " + temp.x
//									+ " and " + Y + " is less than " + temp.y);
//							System.out.println();
							temp.files.add(myPeer.files.get(i));
							myPeer.files.remove(i);
						}

					}

					writePeer();
					myPeer = readPeer();

					// updating cords of neighbors
					for (int i = 0; i < myPeer.neighbor.size(); i++) {
						Socket requestSocket1 = new Socket(
								myPeer.neighbor.get(i).ip.substring(1),
								myPeer.neighbor.get(i).port);

						ObjectOutputStream out4 = new ObjectOutputStream(
								requestSocket1.getOutputStream());
					//	System.out.println("Updating cords");
						out4.writeObject("Updatecord%" + myPeer.name + "%"
								+ myPeer.startX + "%" + myPeer.startY + "%"
								+ myPeer.x + "%" + myPeer.y);
					}
					writePeer();
					myPeer = readPeer();
					// updating the possible neighbors
					for (int i = 0; i < myPeer.neighbor.size(); i++) {
						if (possibleNeighbor(myPeer.neighbor.get(i), temp)) {
//							System.out.println(myPeer.neighbor.get(i).name
//									+ " is possible neighbor of " + temp.name);
//							System.out.println("Will connect to "
//									+ myPeer.neighbor.get(i).ip.substring(1)
//									+ ":" + myPeer.neighbor.get(i).port);
							Socket requestSocket = new Socket(
									myPeer.neighbor.get(i).ip.substring(1),
									myPeer.neighbor.get(i).port);

							ObjectOutputStream out3 = new ObjectOutputStream(
									requestSocket.getOutputStream());

							ObjectInputStream in3 = new ObjectInputStream(
									requestSocket.getInputStream());
							out3.writeObject("iamneighbor");
							//System.out.println("sending message to neighbor");
							String message = "";

							do {
								message = (String) in3.readObject();
							} while (!message.equals("ok"));
//							System.out.println(message);
//							System.out.println("neighbor sent message");
							out3.writeObject(temp);
//							System.out.println("Write successfull");
//							
							temp = (Peer) in3.readObject();
						//	System.out.println("temp has been read");
						}
					}

					myPeer = readPeer();
//					System.out.println("Updating not neighbors");

					// Removing neighbors according to new cords
					for (int i = 0; i < myPeer.neighbor.size(); i++) {
						if (!possibleNeighbor(myPeer.neighbor.get(i), myPeer)) {
							Socket requestSocket = new Socket(
									myPeer.neighbor.get(i).ip.substring(1),
									myPeer.neighbor.get(i).port);

							ObjectOutputStream out3 = new ObjectOutputStream(
									requestSocket.getOutputStream());

							ObjectInputStream in3 = new ObjectInputStream(
									requestSocket.getInputStream());
							out3.writeObject("iamnotneighbor%" + myPeer.name);
							// out3.flush();
							String message = "";
							do {
								message = (String) in3.readObject();
							} while (!message.equals("ok"));
							myPeer.neighbor.remove(i);
							writePeer();
						}
					}
					// adding itself as a neighbor to new peer
					myPeer.neighbor.add(temp);

					// temp.neighbor.set(myPeer);
					out1.writeObject(temp);
					// out1.flush();
					writePeer();
	//				clientProgram.showView();
				}
				// if not in that zone
				else {
					// finding closest zone
					double minDist = linedist(
							x,
							y,
							((myPeer.neighbor.get(0).x + myPeer.neighbor.get(0).startX) / 2),
							(myPeer.neighbor.get(0).x + myPeer.neighbor.get(0).startY) / 2);
					int minID = 0;
					for (int i = 0; i < myPeer.neighbor.size(); i++) {
						if (linedist(x, y,
								((myPeer.neighbor.get(i).x + myPeer.neighbor
										.get(i).startX) / 2),
								(myPeer.neighbor.get(i).x + myPeer.neighbor
										.get(i).startY) / 2) < minDist) {
							minID = i;
						}
					}
					System.out.println(myPeer.neighbor.get(minID).ip + ":"
							+ myPeer.neighbor.get(minID).port);
					Socket requestSocket = new Socket(
							myPeer.neighbor.get(minID).ip.substring(1),
							myPeer.neighbor.get(minID).port);

					ObjectOutputStream out3 = new ObjectOutputStream(
							requestSocket.getOutputStream());

					ObjectInputStream in3 = new ObjectInputStream(
							requestSocket.getInputStream());

					out3.writeObject(checker);
					Peer temp = (Peer) in3.readObject();

					out1.writeObject(temp);
					out1.flush();
				}

			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	/*
	 * function to write peer info to file
	 */
	public static void writePeer() throws IOException {

		FileOutputStream fout = new FileOutputStream("peerInfo.ser");
		ObjectOutputStream oos = new ObjectOutputStream(fout);
		oos.writeObject(myPeer);
		oos.close();

	}

	/*
	 * function to read peer info from file
	 */
	public static Peer readPeer() throws  IOException {

		InputStream file = new FileInputStream("peerInfo.ser");
		InputStream buffer = new BufferedInputStream(file);
		ObjectInputStream input1 = new ObjectInputStream(buffer);
		Peer ret = null;
		try {
			ret = (Peer) input1.readObject();
		} catch (ClassNotFoundException e) {
			System.out.println("Please run clientProgram first.");
		}
		return ret;

	}

	/*
	 * checking of two peers are neighbors
	 */
	public boolean possibleNeighbor(Peer p1, Peer p2) {
		// calculating cords of the peers
		if (diagnolLength(p1) < diagnolLength(p2)) {
			Peer temp = p1;
			p2 = p1;
			p1 = temp;

		}

		double rightDownX1, rightDownY1, rightUpX1, rightUpY1;
		double rightDownX2, rightDownY2, rightUpX2, rightUpY2;

		double leftDownX1, leftDownY1, leftUpX1, leftUpY1;
		double leftDownX2, leftDownY2, leftUpX2, leftUpY2;

		rightDownX1 = p1.x;
		rightDownY1 = p1.startY;
		rightUpX1 = p1.x;
		rightUpY1 = p1.y;

		rightDownX2 = p2.x;
		rightDownY2 = p2.startY;
		rightUpX2 = p2.x;
		rightUpY2 = p2.y;

		leftDownX1 = p1.startX;
		leftDownY1 = p1.startY;
		leftUpX1 = p1.startX;
		leftUpY1 = p1.y;

		leftDownX2 = p2.startX;
		leftDownY2 = p2.startY;
		leftUpX2 = p2.startX;
		leftUpY2 = p2.y;

		Line l1_1 = new Line(leftDownX1, leftDownY1, leftUpX1, leftUpY1);
		Line l1_2 = new Line(rightDownX1, rightDownY1, rightUpX1, rightUpY1);

		Line l1_3 = new Line(rightDownX1, rightDownY1, leftDownX1, leftDownY1);

		Line l1_4 = new Line(rightUpX1, rightUpY1, leftUpX1, leftDownY1);
/*
		Line l2_1 = new Line(leftDownX2, leftDownY2, leftUpX2, leftUpY2);
		Line l2_2 = new Line(rightDownX2, rightDownY2, rightUpX2, rightUpY2);

		Line l2_3 = new Line(rightDownX2, rightDownY2, leftDownX2, leftDownY2);

		Line l2_4 = new Line(rightUpX2, rightUpY2, leftUpX2, leftDownY2);
*/
		int pointShared = 0;
		// to check if point is shared on a line
		if (onStraightLine(l1_1, leftDownX2, leftDownY2))
			pointShared++;
		if (onStraightLine(l1_2, leftDownX2, leftDownY2))
			pointShared++;
		if (onStraightLine(l1_3, leftDownX2, leftDownY2))
			pointShared++;
		if (onStraightLine(l1_4, leftDownX2, leftDownY2))
			pointShared++;

		if (onStraightLine(l1_1, leftUpX2, leftUpY2))
			pointShared++;
		if (onStraightLine(l1_2, leftUpX2, leftUpY2))
			pointShared++;
		if (onStraightLine(l1_3, leftUpX2, leftUpY2))
			pointShared++;
		if (onStraightLine(l1_4, leftUpX2, leftUpY2))
			pointShared++;

		if (onStraightLine(l1_1, rightUpX2, rightUpY2))
			pointShared++;
		if (onStraightLine(l1_2, rightUpX2, rightUpY2))
			pointShared++;
		if (onStraightLine(l1_3, rightUpX2, rightUpY2))
			pointShared++;
		if (onStraightLine(l1_4, rightUpX2, rightUpY2))
			pointShared++;

		if (onStraightLine(l1_1, rightDownX2, rightDownY2))
			pointShared++;
		if (onStraightLine(l1_2, rightDownX2, rightDownY2))
			pointShared++;
		if (onStraightLine(l1_3, rightDownX2, rightDownY2))
			pointShared++;
		if (onStraightLine(l1_4, rightDownX2, rightDownY2))
			pointShared++;
/*
		int pointShared2 = 0;
		if (onStraightLine(l2_1, rightDownX1, rightDownY1))
			pointShared2++;
		if (onStraightLine(l2_2, rightDownX1, rightDownY1))
			pointShared2++;
		if (onStraightLine(l2_3, rightDownX1, rightDownY1))
			pointShared2++;
		if (onStraightLine(l2_4, rightDownX1, rightDownY1))
			pointShared2++;

		if (onStraightLine(l2_1, leftDownX1, leftDownY1))
			pointShared2++;

		if (onStraightLine(l2_2, leftDownX1, leftDownY1))
			pointShared2++;

		if (onStraightLine(l2_3, leftDownX1, leftDownY1))
			pointShared2++;

		if (onStraightLine(l2_4, leftDownX1, leftDownY1))
			pointShared2++;

		if (onStraightLine(l2_1, leftUpX1, leftUpY1))
			pointShared2++;
		if (onStraightLine(l2_2, leftUpX1, leftUpY1))
			pointShared2++;
		if (onStraightLine(l2_3, leftUpX1, leftUpY1))
			pointShared2++;
		if (onStraightLine(l2_4, leftUpX1, leftUpY1))
			pointShared2++;

		if (onStraightLine(l2_1, rightUpX1, rightUpY1))
			pointShared2++;
		if (onStraightLine(l2_2, rightUpX1, rightUpY1))
			pointShared2++;
		if (onStraightLine(l2_3, rightUpX1, rightUpY1))
			pointShared2++;
		if (onStraightLine(l2_4, rightUpX1, rightUpY1))
			pointShared2++;
*/
		// if more than 2 points are shared by the neighbors
	//	if ((pointShared >= 2 && pointShared2 >= 1)
		//		|| (pointShared >= 1 && pointShared2 >= 2))
		if(pointShared>=2)	
		return true;
		else
			return false;
	}

	/*
	 * function to check if point lies on a line
	 */
	boolean onStraightLine(Line l, double X, double Y) {
		if (X >= l.x1 && X <= l.x2) {
			if (Y == l.y1 && Y == l.y2)
				return true;
		}
		if (Y >= l.y1 && Y <= l.y2) {
			if (X == l.x1 && X == l.x2)
				return true;
		}
		return false;
	}

	double diagnolLength(Peer shape) {
		double diagnol = linedist(shape.startX, shape.startY, shape.x, shape.y);

		return diagnol;
	}

	/*
	 * function to check if square is made out of cords
	 */
	boolean checkSquare(double startx, double starty, double X, double Y) {
		if (linedist(startx, starty, X, starty) == linedist(startx, starty,
				startx, Y))
			return true;
		return false;
	}

	/*
	 * function to find distance between line
	 */

	double linedist(double x1, double y1, double x2, double y2) {
		double ans = Math.abs(Math.sqrt(Math.pow((x2 - x1), 2)
				+ Math.pow((y2 - y1), 2)));
		return ans;
	}
}
