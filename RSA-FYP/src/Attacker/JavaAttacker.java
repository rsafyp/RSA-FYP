package Attacker;
import Utility.RSA;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;

import Server.JavaServer;
import Utility.SHA1;
import Utility.AES;

public class JavaAttacker {
	public static DatagramSocket ds;
	private LoginView loginView;
	private DataOutputStream outToServer;
	private BufferedReader inFromServer;
	private Socket clientSocket;
	private Vidshow vd;
	public static AES aes;
	private CThread write;
	private CThread read;
	private String ipaddress;
	public JavaAttacker() throws Exception {

		// LOGIN
		loginView = new LoginView(this);
		loginView.showLoginView();



	}

	public void loginViewCallBack (String username, String address) {
		SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
			public Void doInBackground() throws Exception {
				ipaddress= address;



				ds = new DatagramSocket();
				byte[] init = new byte[62000];
				init = "givedata".getBytes();

				InetAddress addr = InetAddress.getByName(ipaddress);

				DatagramPacket dp = new DatagramPacket(init,init.length,addr,4321);

				ds.send(dp);

				DatagramPacket rcv = new DatagramPacket(init, init.length);

				ds.receive(rcv);
				System.out.println(new String(rcv.getData()));

				System.out.println(ds.getPort());


				InetAddress inetAddress = InetAddress.getByName(ipaddress);

				System.out.println(inetAddress);

				clientSocket = new Socket(inetAddress, 6782);
				outToServer =
						new DataOutputStream(clientSocket.getOutputStream());

				inFromServer =
						new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				outToServer.flush();

				RSA rsa = new RSA(64);
				BigInteger[] publicKey = new BigInteger[2];
				BigInteger[] privateKey = new BigInteger[2];
				rsa.getPublicKey(publicKey);
				rsa.getPrivateKey(privateKey);
				String message = "Attacker|" + username + "|" + "|" + publicKey[0] +"|" + publicKey[1] +"\n";

				try {
					outToServer.writeBytes(message);
					outToServer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}

				String result = "";
				try {
					result = inFromServer.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				String[] str = result.split("[,]");
				//get AES or hash
				if (str[0].equals("granted")) {
					

				//	String dec = rsa.RSADecrypt("lala");

				
					
				//	aes = new AES (Arrays.copyOf(new SHA1().hexaStringToByteArray(dec), 16));
					System.out.println(str[1].toString());
					loginView.hideLoginView();
				}
				else {
					throw new Exception("Please try again");
				}

				publish("login fail");

				return null;
			}

			public void done() {
				try {
					get();
					try {
						outToServer.writeBytes("Thanks man\n");
					} catch (IOException e) {
						e.printStackTrace();
					}

					vd.show();

				} catch (ExecutionException e) {
					loginView.showMessage(e.getCause().getMessage());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			protected void process (List<String> chunks) {
				try {
					vd = new Vidshow();
				} catch (Exception e) {
					e.printStackTrace();
				}
				vd.start();
				write = new CThread(inFromServer, outToServer, 0);
				read = new CThread(inFromServer, outToServer, 1);
			}
		};
		worker.execute();
	}

	public static void main(String[] args) throws Exception {
		new JavaAttacker();
	}
}

class Vidshow extends Thread {

	JFrame jf = new JFrame();
	public static JPanel jp = new JPanel(new GridLayout(2,1));
	public static JPanel half = new JPanel(new GridLayout(3,1));
	JLabel jl = new JLabel();
	public static JTextArea ta,tb;

	byte[] rcvbyte = new byte[62000];

	DatagramPacket dp = new DatagramPacket(rcvbyte, rcvbyte.length);
	BufferedImage bf;
	ImageIcon imc;


	public Vidshow() throws Exception {

		jf.setSize(640, 960);
		jf.setTitle("RSA CLIENT");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setAlwaysOnTop(true);
		jf.setLayout(new BorderLayout());

		jp.add(jl);
		jp.add(half);
		jf.add(jp);


		JScrollPane jpane = new JScrollPane();
		jpane.setSize(300, 200);
		ta = new JTextArea();
		tb = new JTextArea();

		jpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jpane.add(ta);
		jpane.setViewportView(ta);
		half.add(jpane);
		half.add(tb);
		ta.setText("Begins\n");

	}

	public void show() {
		jf.setVisible(true);
	}


	@Override
	public void run() {

		try {
			System.out.println("got in");
			do {


				JavaAttacker.ds.receive(dp);
				// decrypt using AES
				byte[] cipher = Arrays.copyOf(rcvbyte, dp.getLength());
				
				cipher = JavaAttacker.aes.decrypt(cipher);


				ByteArrayInputStream bais = new ByteArrayInputStream(cipher);

				bf = ImageIO.read(bais);

				if (bf != null) {

					imc = new ImageIcon(bf);
					jl.setIcon(imc);

					Thread.sleep(15);
				}
				jf.revalidate();
				jf.repaint();


			} while (true);

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("couldnt do it");
		}
	}
}

class CThread extends Thread {

	BufferedReader inFromServer;
	Button sender = new Button("Send Text");
	DataOutputStream outToServer;
	public static String sentence;
	int RW_Flag;

	public CThread(BufferedReader in, DataOutputStream out, int rwFlag) {
		inFromServer = in;
		outToServer = out;
		RW_Flag = rwFlag;
		if(rwFlag == 0)
		{
			Vidshow.half.add(sender);
			sender.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					sentence = Vidshow.tb.getText();
					Vidshow.ta.append("From myself: "+sentence+"\n");
					try{
						outToServer.writeBytes(sentence + '\n');
					}
					catch(Exception E)
					{

					}
					Vidshow.tb.setText(null);
				}
			});
		}
		start();
	}

	public void run() {
		String mysent;
		try {
			while (true) {
				if (RW_Flag == 0) {
					if(sentence.length()>0)
					{

						Vidshow.ta.append(sentence+"\n");
						Vidshow.ta.setCaretPosition(Vidshow.ta.getDocument().getLength());
						Vidshow.half.revalidate();
						Vidshow.half.repaint();
						Vidshow.jp.revalidate();
						Vidshow.jp.repaint();
						outToServer.writeBytes(sentence + '\n');
						sentence = null;
						Vidshow.tb.setText(null);
					}
				} else if (RW_Flag == 1) {
					mysent = inFromServer.readLine();

					Vidshow.ta.append(mysent+"\n");
					Vidshow.ta.setCaretPosition(Vidshow.ta.getDocument().getLength());
					Vidshow.half.revalidate();
					Vidshow.half.repaint();
					Vidshow.jp.revalidate();
					Vidshow.jp.repaint();



					System.out.println("From : " + sentence);
					sentence = null;

				}
			}
		} catch (Exception e) {
		}
	}
}

