package edu.cmu.side.view.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class MemoryMonitorPanel extends JPanel
{
	JLabel textMonitor = new JLabel();
	WarningButton warnButton = new WarningButton();
	JButton bugButton = new JButton("<html><u>Report a Bug</u></html>", new ImageIcon("toolkits/icons/bug.png"));
	JButton garbageButton = new JButton(new ImageIcon("toolkits/icons/bin_closed.png"));
	boolean warned = false;

	public MemoryMonitorPanel()
	{
		JPanel memories = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
		this.setLayout(new BorderLayout(10, 0));
		this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		warnButton.setBorder(BorderFactory.createEmptyBorder());
		garbageButton.setBorder(BorderFactory.createEmptyBorder());
		bugButton.setBorder(BorderFactory.createEmptyBorder(0, 10, 5, 10));
		textMonitor.setBorder(BorderFactory.createEmptyBorder());
		textMonitor.setFont(textMonitor.getFont().deriveFont(10.0f));
		memories.add(warnButton);
		memories.add(textMonitor);
		memories.add(garbageButton);
		this.add(bugButton, BorderLayout.WEST);

		garbageButton.setToolTipText("Force Memory Cleanup");
		garbageButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				int result = JOptionPane.showConfirmDialog(null,
						"Do you want to try forcing memory cleanup?\nThis will freeze LightSide for a while.", "Force Garbage Collection",
						JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION)
				{
					forceCollection();
				}
			}
		});

		bugButton.setBorderPainted(false);
		bugButton.setContentAreaFilled(false);
		bugButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				try
				{
					Desktop.getDesktop().browse(new URI("https://bitbucket.org/lightsidelabs/lightside/issues?status=new&status=open"));
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (URISyntaxException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		this.add(memories, BorderLayout.EAST);
		new Timer().scheduleAtFixedRate(new TimerTask()
		{

			@Override
			public void run()
			{
				double gigs = 1024 * 1024 * 1024;
				MemoryUsage usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();

				double beanMax = usage.getMax() / gigs;
				double beanUsed = usage.getUsed() / gigs;
				double beanCommitted = usage.getCommitted() / gigs;
				//
				String memoryString = String.format("%.1f GB used, %.1f GB max", beanUsed, beanMax);
				textMonitor.setText(memoryString);

				double fractionUsed = beanUsed / (double) beanMax;
				if (fractionUsed >= 0.7)
				{
					textMonitor.setForeground(Color.red.darker());
					warnButton
							.setWarning("<html>You're running out of memory!<br> Delete some old feature tables or models,<br>or give LightSIDE more memory<br>(by editing LightSIDE.bat on Windows, or run.sh on Mac/Linux)</html>");

					if (fractionUsed >= 0.8 && !warned)
					{
						warnButton.doClick();
						warned = true;
					}
				}
				else
				{
					textMonitor.setForeground(Color.black);
					warnButton.clearWarning();
				}

			}

		}, 100, 10000);
	}

	protected void forceCollection()
	{
		double gigs = 1024 * 1024 * 1024;
		MemoryUsage usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();

		double beanMax = usage.getMax() / gigs;
		double beanUsed = usage.getUsed() / gigs;

		System.out.println(String.format("MMP 137: %.1f GB used now... attempting GC", beanUsed, beanMax));

		System.gc();

		usage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();

		beanMax = usage.getMax() / gigs;
		beanUsed = usage.getUsed() / gigs;

		System.out.println(String.format("MMP 146: %.1f GB used after garbage collection.", beanUsed, beanMax));
	}
}
