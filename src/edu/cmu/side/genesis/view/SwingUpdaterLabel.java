package edu.cmu.side.genesis.view;

import javax.swing.JLabel;

import edu.cmu.side.genesis.control.GenesisUpdater;

public class SwingUpdaterLabel extends JLabel implements GenesisUpdater{

	@Override
	public void update(String updateText) {
		setText(updateText);
	}

}
