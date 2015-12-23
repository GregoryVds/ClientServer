/**
 * Measurement4 acts a load generator to perform various measurements required by HW2.
 * 
 * Specifically, we will measure the performance depending on the number of server threads.
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

public class Measurement4 {
	static Client client;
	static ExecutorService threadPool;
	static String input;
	
	// SETUP MEASUREMENT 4.A - Response Time with multiple number of threads. 
	static final int MAX_THREADS 				= 4;
	static final int MIN_REQUEST_RATE 			= 10;
	static final int MAX_REQUEST_RATE 			= 80;
	static final int REQUEST_RATE_INCREMENT 	= 5;
	static final int MATRIX_SIZE 				= 4;
	static final int REQUESTS_PER_SAMPLE 		= 100;
	static final boolean USE_RANDOM_SLEEP_TIME 	= true;
	static final boolean USE_RANDOM_DIFFICULTY 	= true;
	static final double DIFFICULTY_MEAN 		= 50000;
	static String PLOT1_TITLE 			= "Average response time";
	static String PLOT1_X_AXIS_LABEL 	= "Mean Request Rate (exponential distribution)";
	static String PLOT1_Y_AXIS_LABEL 	= "Response Time (ms)";
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) throws Exception {
		client = new Client(Constants.URL);
		threadPool = Executors.newCachedThreadPool();
		input = Lib.generateSquareMatrix(MATRIX_SIZE);		
		XYDataset[] datasets = createDataset();
		final JFreeChart chartResponseTimes = ChartFactory.createXYLineChart(PLOT1_TITLE, PLOT1_X_AXIS_LABEL, PLOT1_Y_AXIS_LABEL, datasets[0]);
		final JFreeChart chartCpusUsages 	= ChartFactory.createXYLineChart(PLOT1_TITLE, PLOT1_X_AXIS_LABEL, PLOT1_Y_AXIS_LABEL, datasets[1]);
		threadPool.shutdown();
		
		ChartApp.displayChart(chartResponseTimes, PLOT1_TITLE);
		ChartApp.displayChart(chartCpusUsages, PLOT1_TITLE);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	
	private static XYDataset[] createDataset() throws Exception {		
		XYSeries[] avgResponseTime = new XYSeries[MAX_THREADS+1];
		XYSeries[] avgReponseModel = new XYSeries[MAX_THREADS+1];
		XYSeries[] cpuUsage		   = new XYSeries[MAX_THREADS+1];
		
		for (int i=1; i<=MAX_THREADS; i++) {
			avgResponseTime[i] 	= new XYSeries(String.format("%d threads",i), false, false);
			avgReponseModel[i] 	= new XYSeries(String.format("%d threads",i), false, false);
			cpuUsage[i]			= new XYSeries(String.format("%d threads",i), false, false);
		}

		for (int i=1; i<=MAX_THREADS; i++) {
			client.issueSetThreadsCountRequest(i);
			simulate(i, i*23, avgResponseTime[i], avgReponseModel[i], cpuUsage[i]);
		}
	
		// Create dataset
        final DefaultTableXYDataset avgResponseTimeDataset = new DefaultTableXYDataset();
        for (int i=1; i<=MAX_THREADS; i++) {
        	avgResponseTimeDataset.addSeries(avgResponseTime[i]);
        	avgResponseTimeDataset.addSeries(avgReponseModel[i]);
        }
        
        final DefaultTableXYDataset cpuUsageDataSet = new DefaultTableXYDataset();
        for (int i=1; i<=MAX_THREADS; i++) 
        	cpuUsageDataSet.addSeries(cpuUsage[i]);
        		
		return new XYDataset[]{avgResponseTimeDataset, cpuUsageDataSet};
    }
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
	private static void simulate(int threadsCount, int maxRequestRate, XYSeries responseTime, XYSeries responseTimeModel, XYSeries cpusUsage) throws Exception {		
		for (int requestRate=MIN_REQUEST_RATE; requestRate <= maxRequestRate; requestRate+=REQUEST_RATE_INCREMENT) {
			client.issueStartRecordingRequest();
			System.out.println(String.format("Starte new sample for request rate of %d (Sleep avg: %f)", requestRate, 1.0/requestRate));
			// Instantiate new exponential distribution with current request rate.
			ExponentialDistribution randomInterRequest 	= new ExponentialDistribution(1.0/requestRate);
			ExponentialDistribution randomDifficulty 	= new ExponentialDistribution(DIFFICULTY_MEAN);

			// Create array to hold futures.
			@SuppressWarnings("unchecked")
			Future<ComputationResult>[] futures = (Future<ComputationResult>[]) new Future[REQUESTS_PER_SAMPLE];
			
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
			
			// Record results.
			responseTime.add(requestRate, totalResponseTime/REQUESTS_PER_SAMPLE);
			cpusUsage.add(requestRate, res.getCpusUsage());
			
			System.out.format("Model for rate %d and threads %d: %f\n", requestRate, threadsCount, model(requestRate, threadsCount));
			System.out.format("Add: (%d,%f)", requestRate, model(requestRate, threadsCount)); 
			responseTimeModel.add(requestRate, model(requestRate, threadsCount));
		}
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	
    public static double factorial(double n) {
        double fact = 1;
        for (int i = 1; i <= n; i++) 
            fact *= i;
        return fact;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    public static double sum(int m, double a) {
    	double sum = 0;
    	for (int i=0; i<m; i++) 
    		sum+=(Math.pow(a,i)/factorial(i));
    	return sum;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
	public static double model(int requestRate, int threadsCount) {
		int m 	  	  = threadsCount;
		double lambda = requestRate; 
		double mu 	  = 1/(Constants.AVG_COMPUT_TIME_4x4_50000/1000.0);
		double a	  = lambda/mu;
		double xi	  = a/m;
		
		double pi_zero_term1 		= sum(m, a);
		double pi_zero_term2_num 	= Math.pow(a,m);
		double pi_zero_term2_dem 	= factorial(m)*(1-xi);
		double pi_zero 				= 1/(pi_zero_term1 + (pi_zero_term2_num/pi_zero_term2_dem));

		double term2_num = xi*Math.pow(a,m)*pi_zero;
		double term2_den = Math.pow(1-xi,2)*factorial(m);
		double sum		 = a+(term2_num/term2_den);
		double esp       = (1/lambda)*sum; 
		
		if (requestRate > mu*threadsCount) esp = 3; // Model becomes instable.
		double estimate  = Constants.NETWORK_LATENCY+esp*1000;
		return estimate;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
}