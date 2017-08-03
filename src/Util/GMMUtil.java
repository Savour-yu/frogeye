package Util;

import java.util.ArrayList;
import java.util.Random;

class MixData
{
	double sortKey;
	double weight;
	double mean;
	double var;
};

/**
 * 工具类，提供一些工具，矩阵运算等
 * 
 * @author Yu
 *
 */
public class GMMUtil
{
	static final int defaultNMixtures = 10;// 默认混合模型个数
	static final int defaultHistory = 200;// 默认历史帧数
	static final double defaultBackgroundRatio = 0.7;// 默认背景门限
	static final double defaultVarThreshold = 2.5 * 2.5;// 默认方差门限
	static final double defaultNoiseSigma = 30 * 0.5;// 默认噪声方差
	static final double defaultInitialWeight = 0.05;// 默认初始权值
	private static final double FLT_EPSILON = 0;

	/**
	 * 计算两个向量向量之间的距离
	 * 
	 * @param d1
	 * @param d2
	 * @return double
	 */
	public static double computeDistance(ArrayList<Double> d1, ArrayList<Double> d2)
	{
		double squareSum = 0;
		for (int i = 0; i < d1.size(); i++)
		{
			squareSum += (d1.get(i) - d2.get(i)) * (d1.get(i) - d2.get(i));
		}
		return Math.sqrt(squareSum);
	}

	/**
	 * 计算马氏距离
	 * 
	 * @param test带计算向量
	 * @param mean均值向量
	 * @param cov协方差
	 * @return 马氏距离
	 */
	public static double computeMahalanobisDistance(ArrayList<Double> test, ArrayList<Double> mean,
			ArrayList<Double> cov)
	{
		double MahalanobisDistance;
		ArrayList<Double> diff = arrayMinus(test, mean);// 测试向量与均值向量的差
		double[][] AT = toArray1(diff);// 向量转换成矩阵的第一行,此矩阵为差向量的转置。一行N列
		double[][] A = matrixReverse(AT);// 转置矩阵的转置为差向量,N行一列
		// 马氏距离为AT*B-1*A，其中A为数据与均值的差，AT是其转置，B为协方差矩阵，B-1是协方差矩阵的逆,1*d * d*d * d*1
		// 首先我们将协方差向量还原为协方差矩阵
		double[][] covMatrix = diag(toArray1(cov)[0]);
		// 然后我们将三者乘积计算出来，乘积的结果为1*1的矩阵，其中唯一的元素就是马氏距离
		double[][] temp = matrixMultiply(AT, covMatrix);
		MahalanobisDistance = matrixMultiply(temp, A)[0][0];

		return MahalanobisDistance;
	}

	/**
	 * 计算数据集中任意两组数据的协方差
	 * 
	 * @param dataSet
	 *            数据集
	 * @param dataDimen
	 *            数据维度
	 * @param dataNum
	 *            数据集长度
	 * @return
	 */
	public static ArrayList<ArrayList<Double>> computeCov(ArrayList<ArrayList<Double>> dataSet, int dataDimen,
			int dataNum)
	{
		ArrayList<ArrayList<Double>> res = new ArrayList<ArrayList<Double>>();

		// 计算每一维数据的均值
		double[] sum = new double[dataDimen];
		for (ArrayList<Double> data : dataSet)
		{
			for (int i = 0; i < dataDimen; i++)
			{
				sum[i] += data.get(i);
			}
		}
		for (int i = 0; i < dataDimen; i++)
		{
			sum[i] = sum[i] / dataNum;
		}

		// 计算任意两组数据的协方差
		for (int i = 0; i < dataDimen; i++)
		{
			ArrayList<Double> tmp = new ArrayList<Double>();
			for (int j = 0; j < dataDimen; j++)
			{
				double cov = 0;
				for (ArrayList<Double> data : dataSet)
				{
					cov += (data.get(i) - sum[i]) * (data.get(j) - sum[j]);
				}
				tmp.add(cov);
			}
			res.add(tmp);
		}
		return res;
	}

