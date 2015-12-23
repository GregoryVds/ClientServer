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
import lib.Constants;
import lib.Lib;

///////////////////////////////////////////////////////////////////////////////////////////////////

public class Measurement1{
	// SETUP MEASUREMENT 1-A - Varies Exp
	/*
	static final int MIN_DIFFICULTY			= 1;
	static final int MAX_DIFFICULTY 		= 1000000;
	static final int DIFFICULTY_INCREMENT 	= 1000;
	static final int TRIES_PER_DIFFICULTY   = 10;
	static final boolean VARIES_EXPONENT  	= true;
	static final int FIXED_PARAMETER		= 4;
	static String PLOT_TITLE 	= "Time vs Difficulty";
	static String X_AXIS_LABEL 	= "Difficulty (Exponent)";
	static String Y_AXIS_LABEL 	= "Time in ms";
	
	*/
	
	// SETUP MEASUREMENT 1-B - Varies Size
	/*
	static final int MIN_DIFFICULTY			= 1;
	static final int MAX_DIFFICULTY 		= 151;
	static final int DIFFICULTY_INCREMENT 	= 1;
	static final int TRIES_PER_DIFFICULTY   = 10;
	static final boolean VARIES_EXPONENT  	= false;
	static final int FIXED_PARAMETER 		= 2;
	static String PLOT_TITLE 	= "Time vs Difficulty";
	static String X_AXIS_LABEL 	= "Difficulty (Matrix Size)";
	static String Y_AXIS_LABEL 	= "Time in ms";
	*/
	
	static final int MIN_DIFFICULTY			= 200000;
	static final int MAX_DIFFICULTY 		= 200000;
	static final int DIFFICULTY_INCREMENT 	= 100;
	static final int TRIES_PER_DIFFICULTY   = 30;
	static final boolean VARIES_EXPONENT  	= true;
	static final int FIXED_PARAMETER		= 4;
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
		Client client = new Client(Constants.URL);
		XYSeries networkTimeSerie 	  = new XYSeries("Network Time", false, false);
		XYSeries computationTimeSerie = new XYSeries("Computation Time", false, false);
		
		for (int difficulty=MIN_DIFFICULTY; difficulty <= MAX_DIFFICULTY; difficulty+=DIFFICULTY_INCREMENT) {
			System.out.format("Started difficulty: %d.\n", difficulty);
			
			// Create input.
			String input = VARIES_EXPONENT ? Lib.generateSquareMatrix(FIXED_PARAMETER) : Lib.generateSquareMatrix(difficulty);

			long networkTime 	 = 0;
			long computationTime = 0;
					
			// We perform several tries and take the average to limit volatility of results.
			for (int i=0; i<TRIES_PER_DIFFICULTY; i++) {
				ComputationResult res = VARIES_EXPONENT ? client.issueComputationRequest(input, difficulty) : client.issueComputationRequest(input, FIXED_PARAMETER);
				
				networkTime += res.getNetworkTime();
				computationTime += res.getComputationTime();
				System.out.println(res.getComputationTime());
			}
			
			networkTimeSerie.add(difficulty, networkTime/TRIES_PER_DIFFICULTY);
			computationTimeSerie.add(difficulty, computationTime/TRIES_PER_DIFFICULTY);
			System.out.format("Mean: %f", ((float)networkTime)/TRIES_PER_DIFFICULTY);
		}	
	
        final DefaultTableXYDataset dataset = new DefaultTableXYDataset();
		dataset.addSeries(networkTimeSerie);
		dataset.addSeries(computationTimeSerie);
	
		return dataset;
   }
	
	///////////////////////////////////////////////////////////////////////////////////////////////
}