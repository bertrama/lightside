package edu.cmu.side.genesis.control;

import javax.swing.JProgressBar;

public interface GenesisUpdater {

	public void update(String updateSlot, int slot1, int slot2);

	public void reset();
}