	/**
	 * 计算矩阵的逆矩阵
	 * 
	 * @param dataSet
	 *            数据集ArrayList<ArrayList<Double>>
	 * @return double[][]逆矩阵
	 */
	public static double[][] computeInv(ArrayList<ArrayList<Double>> dataSet)
	{
		int dataDimen = dataSet.size();
		double[][] res = new double[dataDimen][dataDimen];

		// 将list转化为array
		double[][] a = toArray(dataSet);

		// 计算伴随矩阵
		double detA = computeDet(dataSet, dataDimen); // 整个矩阵的行列式
		for (int i = 0; i < dataDimen; i++)
		{
			for (int j = 0; j < dataDimen; j++)
			{
				double num;
				if ((i + j) % 2 == 0)
				{
					num = computeDet(toList(computeAC(a, i + 1, j + 1)), dataDimen - 1);
				} else
				{
					num = -computeDet(toList(computeAC(a, i + 1, j + 1)), dataDimen - 1);
				}
				res[j][i] = num / detA;
			}
		}
		return res;
	}

	/**
	 * 计算矩阵制定行列的代数余子式
	 * 
	 * @param dataSet
	 * @param r
	 * @param c
	 * @return
	 */
	public static double[][] computeAC(double[][] dataSet, int r, int c)
	{
		int H = dataSet.length;
		int V = dataSet[0].length;
		double[][] newData = new double[H - 1][V - 1];

		for (int i = 0; i < newData.length; i++)
		{
			if (i < r - 1)
			{
				for (int j = 0; j < newData[i].length; j++)
				{
					if (j < c - 1)
					{
						newData[i][j] = dataSet[i][j];
					} else
					{
						newData[i][j] = dataSet[i][j + 1];
					}
				}
			} else
			{
				for (int j = 0; j < newData[i].length; j++)
				{
					if (j < c - 1)
					{
						newData[i][j] = dataSet[i + 1][j];
					} else
					{
						newData[i][j] = dataSet[i + 1][j + 1];
					}
				}

			}
		}
		return newData;
	}

	/**
	 * 计算行列式
	 * 
	 * @param dataSet
	 * @param dataDimen
	 * @return
	 */
	public static double computeDet(ArrayList<ArrayList<Double>> dataSet, int dataDimen)
	{
		// 将list转化为array
		double[][] a = toArray(dataSet);

		if (dataDimen == 2)
		{
			return a[0][0] * a[1][1] - a[0][1] * a[1][0];
		}
		double res = 0;
		for (int i = 0; i < dataDimen; i++)
		{
			if (i % 2 == 0)
			{
				res += a[0][i] * computeDet(toList(computeAC(toArray(dataSet), 1, i + 1)), dataDimen - 1);
			} else
			{
				res += -a[0][i] * computeDet(toList(computeAC(toArray(dataSet), 1, i + 1)), dataDimen - 1);
			}
		}

		return res;
	}

	/**
	 * 把double[][]变成ArrayList<ArrayList<double>>
	 * 
	 * @param a
	 *            double[][]
	 * @return ArrayList<ArrayList<double>>
	 */
	public static ArrayList<ArrayList<Double>> toList(double[][] a)
	{
		ArrayList<ArrayList<Double>> res = new ArrayList<ArrayList<Double>>();
		for (int i = 0; i < a.length; i++)
		{
			ArrayList<Double> tmp = new ArrayList<Double>();
			for (int j = 0; j < a[i].length; j++)
			{
				tmp.add(a[i][j]);
			}
			res.add(tmp);
		}
		return res;
	}

	/**
	 * 矩阵乘法，矩阵行乘以矩阵列 m*n 乘以 n*k
	 * 
	 * @param a
	 *            m*n
	 * @param b
	 *            n*k
	 * @return m*k
	 */
	public static double[][] matrixMultiply(double[][] a, double[][] b)
	{
		if (a[0].length == b.length)
		{

			double[][] res = new double[a.length][b[0].length];
			for (int i = 0; i < a.length; i++)
			{
				for (int j = 0; j < b[0].length; j++)
				{
					for (int k = 0; k < a[0].length; k++)
					{
						res[i][j] += a[i][k] * b[k][j];
					}
				}
			}
			return res;
		} else
		{
			System.out.print("the arraylist1 and arraylist2 is not comparable");
			System.exit(1);
			return null;
		}
	}

	/**
	 * 矩阵点乘，对应元素相乘
	 * 
	 * @param a
	 * @param b
	 * @return 矩阵 double[]
	 */
	public static double[][] dotMatrixMultiply(double[][] a, double[][] b)
	{
		if (a.length == b.length && a[0].length == b[0].length)
		{
			double[][] res = new double[a.length][a[0].length];
			for (int i = 0; i < a.length; i++)
			{
				for (int j = 0; j < a[0].length; j++)
				{
					res[i][j] = a[i][j] * b[i][j];
				}
			}
			return res;
		} else
		{
			System.out.print("the arraylist1 and arraylist2 is not comparable");
			System.exit(1);
			return null;
		}

	}

