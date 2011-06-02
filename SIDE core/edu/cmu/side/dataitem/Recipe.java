package edu.cmu.side.dataitem;

/*
 * 	<recipe name="">
 <conditions>
 <condition name="First Paragraph" id="1">
 <filter name="Paragraphs" path="c:/side workspace/filters/testfilter.flt"/>
 <label>first paragraph</label>
 </condition>
 <condition name="Key Sentence" id="2">
 <filter name="Sentences" path="c:/side workspace/filters/testfilter.flt"/>
 <label name="key sentence"/>
 </condition> 
 </conditions>
 <expression>
 <and>
 <Recipe id="1"/>
 <or>
 <Recipe id="2"/>
 <Recipe id="3"/>
 </or>
 </and>
 </expression>
 <options>
 <rank apply="yes|no">
 <metric>
 <pluginWrapper name="Average TFIDF Score" classname="edu.cmu.side.tfidf"/>
 <options>
 <option name="scale"><![CDATA[0.75]]></option>
 </options>
 </metric>
 </rank>
 <limit apply="yes|no">
 <type top="" percent="yes|no">5</type>
 </limit>
 <restore apply="yes"/>
 </options>
 </recipe> 

 */
import java.io.File;
import java.io.IOException;

import org.w3c.dom.Element;

import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.xml.XMLable;

import edu.cmu.side.SIDEToolkit.FileType;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;

public abstract class Recipe extends DataItem implements XMLable {
	public abstract Object summarize(DocumentList documentList);


	protected abstract String getRecipeTypeName();
	public CharSequence wrapContentByRecipeTag(CharSequence content){
		StringBuilder builder = new StringBuilder();
		builder.append("<recipe type=\""+getRecipeTypeName()+"\">");
		builder.append(content);
		builder.append("</recipe>");
		return builder;
	}

	public static Recipe create(Element element) {
		String recipeTypeName = element.getAttribute("type");
		if(recipeTypeName.equals(TextRecipe.recipeTypeName)){
			TextRecipe textRecipe = TextRecipe.create(element);
			return textRecipe;
		}else{
			throw new UnsupportedOperationException();
		}
	}

	public FileType getFileType() {
		return FileType.summary;
	}

	public void save(File selectedFile) throws IOException {
		FileToolkit.writeTo(selectedFile, this.toXML());
	}

	public static final File folder = FileType.recipe.getDefaultFolder();

	@Override
	public File getDefaultFolder() {
		return folder;
	}
}
