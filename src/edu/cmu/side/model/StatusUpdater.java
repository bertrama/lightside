package edu.cmu.side.model;


public interface StatusUpdater {

	public void update(String updateSlot, int slot1, int slot2);

	public void update(String update);
	public void reset();
}

