package nju.iip.clustering;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import nju.iip.preprocess.Tools;


/**
 * @description k-means方法实现聚类
 * @author wangqiang
 * @time 2014-10-23
 */
public class KMeans {
	
	private static String SamplePath="lily";//文本路径
	
	private static int keyWordsNum=1000;//特征词个数
	/**
	 * 10个聚类
	 */
	private static HashMap<Integer,ArrayList<ArrayList<Double>>>clusterMap=new HashMap<Integer,ArrayList<ArrayList<Double>>>();
	
	/**
	 * 十个中心点
	 */
	private static ArrayList<ArrayList<Double>>meanVectors=new ArrayList<ArrayList<Double>>();
	
	/**
	 * 整个样本二维矩阵
	 */
	private static ArrayList<ArrayList<Double>>allMatrix=new ArrayList<ArrayList<Double>>();
	
	/**
	 * @description 获得整个样本的特征向量
	 * @return Map<String,ArrayList<ArrayList<Double>>>allVector
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static ArrayList<ArrayList<Double>>getAllMatrix() throws FileNotFoundException, IOException{
		List<String>fileList=Tools.readDirs(SamplePath);
		Double ID=1.0;
		for(String article:fileList){
			allMatrix.addAll(Tools.getOneClassVector(article,ID));
			ID++;
		}
		
		return allMatrix;
	}
	
	
	/**
	 * @description 初始化中心点
	 * @return meanVectors
	 */
	public static ArrayList<ArrayList<Double>>initializeMeanVectors(){
		for(int i=0;i<10;i++){
			int num=new Random().nextInt(allMatrix.size());
			meanVectors.add(allMatrix.get(num));
			//System.out.println(num);
		}
		return meanVectors;
	}
	
	
	/**
	 * @description 求出某个向量最相似的簇
	 * @param x
	 * @return
	 */
	public static int getClosestMeanNum(ArrayList<Double>x){
		int result=0;
		Double temp=Tools.vectorCosTheta(x, meanVectors.get(0));
		for(int i=1;i<=9;i++){
			Double costheta=Tools.vectorCosTheta(x, meanVectors.get(i));
			if(costheta>temp){
				temp=costheta;
				result=i;
			}
		}
		return result;
	}
	
	/**
	 * @description 初始化clusterMap
	 * @return
	 */
	public static HashMap<Integer,ArrayList<ArrayList<Double>>>initializeClusterMap(){
		clusterMap.clear();
		for(int i=0;i<10;i++){
			ArrayList<ArrayList<Double>>list=new ArrayList<ArrayList<Double>>();
			clusterMap.put(i, list);
		}
		return clusterMap;
	}
	
    /**
     * @description 对当前mean进行分类
     * @return clusterMap
     */
	public static HashMap<Integer,ArrayList<ArrayList<Double>>>getClusterMap(){
		for(int i=0;i<allMatrix.size();i++){
			int result=getClosestMeanNum(allMatrix.get(i));
			clusterMap.get(result).add(allMatrix.get(i));
		}
		return clusterMap;
	}
	
	/**
	 * @description 计算某一类下的中心点
	 * @param x
	 * @return
	 */
	public static ArrayList<Double> getMeanVectors(ArrayList<ArrayList<Double>>x){
		ArrayList<Double>meanVector=new ArrayList<Double>();
		for(int i=0;i<keyWordsNum-1;i++){
			Double sum=0.0;
			for(int j=0;j<x.size();j++){
				sum=sum+x.get(j).get(i);
			}
			meanVector.add(sum/x.size());
			
		}
		meanVector.add(0.0);
		return meanVector;
	}
	
	
	public static void process(){
		meanVectors=initializeMeanVectors();
		for(int n=0;n<10;n++){
			initializeClusterMap();
			clusterMap=getClusterMap();
			meanVectors.clear();
			for(int i=0;i<10;i++){
				meanVectors.add(getMeanVectors(clusterMap.get(i)));
			}
			
		}
		
		
	}
	
	
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		getAllMatrix();
		process();
	    System.out.println(clusterMap.size());
	    for(int i=0;i<clusterMap.size();i++){
	    	 System.out.println(clusterMap.get(i).size());
	    }
	}

}
