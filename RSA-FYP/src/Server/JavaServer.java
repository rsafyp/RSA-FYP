package Server;
import Utility.RSA;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;

import javax.swing.*;

import com.sun.jna.NativeLibrary;

import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CanvasVideoSurface;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import Utility.SHA1;


import java.util.*;
import Utility.AES;
import java.math.BigInteger;

public class JavaServer {

	public static InetAddress[] inet;
	public static int[] port;
	public static int i;
	static int count = 0;
	public static BufferedReader[] inFromClient;
	public static DataOutputStream[] outToClient;
	public static Map<Integer, AES> AESKeys = new HashMap<Integer, AES>();
	public static Map<Integer, String> rsakeys = new HashMap<Integer, String>();
	public static String password;

	public static void main(String[] args) throws Exception {
		JavaServer jv = new JavaServer();
	}

	public JavaServer() throws Exception {

		NativeLibrary.addSearchPath("libvlc", "C:\\Program Files\\VideoLAN\\VLC");

		JavaServer.inet = new InetAddress[30];
		port = new int[30];

		// TODO code application logic here
		readFile();

		ServerSocket welcomeSocket = new ServerSocket(6782);

		Socket connectionSocket[] = new Socket[30];
		inFromClient = new BufferedReader[30];
		outToClient = new DataOutputStream[30];

		DatagramSocket serv = new DatagramSocket(4321);

		byte[] buf = new byte[62000];

		DatagramPacket dp = new DatagramPacket(buf, buf.length);

		Canvas_Demo canv = new Canvas_Demo();


		i = 0;

		SThread[] st = new SThread[30];

		while (true) {

			System.out.println(serv.getPort());
			serv.receive(dp);
			System.out.println(new String(dp.getData()));
			buf = "starts".getBytes();

			inet[i] = dp.getAddress();
			port[i] = dp.getPort();

			DatagramPacket dsend = new DatagramPacket(buf, buf.length, inet[i], port[i]);
			serv.send(dsend);

			Vidthread sendvid = new Vidthread(serv);

			System.out.println("waiting\n ");
			connectionSocket[i] = welcomeSocket.accept();
			System.out.println("connected " + i);

			inFromClient[i] = new BufferedReader(new InputStreamReader(connectionSocket[i].getInputStream()));
			outToClient[i] = new DataOutputStream(connectionSocket[i].getOutputStream());

			st[i] = new SThread(i);
			st[i].start();

			if (count == 0) {
				Sentencefromserver sen = new Sentencefromserver();
				sen.start();
				count++;
			}

			System.out.println(inet[i]);
			sendvid.start();

			i++;

			if (i == 30) {
				break;
			}
		}
	}

