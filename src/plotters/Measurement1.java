/**
 * Measurement1 allows to easily perform the Measurement1 from HW2.
 * 
 * It measures the computation and network times as a function of the request difficulty.
 * Parameters are easily adjustable to automate the generation of the plots.
 * It display a Chart with the results.
 *  
 * @author      Grégory Vander Schueren
 * @author      Jérôme Lemaire
 * @date 		December 24h, 2015
 */

///////////////////////////////////////////////////////////////////////////////////////////////////

package plotters;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

import client.Client;
import client.ComputationResult;
import lib.Lib;

///////////////////////////////////////////////////////////////////////////////////////////////////

public class Measurement1{
	static final int MAX_DIFFICULTY 		= 1000000;
	static final int DIFFICULTY_INCREMENT 	= 1000;
	static final int TRIES_PER_DIFFICULTY   = 10;
	static final String URL					= "http://85.26.33.41:3002";
	
	static String FILE_PATH 	= "input_small.txt";
	
	static String PLOT_TITLE 	= "Time vs Difficulty";
	static String X_AXIS_LABEL 	= "Difficulty (Exponent)";
	static String Y_AXIS_LABEL 	= "Time in ms";
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) throws Exception {
		final JFreeChart chart = ChartFactory.createStackedXYAreaChart(PLOT_TITLE, X_AXIS_LABEL, Y_AXIS_LABEL, createDataset());
		ChartApp.displayChart(chart, PLOT_TITLE);
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	private static DefaultTableXYDataset createDataset() throws Exception {
		String input = Lib.stringFromFile(FILE_PATH);
		Client client = new Client(URL);
		
		XYSeries networkTimeSerie 	  = new XYSeries("Network Time", false, false);
		XYSeries computationTimeSerie = new XYSeries("Computation Time", false, false);
		
		for (int difficulty=1; difficulty <= MAX_DIFFICULTY; difficulty+=DIFFICULTY_INCREMENT) {
			System.out.format("Started difficulty: %d.\n", difficulty);
			long networkTime = 0;
			long computationTime = 0;
					
			for (int i=1; i<TRIES_PER_DIFFICULTY; i++) { // We perform many tries and take the average.
				ComputationResult res = client.issueComputationRequest(input, difficulty);
				networkTime += res.getNetworkTime();
				computationTime += res.getComputationTime();
			}
			
			networkTimeSerie.add(difficulty, networkTime/TRIES_PER_DIFFICULTY);
			computationTimeSerie.add(difficulty, computationTime/TRIES_PER_DIFFICULTY);
		}
		
        final DefaultTableXYDataset dataset = new DefaultTableXYDataset();
		dataset.addSeries(networkTimeSerie);
		dataset.addSeries(computationTimeSerie);
	
		return dataset;
   }
	
	///////////////////////////////////////////////////////////////////////////////////////////////
}