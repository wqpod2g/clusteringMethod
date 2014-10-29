package nju.iip.clustering;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import nju.iip.preprocess.Tools;

/**
 * @description Clustering by fast search and find of density peaks
 * @author wangqiang
 * @time 2014-10-28
 *
 */
public class ClusterByFindDensityPeaks {
	
	private static Double dc=0.2;//截断距离dc
	
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
		System.out.println("预处理完毕！");
		return allMatrix;
	}
	
	
	/**
	 * @description 计算每个点的密度ρi
	 */
	public static void getDensity(){
		//int count=0;
		for(int i=0;i<allMatrix.size();i++){
			Double n=0.0;
			for(int j=0;j<allMatrix.size();j++){
				if(Tools.vectorCosTheta(allMatrix.get(i),allMatrix.get(j))>dc){
					n=n+1;
				}
			}
			allMatrix.get(i).add(n);
		}
		System.out.println("每个点的密度计算完毕！");
	}
	
	
	/**
	 * @description 计算每个点Delta的值
	 */
	public static void getDelta(){
		for(int i=0;i<allMatrix.size();i++){
			if(allMatrix.get(i).get(keyWordsNum+1)==233){
				Double dis=0.0;
				for(int j=0;j<allMatrix.size();j++){
					Double temp=1-Tools.vectorCosTheta(allMatrix.get(i),allMatrix.get(j));
					if(temp>dis){
						dis=temp;
					}
				}
				allMatrix.get(i).add(dis);
			}
			
			else{
				Double dis=1.0;
				for(int j=0;j<allMatrix.size();j++){
					if(allMatrix.get(j).get(keyWordsNum+1)>allMatrix.get(i).get(keyWordsNum+1)){
						Double temp=1-Tools.vectorCosTheta(allMatrix.get(i),allMatrix.get(j));
						if(temp<dis){
							dis=temp;
						}
					}
				}
				allMatrix.get(i).add(dis);
			}
		}
		System.out.println("每个点的delta计算完毕！");
	}
	
	
	
	
	/**
	 * @description 计算rho和theta前10大的点
	 */
	public static void getMeanVector(){
		System.out.println("10个中心点为:");
		for(int i=0;i<allMatrix.size();i++){
			if(allMatrix.get(i).get(keyWordsNum+1)>=130&&allMatrix.get(i).get(keyWordsNum+2)>0.60&&allMatrix.get(i).get(keyWordsNum+2)!=0.6176404435490638){
				
				System.out.println("rho="+allMatrix.get(i).get(keyWordsNum+1)+"  theta="+allMatrix.get(i).get(keyWordsNum+2));
				meanVectors.add(allMatrix.get(i));
			}
		}
	}
	
	
	
	/**
	 * @description 求出某个向量最相似的簇
	 * @param x
	 * @return
	 */
	public static int getClosestMeanNum(ArrayList<Double>x){
		int result=0;
		Double temp=Tools.vectorCosTheta(x, meanVectors.get(0));
		for(int i=1;i<10;i++){
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
		initializeClusterMap();
		getDensity();
		getDelta();
		getMeanVector();
		clusterMap=getClusterMap();
		System.out.println("ClusterByFindDensityPeaks的NMI值为:"+getNMI());
	}
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		
		getAllMatrix();
		process();
		
	}

}
