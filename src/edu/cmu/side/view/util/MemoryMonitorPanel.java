package edu.cmu.side.view.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import se.datadosen.component.RiverLayout;

public class MemoryMonitorPanel extends JPanel
{
	JLabel textMonitor = new JLabel();
	WarningButton warn = new WarningButton();
	public MemoryMonitorPanel()
	{
		this.setLayout(new FlowLayout(FlowLayout.RIGHT, 10,0));
		this.setBorder(BorderFactory.createEmptyBorder(0,0, 0, 0));
		warn.setBorder(BorderFactory.createEmptyBorder());
		textMonitor.setBorder(BorderFactory.createEmptyBorder());
		textMonitor.setFont(textMonitor.getFont().deriveFont(10.0f));
		this.add(textMonitor);
		this.add(warn);
		new Timer().scheduleAtFixedRate(
				new TimerTask()
				{

					@Override
					public void run()
					{
						double gigs = 1024*1024*1024;
						MemoryUsage usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
						
						double beanMax = usage.getMax()/gigs;
						double beanUsed = usage.getUsed()/gigs;
						double beanCommitted = usage.getCommitted()/gigs;
//						
						textMonitor.setText(String.format("%.1f GB used, %.1f GB max", beanUsed, beanMax));
						
						if(beanUsed/(double)beanMax > 0.75)
						{
							textMonitor.setForeground(Color.red.darker());
							warn.setWarning("<html>You're running out of memory!<br> Delete some old feature tables or models, or give LightSIDE more memory<br>(by editing LightSIDE.bat on Windows, or run.sh on Mac/Linux)</html>" );
						}
						else
						{
							textMonitor.setForeground(Color.black);
							warn.clearWarning();
						}
						
					}
					
				}, 100, 10000);
	}
}
