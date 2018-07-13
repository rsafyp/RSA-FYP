package Attacker;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AttackerView {
	private JFrame frame;
	private JPanel panel;
	private JLabel attackerLabel;
	private JLabel addressLabel;
	private JTextField addressField;
	private JButton submit;
	private JavaAttacker attacker;
	
	public AttackerView(JavaAttacker attacker) {
		this.attacker = attacker;
		
		frame = new JFrame("ATTACKER");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(200, 200);
		frame.setAlwaysOnTop(true);

		panel = new JPanel();
		panel.setPreferredSize(new Dimension(350, 150));
		panel.setLayout(null);

		attackerLabel = new JLabel("ATTACKER");
		attackerLabel.setFont(new Font("Verdana", Font.BOLD, 16));
		attackerLabel.setBounds(125, 10, 200, 30);
		panel.add(attackerLabel);

		Font f = new Font("Verdana", Font.PLAIN, 15);

		addressLabel = new JLabel("Server IP Address");
		addressField = new JTextField(30);
		addressField.setFont(f);
		addressLabel.setFont(f);
		addressLabel.setBounds(20, 65, 200, 20);
		addressField.setBounds(170, 65, 170, 20);
		panel.add(addressLabel);
		panel.add(addressField);

		submit = new JButton("Submit");
		submit.setFont(f);
		submit.setBounds(100, 110, 150, 30);
		submit.addActionListener(new SubmitListener());
		panel.add(submit);

		frame.add(panel);
		frame.pack();
	}

	private class SubmitListener implements ActionListener {
		public void actionPerformed (ActionEvent e) {
			String address = addressField.getText();
			boolean error = false;

			if (address.equals("")) {
				JOptionPane.showMessageDialog(null, "Empty IP Address");
				error = true;
			}
			if (!error) {
				attacker.run(address);
			}
		}
	}

	public void showAttackerView() {
		frame.setVisible(true);
	}

	public void hideAttackerView() {
		frame.setVisible(false);
	}

	public void showMessage(String m) {
		JOptionPane.showMessageDialog(null, m);
	}
}
