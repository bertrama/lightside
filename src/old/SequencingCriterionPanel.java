package old;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.model.feature.Feature;

public class SequencingCriterionPanel extends JPanel{

	static Feature a; static Feature b;
	static JTextField displayA = new JTextField();
	static JTextField displayB = new JTextField();

	public static Integer turn = -1;
	public static String direction = "";
	public static String dist = "";
	static final JComboBox turns = new JComboBox(new Integer[]{1,2,3,4,5});
	static final JComboBox setting = new JComboBox(new String[]{"before", "after", "before or after"});
	static final JComboBox distance = new JComboBox(new String[]{"turns", "words"});
	public static void refreshSettings(){
		SequencingCriterionPanel.setTurn((Integer)turns.getSelectedItem());
		SequencingCriterionPanel.setDirection(setting.getSelectedItem().toString());
	}
	
	public SequencingCriterionPanel(Feature a, Feature b){
		this.a = a; this.b = b;
		this.setLayout(new RiverLayout());
		this.add("left", new JLabel("Find each result of this search:"));
		displayA.setText(a.toString());
		displayB.setText(b.toString());
		this.add("br hfill", displayA);
		this.add("br left", new JLabel("That is within "));
		turns.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				SequencingCriterionPanel.setTurn((Integer)turns.getSelectedItem());
			}
		});
		
		this.add("left", turns);
		distance.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				SequencingCriterionPanel.setDistance(distance.getSelectedItem().toString());
			}
		});
//		this.add("left", distance);
		this.add("left", new JLabel("turns"));
		setting.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				SequencingCriterionPanel.setDirection(setting.getSelectedItem().toString());
			}
		});
		this.add("left", setting);
		this.add("br hfill", new JLabel("one or more of the results of this search:"));
		this.add("br hfill", displayB);
		JButton swap = new JButton("swap searches");
		swap.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae){
				SequencingCriterionPanel.swap();
			}
		});
		this.add("br hfill", swap);
	}
	
	public static void setTurn(Integer i){
		turn = i;
	}
	
	public static void setDirection(String i){
		direction = i;
	}
	
	public static void setDistance(String i){
		dist = i;
	}
	
	public static void swap(){
		Feature temp = a;
		a = b;
		b = temp;
		displayA.setText(a.toString());
		displayB.setText(b.toString());
	}
}
