package plotters;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

import client.Client;
import client.ComputationResult;
import lib.Lib;

public class Measurement1{
	static final int MAX_DIFFICULTY = 1000000;
	static String FILE_PATH 	= "input.txt";
	
	static String PLOT_TITLE 	= "Time vs Difficulty";
	static String X_AXIS_LABEL 	= "Difficulty";
	static String Y_AXIS_LABEL 	= "Time in ms";
				
	public static void main(String[] args) throws Exception {
		final JFreeChart chart = ChartFactory.createStackedXYAreaChart(PLOT_TITLE, X_AXIS_LABEL, Y_AXIS_LABEL, createDataset());
		ChartApp.displayChart(chart, PLOT_TITLE);
	}
	
	static DefaultTableXYDataset createDataset() throws Exception {
		String input = Lib.stringFromFile(FILE_PATH);
		Client client = new Client();
		
		XYSeries networkTimeSerie 	  = new XYSeries("Network Time", false, false);
		XYSeries computationTimeSerie = new XYSeries("Computation Time", false, false);
		
		for (int i=1; i <= MAX_DIFFICULTY; i+=1000) {
			ComputationResult res = client.issueComputationRequest(input, i);
			networkTimeSerie.add(i, res.getNetworkTime());
			computationTimeSerie.add(i, res.getComputationTime());
		}
		
        final DefaultTableXYDataset dataset = new DefaultTableXYDataset();
		dataset.addSeries(networkTimeSerie);
		dataset.addSeries(computationTimeSerie);
	
		return dataset;
   }
}