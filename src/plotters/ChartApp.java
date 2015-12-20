package plotters;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class ChartApp extends ApplicationFrame {
	private static final long serialVersionUID = 1L;

	public ChartApp(JFreeChart chart, String appTitle) {
		super(appTitle);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(675,450));
		setContentPane(chartPanel);
	}
	
	public static void displayChart(JFreeChart chart, String appTitle) {
		ChartApp appFrame = new ChartApp(chart, appTitle);
		appFrame.pack();
		RefineryUtilities.centerFrameOnScreen(appFrame);
		appFrame.setVisible(true);
	}
}