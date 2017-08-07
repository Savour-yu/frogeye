package GMM;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import Util.GMMUtil;
import drawChart.GMMProbabilityChart;

public class GaussianMM
{
	private int K;
	private ArrayList<Model> models = new ArrayList<Model>();
	// private double defaultAlpha = 0.01;// 默认学习率 default learning rate
	private double diffRatio = 2.5;// 差异率，我们定义这个为论文中的lambda，也就是马氏距离能容忍的范围
	private ArrayList<Double> tempVec = new ArrayList<Double>();
	private int dimension = 3;
	private GMMProbabilityChart chart;
	private static Logger gmmLogger = Logger.getLogger("GMMlogger");
	private int countForRecievedVector = 0;

	public GaussianMM(int K, int dimension)
	{

		// TODO Auto-generated constructor stub
		this.K = K;
		this.dimension = dimension;
		chart = new GMMProbabilityChart("GMM Probability for one tag in one dimension", "Probability Distrubutions");

		Random random=new Random();
		ArrayList<ArrayList<Double>> centers=new ArrayList<>();
		for(int i = 0; i < K; i++)
		{
			ArrayList<Double> tmp = new ArrayList<>();
			for(int j=0;j<dimension;j++) {
				tmp.add(random.nextDouble()*10+50);
			}
			centers.add(tmp);
		}
		for (ArrayList<Double> item : centers)
		{
			models.add(new Model(item));
		}
		System.out.println(toString());
	}

	public void repaintChart()
	{
		chart.dispose();
		chart = new GMMProbabilityChart("GMM Probability for one tag in one dimension", "Probability Distrubutions");
		chart.drawGMM(this);
	}

	public int getDimension()
	{
		return dimension;
	}

	public void receiveData(int RSSI, int index)
	{

	}

	/**
	 * 计算测试数据在高斯混合模型中的概率
	 * 
	 * @param test
	 *            待测数据向量
	 * @return
	 */
	private double computeProbability(ArrayList<Double> test)
	{
		double[] probability = new double[K];// 每个模型的概率
		double sum = 0.0;// 概率和
		for (int i = 0; i < K; i++)
		{
			Model model = models.get(i);
			probability[i] = model.getWeight();
			// 计算每个维度的概率，然后相乘
			for (int j = 0; j < Model.getDimension(); j++)
			{
				double var = model.getCov().get(j);
				double diff = test.get(j) - model.getCenter().get(j);
				double diff_2 = diff * diff;
				probability[i] *= Math.exp(-diff_2 / var) / Math.sqrt(2 * Math.PI * var);
			}

			sum += probability[i];
		}
		return sum;

	}

	/**
	 * 计算测试数据在高斯混合模型中的概率,按维度计算
	 * 
	 * @param test
	 *            待测数据
	 * @param dimension
	 *            计算的维度
	 * @return
	 */
	public double computeProbabilityByDimension(double test, int dimension)
	{
		double[] probability = new double[K];// 每个模型的概率
		double sum = 0.0;// 概率和
		for (int i = 0; i < K; i++)
		{
			Model model = models.get(i);
			probability[i] = model.getWeight() / Model.getDimension();
			// 计算每个维度的概率，然后相乘
			double var = model.getCov().get(dimension);
			double diff = test - model.getCenter().get(dimension);
			double diff_2 = diff * diff;
			probability[i] *= Math.exp(-diff_2 / var) / Math.sqrt(2 * Math.PI * var);
			sum += probability[i];
		}
		return sum;

	}