	/**
	 * 矩阵的点除，即对应元素相除
	 * 
	 * @param a
	 *            被除数
	 * @param b
	 *            除数
	 * @return
	 */
	public static double[][] dotMatrixDivide(double[][] a, double[][] b)
	{
		if (a.length == b.length && a[0].length == b[0].length)
		{
			double[][] res = new double[a.length][a[0].length];
			for (int i = 0; i < a.length; i++)
			{
				for (int j = 0; j < a[0].length; j++)
				{
					res[i][j] = a[i][j] / b[i][j];
				}
			}
			return res;
		} else
		{
			System.out.print("the arraylist1 and arraylist2 is not comparable");
			System.exit(1);
			return null;
		}

	}

	// /**
	// *
	// * @Title: repmat
	// * @Description: 对应matlab的repmat的函数，对矩阵进行横向或纵向的平铺
	// * @return double[][]
	// * @throws
	// */
	// public static double[][] repmat(double[][] a, int row, int clo) {
	// double[][] res = new double[a.length * row][a[0].length * clo];
	//
	// return null;
	// }

	/**
	 * 求两个矩阵的差
	 * 
	 * @Title: matrixMinux
	 * @Description: 计算矩阵值差
	 * @param: a1
	 *             被减数
	 * @param a2
	 *            减数
	 * @return ArrayList<ArrayList<Double>> @throws
	 */
	public static ArrayList<ArrayList<Double>> matrixMinus(ArrayList<ArrayList<Double>> a1,
			ArrayList<ArrayList<Double>> a2)
	{
		if (a1.size() == a2.size() && a1.get(0).size() == a2.get(0).size())
		{
			ArrayList<ArrayList<Double>> res = new ArrayList<>();
			for (int i = 0; i < a1.size(); i++)
			{
				res.add(arrayMinus(a1.get(i), a2.get(i)));
			}
			return res;
		} else
		{
			System.out.print("the arraylist1 and arraylist2 is not comparable");
			System.exit(1);
			return null;

		}
	}

	/**
	 * 求两个向量的差
	 * 
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static ArrayList<Double> arrayMinus(ArrayList<Double> a1, ArrayList<Double> a2)
	{

		if (a1.size() != a2.size())
		{
			System.out.print("the arraylist1 and arraylist2 is not comparable");
			System.exit(1);
			return null;

		} else
		{
			ArrayList<Double> result = new ArrayList<>();
			for (int i = 0; i < a1.size(); i++)
			{
				result.add(a1.get(i) - a2.get(i));
			}
			return result;
		}

	}

	/**
	 * 返回矩阵每行之和(mark==2)或每列之和(mark==1)
	 * 
	 * @param a
	 * @param mark
	 *            1 for summing every col and 2 for summing every row
	 * @return ArrayList<Double>
	 */
	public static double[] matrixSum(double[][] a, int mark)
	{
		double res[] = new double[a.length];
		if (mark == 1)
		{ // 计算每列之和，返回行向量
			res = new double[a[0].length];
			for (int i = 0; i < a[0].length; i++)
			{
				for (int j = 0; j < a.length; j++)
				{
					res[i] += a[j][i];
				}
			}
		} else if (mark == 2)
		{ // 计算每行之和， 返回列向量
			for (int i = 0; i < a.length; i++)
			{
				for (int j = 0; j < a[0].length; j++)
				{
					res[i] += a[i][j];
				}
			}

		}
		return res;
	}

	/**
	 * ArrayList<ArrayList<double>> to double[][]
	 * 
	 * @param a
	 * @return
	 */
	public static double[][] toArray(ArrayList<ArrayList<Double>> a)
	{
		int row = a.size();
		int col = a.get(0).size();
		double[][] res = new double[row][col];

		for (int i = 0; i < row; i++)
		{
			for (int j = 0; j < col; j++)
			{
				res[i][j] = a.get(i).get(j);
			}
		}

		return res;
	}

	/**
	 * ArrayList<Double> to double[0][] 将一个double数组变成一个矩阵的第一行
	 * 
	 * @param a
	 * @return
	 */
	public static double[][] toArray1(ArrayList<Double> a)
	{

		int dataDimen = a.size();
		double[][] res = new double[1][dataDimen];

		for (int i = 0; i < dataDimen; i++)
		{
			res[0][i] = a.get(i);
		}

		return res;
	}

