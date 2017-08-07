package drawChart;

import org.jfree.chart.ChartPanel;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.awt.Color;
import java.awt.BasicStroke;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.xy.XYSeries;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

import GMM.GaussianMM;

import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

public class GMMProbabilityChart extends ApplicationFrame
{

	private XYSeriesCollection dataset = new XYSeriesCollection();
	private ArrayList<Integer> AntennaIDs = new ArrayList<>();
	private ArrayList<Integer> counts = new ArrayList<>();
	private Date startTime;
	private boolean startFlag = false;

	public GMMProbabilityChart(String applicationTitle, String chartTitle)
	{
		super(applicationTitle);
		JFreeChart xylineChart = ChartFactory.createXYLineChart(chartTitle, "RSSI", "Frequency", dataset,
				PlotOrientation.VERTICAL, true, true, false);

		ChartPanel chartPanel = new ChartPanel(xylineChart);
		chartPanel.setPreferredSize(new java.awt.Dimension(1920, 1080));
		final XYPlot plot = xylineChart.getXYPlot();
		plot.setBackgroundPaint(Color.WHITE);
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setSeriesPaint(1, Color.GREEN);
		renderer.setSeriesPaint(2, Color.BLUE);
		renderer.setSeriesPaint(4, Color.YELLOW);
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesStroke(1, new BasicStroke(2.0f));
		renderer.setSeriesStroke(2, new BasicStroke(2.0f));
		renderer.setSeriesStroke(4, new BasicStroke(2.0f));
		plot.setRenderer(renderer);
		setContentPane(chartPanel);

		NumberAxis numAxis = (NumberAxis) plot.getRangeAxis();
		// numAxis.setAutoRange(false);
		numAxis.setAutoRangeIncludesZero(false);
		numAxis.setNegativeArrowVisible(false);
		start();
	}

	public void receiveData(double rssi, double probability, int antennaID)
	{

		if (startFlag == false)
		{
			startTime = new Date();
		}
		Date current = new Date();

		startFlag = true;
		if (!(AntennaIDs.contains(antennaID)))
		{
			AntennaIDs.add(antennaID);
			int index = AntennaIDs.indexOf(antennaID);
			counts.add(index, 0);
			XYSeries nSeries = new XYSeries("Ant " + antennaID);
			// nSeries.setMaximumItemCount(100);// 每一个轴只显示一百个点
			dataset.addSeries(nSeries);

		}
		int index = AntennaIDs.indexOf(antennaID);
		counts.set(index, counts.get(index) + 1);
		// rssi += (index) * 30;//将不同的天线的值划分在不同的范围，便于观察
		dataset.getSeries(index).add(rssi, probability);
		// System.out.println(
		// "rssi:" + rssi + "\tant:" + antennaID + "\ttime:" + (current.getTime() -
		// startTime.getTime()) / 100);
		// // if (current.getTime() - startTime.getTime() > 20000)
		// {
		// }
	}

	public void drawGMM(GaussianMM model)
	{
		double start = -90.0;
		double end = -20;
		double slice = 1;

		for (double i = start; i < end; i += slice)
		{
			for (int d = 0; d < model.getDimension(); d++)
			{
				double probability = model.computeProbabilityByDimension(i, d);
				receiveData(i, probability, d);
			}
		}
	}

	public void start()
	{
		pack();
		RefineryUtilities.centerFrameOnScreen(this);
		setVisible(true);
	}

	public static void main(String[] args)
	{
		GMMProbabilityChart chart = new GMMProbabilityChart("GMM Probability for one tag in one dimension",
				"Probability Distrubutions");

		Random random = new Random();
		while (true)
		{
			try
			{
				Thread.sleep(10);
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int rssi = random.nextInt(60) + 30;
			int ant = random.nextInt(3);

		}
	}

}
