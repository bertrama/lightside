package old;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import se.datadosen.component.RiverLayout;
import edu.cmu.side.model.feature.Feature;

/**
 * Popup menu for converting numeric features to boolean based on a threshold.
 * @author emayfiel
 *
 */
public class InequalityCriterionPanel extends JPanel{
	private static final long serialVersionUID = -7758759064879102529L;
	static Feature highlight;
	static JComboBox ineqBox;
	static JTextField comparisonValue;
	public InequalityCriterionPanel(Feature h){
		highlight = h;
		ineqBox = new JComboBox(new String[]{">",">=", "=", "<=", "<"});
		ineqBox.setSelectedIndex(0);
		comparisonValue = new JTextField(6);
		comparisonValue.setText("0");
		setLayout(new RiverLayout());
		add("right", new JLabel(highlight.getFeatureName()));
		add("right", ineqBox);
		add("right", comparisonValue);
	}
	
	public double getComparisonValue(){
		return Double.parseDouble(comparisonValue.getText());
	}
	
	public String getInequality(){
		return ineqBox.getSelectedItem().toString();
	}
	
	public Feature getHighlightedFeature(){
		return highlight;
	}
	
	public void refreshSettings(){
		ineqBox.setSelectedIndex(0);
		comparisonValue.setText("0");
	}
}
