package server;

import java.io.*;

public class Server {
	public static void main(String[] args) throws IOException {
		FileOutputStream newFile = null;
		FileInputStream takeInput =null;
		byte[] buffer = new byte[1024];
		//int numFiles = SplitFiles.numChunks;
		int bufSize;
		
		try{
			newFile = new FileOutputStream("src/server/newfile.pptx");
			String mainFilePath = "src/testfile1.pptx";
			SplitFiles.splitFiles(mainFilePath);
			
			for (int i = 1; i <= 60; i++) {
				takeInput = new FileInputStream("src/server/Part"+i);
				while ((bufSize = takeInput.read(buffer)) != -1){
					newFile.write(buffer, 0, bufSize);
				}
				if(takeInput != null){
					takeInput.close();
				}
			}
		}
		finally{
			if(newFile != null){
				newFile.close();
			}
		}
	}
}