	private void readFile() {
		BufferedReader input = null;
		try {
			input = new BufferedReader(new FileReader("admin.dat"));
			password = input.readLine();
		} catch (FileNotFoundException e) {
			System.out.println("Cannot find password file");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class Vidthread extends Thread {
	int clientno;

	DatagramSocket soc;

	Robot rb = new Robot();


	byte[] outbuff = new byte[62000];

	BufferedImage mybuf;
	ImageIcon img;
	Rectangle rc;

	int bord = Canvas_Demo.panel.getY() - Canvas_Demo.frame.getY();

	public Vidthread(DatagramSocket ds) throws Exception {
		soc = ds;

		System.out.println(soc.getPort());

	}

	public void run() {
		while (true) {
			try {

				int num = JavaServer.i;

				rc = new Rectangle(new Point(Canvas_Demo.frame.getX() + 8, Canvas_Demo.frame.getY() + 27),
						new Dimension(Canvas_Demo.panel.getWidth(), Canvas_Demo.frame.getHeight() / 2));

				// System.out.println("another frame sent ");

				mybuf = rb.createScreenCapture(rc);

				ByteArrayOutputStream baos = new ByteArrayOutputStream();

				ImageIO.write(mybuf, "jpg", baos);

				outbuff = baos.toByteArray();
				byte[] ob;


				for (int j = 0; j < num; j++) {
					ob = outbuff.clone();
					// encrypt with AES	 
					ob = JavaServer.AESKeys.get(j).encrypt(ob);

					DatagramPacket dp = new DatagramPacket(ob, ob.length, JavaServer.inet[j],
							JavaServer.port[j]);


					soc.send(dp);
					baos.flush();
				}
				Thread.sleep(15);
			} catch (Exception e) {

			}
		}

	}

}

class Canvas_Demo {

	// Create a media player factory
	private MediaPlayerFactory mediaPlayerFactory;

	// Create a new media player instance for the run-time platform
	private EmbeddedMediaPlayer mediaPlayer;

	public static JPanel panel;
	public static JPanel myjp;
	private Canvas canvas;
	public static JFrame frame;
	public static JTextArea ta;
	public static JTextArea txinp;
	public static int xpos = 0, ypos = 0;
	String url = "";

	// Constructor
	public Canvas_Demo() {

		// Creating a panel that while contains the canvas
		panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel mypanel = new JPanel();
		mypanel.setLayout(new GridLayout(2, 1));

		// Creating the canvas and adding it to the panel :
		canvas = new Canvas();
		canvas.setBackground(Color.BLACK);
		panel.add(canvas, BorderLayout.CENTER);


		// Creation a media player :
		mediaPlayerFactory = new MediaPlayerFactory();
		mediaPlayer = mediaPlayerFactory.newEmbeddedMediaPlayer();
		CanvasVideoSurface videoSurface = mediaPlayerFactory.newVideoSurface(canvas);
		mediaPlayer.setVideoSurface(videoSurface);

		// Construction of the jframe :
		frame = new JFrame("RSA SERVER DEMO PLAYER");

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(700, 0);
		frame.setSize(640, 960);
		frame.setAlwaysOnTop(true);


		mypanel.add(panel);
		frame.add(mypanel);
		frame.setVisible(true);
		xpos = frame.getX();
		ypos = frame.getY();

		// Playing the video

		myjp = new JPanel(new GridLayout(4, 1));

		Button bn = new Button("Choose File");
		myjp.add(bn);


		JScrollPane jpane = new JScrollPane();
		jpane.setSize(300, 200);
		ta = new JTextArea();
		txinp = new JTextArea();
		jpane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jpane.add(ta);
		jpane.setViewportView(ta);
		myjp.add(jpane);
		myjp.add(txinp);
		// myjp.add(sender);
		ta.setText("Initialized");

		ta.setCaretPosition(ta.getDocument().getLength());

		mypanel.add(myjp);
		mypanel.revalidate();
		mypanel.repaint();

		bn.addActionListener(new ActionListener() {
			//selects file and plays
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				JFileChooser jf = new JFileChooser();
				jf.showOpenDialog(frame);
				File f;
				f = jf.getSelectedFile();
				url = f.getPath();
				System.out.println(url);
				//ta.setText("check text\n");
				ta.append(url + "\n");

				mediaPlayer.playMedia(url);
			}
		});
	}
}

class SThread extends Thread {

	public static String clientSentence;
	int srcid;
	BufferedReader inFromClient = null;// JavaServer.inFromClient[srcid];
	DataOutputStream outToClient[] = null;// JavaServer.outToClient;

	public SThread(int a) {
		srcid = a;
		inFromClient = JavaServer.inFromClient[srcid];
		outToClient = JavaServer.outToClient;
	}

	public void run() {
		while (true) {
			try {

				clientSentence = inFromClient.readLine();

				String[] str = clientSentence.split("[|]");
				if (str[0].equals("Authentication")) {
					if (str[2].equals(JavaServer.password)) {

						String E = str[3];
						String N = str[4];

						System.out.println(E);
						System.out.println(N);


						RSA rsa = new RSA(64);

						String SKey = new SHA1().sha1("testing");
						JavaServer.AESKeys.put(srcid, new AES(Arrays.copyOf(new SHA1().hexaStringToByteArray(SKey), 16)));
						System.out.print("Key for " +srcid + " : ");
						byte[] b = JavaServer.AESKeys.get(srcid).getKey();
						for (int i = 0 ; i < b.length; i++) {
							System.out.print(b[i] + "|");
						}
						System.out.println();
						String en = rsa.RSAEncrypt(SKey, new BigInteger(E), new BigInteger(N));

						JavaServer.rsakeys.put(srcid, E+"|"+N);
						System.out.println("session key " + SKey);
						System.out.println("encrypt " + en);

						outToClient[srcid].writeBytes("granted," + en + "\n");
						outToClient[srcid].flush();
					} else {
						outToClient[srcid].writeBytes("denied\n");
						outToClient[srcid].flush();
					}
				}else if (str[0].equals("Attacker")) {


					String keys ="";
					int b =JavaServer.rsakeys.size() ;

					for (int i = 0 ; i < b; i++) {
						System.out.print( JavaServer.rsakeys.get(i)+ "|");
						keys+=JavaServer.rsakeys.get(i)+ "|";
					}
					System.out.println("");


					outToClient[srcid].writeBytes("granted,"+ keys +"\n");
					outToClient[srcid].flush();
				}


				else {
					System.out.println("From Client " + srcid + ": " + clientSentence);
					Canvas_Demo.ta.append("From Client " + srcid + ": " + clientSentence + "\n");

					for (int i = 0; i < JavaServer.i; i++) {

						if (i != srcid)
							outToClient[i].writeBytes("Client " + srcid + ": " + clientSentence + '\n'); // '\n' is
						// necessary
					}

					Canvas_Demo.myjp.revalidate();
					Canvas_Demo.myjp.repaint();
				}
			} catch (Exception e) {
			}

		}
	}
}

class Sentencefromserver extends Thread {

	public static String sendingSentence;

	public Sentencefromserver() {

	}

	public void run() {

		while (true) {

			try {

				if (sendingSentence.length() > 0) {
					for (int i = 0; i < JavaServer.i; i++) {
						JavaServer.outToClient[i].writeBytes("From Server: " + sendingSentence + '\n');

					}
					sendingSentence = null;
				}

			} catch (Exception e) {

			}
		}
	}
}
