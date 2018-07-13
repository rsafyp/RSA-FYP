package Attacker;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class UpdateAttackView {
	private JFrame frame;
	private JPanel panel;
	private JLabel attackerLabel;
	private JTextArea update;
	private JScrollPane scroll;
	private JButton watchVideo;
	private JavaAttacker attacker;
	
	public UpdateAttackView(JavaAttacker attacker) {
		this.attacker = attacker;
		
		frame = new JFrame("ATTACKER");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(10, 100);
		frame.setAlwaysOnTop(true);

		panel = new JPanel();
		panel.setPreferredSize(new Dimension(640, 530));
		panel.setLayout(null);

		attackerLabel = new JLabel("ATTACKER");
		attackerLabel.setFont(new Font("Verdana", Font.BOLD, 16));
		attackerLabel.setBounds(270, 10, 200, 30);
		panel.add(attackerLabel);

		Font f = new Font("Verdana", Font.PLAIN, 15);

		update = new JTextArea ();
		update.setFont(f);
		update.setEditable(false);
		update.setText("Initialized\n");
		scroll = new JScrollPane (update, 
		   JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(10, 50, 620, 420);
		panel.add(scroll);

		watchVideo = new JButton("Watch Video");
		watchVideo.setBounds(240, 480, 150, 30);
		watchVideo.addActionListener(new WatchVideoListener());
		
		frame.add(panel);
		frame.pack();
	}
	
	private class WatchVideoListener implements ActionListener {
		public void actionPerformed (ActionEvent e) {
			attacker.showVideo();
		}
	}
	
	public void show() {
		frame.setVisible(true);
	}
	
	public void hide() {
		frame.setVisible(false);
	}
	
	public void updateText(String str) {
		update.append(str);
	}
	
	public void updateWatchVideoButton() {
		panel.add(watchVideo);
		frame.revalidate();
		frame.repaint();
	}
	
}