	/**
	 * 得到转置矩阵，i*j to j*i
	 * 
	 * @param a
	 *            double[][]
	 * @return double [][]
	 */
	public static double[][] matrixReverse(double[][] a)
	{
		double[][] res = new double[a[0].length][a.length];
		for (int i = 0; i < a.length; i++)
		{
			for (int j = 0; j < a[0].length; j++)
			{
				res[j][i] = a[i][j];
			}
		}
		return res;
	}

	/**
	 * 向量对角化,把数组中的数全部放到矩阵的对角线上
	 * 
	 * @param a
	 *            double[]
	 * @return double [][]
	 */
	public static double[][] diag(double[] a)
	{
		double[][] res = new double[a.length][a.length];
		for (int i = 0; i < a.length; i++)
		{
			for (int j = 0; j < a.length; j++)
			{
				if (i == j)
				{
					res[i][j] = a[i];
				}
			}
		}
		return res;
	}

	/**
	 * 二维double数组输出
	 * 
	 * @param arrayList
	 * @return
	 */
	public static String Array2ToString(ArrayList<ArrayList<Double>> arrayList)
	{

		String result = "[";
		for (ArrayList<Double> aDoubles : arrayList)
		{
			result += aDoubles.toString() + ",";
		}
		result = result.substring(0, result.length() - 1);
		result += "]";
		return result;

	}

	public static String Array2ToString(double[][] arrayList)
	{
		ArrayList<ArrayList<Double>> arrayList2 = GMMUtil.toList(arrayList);
		return GMMUtil.Array2ToString(arrayList2);

	}

	static void process8uC1(final ArrayList<ArrayList<Double>> image, ArrayList<ArrayList<Double>> fgmask,
			double learningRate, ArrayList<ArrayList<Double>> bgmodel, int nmixtures, double backgroundRatio,
			double varThreshold, double noiseSigma)
	{
		int x, y, k, k1, rows = image.size(), cols = image.get(0).size();
		double alpha = (double) learningRate, T = (double) backgroundRatio, vT = (double) varThreshold;// 学习速率、背景门限、方差门限
		int K = nmixtures;// 混合模型个数
		ArrayList<MixData> mptr = new ArrayList<MixData>();
		/* = bgmodel.data */;

		final double w0 = (double) defaultInitialWeight;// 初始权值
		final double sk0 = (double) (w0 / (defaultNoiseSigma * 2));// 初始优先级
		final double var0 = (double) (defaultNoiseSigma * defaultNoiseSigma * 4);// 初始方差
		final double minVar = (double) (noiseSigma * noiseSigma);// 最小方差

		for (y = 0; y < rows; y++)
		{
			final ArrayList<Double> src = image.get(y);
			ArrayList<Double> dst = fgmask.get(y);

			if (alpha > 0)// 如果学习速率为0，则退化为背景相减
			{
				int m = 0;
				for (x = 0; x < cols; x++, m += K)
				{
					double wsum = 0;
					Double pix = src.get(x);// 每个像素
					int kHit = -1, kForeground = -1;// 是否属于模型，是否属于前景

					for (k = 0; k < K; k++)// 每个高斯模型
					{
						double w = mptr.get(m + k).weight;// 当前模型的权值
						wsum += w;// 权值累加
						if (w < FLT_EPSILON)
							break;
						Double mu = mptr.get(m + k).mean;// 当前模型的均值
						double var = mptr.get(m + k).var;// 当前模型的方差
						double diff = pix - mu;// 当前像素与模型均值之差
						double d2 = diff * diff;// 平方
						if (d2 < vT * var)// 是否小于方门限，即是否属于该模型
						{
							wsum -= w;// 如果匹配，则把它减去，因为之后会更新它
							double dw = alpha * (1.f - w);
							mptr.get(m + k).weight = w + dw;// 增加权值
							// 注意源文章中涉及概率的部分多进行了简化，将概率变为1
							mptr.get(m + k).mean = mu + alpha * diff;// 修正均值
							var = Math.max(var + alpha * (d2 - var), minVar);// 开始时方差清零0，所以这里使用噪声方差作为默认方差，否则使用上一次方差
							mptr.get(m + k).var = var;// 修正方差
							mptr.get(m + k).sortKey = w / Math.sqrt(var);// 重新计算优先级，貌似这里不对，应该使用更新后的mptr[k].weight而不是w

							for (k1 = k - 1; k1 >= 0; k1--)// 从匹配的第k个模型开始向前比较，如果更新后的单高斯模型优先级超过他前面的那个，则交换顺序
							{
								if (mptr.get(m + k1).sortKey >= mptr.get(m + k1 + 1).sortKey)// 如果优先级没有发生改变，则停止比较
									break;

								MixData temp = new MixData();
								temp = mptr.get(m + k1 + 1);
								mptr.set(m + k1 + 1, mptr.get(m + k1));
								mptr.set(m + k1, temp);// 交换它们的顺序，始终保证优先级最大的在前面
							}

							kHit = k1 + 1;// 记录属于哪个模型
							break;
						}
					}

					if (kHit < 0) // no appropriate gaussian mixture found at all, remove the weakest mixture and
									// create a new one
									// 当前像素不属于任何一个模型
					{
						// 初始化一个新模型
						kHit = k = Math.min(k, K - 1);// 有两种情况，当最开始的初始化时，k并不是等于K-1的
						wsum += w0 - mptr.get(m + k).weight;// 从权值总和中减去原来的那个模型，并加上默认权值
						mptr.get(m + k).weight = w0;// 初始化权值
						mptr.get(m + k).mean = pix;// 初始化均值
						mptr.get(m + k).var = var0; // 初始化方差
						mptr.get(m + k).sortKey = sk0;// 初始化权值
					} else
						for (; k < K; k++)
							wsum += mptr.get(m + k).weight;// 求出剩下的总权值

					double wscale = 1.f / wsum;// 归一化
					wsum = 0;
					for (k = 0; k < K; k++)
					{
						wsum += mptr.get(m + k).weight *= wscale;
						mptr.get(m + k).sortKey *= wscale;// 计算归一化权值
						if (wsum > T && kForeground < 0)
							kForeground = k + 1;// 第几个模型之后就判为前景了
					}
					dst.set(x, (double) ((kHit >= kForeground) ? 255 : 0));// 判决：(ucahr)(-true) = 255;(uchar)(-(false))
																			// = 0;
				}
			} else// 如果学习速率小于等于0，则没有背景更新过程，其他过程类似
			{
				int m = 0;
				for (x = 0; x < cols; x++, m += K)
				{
					double pix = src.get(x);
					int kHit = -1, kForeground = -1;

					for (k = 0; k < K; k++)
					{
						if (mptr.get(m + k).weight < FLT_EPSILON)
							break;
						double mu = mptr.get(m + k).mean;
						double var = mptr.get(m + k).var;
						double diff = pix - mu;
						double d2 = diff * diff;
						if (d2 < vT * var)
						{
							kHit = k;
							break;
						}
					}

					if (kHit >= 0)
					{
						double wsum = 0;
						for (k = 0; k < K; k++)
						{
							wsum += mptr.get(m + k).weight;
							if (wsum > T)
							{
								kForeground = k + 1;
								break;
							}
						}
					}

					dst.set(x, (double) (kHit < 0 || kHit >= kForeground ? 255 : 0));
				}
			}
		}
	}

