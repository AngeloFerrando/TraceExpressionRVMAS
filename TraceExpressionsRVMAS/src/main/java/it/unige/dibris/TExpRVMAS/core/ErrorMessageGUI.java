package it.unige.dibris.TExpRVMAS.core;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import it.unige.dibris.TExpRVMAS.core.monitor.Sniffer;
import javax.swing.JButton;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class ErrorMessageGUI extends JFrame {

	private JPanel contentPane;
	private JScrollPane scrollPane;
	private JTextPane textPane;
	private boolean paused;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ErrorMessageGUI frame = new ErrorMessageGUI();
					frame.setVisible(true);
					Monitor m1 = new Sniffer("m1");
					frame.addMessageLog(m1, "ciao");
					Monitor m2 = new Sniffer("m2");
					frame.addMessageLog(m2, "ciao");
					Monitor m3 = new Sniffer("m3");
					frame.addMessageLog(m3, "ciao");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ErrorMessageGUI() {
		setTitle("Monitor Logs GUI");
		setBounds(100, 100, 450, 300);
		setSize(600,400);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
		
		scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 30, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 0, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, 0, SpringLayout.SOUTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, 0, SpringLayout.EAST, contentPane);
		contentPane.add(scrollPane);
		
		textPane = new JTextPane();
		scrollPane.setViewportView(textPane);
		
		JButton btnPause = new JButton("pause");
		btnPause.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnPause.setText(paused ? "pause" : "play");
				paused = !paused;
			}
		});
		btnPause.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		sl_contentPane.putConstraint(SpringLayout.NORTH, btnPause, 0, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, btnPause, 0, SpringLayout.WEST, contentPane);
		contentPane.add(btnPause);

		scrollPane.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {  
	        public void adjustmentValueChanged(AdjustmentEvent e) {  
	        	if(!paused){
	        		e.getAdjustable().setValue(e.getAdjustable().getMaximum());  
	        	}
	        }
	    });
	}
	
	public void addMessageLog(Monitor monitor, String msg){
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if(paused) return;
				StyledDocument doc = textPane.getStyledDocument();
				Style monitorStyle = doc.getStyle(monitor.getMonitorName()+"style");
				
				if(monitorStyle == null){
					Random random = new Random();
					float hue = random.nextFloat();
					float saturation = 1;
					float luminance = 0.9f;
					Color color = Color.getHSBColor(hue, saturation, luminance);
					monitorStyle = textPane.addStyle(monitor.getMonitorName()+"style", null);
			        StyleConstants.setForeground(monitorStyle, color);
				} 
				
		        try { 
					doc.insertString(doc.getLength(), "[" + monitor.getMonitorName() + "]: " + msg + "\n", monitorStyle);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
