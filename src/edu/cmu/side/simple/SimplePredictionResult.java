package edu.cmu.side.simple;

import javax.swing.*;

import edu.cmu.side.simple.feature.FeatureTable;

import se.datadosen.component.RiverLayout;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

public class SimplePredictionResult {

	private String name;
	private FeatureTable test;
	private boolean[] mask;
	private List<? extends Comparable> predictions;

	public String toString(){
		return name;
	}

	public String getName(){
		return name;
	}

	public void setName(String n){
		name = n;
	}
	
	public SimplePredictionResult(FeatureTable te, boolean[] m, List<? extends Comparable> pred){
		test = te; mask = m; predictions = pred;
	}
	
	public List<? extends Comparable> getPredictions(){
		return predictions;
	}
}
