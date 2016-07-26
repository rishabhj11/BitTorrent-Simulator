package client;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Uploader implements Runnable {
	ServerSocket serverSocket = null;
	Socket clientSocket = null;
	BufferedReader in = null;
	boolean[] serverChunkList;
	boolean[] updatedServerChunkList;
	ObjectOutputStream oos = null;
	ObjectInputStream ois = null;
	int clientID = 0;

	public Uploader(ServerSocket upsocket, int clientNum) {
		this.serverSocket = upsocket;
		this.serverChunkList = Client.getList();
		this.clientID = clientNum;
	}

	public void run() {
		System.out.println("Uploader thread started");
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		while (true) {
			updatedServerChunkList = Client.getList();
			serverChunkList = updatedServerChunkList;
			try {
				clientSocket = serverSocket.accept();
				//System.out.println("Comm established");
				oos = new ObjectOutputStream(clientSocket.getOutputStream());
				ois = new ObjectInputStream(clientSocket.getInputStream());

				oos.writeObject(serverChunkList);
				System.out.println("serverChunkList sent");
				Thread.sleep(1000);
				int fileNum;
				fileNum = (int) ois.readObject();

				File myFile = new File("src/client" + clientID + "/Part" + fileNum);
				byte[] mybytearray = new byte[(int) myFile.length()];
				fis = new FileInputStream(myFile);
				bis = new BufferedInputStream(fis);

				oos.writeObject(myFile.length()); // send size of file

				bis.read(mybytearray, 0, mybytearray.length);
				System.out.println("Sending " + "Part" + fileNum + "(" + mybytearray.length + " bytes)");
				oos.write(mybytearray, 0, mybytearray.length);
				oos.flush();
				System.out.println("Sent");

				if (fis != null) {
					fis.close();
				}
				if (bis != null) {
					bis.close();
				}
				if (oos != null) {
					oos.close();
				}
				if (ois != null) {
					ois.close();
				}
				if (clientSocket != null) {
					clientSocket.close();
				}

			} catch (EOFException e) {
				// e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Error while listening");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
