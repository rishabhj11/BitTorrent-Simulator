package server;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class SplitFiles {
	public static byte[] storeBytes;
	public static int numBytesRead;
	public static int numChunks = 0;

	public static int splitFiles(String mainFilePath) throws IOException {
		File mainFile = null;
		FileInputStream aFile = null;
		FileOutputStream savePart = null;
		try {
			mainFile = new File(mainFilePath);
			aFile = new FileInputStream(mainFile);
			FileChannel inChannel = aFile.getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(102400); // 100KB
			storeBytes = new byte[102400];
			while ((numBytesRead = inChannel.read(buffer)) != -1) {
				buffer.flip();
				buffer.get(storeBytes, 0, numBytesRead);
				savePart = new FileOutputStream("src/server/Part" + numChunks);
				savePart.write(storeBytes, 0, numBytesRead);
				savePart.close();
				numChunks++;
				buffer.clear();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (aFile != null) {
				aFile.close();
			}
			if (savePart != null) {
				savePart.close();
			}

		}
		return (int) (Math.ceil((((double) mainFile.length()) / 102400)));
	}

}
