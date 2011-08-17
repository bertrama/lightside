package edu.cmu.side.simple.newui.machinelearning;

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import se.datadosen.component.RiverLayout;

/**
 * This holds all of the panels for analyzing a trained model.
 * @author emayfiel
 *
 */
public class LearningRightPanel extends JPanel {
	private static final long serialVersionUID = 7572152691679941456L;

	private ConfusionMatrixPanel matrixPanel = new ConfusionMatrixPanel();
	private MiniErrorAnalysisPanel errorPanel = new MiniErrorAnalysisPanel();
	private LearningOutputPanel outputPanel = new LearningOutputPanel();
	private DocumentDisplayPanel documentPanel = new DocumentDisplayPanel();

	/**
	 * Everything is in SplitPanes so the user can adjust their relative sizes.
	 */
	public LearningRightPanel(){
		setBorder(null);
		setPreferredSize(new Dimension(625,625));
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		JSplitPane splitTop = new JSplitPane();
		matrixPanel.setPreferredSize(new Dimension(250, 300));
		matrixPanel.setBorder(null);
		errorPanel.setPreferredSize(new Dimension(225, 300));
		errorPanel.setBorder(null);
		splitTop.setLeftComponent(matrixPanel);
		splitTop.setRightComponent(errorPanel);
		splitTop.setBorder(null);
		splitTop.setPreferredSize(new Dimension(600, 325));
		
		JTabbedPane bottomPane = new JTabbedPane();
		bottomPane.addTab("Learning Output", outputPanel);
		bottomPane.addTab("Selected Documents Display", documentPanel);
		bottomPane.setPreferredSize(new Dimension(600, 275));
		split.setTopComponent(splitTop);
		split.setBottomComponent(bottomPane);
		
		setLayout(new RiverLayout());
		add("hfill", split);
	}
	public void refreshPanel(){
		matrixPanel.refreshPanel();
		errorPanel.refreshPanel();
		outputPanel.refreshPanel();
		documentPanel.refreshPanel();
		repaint();
	}
}
