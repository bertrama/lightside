package edu.cmu.side.simple;

import javax.swing.*;

import se.datadosen.component.RiverLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

public class SimplePredictionResult {

	public static void main(String[] args){
		String[] Listofmonths = new String[]{
				"January","February","March","April","May","June","July","August","September","October",
				"November","December"

		};


		JFrame frame = new JFrame();
		frame.setVisible(true);

		JPanel panel = new JPanel();
		frame.add(panel);

		panel.setLayout(

				new RiverLayout()

				);

		frame.setSize(400,400);

		JPanel monthPanel = new JPanel ();

		frame.setTitle("Calendar");

		JSeparator blank = new JSeparator(SwingConstants.VERTICAL);
		blank.setPreferredSize(new Dimension(25,25));
		JLabel Month = new JLabel ("");
		panel.add(blank);
		panel.add("br center",Month);


		Month.setFont(new Font("Serif" , Font.BOLD, 32));

		JButton monthSelect = new JButton("Change Month");
		panel.add("br",monthSelect);

		monthSelect.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent ae){



					}
		});

	}
}
