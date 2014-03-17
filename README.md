README


Files:

1) All on files in the “clientSideFiles” folder should run on the Peer end.
2) All files in the “bootStrapFiles” folder should run on the Bootstrap Server.

Steps to Run :

1) Compile all java files in “bootStrapFiles” folder.

2) Run bootstrapServer class file as “java bootstrapServer <portNumber>”. Here portNumber is the port number the bootstrap server will listen on. 
For example : java bootstrapServer 3500.

So here bootstrap server will listen on port 3500.

3) Compile all java files in “clientSideFiles” folder.

4) Run clientProgram class file as so :

java clientProgram <bootStrapServer_address> <BoostrapServerPortNo>

bootStrapServer_address is the address of the bootstrap server and BoostrapServerPortNo is port number of the bootstrap server. 

For example:

java clientProgram glados.cs.rit.edu 3500

5) join the CAN network by typing join. 

6) Once you join the network run clientWork file on another terminal window as so:
 
java clientWork

Do not run this until you do not join the network. This process will run in the background.

7) You can use insert, view and search after you join the method. (In clientProgram file)

8) Similarly use join and other functions on the other peers.

9) Leave has not been implemented for this network so if any connection is stopped abruptly, this network will fail.

Using insert, search and view

insert : insert <fileName>

This will insert the file into the network on appropriate peer and display the path to insert it

search : search <fileName> 

This is will search for the file on the network and display the path is followed to search for it.

view: view

Display information about the peer and its neighbors.