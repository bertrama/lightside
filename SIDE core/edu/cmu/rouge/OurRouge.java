package edu.cmu.rouge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yerihyo.yeritools.collections.CollectionsToolkit;

public class OurRouge
{
	protected static void test02(){
		String[] a = new String[]{"a", "b"};
		String[] b = new String[]{"a", "b"};
		
		System.out.println(CollectionsToolkit.equals(a, b));
		System.out.println(a.equals(b));
	}
	
	public static void main(String[] args)
	{
		test02();
	}
	protected static void test01(){
		OurRouge or = new OurRouge();
		String text01 = "Hello, this is not good";
		String text02 = "this hello is not so bad but it could be worse";
		double r2recall = or.getRouge2Recall(text01, text02);
		double r1FMeasure = or.getRouge1FMeasure(text01, text02);
		System.out.println(r1FMeasure + " and " + r2recall);
	}
	
	public double getRouge1Recall(String ref, String model)
	{
		ArrayList<String> refList = getTokenList(ref);
		ArrayList<String> modelList = getTokenList(model);
		double matches = getMatches(refList, modelList);
		double denom = refList.size();
		
		return matches/denom;
	}
	
	
	
	public int getMatches(ArrayList<String> refList, ArrayList<String> modelList)
	{
		int count = 0;
		Map<String,Integer> refMap = new HashMap<String,Integer>();
		Map<String,Integer> modelMap = new HashMap<String,Integer>();		
		for(String ref: refList)
		{
			if(refMap.containsKey(ref))
			{
				int num = (Integer) refMap.get(ref);
				refMap.put(ref, num+1);
			}
			else
			{
				refMap.put(ref, 1);
			}
		}		
		for(String model: modelList)
		{
			if(modelMap.containsKey(model))
			{
				int num = (Integer) modelMap.get(model);
				modelMap.put(model, num+1);
			}
			else
			{
				modelMap.put(model, 1);
			}
		}
		
		Set<String> keys = modelMap.keySet();
		for(String key: keys)
		{
			 int num = (Integer) modelMap.get(key);
			 if(!refMap.containsKey(key))
				 continue;
			 int numRef = (Integer) refMap.get(key);
			 if(num<numRef)
				 count+=num;
			 else
				 count+=numRef;
		}
		return count;
	}
	
	public ArrayList<String> getTokenList(String string)
	{
		ArrayList<String> result = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(string.trim());
		Pattern p = Pattern.compile("\\p{Punct}");
		
		ArrayList<String> allList = new ArrayList<String>();
		while(st.hasMoreTokens())
		{
			String token = st.nextToken();
			allList.addAll(getBrokenUpTokenList(token));
		}		
		for(String token:allList)
		{
			Matcher matcher = p.matcher(token);
			boolean matchFound = matcher.matches();
			token = token.toLowerCase();
			if(matchFound)
				continue;
			result.add(token);	
		}
		return result;
	}
	
	public List<String> getBrokenUpTokenList(String token)
	{
		String[] res = token.split("[^a-zA-Z0-9]");
		List<String> result = new ArrayList<String>();
		for(String string: res)
		{
			string = string.trim();
			if(string==null||string.equals(""))
				continue;
			result.add(string);
		}
		return result;
	}
	
	public double getRouge1Precision(String ref, String model)
	{
		ArrayList<String> refList = getTokenList(ref);
		ArrayList<String> modelList = getTokenList(model);
		double matches = getMatches(refList, modelList);

		double denom = modelList.size();
		
		return matches/denom;
	}
	
	public double getRouge1FMeasure(String ref, String model)
	{
		double p = getRouge1Precision(ref,model);
		double r = getRouge1Recall(ref,model);
		
		double num = 2*p*r;
		double denom = (p+r);
		
		if(denom==0)
			return 0;
		
		return num/denom;
	}
	
	public ArrayList<String> getTokenList2(String string)
	{
		ArrayList<String> result = getTokenList(string);
		ArrayList<String> list = new ArrayList<String>();
		int size1 = result.size();
		for(int i = 1; i < size1; i ++)
		{
			String bigram = result.get(i-1)+" "+result.get(i);
			list.add(bigram);
		}		
		return list;
	}
	
	public double getRouge2Recall(String ref, String model)
	{
		ArrayList<String> refList = getTokenList2(ref);
		ArrayList<String> modelList = getTokenList2(model);
		double matches = getMatches(refList, modelList);
		double denom = refList.size();
		
		return matches/denom;
	}
	
	
	
	public double getRouge2Precision(String ref, String model)
	{
		ArrayList<String> refList = getTokenList2(ref);
		ArrayList<String> modelList = getTokenList2(model);
		double matches = getMatches(refList, modelList);
		double denom = modelList.size();
		return matches/denom;
	}
	
	public double getRouge2FMeasure(String ref, String model)
	{
		double p = getRouge2Precision(ref,model);
		double r = getRouge2Recall(ref,model);
		
		double num = 2*p*r;
		double denom = (p+r);
		
		if(denom==0)
			return 0;
		
		return num/denom;
	}
}