package edu.cmu.side.view.util;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.model.StatusUpdater;

public class ParallelTaskUpdater extends JPanel implements StatusUpdater
{	
	public enum Completion
	{
		//WAITING(new Color(255, 248, 224)), STARTED(new Color(255, 224, 100)), PROGRESS(new Color(248, 192, 100)), DONE(new Color(240, 128, 25));
		WAITING(new Color(248, 248, 255)), STARTED(new Color(224, 224, 255)), PROGRESS(new Color(192, 192, 248)), DONE(new Color(128, 128, 240));
		
		Color color;
		Completion(Color c)
		{
			this.color = c;
		}
		
	};
	JLabel textLabel = new JLabel();
	Completion[] completion;
	Canvas canvas;
	
	public ParallelTaskUpdater(int tasks)
	{
		this.setLayout(new RiverLayout());
		//this.add(textLabel, BorderLayout.WEST);
		completion = new Completion[tasks];
		
		canvas = new Canvas()
		{
			@Override
			synchronized public void paint(Graphics g)
			{
				Graphics2D g2 = (Graphics2D)g;
				int numTasks = completion.length;
				int h = this.getHeight();
				int w = this.getWidth();
				
				int rows = (int)Math.max(1, Math.ceil(Math.sqrt(h*numTasks/w)));
				int columns = (int) Math.ceil(1.0*numTasks/rows);
				
				double cellWidth = w/(double)columns;
				double cellHeight= h/(double)rows;
				
				for(int i = 0; i < numTasks; i++)
				{	
					int r = i/rows;
					int c = i%rows;
					Completion state = completion[i];
					
					Color completionColor = state == null ? Color.white : state.color;
					
					Shape rect = new Rectangle2D.Double(r*cellWidth, c*cellHeight, cellWidth, cellHeight);
					
					g2.setColor(completionColor);
					g2.fill(rect);
//					g2.fillRect(r*cellWidth, c*cellHeight, cellWidth, cellHeight);
					if(numTasks < Math.sqrt(getWidth()*getHeight()))
					{
						g2.setColor(Color.WHITE);
						g2.draw(rect);
//						g2.drawRect(r*cellWidth, c*cellHeight, cellWidth, cellHeight);
					}
				}
			}
		};
		textLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		textLabel.setBackground(Color.WHITE);
		canvas.setBackground(Color.WHITE);
		canvas.setSize(100, 20);
		canvas.setMinimumSize(new Dimension(100, 20));
		this.setMinimumSize(new Dimension(200, 20));
		this.setBackground(Color.WHITE);
		this.add("left hfill", textLabel);
		this.add("right", canvas);
	}
	
	synchronized public void updateCompletion(String textSlot, int taskNumber, Completion state) 
	{
		//System.out.println("Completion: " + textSlot+" "+taskNumber + "/"+completion.length+": "+state);
		if(taskNumber < completion.length)
		{
			completion[taskNumber] = state;
			textLabel.setText(textSlot + " " + taskNumber+"/"+completion.length);
			canvas.repaint();
		}
	}
	
	@Override
	public void update(String textSlot, int slot1, int slot2) 
	{
		textLabel.setText(textSlot + " " + slot1 + "/" + slot2);
	}
	
	@Override
	public void update(String text){
		textLabel.setText(text);
	}

	@Override
	synchronized public void reset() {
		textLabel.setText("");
		completion = new Completion[completion.length];
		System.out.println(completion.length+" tasks");
		canvas.repaint();
	}
	
	synchronized public void setTasks(int numTasks)
	{
		textLabel.setText("");
		completion = new Completion[numTasks];
		System.out.println(completion.length+" tasks");
		canvas.repaint();
	}
}
