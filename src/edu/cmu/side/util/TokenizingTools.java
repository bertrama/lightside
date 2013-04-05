package edu.cmu.side.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JOptionPane;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.StringUtils;

public class TokenizingTools
{
	private static MaxentTagger tagger;
	private static TokenizerFactory factory;

	static
	{
		try
		{
			tagger = new MaxentTagger("toolkits/maxent/left3words-wsj-0-18.tagger");
//			factory = PTBTokenizerFactory.newPTBTokenizerFactory(false, true);
			factory = PTBTokenizerFactory.newTokenizerFactory();
			// check if we are to use a custom stoplist
			//
			// this should be only a file name with the file being present in
			// the etc/ directory of TagHelperTools2
		}
		catch (Exception e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not find MaxentTagger files", "ERROR", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
//	public static List<? extends CoreLabel> tokenizeInvertible(String s)
//	{
//		StringReader reader = new StringReader(s.toLowerCase());
//		Tokenizer<? extends CoreLabel> tokenizer = factory.getTokenizer(reader);
//
//		System.out.println(tokenizer);
//		
//		List<? extends CoreLabel> tokens = tokenizer.tokenize();
//		return tokens;
//	}


	public static List<String> tokenize(String s)
	{
		StringReader reader = new StringReader(s.toLowerCase());
		Tokenizer<HasWord> tokenizer = factory.getTokenizer(reader);
		List<String> tokens = new ArrayList<String>();

		while (tokenizer.hasNext())
		{
			HasWord token = tokenizer.next();
			
			tokens.add(token.word());
		}
		return tokens;
	}
	
	public static Map<String, List<String>> tagAndTokenize(String s)
	{
		
		Map<String, List<String>> tagsAndTokens = new HashMap<String, List<String>>();
		List<String> posTags = new ArrayList<String>();
		List<String> surfaceTokens = tokenize(s);
		
		String tokenized = StringUtils.join(surfaceTokens, " ");
		String tagged = tagger.tagTokenizedString(tokenized);
		
//		String tagged = tagger.tagString(s);

		String[] taggedTokens = tagged.split("\\s+");
		tagsAndTokens.put("tokens", surfaceTokens);
		tagsAndTokens.put("POS", posTags);
		
		for (String t : taggedTokens)
		{
			if (t.contains("_"))
			{
				String[] parts = t.split("_");
				posTags.add(parts[1]);
			}
			else
			{
				System.out.println("TT 84: no POS tag? "+t);
				posTags.add(t);
			}
		}
		
		return tagsAndTokens;
	}
	
	public static void main(String[] args)
	{
		Scanner skinner = new Scanner(System.in);
		
		while(skinner.hasNextLine())
		{
			String line = skinner.nextLine();
			if(line.equals("q"))
				return;
			List<String> tokenized = tokenize(line);
//			List<? extends CoreLabel> tokenizedToo = tokenizeInvertible(line);
			Map<String, List<String>> posTokens = tagAndTokenize(line);

//			System.out.println(tokenizedToo.size());
			System.out.println(tokenized.size()+":\t"+tokenized);
			System.out.println(posTokens.get("POS").size()+":\t"+posTokens);
		}
	}
}
