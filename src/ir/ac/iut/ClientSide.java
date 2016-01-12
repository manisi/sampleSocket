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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

public class ClientSide {
	private static Text text_Ip;
	private static Text text_Port;
	private static Text text_Command;
	private static Text text_Info;
	private static Label lblNewLabel;

	/*##################################################################*/
	public static void main(String[] args) {
		Display display = Display.getDefault();
		Shell shlSimpleFtpFor = new Shell();
		shlSimpleFtpFor.setToolTipText("Mohammad Anisi ");
		shlSimpleFtpFor.setSize(811, 545);
		shlSimpleFtpFor.setText("Simple FTP              ITE Course Project ");
		
		SashForm sashForm = new SashForm(shlSimpleFtpFor, SWT.BORDER | SWT.SMOOTH);
		sashForm.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BACKGROUND));
		sashForm.setBounds(24, 10, 746, 33);
		
		Composite composite = new Composite(sashForm, SWT.NONE);
		composite.setLayout(null);
		
		Label lblServerIp = new Label(composite, SWT.RIGHT);
		lblServerIp.setBounds(10, 5, 65, 21);
		lblServerIp.setText("Server IP : ");
		
		text_Ip = new Text(composite, SWT.BORDER);
		text_Ip.setBounds(79, 3, 122, 23);
		
		Label lblPortNumber = new Label(composite, SWT.NONE);
		lblPortNumber.setAlignment(SWT.RIGHT);
		lblPortNumber.setBounds(224, 7, 82, 15);
		lblPortNumber.setText("Port Number :");
		
		text_Port = new Text(composite, SWT.BORDER);
		text_Port.setBounds(312, 3, 75, 23);
		sashForm.setWeights(new int[] {1});
		
		SashForm sashForm_1 = new SashForm(shlSimpleFtpFor, SWT.BORDER);
		sashForm_1.setBounds(24, 114, 746, 385);
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(sashForm_1, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		text_Info = new Text(scrolledComposite, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		text_Info.setFont(SWTResourceManager.getFont("Segoe UI", 11, SWT.NORMAL));
		text_Info.setForeground(SWTResourceManager.getColor(0, 0, 153));
		text_Info.setBackground(SWTResourceManager.getColor(SWT.COLOR_INFO_BACKGROUND));
		scrolledComposite.setContent(text_Info);
		scrolledComposite.setMinSize(text_Info.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sashForm_1.setWeights(new int[] {1});
		
		SashForm sashForm_2 = new SashForm(shlSimpleFtpFor, SWT.BORDER | SWT.SMOOTH);
		sashForm_2.setBounds(24, 47, 746, 30);
		
		Composite composite_1 = new Composite(sashForm_2, SWT.NONE);
		composite_1.setLayout(null);
		
		Label lblCommand = new Label(composite_1, SWT.CENTER);
		lblCommand.setBounds(0, 4, 70, 21);
		lblCommand.setText("Command:");
		
		text_Command = new Text(composite_1, SWT.BORDER);
		text_Command.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
			@Override
			public void widgetSelected(SelectionEvent arg0) {
			}
		});
		text_Command.addKeyListener(new KeyAdapter() {
			private InputStream inStream;
			private OutputStream outStream;
			private PrintWriter writer;
			private BufferedReader reader;
			private volatile int transferred;
			private int percent;
			private int lastPercent;

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.keyCode==SWT.CR){
					boolean downloaded = false;
					lblNewLabel.setText("");
					try {
						String host = text_Ip.getText();
						int port = Integer.parseInt(text_Port.getText());

						Socket client = new Socket(host, port);
						inStream = client.getInputStream();
						outStream = client.getOutputStream();
						writer = new PrintWriter(outStream);
						reader = new BufferedReader(new InputStreamReader(inStream));

						reader.readLine();

						String requst = text_Command.getText();
						String filename = "";
						if (requst.startsWith("upfile")) {
							filename = requst.substring(7);
							File file = new File(filename);

							if (!file.exists()) {
								writer.println("ERROR");
								writer.flush();
								System.out.println("ERROR UP");
							} else {
								FileInputStream fileInStream = new FileInputStream(
										filename);
								upFile(fileInStream, file.length(), outStream);
								fileInStream.close();
							}
							client.close();
						} else if (requst.startsWith("reqfile")) {
							filename = requst.substring(8);
							writer.println(requst); // sends the file name
							writer.flush();

							String fileSize = reader.readLine(); 
							
							if (!fileSize.equalsIgnoreCase("ERROR")) {
								reqFile("CopyOf_" + filename, Long
										.parseLong(fileSize), inStream);
								downloaded = true;
							} else
								downloaded = false;

							if (downloaded)
								if (filename.toLowerCase().endsWith("txt"))
									displayFile("CopyOf_" + filename);
								else
									text_Info.setText("\n\n\n"
													+ filename
													+ " has been downloaded and saved as "
													+ "CopyOf_" + filename);
							else
								text_Info.setText("\n\n\nFile " + filename
										+ " Not Founded and Could not be downloaded");

							client.close();
						}

						else if (requst.startsWith("lsdir")) {
							writer.println(requst);
							writer.flush();
							String s;
							text_Info.setText("");
							while ((s = reader.readLine()) != null)
								text_Info.append(s + "\n");
							client.close();
						} else {
							System.out
									.println("request incorrect!!!!!!!!!!!!!!!!!!!!");
						}
					} catch (Exception ex) {
						System.out.println("Exception::client:: " + ex);
					}
				}
			}

	/*##################################################################*/
			private void reqFile(String file, long fileSize,
					InputStream inStream) throws IOException {
				
				FileOutputStream fileOut = new FileOutputStream(file);
				byte[] buffer = new byte[200];
				transferred = 0;
				text_Info.setText("\n");
				while (true) {
					int bytes = inStream.read(buffer);
					if (bytes < 0)
						break;
					fileOut.write(buffer, 0, bytes);
					transferred += bytes;
					progressBarDown(fileSize, "  Recived.");
				}
				fileOut.close();
			}
			
	/*##################################################################*/	
			private void upFile(FileInputStream file,long size,OutputStream outStream)
			throws IOException {
			byte[] buffer = new byte[200];
			transferred = 0;
			while (true) {
				int bytes = file.read(buffer);
				if (bytes < 0)
					break;
				outStream.write(buffer, 0, bytes);
				transferred += bytes;
				progressBarUp(size, "  UpLoaded.");
			}
			outStream.flush();
			text_Info.setText("\n\nSuccessFully uploded...");
		}
			
			
	/*##################################################################*/
			public void displayFile(String fname) {
				try {
					BufferedReader file = new BufferedReader(new FileReader(
							fname));
					String s;
					text_Info.setText("\n\n\n");
					while ((s = file.readLine()) != null)
						text_Info.append(s + "");
					file.close();
				} catch (Exception ex) {
					System.out.println("Exception: " + ex);
				}
			}

	/*##################################################################*/		
			private void progressBarDown(long size, String status) {
				percent = (int) (transferred * 100 / size);
				lastPercent = 0;
				if (percent - lastPercent >= 10) {
					lastPercent = percent;
					lblNewLabel.setText(percent + "%  Of  " + status);
				}
			}
	/*##################################################################*/
			private void progressBarUp(Long size, String status) {
				percent = (int) (transferred * 100 / size);
				lastPercent = 0;
				if (percent - lastPercent >= 10) {
					lastPercent = percent;
					lblNewLabel.setText(percent + "%  Of  " + status);
				}
			}

		});
		


		
		
		text_Command.setBounds(70, 2, 302, 23);
		
		Label lblNewLabel_1 = new Label(composite_1, SWT.NONE);
		lblNewLabel_1.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		lblNewLabel_1.setForeground(SWTResourceManager.getColor(0, 51, 153));
		lblNewLabel_1.setBounds(397, 4, 335, 19);
		lblNewLabel_1.setText("      Available command :   reqfile  ,  upfile  ,  lsdir ");
		sashForm_2.setWeights(new int[] {1});
		
		SashForm sashForm_3 = new SashForm(shlSimpleFtpFor, SWT.BORDER);
		sashForm_3.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
		sashForm_3.setForeground(SWTResourceManager.getColor(0, 0, 255));
		sashForm_3.setEnabled(false);
		sashForm_3.setBounds(24, 80, 746, 30);
		
		Composite composite_2 = new Composite(sashForm_3, SWT.NONE);
		composite_2.setToolTipText("Mohammad Anisi");
		
		lblNewLabel = new Label(composite_2, SWT.SHADOW_IN);
		lblNewLabel.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLUE));
		lblNewLabel.setAlignment(SWT.CENTER);
		lblNewLabel.setBounds(10, 4, 726, 22);
		sashForm_3.setWeights(new int[] {1});

		shlSimpleFtpFor.open();
		shlSimpleFtpFor.layout();
		while (!shlSimpleFtpFor.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}
