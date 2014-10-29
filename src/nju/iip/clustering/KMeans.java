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
	 * 每个类的帖子数
	 */
	private static ArrayList<Integer>classAricleNum=new ArrayList<Integer>();
	
	
	
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
			ArrayList<ArrayList<Double>>OneClassVector=Tools.getOneClassVector(article,ID);
			allMatrix.addAll(OneClassVector);
			classAricleNum.add(OneClassVector.size());
			ID++;
		}
		
		return allMatrix;
	}
	
	
	/**
	 * @description 初始化中心点
	 * @return meanVectors
	 */
	public static ArrayList<ArrayList<Double>>initializeMeanVectors(){
		int sum=0;
		for(int i=0;i<10;i++){
			int num=new Random().nextInt(classAricleNum.get(i));
			meanVectors.add(allMatrix.get(num+sum));
			sum=sum+classAricleNum.get(i);
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
     * @description 将每个点进行分类
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
		for(int i=0;i<keyWordsNum;i++){
			Double sum=0.0;
			for(int j=0;j<x.size();j++){
				sum=sum+x.get(j).get(i);
			}
			meanVector.add(sum/x.size());
			
		}
		meanVector.add(0.0);
		return meanVector;
	}
	
	
	/**
	 * @description 计算I(omega,c)
	 * @return
	 */
	public static Double getI(){
		Double N=(double) allMatrix.size();
		
		Double I=0.0;
		for(int i=0;i<10;i++){
			ArrayList<ArrayList<Double>>oneCluster=clusterMap.get(i);
			int w=clusterMap.get(i).size();
			if(w==0){
				w=w+1;
			}
			//System.out.println(w);
			for(int j=0;j<10;j++){
				Double n=0.0;
				for(int k=0;k<oneCluster.size();k++){
					if(oneCluster.get(k).get(keyWordsNum)==j){
						n++;
					}
				}
				if(n==0){
					n=n+0.1;
				}
				I=I+(n/N)*(Math.log10((N*n)/(w*classAricleNum.get(j))));
			}
		}
		return I;
	}
	
	/**
	 * @description 计算H(omega)
	 * @return
	 */
	public static Double getHOmega(){
		Double HOmega=0.0;
		Double N=(double) allMatrix.size();
		for(int i=0;i<10;i++){
			double w=clusterMap.get(i).size();
			if(w==0){
				w=w+0.1;
			}
			HOmega=HOmega+(w/N)*Math.log10(w/N);
		}
		return -HOmega;
	}
	
	
	public static Double getHC(){
		Double HC=0.0;
		Double N=(double) allMatrix.size();
		for(int i=0;i<10;i++){
			int c=classAricleNum.get(i);
			HC=HC+(c/N)*Math.log10(c/N);
		}
		return -HC;
		
	}
	
	 /**
     * @decription 计算NMI
     * @return
     */
	public static Double getNMI(){
		
		Double I=getI();
		Double HOmega=getHOmega();
		Double HC=getHC();
		System.out.println("HOmega="+HOmega);
		System.out.println("HC="+HC);
		System.out.println("I="+I);
		Double NMI=2*I/(HOmega+HC);
		return NMI;
	}
	
	
	/**
	 * @description 整个处理过程
	 */
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
		System.out.println("K-Means的NMI值为:"+getNMI());
		
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		
		getAllMatrix();
		process();
		

	}

}
