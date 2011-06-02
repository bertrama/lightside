package edu.cmu.side.ml;

import java.io.BufferedReader;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class EvaluationToolkit {

	public static final int SELECT = 2;
	public static enum ROUGE {R1, R2, R3, SU4};

	public static List<String> simpleTokenize(String line, int n){
		List<String> out = new ArrayList<String>();
		if (line == null || (line.trim().length() == 0))
			return out;
		//		line = line.trim().toLowerCase();
		String[] split = line.split("([\\W|_&&[^'-<>]])");
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < split.length; i++){
			sb.setLength(0);
			for(int j = 0; j < n; j++){
				if(i+j < split.length)
					sb.append(split[i+j]); sb.append("_");
			}
			String toAdd = sb.toString();
			if(toAdd.length() > 1) out.add(toAdd);
		}
		return out;
	}

	public static double getROUGE(ROUGE form, List<String> summT, List<String> refT){
		double eval = 0.0;
		int n = 0;
		switch(form){
		case R1:
			n = 1;
			break;
		case R2:
			n = 2;
			break;
		case R3:
			n = 3;
			break;
		case SU4:
			System.err.println("This evaluation format not available yet.");
			break;
		}
		if(summT.size() > refT.size()){
			List<String> temp = refT;
			refT = summT;
			summT = temp;
		}
		double count = 0;
		for(String s : summT) if(refT.contains(s)) count++;
		eval = count/(0.0+refT.size());
		return eval;
	}

	public static double getROUGE(ROUGE form, String summ, String ref){
		double eval = 0.0;
		int n = 0;
		switch(form){
		case R1:
			n = 1;
			break;
		case R2:
			n = 2;
			break;
		case R3:
			n = 3;
			break;
		case SU4:
			System.err.println("This evaluation format not available yet.");
			break;
		}
		List<String> summT = simpleTokenize(summ,n);
		List<String> refT = simpleTokenize(ref,n);
		if(summT.size() > refT.size()){
			List<String> temp = refT;
			refT = summT;
			summT = temp;
		}
		double count = 0.0;
		for(String s : summT) if(refT.contains(s)) count++;
		eval = count/(0.0+refT.size());
		return eval;
	}

	public static void main(String[] args){
		try{
			BufferedReader in = new BufferedReader(new FileReader("/Users/emayfiel/Downloads/files_for_elijah/laum_1_rm_graded_w_psych_extras.csv"));
			String line = in.readLine();
			Map<Integer, List<List<String>>> examples = new TreeMap<Integer, List<List<String>>>();
			Map<Integer, List<String>> exampleStrs = new TreeMap<Integer, List<String>>();
			while((line = in.readLine()) != null){
				String[] broken = line.split(",");
				Integer key = Integer.parseInt(broken[1]);
				String answer = "";
				for(int i = 2; i < broken.length; i++){
					answer += broken[i]; if(i+1 != broken.length) answer += ",";
				}
				if(!examples.containsKey(key)){
					examples.put(key, new ArrayList<List<String>>());
					exampleStrs.put(key, new ArrayList<String>());
				}
				examples.get(key).add(simpleTokenize(answer.trim().toLowerCase(), SELECT));
				exampleStrs.get(key).add(answer.trim().toLowerCase());
			}
			in.close();
			for(Integer score : examples.keySet()){
				List<List<String>> answersAtScore = examples.get(score);
				List<String> answers = exampleStrs.get(score);
				for(Integer other : examples.keySet()){
					if(!other.equals(score)) ;
				}
				double total = 0.0;
				double max = 0.0;
				for(String str : answers){
					List<String> s = simpleTokenize(str,SELECT);
					for(int i = 0; i < answersAtScore.size(); i++){
						List<String> o = answersAtScore.get(i);
						String oSt = answers.get(i);
						double rge = getROUGE(ROUGE.R2,s,o);
						total += rge;
						if(rge > max) max = rge;
						if(rge > 0.7 && rge < 1.0){
							System.out.println(rge + "\n" + str + "\n" + oSt + "\n");
						}
					}
				}
				double denom = (answersAtScore.size()*(answersAtScore.size()-1));
				for(Integer oscore : examples.keySet()){
					if(!oscore.equals(score)){
						List<List<String>> answersOther = examples.get(oscore);
						List<String> answersOtherStrs = exampleStrs.get(oscore);
						total = 0.0;
						max = 0.0;
						for(String str : answers){
							List<String> s = simpleTokenize(str,SELECT);
							for(int i = 0; i < answersOther.size(); i++){
								List<String> o = answersOther.get(i);
								String oSt = answersOtherStrs.get(i);
								double rge = getROUGE(ROUGE.R2,s,o);
								total += rge;
								if(rge > max) max = rge;
							}
						}
						denom = (answersAtScore.size()*(answersOther.size()));
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
