/*
 * Peer.java
 *
 * Version: 1.0
 *
 * Revisions:
 *     Monday, March 17, 2014
 */

import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;

/**
* Serializable Peer class 
* 
* @author Harsh Hiro Sadhvani
* 
*/
public class Peer implements Serializable {

	/**
	 *  information of that peer
	 */
	private static final long serialVersionUID = 1L;

	String name;

	String ip;
	ArrayList<String> files = new ArrayList<String>();
	double startX, startY;
	double x, y;
	int port;
		ArrayList<Peer> neighbor = new ArrayList<Peer>();;

	Peer(String name, String ip, double startX, double startY, double x,
			double y) {
		this.name = name;
		this.ip = ip;
		this.startX = startX;
		this.startY = startY;
		this.x = x;
		this.y = y;
	}
	@Override
    public String toString() {
		return name + "%" + ip + "%" + startX + "%" + startY + "%" + x + "%"
				+ y;
	}
}
