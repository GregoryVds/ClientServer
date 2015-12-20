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
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

public class Measurement2 {
	static final int MAX_REQUEST_RATE = 100;
	static final int REQUESTS_PER_SAMPLE = 10;
	static final double DIFFICULTY_MEAN = 1000;
	static final double DIFFICULTY_STDEV = 100;
	
	static String FILE_PATH = "input.txt";
	
	static String PLOT1_TITLE 	= "CPUs/Network Load vs Request Rate";
	static String PLOT1_X_AXIS_LABEL 	= "Request Rate";
	static String PLOT1_Y_AXIS_LABEL 	= "Load (%)";
	
	static String PLOT2_TITLE = "Response Time vs Request Rate";
	static String PLOT2_X_AXIS_LABEL 	= "Request Rate";
	static String PLOT2_Y_AXIS_LABEL 	= "Response Time (Ms)";
	
	static NormalDistribution difficultyGenerator;
	
	public static void main(String[] args) throws Exception {
		XYDataset[] datasets = createDataset();
		final JFreeChart chart1 = ChartFactory.createXYLineChart(PLOT1_TITLE, PLOT1_X_AXIS_LABEL, PLOT1_Y_AXIS_LABEL, datasets[0]);
		final JFreeChart chart2 = ChartFactory.createXYLineChart(PLOT2_TITLE, PLOT2_X_AXIS_LABEL, PLOT2_Y_AXIS_LABEL, datasets[1]);
		
		ChartApp.displayChart(chart1, PLOT1_TITLE);
		ChartApp.displayChart(chart2, PLOT2_TITLE);
	}
	
	public static int getRandomDifficulty() {
		if (difficultyGenerator==null) 
			difficultyGenerator = new NormalDistribution(DIFFICULTY_MEAN, DIFFICULTY_STDEV);
		return (int) Math.round(difficultyGenerator.sample());
	}
		
	static XYDataset[] createDataset() throws Exception {
		String input = Lib.stringFromFile(FILE_PATH);
		Client client = new Client();
		
		XYSeries cpusUsage      = new XYSeries("CPUs Usage", false, false);
		XYSeries networkUsage   = new XYSeries("Network Usage", false, false);
		XYSeries avgReponseTime = new XYSeries("Avg Response Time", false, false);
		
		for (int requestRate=1; requestRate <= MAX_REQUEST_RATE; requestRate+=10) {
			System.out.println(String.format("NEW SAMPLE FOR RATE %d (%f)", requestRate, 1.0/requestRate));
			// Instantiate new exponential distribution with current request rate.
			ExponentialDistribution dist = new ExponentialDistribution(1.0/requestRate);
			client.issueStartRecordingRequest();
			long totalReponsesTime = 0;
			
			for (int i=0; i<REQUESTS_PER_SAMPLE; i++) {
				// Issue request.
				ComputationResult res = client.issueComputationRequest(input, getRandomDifficulty());
				
				// Track response time.
				totalReponsesTime+=res.getTotalTime();
				
				// Sleep for random time.
				long sleepTime = (long)(dist.sample()*1000);
				System.out.println("Sleep for: "+sleepTime);
				Thread.sleep(sleepTime);
			}
	
			// Record results.
			RecordingResult res = client.issueStopRecordingRequest();
			cpusUsage.add(requestRate, res.getCpusUsage());
			networkUsage.add(requestRate, res.getNetworkUsage());
			avgReponseTime.add(requestRate, totalReponsesTime/REQUESTS_PER_SAMPLE);
		}
		
        final DefaultTableXYDataset dataset1 = new DefaultTableXYDataset();
		dataset1.addSeries(cpusUsage);
		dataset1.addSeries(networkUsage);
		
		final DefaultTableXYDataset dataset2 = new DefaultTableXYDataset();
		dataset2.addSeries(avgReponseTime);
	
		return new XYDataset[]{dataset1, dataset2};
   }
}