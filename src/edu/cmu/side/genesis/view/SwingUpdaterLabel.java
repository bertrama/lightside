package edu.cmu.side.genesis.view;

import javax.swing.JLabel;

import edu.cmu.side.genesis.control.GenesisUpdater;

public class SwingUpdaterLabel extends JLabel implements GenesisUpdater{

	@Override
	public void update(String textSlot, int slot1, int slot2) {
		setText(textSlot + " " + slot1 + "/" + slot2);
	}

}
