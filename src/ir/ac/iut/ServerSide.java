package ir.ac.iut;

/**
 * @author Mohammad Anisi  [anisi.1976@gmail.com]
 * 
 *         
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServerSide
{
	static File localDir;

	public static void main(String[] args)
	{
		int port = 9999;
		BufferedReader reader;
		PrintWriter writer;
		InputStream inStream;
		OutputStream outStream;

		try {
	   	ServerSocket server = new ServerSocket(port);
		
			while(true) {
				System.out.println("Waiting for clients on port " + port);
				Socket client = server.accept();
				
				inStream = client.getInputStream();
				outStream = client.getOutputStream();				
				reader = new BufferedReader(new InputStreamReader(inStream));
				writer = new PrintWriter(outStream);
					
				writer.println("Welcome to my file server");
				writer.flush();
					
				String cmd=reader.readLine();
				
				if(cmd.startsWith("reqfile")){
				String filename = cmd.substring(8);
				File file = new File(filename);
				if (!file.exists()){
					writer.println("ERROR");
					writer.flush();
				}
				else {
					FileInputStream	fileInStream = new FileInputStream(filename);		
					writer.println(""+file.length());
					writer.flush();
					sendFile(fileInStream, file.length(), outStream);
					fileInStream.close();
				}
			}
				else if(cmd.startsWith("upfile")){
					String fileName = cmd.substring(7);
					File file = new File("Uploaded_"+fileName);
					  FileOutputStream fileOutStream = new FileOutputStream(file);
					  reciveFile(fileOutStream, file.length(), inStream);
					  fileOutStream.close();
				}
				
				else if(cmd.startsWith("lsdir")){
					ListOfFiles(writer);
				}
				client.close();		
		} 
		}catch(Exception ex) {
			System.out.println("Connection error: "+ex);
		}
	}
	
/*##################################################################*/
	public static void sendFile(FileInputStream file, long size, OutputStream outStream) {
		byte[] buffer = new byte[1024];
		int sofar = 0;
		
		try {
		
			while (sofar < size) {
				int read = file.read(buffer, 0, buffer.length);
				sofar+=read;
				outStream.write(buffer, 0, read);
				outStream.flush();
			}
		}
		catch (Exception ex) {
			System.out.println("Exception: "+ex);
		}
	}
	/*##################################################################*/
	public static void reciveFile(FileOutputStream fileOut, long size, InputStream inStream){
		byte[] buffer = new byte[1024];
		try {
				while (true) {
					int bytes = inStream.read(buffer);
					if (bytes < 0)
						break;
					fileOut.write(buffer, 0, bytes);
			}
				inStream.close();
		}
		catch (Exception ex) {
			System.out.println("Exception: "+ex);
		}
 }
	
	
	/*##################################################################*/
	private static void ListOfFiles(PrintWriter writer) throws IOException {
		localDir = new File(new File(".").getCanonicalPath());
		List<File> list = listLocal();
		long lengths = 0;
		int dirNum = 0;
		int fileNum = 0;
		String deli = "<DIR>";
		writer.write("\n\r\n\r");
		writer.write(" Directory Of: " + localDir + "\r\n");
		writer.write(" \t\t\t\t\t\t\t  Date Madified \t\t\t\t Size \t\t\t Name \n");
		writer.write(" \t\t\t\t\t---------------------------------------------"
				+ "----------------------------------------------------\r\n");
		for (int i = 0; i < listLocal().size(); i++) {
			lengths += list.get(i).length();
			if (list.get(i).isDirectory())
				dirNum++;
			else
				fileNum++;
			writer.write("\t\t\t\t---->> "
					+ (i + 1)
					+ " ) : "
					+ new Date(list.get(i).lastModified())
					+ "\t\t"
					+ (((list.get(i).isDirectory()) == true) ? deli
							: formatFileSize(list.get(i).length())) + "\t\t"
					+ list.get(i).getName() + "\r\n");
		}
		writer.write("\r\n");
		writer.write("\t\t\t\t\t\t" + fileNum + " File(s)" + "\t\t" + dirNum
				+ " Directory");
		writer.write("\tTotal Size: " + lengths+" KB");
		writer.flush();
	}
	
	/*##################################################################*/
	public static List<File> listLocal() {
		List<File> result = new ArrayList<File>();
		File[] files = localDir.listFiles();
		for (File f : files) {
			result.add(new File(f.getName()));
		}
		return result;
	}
	
	/*##################################################################*/
	public static String formatFileSize(long fileSzie) {
		long n = fileSzie;
		if (n < 1024)
			return n + " B";
		if (n < 1024 * 1024)
			return (n / 1024) + "." + ((n % 1024) / 103) + " KB";
		if (n < 1024 * 1024 * 1024)
			return (n / (1024 * 1024)) + "."
					+ ((n % (1024 * 1024)) / (103 * 1024)) + " MB";
		if (n <= 1024l * 1024l * 1024l * 1024l)
			return (n / (1024l * 1024l * 1024l)) + "."
					+ ((n % (1024l * 1024l * 1024l)) / (103l * 1024l * 1024l))
					+ " GB";
		return "A lot !!!";
	}

	/*##################################################################*/
	public static String formatFileTime(long fileSzie) {
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
		Date resultdate = new Date(fileSzie);
		// System.out.println(sdf.format(resultdate));
		return sdf.format(resultdate);

	}
	
	
}
