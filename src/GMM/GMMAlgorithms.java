package GMM;

import java.util.ArrayList;
import java.util.List;

import Util.GMMUtil;

public class GMMAlgorithms
{

	/**
	 * GMM聚类算法实现
	 * @param dataSet 数据集
	 * @param pCentricPoint 中心节点集
	 * @param dataNum 数据集长度
	 * @param k 模型数量
	 * @param dataDimen 数据维度
	 * @return  每条数据(n)的类别(0~k-1)
	 */
	public int[] GMMCluster(ArrayList<ArrayList<Double>> dataSet, ArrayList<ArrayList<Double>> pCentricPoint, int dataNum, int k,
			int dataDimen)
	{
		Parameter parameter = iniParameters(dataSet, dataNum, k, dataDimen);
		double Lpre = -1000000; // 上一次聚类的误差
		double threshold = 0.0001;
		while (true)
		{
			ArrayList<ArrayList<Double>> px = computeProbablity(dataSet, pCentricPoint, dataNum, k, dataDimen);
			double[][] pGama = new double[dataNum][k];
			for (int i = 0; i < dataNum; i++)
			{
				for (int j = 0; j < k; j++)
				{
					pGama[i][j] = px.get(i).get(j) * parameter.getpWeights().get(j);
				}
			}

			double[] sumPGama = GMMUtil.matrixSum(pGama, 2);
			for (int i = 0; i < dataNum; i++)
			{
				for (int j = 0; j < k; j++)
				{
					pGama[i][j] = pGama[i][j] / sumPGama[i];
				}
			}

			double[] NK = GMMUtil.matrixSum(pGama, 1); // 第k个高斯生成每个样本的概率的和，所有Nk的总和为N

			// 更新pMiu
			double[] NKReciprocal = new double[NK.length];
			for (int i = 0; i < NK.length; i++)
			{
				NKReciprocal[i] = 1 / NK[i];
			}
			double[][] pMiuTmp = GMMUtil.matrixMultiply(
					GMMUtil.matrixMultiply(GMMUtil.diag(NKReciprocal), GMMUtil.matrixReverse(pGama)),
					GMMUtil.toArray(dataSet));

			// 更新pPie
			double[][] pPie = new double[k][1];
			for (int i = 0; i < NK.length; i++)
			{
				pPie[i][1] = NK[i] / dataNum;
			}

			// 更新k个pSigma
			double[][][] pSigmaTmp = new double[dataDimen][dataDimen][k];
			for (int i = 0; i < k; i++)
			{
				double[][] xShift = new double[dataNum][dataDimen];
				for (int j = 0; j < dataNum; j++)
				{
					for (int l = 0; l < dataDimen; l++)
					{
						xShift[j][l] = pMiuTmp[i][l];
					}
				}

				double[] pGamaK = new double[dataNum]; // 第k条pGama值
				for (int j = 0; j < dataNum; j++)
				{
					pGamaK[j] = pGama[j][i];
				}
				double[][] diagPGamaK = GMMUtil.diag(pGamaK);

				double[][] pSigmaK = GMMUtil.matrixMultiply(GMMUtil.matrixReverse(xShift),
						(GMMUtil.matrixMultiply(diagPGamaK, xShift)));
				for (int j = 0; j < dataDimen; j++)
				{
					for (int l = 0; l < dataDimen; l++)
					{
						pSigmaTmp[j][l][k] = pSigmaK[j][l] / NK[i];
					}
				}
			}

			// 判断是否迭代结束
			double[][] a = GMMUtil.matrixMultiply(GMMUtil.toArray(px), pPie);
			for (int i = 0; i < dataNum; i++)
			{
				a[i][0] = Math.log(a[i][0]);
			}
			double L = GMMUtil.matrixSum(a, 1)[0];

			if (L - Lpre < threshold)
			{
				break;
			}
			Lpre = L;
		}
		return null;
	}

