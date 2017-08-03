package GMM;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import Util.GMMUtil;

/***
 * 高斯模型，一个峰由权重、标准差、中心向量衡量，且我们每次把权重除以标准差的值作为优先级rank
 * 以后尝试拟合一个模型时，按照rank大小优先拟合大的，所有都未成功拟合则将rank最小的取代。
 * 
 * @author Yu
 *
 */
public class Model
{
	private static double defaultWight = 0.05;// 默认的权重
	private static double[][] defaultCovMatrix = { { 900, 900, 900 } };
	private static ArrayList<Double> defaultCov = new ArrayList<>(GMMUtil.toList(defaultCovMatrix).get(0));// 默认的协方差矩阵
	private static double defaultWeightSum = 1.0;// 所有模型的默认权重和
	private double weight;// 峰的权重
	private ArrayList<Double> center = new ArrayList<Double>();// 峰中心向量
	private double rank;// 排序，首先拟合rank大的，所有全部未拟合则替代rank最小的峰
	private static int dimension = 3;// 维度
	/**
	 * 协方差，假如有三个维度，那么我们的协方差矩阵为3*3，对角线为每个维度方差
	 * 此处我们认为每个维度是相互独立的，也就是说协方差矩阵变成了一个对角阵，对角线上为每个维度的方差
	 */
	private ArrayList<Double> cov = new ArrayList<>();

	public Model(ArrayList<Double> aCenter)
	{
		weight = defaultWight;
		cov = defaultCov;
		center = new ArrayList<Double>(aCenter);
	}

	public Model(double aWeight, ArrayList<Double> cov, ArrayList<Double> aCenter)
	{

		weight = aWeight;
		this.cov = cov;
		center = new ArrayList<Double>(aCenter);
		dimension = center.size();
		if (center.size() != cov.size())
		{
			System.out.println("the cov and center don't match(dimension not equal");
			System.exit(1);
		}
	}

	/**
	 * 按向量的维度随机生成一个模型
	 * 
	 * @param d
	 */
	public Model(int d)
	{
		dimension = d;
		weight = defaultWight;
		cov = defaultCov;
		Random random = new Random();
		double d1;

		for (int j = 0; j < dimension; j++)
		{
			d1 = random.nextDouble();
			center.add(d1);
		}

	}

	public double getWeight()
	{
		return weight;
	}

	public void setWeight(double weight)
	{
		this.weight = weight;
	}

	public ArrayList<Double> getCenter()
	{
		return center;
	}

	public void setCenter(ArrayList<Double> center)
	{
		this.center = center;
	}

	/**
	 * 使用weight除以标准差，也就是协方差的行列式开根号
	 * 
	 * @return
	 */
	public double getRank()
	{
		double multiply = 1.0;
		for (double var : cov)
		{
			multiply *= var;
		}

		rank = weight / Math.sqrt(multiply);
		return rank;
	}

	/**
	 * 按照Rank排序，从大到小
	 * 
	 * @param ModelList
	 */
	public static void sortModel(ArrayList<Model> ModelList)
	{
		rejustWeight(ModelList);
		Comparator<Model> ModelComparator = new Comparator<Model>()
		{
			public int compare(Model s1, Model s2)
			{
				// 按照Rank排序
				if (s1.getRank() > s2.getRank())
				{
					return -1;
				} else if (s1.getRank() < s2.getRank())
				{
					return 1;
				} else
				{
					return 0;
				}
			}
		};
		ModelList.sort(ModelComparator);

	}

	/**
	 * 将所有模型的权重相加，每个模型除以权重和，乘以默认权重和
	 * 
	 * @param models
	 */
	public static void rejustWeight(ArrayList<Model> models)
	{
		double sum = 0.0;// 所有模型的权重和
		double rejust = 0.0;// 需要调整的倍数
		for (Model model : models)
		{
			sum += model.getWeight();
		}
		rejust = defaultWeightSum / sum;
		for (Model model : models)
		{
			model.setWeight(model.getWeight() * rejust);
		}

	}

	public String toString()
	{

		return String.format("Model:\tcenter:%s\n\tweight:\t%s\n\tcov:\t%s\n\trank:\t%s\n", getCenter().toString(),
				getWeight(), getCov().toString(), getRank());

	}

	public static void main(String args[])
	{
		ArrayList<Model> models = new ArrayList<Model>();
		Random random = new Random();
		for (int i = 0; i < 5; i++)
		{
			Model model = new Model(3);
			model.weight = random.nextDouble();
			ArrayList<Double> temp = new ArrayList<>();
			for (int j = 0; j < Model.getDimension(); j++)
			{
				temp.add(random.nextDouble());
			}
			// model.setCov(temp);
			models.add(model);
		}

		// for (Model model : models)
		// {
		// System.out.print(model.toString());
		//
		// }
		Model.sortModel(models);

		for (Model model : models)
		{
			System.out.print(model.toString());

		}
		System.out.println();

		// Model.rejustWeight(models);
		//
		// for (Model model : models)
		// {
		// System.out.print(model.toString());
		//
		// }

	}

	public static int getDimension()
	{
		return dimension;
	}

	public static void setDimension(int dimension)
	{
		Model.dimension = dimension;
	}

	public ArrayList<Double> getCov()
	{
		return cov;
	}

	public void setCov(ArrayList<Double> cov)
	{
		this.cov = cov;
	}

}
