package nju.iip.preprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description 常用工具类
 * @author wangqiang
 * @time 2014-10-18
 *
 */
public class Tools {
	
	private static int keyWordsNum=1000;//特征词个数
	
	private static final List<String> FileList =new ArrayList<String>();
	  
	/**
	 * 特征向量所包含的关键词Map（每类中取tf*idf前100大的）
	 */	 
	private static Map<String,Double> keywordsMap=new LinkedHashMap<String,Double>();

	//get list of file for the directory, including sub-directory of it
    public static List<String> readDirs(String filepath) throws FileNotFoundException, IOException
    {
        try
        {
            File file = new File(filepath);
            if(!file.isDirectory())
            {
                System.out.println("输入的[]");
                System.out.println("filepath:" + file.getAbsolutePath());
            }
            else
            {
                String[] flist = file.list();
                for(int i = 0; i < flist.length; i++)
                {
                    File newfile = new File(filepath + "\\" + flist[i]);
                    if(!newfile.isDirectory())
                    {
                        FileList.add(newfile.getAbsolutePath());
                    }
                    else if(newfile.isDirectory()) //if file is a directory, call ReadDirs
                    {
                        readDirs(filepath + "\\" + flist[i]);
                    }                    
                }
            }
        }catch(FileNotFoundException e)
        {
            System.out.println(e.getMessage());
        }
        return FileList;
    }
    
    
  //read file
    public static String readFile(String file) throws FileNotFoundException, IOException
    {
        StringBuffer strSb = new StringBuffer(); //String is constant， StringBuffer can be changed.
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")); 
        String line = br.readLine();
        while(line != null){
            strSb.append(line).append("\r\n");
            line = br.readLine();    
        }
        br.close();
        return strSb.toString();
    }
    
    
    /**
     * @decription 计算平均值
     * @param list
     * @return
     */
    public static Double getMean(ArrayList<Double>list){
    	Double sum=0.0;
		for(int i=0;i<list.size();i++){
			sum=sum+list.get(i);
		}
		Double mean=sum/list.size();
		return mean;
    }
    
    /**
     * @description 计算标准差
     * @param list
     * @return
     */
    public static Double getDeviation(ArrayList<Double>list){
    	Double mean=getMean(list);
    	Double deviation=0.0;
    	for(int i=0;i<list.size();i++){
    		deviation=deviation+(list.get(i)-mean)*(list.get(i)-mean);
    	}
    	deviation=Math.sqrt(deviation/(list.size()-1));
    	
    	return deviation ;
    }
    
    
    /**
	 * @description 求2个向量的欧式距离
	 * @param x
	 * @param y
	 * @return 2个点之间的距离distance
	 */
	public static Double vectorDistance(ArrayList<Double>x,ArrayList<Double>y){
		Double value=0.0;
		for(int i=0;i<keyWordsNum;i++){
			Double temp=x.get(i)-y.get(i);
			value=value+temp*temp;
		}
		return Math.sqrt(value);

	}
	
	/**
	 * @description 求x和y的数量积
	 * @param theta
	 * @param x
	 * @return
	 */
	public static Double vectorProduct(ArrayList<Double>x,ArrayList<Double>y){
		Double value=0.0;
		for(int i=0;i<keyWordsNum;i++){
			value=value+y.get(i)*x.get(i);
		}
		return value;

	}
	
	
	/**
	 * @description 计算2个帖子的余弦值
	 * @param x
	 * @param y
	 * @return
	 */
	public static Double vectorCosTheta(ArrayList<Double>x,ArrayList<Double>y){
		Double r1=vectorProduct(x,y);
		Double r2=0.0;
		Double r3=0.0;
		for(int i=0;i<keyWordsNum;i++){
			Double a=x.get(i);
			Double b=y.get(i);
			r2=r2+a*a;
			r3=r3+b*b;
		}
		
		return r1/(Math.sqrt(r2)*Math.sqrt(r3));
	}
	
	
	
	/**
	 * @Description: 返回特征向量词的map<word,idf>，1000个词
	 * @return keywordsMap
	 * @throws IOException
	 */
	public static Map<String,Double>getKeyWordsMap() throws IOException{
		    keywordsMap=new LinkedHashMap<String,Double>();
	        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("keywords.txt"), "UTF8")); 
	        String line = br.readLine();
	        while(line != null){  
	        	String[] str=line.split("=");
	        	Double idf=Double.parseDouble(str[1]);
	        	keywordsMap.put(str[0],idf);
	            line = br.readLine();    
	        }
	        br.close();
	       
		return keywordsMap;
	}
    
	/**
	 * @description 计算一个帖子的特征向量
	 * @param content
	 * @param 帖子所属类别ID
	 * @return ArrayList
	 * @throws IOException 
	 */
	public static ArrayList<Double>getOneArticleVector(String content,Double ID) throws IOException{
		ArrayList<Double>Vector=new ArrayList<Double>();
		Map<String,Long>articleWordsMap=ChineseTokenizer.segStr(content);
//		Set<String>words=articleWordsMap.keySet();
//		Long size=Long.valueOf(0);
//		for(String word:words){
//			size=size+articleWordsMap.get(word);
//		}
		keywordsMap=getKeyWordsMap();
		Set<String>keywords=keywordsMap.keySet();
		for(String keyword:keywords){
			if(articleWordsMap.containsKey(keyword)){
				
				Vector.add(1.0);
			}
			
			else
				Vector.add(Double.valueOf(0));
		}
		Vector.add(ID);//添加帖子所属类别
		return Vector;//返回一个帖子的特征向量X(x1，x2....x1000,id)
	}
	
	
	
	/**
	 * @description 判断一个向量是否为0向量
	 * @return false:不是零向量，true：是零向量
	 */
    public static Boolean IsZeroVector(ArrayList<Double> vector){
    	Boolean result=true;
    	for(int i=0;i<keyWordsNum;i++){
    		if(vector.get(i)!=0){
    			result=false;
    		}
    	}
    	return result;
    }
	
	
	
	
	/**
	 * @description 计算并获得某一类所有帖子的特征向量
	 * @param classification(某一类txt文件的路径)
	 * @return ArrayList<ArrayList<Double>>
	 * @throws IOException
	 */
	
	public static ArrayList<ArrayList<Double>>getOneClassVector(String classification,Double ID) throws IOException{
		ArrayList<ArrayList<Double>>classVector=new ArrayList<ArrayList<Double>>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(classification), "UTF-8")); 
        String line = br.readLine();
        while(line != null){  
        	ArrayList<Double>Vector=getOneArticleVector(line,ID);
        	if(!IsZeroVector(Vector)){
        		classVector.add(Vector);
        	}
        	line = br.readLine();       	
        }
        br.close();
		return classVector;
		
	}
	
	
    public static String getPercent(int x,int total){
		   String result="";//接受百分比的值
		   double x1=x*1.0;
		   double tempresult=x1/total;
		   DecimalFormat df1 = new DecimalFormat("0%");    //##.00%   百分比格式，后面不足2位的用0补齐
		   result= df1.format(tempresult);  
		   return result;
		}
    
//    public static void main(String args[]) throws FileNotFoundException, IOException{
//    	List<String>file=readDirs("D:\\dir");
//    	for(String f:file){
//    		System.out.println(readFile(f));
//    	}
//    }

}
