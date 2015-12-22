/**
 * Measurement2 acts a load generator to perform various measurements required by HW2.
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
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;

import client.Client;
import client.ComputationResult;
import client.RecordingResult;
import lib.Constants;
import lib.Lib;

import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.distribution.ExponentialDistribution;

///////////////////////////////////////////////////////////////////////////////////////////////////

public class Measurement2 {		
	// SETUP MEASUREMENT 2.A - Non random, verification of hypothesis - 1 CPU - No Cache 	
	/*
	static final int MIN_REQUEST_RATE 			= 1;
	static final int MAX_REQUEST_RATE 			= 50;
	static final int REQUEST_RATE_INCREMENT 	= 1;
	static final int MATRIX_SIZE 				= 4;
	static final int REQUESTS_PER_SAMPLE 		= 150;
	static final boolean USE_RANDOM_SLEEP_TIME 	= false;
	static final boolean USE_RANDOM_DIFFICULTY 	= false;
	static final double DIFFICULTY_MEAN 		= 100000;
	static String PLOT1_TITLE 			= "CPUs Load vs Request Rate";
	static String PLOT1_X_AXIS_LABEL 	= "Request Rate";
	static String PLOT1_Y_AXIS_LABEL 	= "Load (%)";
	static String PLOT2_TITLE 			= "Response Time vs Request Rate";
	static String PLOT2_X_AXIS_LABEL 	= "Request Rate";
	static String PLOT2_Y_AXIS_LABEL 	= "Response Time (ms)";
	*/
	
	// SETUP MEASUREMENT 2.B - Plots for Measurement 2 - 1 CPU - No Cache
	/*
	static final int MIN_REQUEST_RATE 			= 1;
	static final int MAX_REQUEST_RATE 			= 50;
	static final int REQUEST_RATE_INCREMENT 	= 1;
	static final int MATRIX_SIZE 				= 4;
	static final int REQUESTS_PER_SAMPLE 		= 150;
	static final boolean USE_RANDOM_SLEEP_TIME 	= true;
	static final boolean USE_RANDOM_DIFFICULTY 	= true;
	static final double DIFFICULTY_MEAN 		= 100000;
	static String PLOT1_TITLE 			= "CPUs Load vs Request Rate";
	static String PLOT1_X_AXIS_LABEL 	= "Mean Request Rate (exponential distribution)";
	static String PLOT1_Y_AXIS_LABEL 	= "Load (%)";
	static String PLOT2_TITLE 			= "Response Time vs Request Rate";
	static String PLOT2_X_AXIS_LABEL 	= "Mean Request Rate (exponential distribution)";
	static String PLOT2_Y_AXIS_LABEL 	= "Response Time (ms)";
	*/	
	
	// SETUP MEASUREMENT 2.C - Compare with model - 1 CPU - No Cache
	static final int MIN_REQUEST_RATE 			= 1;
	static final int MAX_REQUEST_RATE 			= 25;
	static final int REQUEST_RATE_INCREMENT 	= 1;
	static final int MATRIX_SIZE 				= 4;
	static final int REQUESTS_PER_SAMPLE 		= 150;
	static final boolean USE_RANDOM_SLEEP_TIME 	= true;
	static final boolean USE_RANDOM_DIFFICULTY 	= true;
	static final double DIFFICULTY_MEAN 		= 50000;
	static String PLOT1_TITLE 			= "CPUs Load vs Request Rate";
	static String PLOT1_X_AXIS_LABEL 	= "Mean Request Rate (exponential distribution)";
	static String PLOT1_Y_AXIS_LABEL 	= "Load (%)";
	static String PLOT2_TITLE 			= "Response Time vs Request Rate";
	static String PLOT2_X_AXIS_LABEL 	= "Mean Request Rate (exponential distribution)";
	static String PLOT2_Y_AXIS_LABEL 	= "Response Time (ms)";

	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) throws Exception {
		XYDataset[] datasets = createDataset();
		final JFreeChart chart1 = ChartFactory.createXYLineChart(PLOT1_TITLE, PLOT1_X_AXIS_LABEL, PLOT1_Y_AXIS_LABEL, datasets[0]);
		final JFreeChart chart2 = ChartFactory.createXYLineChart(PLOT2_TITLE, PLOT2_X_AXIS_LABEL, PLOT2_Y_AXIS_LABEL, datasets[1]);
		
		ChartApp.displayChart(chart1, PLOT1_TITLE);
		ChartApp.displayChart(chart2, PLOT2_TITLE);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	
	private static XYDataset[] createDataset() throws Exception {
		String input = Lib.generateSquareMatrix(MATRIX_SIZE);
		Client client = new Client(Constants.URL);
		
		ExecutorService threadPool 	= Executors.newCachedThreadPool();
		XYSeries cpusUsage     	 	= new XYSeries("CPUs Load", false, false);
		XYSeries avgReponseTime 	= new XYSeries("Avg. Response Time", false, false);
		XYSeries modelReponseTime	= new XYSeries("Model Response Time Prediction", false, false);
		
		for (int requestRate=MIN_REQUEST_RATE; requestRate <= MAX_REQUEST_RATE; requestRate+=REQUEST_RATE_INCREMENT) {
			System.out.println(String.format("Starte new sample for request rate of %d (Sleep avg: %f)", requestRate, 1.0/requestRate));
			// Instantiate new exponential distribution with current request rate.
			ExponentialDistribution randomInterRequest 	= new ExponentialDistribution(1.0/requestRate);
			ExponentialDistribution randomDifficulty 	= new ExponentialDistribution(DIFFICULTY_MEAN);

			// Create array to hold futures.
			@SuppressWarnings("unchecked")
			Future<ComputationResult>[] futures = (Future<ComputationResult>[]) new Future[REQUESTS_PER_SAMPLE];
			
			// Start recording CPU & Network.
			client.issueStartRecordingRequest();
			for (int i=0; i<REQUESTS_PER_SAMPLE; i++) {
				// Prepare Callable.
		        Callable<ComputationResult> asyncRequest = () -> {
		            return client.issueComputationRequest(input, USE_RANDOM_DIFFICULTY ? (int)randomDifficulty.sample() : (int)DIFFICULTY_MEAN);
		        };
		        
				// Issue request and save future value.
				futures[i] = threadPool.submit(asyncRequest);

				long sleepTime = USE_RANDOM_SLEEP_TIME ? (long)(randomInterRequest.sample()*1000*1000*1000) : (long) (1.0/requestRate*1000*1000*1000);
				long start = System.nanoTime();
				System.out.println("Sleep for: "+sleepTime);
				while(start + sleepTime >= System.nanoTime());
			}
			
			// Compute average request time. 
			long totalResponseTime = 0;
			for (Future<ComputationResult> future : futures)
				totalResponseTime+=future.get().getTotalTime();
			
			// Record results.
			RecordingResult res = client.issueStopRecordingRequest();
			cpusUsage.add(requestRate, res.getCpusUsage());
			avgReponseTime.add(requestRate, totalResponseTime/REQUESTS_PER_SAMPLE);
			if (requestRate < 23) // TODO
				modelReponseTime.add(requestRate, model(requestRate));
		}
		
		// Shutdown thread pool
		threadPool.shutdown();
		
        final DefaultTableXYDataset dataset1 = new DefaultTableXYDataset();
		dataset1.addSeries(cpusUsage);
		// dataset1.addSeries(networkUsage);
		
		final DefaultTableXYDataset dataset2 = new DefaultTableXYDataset();
		dataset2.addSeries(avgReponseTime);
		dataset2.addSeries(modelReponseTime);
	
		return new XYDataset[]{dataset1, dataset2};
    }
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public static double model(int requestRate) {
		double responseTime = (1/((1/(Constants.AVG_COMPUT_TIME_4x4_50000/1000.0))-requestRate));
		return Constants.NETWORK_LATENCY+1000*responseTime;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
}