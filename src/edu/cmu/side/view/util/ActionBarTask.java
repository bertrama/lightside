package edu.cmu.side.view.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

public abstract class ActionBarTask extends SwingWorker<Void, Void> implements PropertyChangeListener
{
	protected List<JProgressBar> progressBarList;

	ActionBar actionBar;
	ActionListener stopListener ;
	
	public abstract void requestCancel();
	protected abstract void doTask();
	

	public void forceCancel()
	{
		cancel(true);
		finishTask();
	}
	
	protected void finishTask()
	{
		actionBar.update.reset();
		actionBar.progressBar.setVisible(false);
		actionBar.cancel.setEnabled(false);
		actionBar.cancel.removeActionListener(stopListener);

		actionBar.endedTask();
	}
	
	@Override
	public Void doInBackground()
	{	
		beginTask();
		
		doTask();
		
		finishTask();
		return null;
	}

	
	protected void beginTask()
	{
		actionBar.cancel.addActionListener(stopListener);
		actionBar.cancel.setEnabled(true);
		actionBar.progressBar.setVisible(true);

		actionBar.startedTask();
	}

	public ActionBarTask(ActionBar action)
	{
		this.progressBarList = new ArrayList<JProgressBar>();
		this.actionBar = action;
		this.addPropertyChangeListener(this);
		
		progressBarList.add(actionBar.progressBar);
		
		stopListener = new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e)
			{
				requestCancel();
			}};
	}

	
	//FROM OnPanelSwingTask
	public void propertyChange(PropertyChangeEvent evt)
	{
		evt.getSource();
		if ("state" != evt.getPropertyName()) { return; }

		SwingWorker.StateValue stateValue = (SwingWorker.StateValue) evt.getNewValue();
		for (JProgressBar progressBar : progressBarList)
		{
			if (stateValue == SwingWorker.StateValue.DONE)
			{
				progressBar.setIndeterminate(false);
				progressBar.setString(null);
				
			}
			else
			{
				progressBar.setIndeterminate(true);
				progressBar.setString("working...");
			}
		}
	}

	public void addProgressBar(JProgressBar progressBar)
	{
		this.progressBarList.add(progressBar);
	}

	public JProgressBar createProgressBar()
	{
		JProgressBar progressBar = new JProgressBar();
		this.progressBarList.add(progressBar);
		return progressBar;
	}


}