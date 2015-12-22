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
import lib.Lib;

import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

///////////////////////////////////////////////////////////////////////////////////////////////////

public class Measurement2 {
	static final int REQUEST_RATE_INCREMENT = 5;
	static final int MAX_REQUEST_RATE 		= 50;
	static final int REQUESTS_PER_SAMPLE 	= 30;
	static final double DIFFICULTY_MEAN 	= 500000;
	static final double DIFFICULTY_STDEV 	= 1;
	static final boolean GAUSSIAN_DISTRIBUTION_FOR_DIFFICULTY = true;  
	
	static String FILE_PATH 			= "input.txt";
	
	static String PLOT1_TITLE 			= "CPUs/Network Load vs Request Rate";
	static String PLOT1_X_AXIS_LABEL 	= "Request Rate";
	static String PLOT1_Y_AXIS_LABEL 	= "Load (%)";
	
	static String PLOT2_TITLE 			= "Response Time vs Request Rate";
	static String PLOT2_X_AXIS_LABEL 	= "Request Rate";
	static String PLOT2_Y_AXIS_LABEL 	= "Response Time (Ms)";
	
	static NormalDistribution gaussianDifficultyGenerator;
	static ExponentialDistribution expDifficultyGenerator;
	
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
		String input = Lib.stringFromFile(FILE_PATH);
		Client client = new Client();
		
		ExecutorService threadPool 	= Executors.newCachedThreadPool();
		XYSeries cpusUsage     	 	= new XYSeries("CPUs Usage", false, false);
		XYSeries networkUsage   	= new XYSeries("Network Usage", false, false);
		XYSeries avgReponseTime 	= new XYSeries("Avg Response Time", false, false);
		
		for (int requestRate=1; requestRate <= MAX_REQUEST_RATE; requestRate+=REQUEST_RATE_INCREMENT) {
			System.out.println(String.format("NEW SAMPLE FOR RATE %d (%f)", requestRate, 1.0/requestRate));
			// Instantiate new exponential distribution with current request rate.
			ExponentialDistribution dist = new ExponentialDistribution(1.0/requestRate);
			
			// Create array to hold futures.
			@SuppressWarnings("unchecked")
			Future<ComputationResult>[] futures = (Future<ComputationResult>[]) new Future[REQUESTS_PER_SAMPLE];
			
			// Start recording CPU & Network.
			client.issueStartRecordingRequest();
			for (int i=0; i<REQUESTS_PER_SAMPLE; i++) {
				// Prepare Callable.
		        Callable<ComputationResult> asyncRequest = () -> {
		            return client.issueComputationRequest(input, getRandomDifficulty());
		        };
		        
				// Issue request and save future value.
				futures[i] = threadPool.submit(asyncRequest);
				
				// Sleep for random time.
				long sleepTime = (long)(dist.sample()*1000*1000*1000);
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
			networkUsage.add(requestRate, res.getNetworkUsage());
			avgReponseTime.add(requestRate, totalResponseTime/REQUESTS_PER_SAMPLE);
		}
		
		// Shutdown thread pool
		threadPool.shutdown();
		
        final DefaultTableXYDataset dataset1 = new DefaultTableXYDataset();
		dataset1.addSeries(cpusUsage);
		dataset1.addSeries(networkUsage);
		
		final DefaultTableXYDataset dataset2 = new DefaultTableXYDataset();
		dataset2.addSeries(avgReponseTime);
	
		return new XYDataset[]{dataset1, dataset2};
    }
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	// Returns a random number for difficulty (Gaussian distribution).
	private static int getGaussianRandomDifficulty() {
		if (gaussianDifficultyGenerator==null) 
			gaussianDifficultyGenerator = new NormalDistribution(DIFFICULTY_MEAN, DIFFICULTY_STDEV);
		return (int) Math.round(gaussianDifficultyGenerator.sample());
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	// Returns a random number for difficulty (Exponential distribution).
	private static int getExponentialRandomDifficulty() {
		if (expDifficultyGenerator==null)
			expDifficultyGenerator = new ExponentialDistribution(DIFFICULTY_MEAN);
		return (int) Math.round(expDifficultyGenerator.sample());	
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	// Generate random difficulty (either Gaussian Distribution or Exponential Distribution).
	private static int getRandomDifficulty() {
		if (GAUSSIAN_DISTRIBUTION_FOR_DIFFICULTY)
			return getGaussianRandomDifficulty();
		else
			return getExponentialRandomDifficulty();
	}
}