	public static void main(String[] args)
	{
		ArrayList<ArrayList<Double>> dataSet = new ArrayList<>();

		Random random = new Random();
		for (int i = 0; i < 4; i++)
		{
			ArrayList<Double> temp = new ArrayList<>();
			for (int j = 0; j < 5; j++)
			{
				temp.add((double) random.nextInt(10));

			}
			dataSet.add(temp);

		}
		System.out.println(GMMUtil.Array2ToString(dataSet));
		ArrayList<ArrayList<Double>> dataSet2 = new ArrayList<>();

		for (int i = 0; i < 5; i++)
		{
			ArrayList<Double> temp = new ArrayList<>();
			for (int j = 0; j < 4; j++)
			{
				temp.add((double) random.nextInt(10));

			}
			dataSet2.add(temp);

		}
		System.out.println(GMMUtil.Array2ToString(dataSet2));

		double[][] d = GMMUtil.matrixMultiply(GMMUtil.toArray(dataSet), GMMUtil.toArray(dataSet2));
		System.out.println(GMMUtil.Array2ToString(d));

		ArrayList<Double> test = new ArrayList<>();
		ArrayList<Double> mean = new ArrayList<>();
		ArrayList<Double> cov = new ArrayList<>();
		for (int j = 0; j < 4; j++)
		{
			test.add((double) random.nextInt(10));
			mean.add((double) random.nextInt(10));
			cov.add((double) random.nextInt(20));

		}
		System.out.println("hello");
		System.out.println(test.toString());
		System.out.println(mean.toString());
		System.out.println(cov.toString());
		System.out.println(computeMahalanobisDistance(test, mean, cov));

		
	}
}