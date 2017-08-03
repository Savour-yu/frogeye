package GMM;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleToLongFunction;

import Util.GMMUtil;

public class GaussianMM
{
	private int K;
	private ArrayList<Model> models = new ArrayList<Model>();
	// private double defaultAlpha = 0.01;// 默认学习率 default learning rate
	private double diffRatio = 2.5;// 差异率，我们定义这个为论文中的lambda，也就是马氏距离能容忍的范围

	public GaussianMM(ArrayList<ArrayList<Double>> dataset, int K)
	{

		// TODO Auto-generated constructor stub
		this.K = K;
		// GenerateCenters gc = new GenerateCenters(dataset, K);
		// ArrayList<ArrayList<Double>> centers = gc.getCenters();
		// for (ArrayList<Double> item : centers)
		// {
		// mixtureModel.add(new Model(item));
		// }

	}

	/**
	 * 计算测试数据在高斯混合模型中的概率
	 * 
	 * @param test
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

	public void fit(ArrayList<Double> aFrame, double alpha)
	{
		int hit = -1;// 是否拟合混合模型中某个模型，-1则无拟合，否则则为拟合模型序号
		Model.sortModel(models);
		if (Model.getDimension() != aFrame.size())
		{
			System.out.print("the data don't match the dimension");
			System.exit(1);
		}
		for (int i = 0; i < models.size(); i++)
		{
			Model model = models.get(i);
			double MahalanobisDistance = GMMUtil.computeMahalanobisDistance(aFrame, model.getCenter(), model.getCov());// 马氏距离的值
			// 拟合该模型
			if (MahalanobisDistance < diffRatio * diffRatio)
			{
				hit = i;
				break;
			}
		} // 拟合某个模型，则识别为背景，更新所有模型
		if (hit > 0)
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
			if (models.get(models.size() - 1).getRank() > model.getRank())
				models.set(models.size() - 1, model);
		}
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

		List<ArrayList<Double>> dataList = new ArrayList<ArrayList<Double>>();
		try
		{
			br = new BufferedReader(new InputStreamReader(new FileInputStream("D:\\iris.dat")));

			while ((data = br.readLine()) != null)
			{
				// System.out.println(data);
				String[] fields = data.split(",");
				List<Double> tmpList = new ArrayList<Double>();
				for (int i = 0; i < fields.length - 1; i++)
					tmpList.add(Double.parseDouble(fields[i]));
				dataList.add((ArrayList<Double>) tmpList);
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

		GaussianMM gMm = new GaussianMM((ArrayList<ArrayList<Double>>) dataList, 3);
		GenerateCenters gc = new GenerateCenters(dataList, gMm.K);
		ArrayList<ArrayList<Double>> centers = gc.getCenters();
		for (ArrayList<Double> item : centers)
		{

			gMm.models.add(new Model(item));
		}

		System.out.print(gMm.toString());

	}
}