	/**
	 * 计算每个节点属于每个分布（共k）的概率
	 * @param dataSet
	 * @param pCentricPoint
	 * @param dataNum
	 * @param k
	 * @param dataDimen
	 * @return
	 */
	public ArrayList<ArrayList<Double>> computeProbablity(ArrayList<ArrayList<Double>> dataSet,
			ArrayList<ArrayList<Double>> pCentricPoint, int dataNum, int k, int dataDimen)
	{
		double[][] px = new double[dataNum][k]; // 每条数据属于每个分布的概率
		int[] type = getTypes(dataSet, pCentricPoint, k, dataNum);

		// 计算k个分布的协方差矩阵
		ArrayList<ArrayList<ArrayList<Double>>> covList = new ArrayList<ArrayList<ArrayList<Double>>>();
		for (int i = 0; i < k; i++)
		{
			ArrayList<ArrayList<Double>> dataSetK = new ArrayList<ArrayList<Double>>();
			for (int j = 0; j < dataNum; j++)
			{
				if (type[k] == i)
				{
					dataSetK.add(dataSet.get(i));
				}
			}
			covList.set(i, GMMUtil.computeCov(dataSetK, dataDimen, dataSetK.size()));
		}

		// 计算每条数据属于每个分布的概率
		for (int i = 0; i < dataNum; i++)
		{
			for (int j = 0; j < k; j++)
			{
				ArrayList<Double> offset = GMMUtil.arrayMinus(dataSet.get(i), pCentricPoint.get(j));
				ArrayList<ArrayList<Double>> invSigma = covList.get(k);
				double[] tmp = GMMUtil
						.matrixSum(GMMUtil.matrixMultiply(GMMUtil.toArray1(offset), GMMUtil.toArray(invSigma)), 2);
				double coef = Math.pow((2 * Math.PI), -(double) dataDimen / 2d)
						* Math.sqrt(GMMUtil.computeDet(invSigma, invSigma.size()));
				px[i][j] = coef * Math.pow(Math.E, -0.5 * tmp[0]);
			}
		}

		return GMMUtil.toList(px);
	}


	/**
	 * 初始化参数
	 * @param dataSet
	 * @param dataNum
	 * @param k
	 * @param dataDimen
	 * @return Parameter
	 */
	public Parameter iniParameters(ArrayList<ArrayList<Double>> dataSet, int dataNum, int k, int dataDimen)
	{
		Parameter res = new Parameter();

		ArrayList<ArrayList<Double>> pCentricPoint = generateCentroids(dataSet, dataNum, k);
		res.setpCentricPoint(pCentricPoint);

		// 计算每个样本节点与每个中心节点的距离，以此为据对样本节点进行分类计数，进而初始化k个分布的权值
		ArrayList<Double> pPi = new ArrayList<Double>();
		int[] type = getTypes(dataSet, pCentricPoint, k, dataNum);
		int[] typeNum = new int[k];
		for (int i = 0; i < dataNum; i++)
		{
			typeNum[type[i]]++;
		}
		for (int i = 0; i < k; i++)
		{
			pPi.add((double) (typeNum[i]) / (double) (dataNum));
		}
		res.setpWeights(pPi);

		// 计算k个分布的k个协方差
		ArrayList<ArrayList<ArrayList<Double>>> pSigma = new ArrayList<ArrayList<ArrayList<Double>>>();
		for (int i = 0; i < k; i++)
		{
			ArrayList<ArrayList<Double>> tmp = new ArrayList<ArrayList<Double>>();
			for (int j = 0; j < dataNum; j++)
			{
				if (type[j] == i)
				{
					tmp.add(dataSet.get(i));
				}
			}
			pSigma.add(GMMUtil.computeCov(tmp, dataDimen, dataNum));
		}
		res.setpSigma(pSigma);

		return res;
	}

	/**
	 * 产生随机的K个中心点
	 * @param data
	 * @param dataNum
	 * @param k
	 * @return k个随机的中心点
	 */
	public ArrayList<ArrayList<Double>> generateCentroids(ArrayList<ArrayList<Double>> data, int dataNum, int k)
	{
		ArrayList<ArrayList<Double>> res = null;
		if (dataNum < k)
		{
			return res;
		} else
		{
			res = new ArrayList<ArrayList<Double>>();

			List<Integer> random = new ArrayList<Integer>();
			// 随机产生不重复的k个数
			while (k > 0)
			{
				int index = (int) (Math.random() * dataNum);
				if (!random.contains(index))
				{
					random.add(index);
					k--;
					res.add(data.get(index));
				}
			}
		}
		return res;
	}


	/**
	 * 返回每条数据的类别
	 * @param dataSet
	 * @param pMiu
	 * @param k
	 * @param dataNum
	 * @return
	 */
	public int[] getTypes(ArrayList<ArrayList<Double>> dataSet, ArrayList<ArrayList<Double>> pCentricPoint, int k, int dataNum)
	{
		int[] type = new int[dataNum];
		for (int j = 0; j < dataNum; j++)
		{
			double minDistance = GMMUtil.computeDistance(dataSet.get(j), pCentricPoint.get(0));
			type[j] = 0; // 0作为该条数据的类别
			for (int i = 1; i < k; i++)
			{
				if (GMMUtil.computeDistance(dataSet.get(j), pCentricPoint.get(i)) < minDistance)
				{
					minDistance = GMMUtil.computeDistance(dataSet.get(j), pCentricPoint.get(i));
					type[j] = k;
				}
			}
		}
		return type;
	}

	public static void main(String[] args)
	{
		ArrayList<Double> pPi = new ArrayList<Double>();
		System.out.println(pPi.get(0));
	}
}
