package client;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Client {
	public static boolean[] chunkList = null;
	public static int totalFilesOnServer = 0;

	public static void main(String[] args) throws IOException {
		int mainServerPort = Integer.parseInt(args[0]);
		int selfPort = Integer.parseInt(args[1]);
		int downloadPort = Integer.parseInt(args[2]);

		Socket client = null;
		Socket downSocket = null;
		ServerSocket upSocket = null;
		String hostName = "localhost";
		int bytesRead = 0;
		int current = 0;
		FileOutputStream fos = null;
		BufferedOutputStream bos = null;
		ObjectInputStream ois = null;
		int numFilesFromServer = 0;
		int fileSize = 0;

		int clientNum = 0;

		try {

			client = new Socket(hostName, mainServerPort);
			ois = new ObjectInputStream(client.getInputStream());
			clientNum = (int) ois.readObject();
			File file = new File("src/client" + clientNum);
			if (!file.exists()) {
				file.mkdir();
			}

			byte[] mybytearray = new byte[1024000];
			totalFilesOnServer = (int) ois.readObject();
			numFilesFromServer = (int) ois.readObject();
			// System.out.println("Numbers of incoming files is " +
			// numFilesFromServer);
			chunkList = new boolean[totalFilesOnServer];

			int filefromServerNum;
			for (int i = 1; i <= numFilesFromServer; i++) {
				fileSize = ((Number) ois.readObject()).intValue();
				filefromServerNum = (int) ois.readObject();

				chunkList[filefromServerNum] = true;
				fos = new FileOutputStream("src/client" + clientNum + "/Part" + filefromServerNum);
				bos = new BufferedOutputStream(fos);
				bytesRead = ois.read(mybytearray, 0, fileSize);
				current = bytesRead;

				do {
					bytesRead = ois.read(mybytearray, current, (fileSize - current));
					if (bytesRead >= 0)
						current += bytesRead;
				} while (bytesRead != 0);

				bos.write(mybytearray, 0, current);
				bos.flush();
				System.out.println("Part" + filefromServerNum + " downloaded");
			}
			System.out.println("Alloted parts downloaded");
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName);
			System.exit(1);
		} catch (IOException e1) {
			System.err.println("Couldn't get I/O for the connection to " + hostName);
			System.exit(1);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				fos.close();
			}
			if (bos != null) {
				bos.close();
			}
			if (ois != null) {
				ois.close();
			}
			if (client != null) {
				client.close();
			}
		}

		/* Creating threads */
		try {
			upSocket = new ServerSocket(selfPort);
			System.out.println("Server started at client" + clientNum);
			Thread Uploader = new Thread(new Uploader(upSocket, clientNum));
			Uploader.start();

			Thread downloader = new Thread(new downloader(hostName, downloadPort, clientNum));
			downloader.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized static boolean[] getList() {
		return chunkList;
	}

	public synchronized static int totalFilesOnServer() {
		return totalFilesOnServer;
	}

}