	/**
	 * 
	 * @param aFrame一帧中的一个像素
	 * @param alpha学习率
	 * @return 是否拟合模型
	 */
	public boolean fit(ArrayList<Double> aFrame, double alpha)
	{
		int hit = -1;// 是否拟合混合模型中某个模型，-1则无拟合，否则则为拟合模型序号
		Model.sortModel(models);
		if (Model.getDimension() != aFrame.size())
		{
			System.out.print("1the data don't match the dimension");
			System.exit(1);
		}
		for (int i = 0; i < models.size(); i++)
		{
			Model model = models.get(i);
			double MahalanobisDistance = GMMUtil.computeMahalanobisDistance(aFrame, model.getCenter(), model.getCov());// 马氏距离的值
			// 拟合该模型
			gmmLogger.error("MahalanobisDistance:%f" + MahalanobisDistance);
			if (MahalanobisDistance < diffRatio * diffRatio)
			{
				hit = i;
				break;
			}

		} // 拟合某个模型，则识别为背景，更新所有模型
		gmmLogger.error(String.format("the test data is %s , hit:%d", aFrame.toString(), hit));
		if (hit >= 0)
		{
			// 在更新任意模型前计算概率
			double probability = computeProbability(aFrame);
			double gamma = alpha * probability;
			for (int j = 0; j < models.size(); j++)
			{
				Model model = models.get(j);
				// 未拟合的模型
				if (j != hit)
				{
					double weight = model.getWeight();
					weight = (1 - alpha) * weight;
					model.setWeight(weight);
				}
				// 拟合的模型
				else
				{
					// 更新拟合的模型
					Model hitModel = model;
					// 更新权重
					double weight = hitModel.getWeight();
					weight = (1 - alpha) * weight + alpha;
					hitModel.setWeight(weight);
					// 更新中心向量和协方差
					ArrayList<Double> newCenter = new ArrayList<>(hitModel.getCenter());
					ArrayList<Double> newCov = new ArrayList<>(hitModel.getCov());
					for (int i = 0; i < Model.getDimension(); i++)
					{
						// 更新均值
						double raw1 = hitModel.getCenter().get(i);
						double res1 = (1.0 - gamma) * raw1 + gamma * aFrame.get(i);
						newCenter.set(i, res1);
						// 更新方差
						double raw2 = hitModel.getCov().get(i);
						double res2 = (1.0 - gamma) * raw2 + gamma * (aFrame.get(i) - raw1) * (aFrame.get(i) - raw1);
						newCov.set(i, res2);
					}
					hitModel.setCenter(newCenter);
					hitModel.setCov(newCov);
				}
			}

		}
		// 所有模型都不拟合，识别为前景，更新所有模型，且将排名最后的模型取代
		else
		{
			Model model = new Model(aFrame);
			// 倒数第二个模型的排序大于默认模型，否则一直将weight缩小直到插入的是最小模型
			while (models.get(models.size() - 2).getRank() < model.getRank())
			{
				model.setWeight(model.getWeight() / 2);
			}
			models.set(models.size() - 1, model);
			Model.rejustWeight(models);// 调整权重

		}
		gmmLogger.error(this.toString());
		return (hit >= 0 ? true : false);
	}

	public String toString()
	{
		String result = String.format("GaussianMixtureModel:%d\n", K);
		for (Model model : models)
		{
			result += model.toString();
		}
		return result;
	}

	public static void main(String[] args)
	{
		BufferedReader br;
		String data = null;
		GaussianMM gmm = new GaussianMM(10, 1);
		for (int i = 0; i < gmm.dimension; i++)
		{
			gmm.tempVec.add(0.0);
		}
		List<ArrayList<Double>> initList = new ArrayList<ArrayList<Double>>();
		List<ArrayList<Double>> trainDataList = new ArrayList<ArrayList<Double>>();
		List<ArrayList<Double>> testDataList = new ArrayList<ArrayList<Double>>();
		try
		{
			br = new BufferedReader(new InputStreamReader(new FileInputStream("D:\\Frame.log")));
			int i = 0;
			while ((data = br.readLine()) != null)
			{
				// System.out.println(data);
				String[] fields = data.split(",");
				ArrayList<Double> tmpList = new ArrayList<Double>();

				if (Double.parseDouble(fields[2]) < 1.1)
				{
					tmpList.add(Double.parseDouble(fields[1]));

					if (i < 100)
					{
						initList.add(tmpList);
						trainDataList.add(tmpList);
					} else if (i < 400)
					{
						trainDataList.add(tmpList);
					} else
					{
						trainDataList.add(tmpList);
						testDataList.add(tmpList);
					}
					i++;
				}
			}
			br.close();
		} catch (FileNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// GenerateCenters gc = new GenerateCenters(initList, gmm.K);
		// ArrayList<ArrayList<Double>> centers = gc.getCenters();
		// System.out.println(centers);
		ArrayList<ArrayList<Double>> centers = new ArrayList<>();
		Random random = new Random();
		for (int i = 0; i < gmm.K; i++)
		{
			centers.add(initList.get(random.nextInt(initList.size() - 1)));
		}
		for (ArrayList<Double> item : centers)
		{
			gmm.models.add(new Model(item));
		}
		System.out.println(gmm.toString());
		for (ArrayList<Double> doubles : trainDataList)
		{
			gmm.fit(doubles, 0.1);
		}
		gmm.repaintChart();
		for (ArrayList<Double> doubles : testDataList)
		{
			gmm.fit(doubles, 0.1);

		}
		gmm.repaintChart();

	}
}
