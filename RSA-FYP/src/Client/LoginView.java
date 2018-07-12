package Client;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginView {
	private JFrame frame;
	private JPanel panel;
	private JLabel loginLabel;
	private JLabel usernameLabel;
	private JTextField usernameField;
	private JLabel passwordLabel;
	private JPasswordField passwordField;
	private JLabel addressLabel;
	private JTextField addressField;
	private JButton submit;
	private JavaClient client;
	
	public LoginView(JavaClient client) {
		this.client = client;
		frame = new JFrame("CLIENT");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(200, 200);
		frame.setAlwaysOnTop(true);
		
		panel = new JPanel();
		panel.setPreferredSize(new Dimension(300, 250));
		panel.setLayout(null);
		
		loginLabel = new JLabel("USER LOGIN");
		loginLabel.setFont(new Font("Verdana", Font.BOLD, 16));
		loginLabel.setBounds(90, 20, 200, 30);
		panel.add(loginLabel);
		
		Font f = new Font("Verdana", Font.PLAIN, 15);
		
		usernameLabel = new JLabel("Username");
		usernameField = new JTextField(30);
		usernameLabel.setFont(f);
		usernameField.setFont(f);
		usernameLabel.setBounds(20, 60, 100, 20);
		usernameField.setBounds(120, 60, 170, 20);
		panel.add(usernameLabel);
		panel.add(usernameField);
		
		passwordLabel = new JLabel("Password");
		passwordField = new JPasswordField(30);
		passwordField.setEchoChar('*');
		passwordField.setFont(f);
		passwordLabel.setFont(f);
		passwordLabel.setBounds(20, 100, 150, 20);
		passwordField.setBounds(120, 100, 170, 20);
		panel.add(passwordLabel);
		panel.add(passwordField);
		
		addressLabel = new JLabel("IP Address");
		addressField = new JTextField(30);
		addressField.setFont(f);
		addressLabel.setFont(f);
		addressLabel.setBounds(20, 135, 200, 20);
		addressField.setBounds(120, 135, 170, 20);
		panel.add(addressLabel);
		panel.add(addressField);
		
		submit = new JButton("Submit");
		submit.setFont(f);
		submit.setBounds(80, 200, 150, 30);
		submit.addActionListener(new SubmitListener());
		panel.add(submit);
		
		frame.add(panel);
		frame.pack();
	}
	
	private class SubmitListener implements ActionListener {
		public void actionPerformed (ActionEvent e) {
			String username = usernameField.getText();
			String password = new String (passwordField.getPassword());
			String address = addressField.getText();
			ArrayList<String> messages = new ArrayList<String>();
			boolean error = false;
			
			if (username.equals("")) {
				messages.add("Empty username");
				error = true;
			} 
			if (password.equals("")) {
				messages.add("Empty password");
				error = true;
			}
			if (password.equals("")) {
				messages.add("Empty password");
				error = true;
			}
			if (address.equals("")) {
				messages.add("Empty IP Address");
				error = true;
			}
			if (error) {
				String errorMessage = "";
				for (String m: messages)
					errorMessage = errorMessage + m + "\n";
				
				JOptionPane.showMessageDialog(null, errorMessage);
			}
			else {
				client.loginViewCallBack(username, password,address);
			}
		}
	}
	
	public void showLoginView() {
		frame.setVisible(true);
	}
	
	public void hideLoginView() {
		frame.setVisible(false);
	}
	
	public void showMessage(String m) {
		JOptionPane.showMessageDialog(null, m);
	}
}
