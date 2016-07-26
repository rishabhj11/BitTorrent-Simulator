package server;

import java.io.*;
import java.net.*;

public class Server {
	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = null;
		Socket clientSocket = null;

		final String hostName = "http://localhost";
		final int portNumber = 8000;
		int numFiles;

		try {
			serverSocket = new ServerSocket(portNumber);
			System.out.println("Server started");
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host " + hostName);
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " + hostName);
			e.printStackTrace();
			System.exit(1);
		}

		// String mainFilePath = "src/server/testfile1.pptx";
		String mainFilePath = "src/server/testfile2.zip";
		System.out.println("Splitting files");
		numFiles = SplitFiles.splitFiles(mainFilePath);

		class serverToClient implements Runnable {
			Socket incomingClient = null;

			int numThread;

			public serverToClient(Socket clientSocket, int numThread) {
				this.incomingClient = clientSocket;
				this.numThread = numThread;
			}

			public void run() {
				FileInputStream fis = null;
				BufferedInputStream bis = null;
				ObjectOutputStream oos = null;
				try {
					oos = new ObjectOutputStream(incomingClient.getOutputStream());
					oos.writeObject(numThread);
					int numFilesToClient = 0;
					for (int i = numThread; i < numFiles; i += 5) {
						numFilesToClient++;
					}
					oos.writeObject(numFiles);
					oos.writeObject(numFilesToClient);
					for (int i = numThread; i < numFiles; i += 5) {
						File myFile = new File("src/server/Part" + i);
						byte[] mybytearray = new byte[(int) myFile.length()];
						fis = new FileInputStream(myFile);
						bis = new BufferedInputStream(fis);
						oos.writeObject(myFile.length());
						oos.writeObject(i);
						bis.read(mybytearray, 0, mybytearray.length);
						System.out.println("Sending " + "Part" + i);
						oos.write(mybytearray, 0, mybytearray.length);
						oos.flush();
						System.out.println("Sent");
					}
					if (fis != null) {
						fis.close();
					}
					if (bis != null) {
						bis.close();
					}
					if (oos != null) {
						oos.close();
					}
					if (incomingClient != null) {
						incomingClient.close();
					}
				} catch (IOException e) {
					System.out.println("Error while listening");
					e.printStackTrace();
				}
			}
		}
		int threadCount = -1;
		while (true) {
			try {
				clientSocket = serverSocket.accept();
				// System.out.println("Server connected to client at port " +
				// clientSocket.getPort());
				threadCount++;
				Thread serverToClient = new Thread(new serverToClient(clientSocket, threadCount));
				// System.out.println("Spawning thread " + threadCount);
				serverToClient.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
