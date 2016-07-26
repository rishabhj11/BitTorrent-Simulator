package client;

import java.io.*;
import java.net.*;
import java.util.*;

public class downloader implements Runnable {
	String hostName = null;
	int portNumber = 0;
	Socket downSocket = null;
	ObjectInputStream ois = null;
	ObjectOutputStream oos = null;
	boolean[] myChunkList = null;
	int dbytesRead = 0;
	int dcurrent = 0;
	FileOutputStream dfos = null;
	BufferedOutputStream dbos = null;
	int dfileSize = 0;
	int clientID = 0;
	byte[] dmybytearray = new byte[1024000];

	public downloader(String hostName, int portNumber, int clientID) throws IOException {
		this.hostName = hostName;
		this.portNumber = portNumber;
		this.myChunkList = Client.getList();
		this.clientID = clientID;
	}

	public void run() {
		System.out.println("Downloader thread started");
		while (!areAllTrue()) {
			myChunkList = Client.getList();
			try {
				boolean checkPeer = false;
				while (!checkPeer) {
					try {
						downSocket = new Socket(hostName, portNumber);
						checkPeer = true;
					} catch (Exception e) {
						checkPeer = false;
					}
				}
				oos = new ObjectOutputStream(this.downSocket.getOutputStream());
				ois = new ObjectInputStream(this.downSocket.getInputStream());
				boolean[] serverChunkList = (boolean[]) ois.readObject();
				System.out.println("Chunk List received");
				for (int i = 0; i < serverChunkList.length; i++) {
					if (!myChunkList[i]) {
						if (serverChunkList[i]) { // download file routine
							receiveFile(i);
							break;
						}
					}
				}
				downSocket.close();
			} catch (IOException e1) {
				System.err.println("Couldn't get I/O for the connection to " + hostName);
				System.exit(1);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		System.out.println("DOWNLOADING COMPLETE, STARTING MERGING");
		mergeFiles();
	}

	public void receiveFile(int i) throws IOException {
		try {
			oos.writeObject(i); // file requested
			dfileSize = ((Number) ois.readObject()).intValue(); // Expected file
																// size
			dfos = new FileOutputStream("src/client" + clientID + "/Part" + i);
			dbos = new BufferedOutputStream(dfos);
			dbytesRead = ois.read(dmybytearray, 0, dfileSize);
			dcurrent = dbytesRead;

			do {
				dbytesRead = ois.read(dmybytearray, dcurrent, (dfileSize - dcurrent));
				if (dbytesRead >= 0)
					dcurrent += dbytesRead;
			} while (dbytesRead != 0);

			dbos.write(dmybytearray, 0, dcurrent);
			dbos.flush();
			myChunkList[i] = true;
			System.out.println("Part" + i + " downloaded");
		} catch (IOException e1) {
			System.err.println("Couldn't get I/O for the connection to " + hostName);
			System.exit(1);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (dfos != null) {
				dfos.close();
			}
			if (dbos != null) {
				dbos.close();
			}
			if (oos != null) {
				oos.close();
			}
			if (ois != null) {
				ois.close();
			}
		}
	}

	public boolean areAllTrue() {
		for (boolean b : myChunkList)
			if (!b)
				return false;
		return true;
	}

	public void mergeFiles() {
		FileOutputStream newFile = null;
		FileInputStream takeInput = null;
		byte[] buffer = new byte[1024];
		int bufSize;

		try {
			// newFile = new FileOutputStream("src/client" + clientID +
			// "/newfile.pptx");
			newFile = new FileOutputStream("src/client" + clientID + "/newfile1.zip");
			int totalFilesToMerge = Client.totalFilesOnServer();
			for (int i = 0; i < totalFilesToMerge; i++) {
				takeInput = new FileInputStream("src/server/Part" + i);
				while ((bufSize = takeInput.read(buffer)) != -1) {
					newFile.write(buffer, 0, bufSize);
				}
				if (takeInput != null) {
					takeInput.close();
				}
			}
			System.out.println("MERGING COMPLETE!");
			if (newFile != null) {
				newFile.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
