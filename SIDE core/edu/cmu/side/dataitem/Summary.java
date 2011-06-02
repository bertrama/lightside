package edu.cmu.side.dataitem;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import org.w3c.dom.Element;

import com.mysterion.xml.XMLBoss;
import com.yerihyo.yeritools.collections.CollectionsToolkit;
import com.yerihyo.yeritools.io.FileToolkit;
import com.yerihyo.yeritools.swing.ListViewerPanel;
import com.yerihyo.yeritools.swing.SwingToolkit;
import com.yerihyo.yeritools.xml.XMLToolkit;
import com.yerihyo.yeritools.xml.XMLable;

import edu.cmu.side.SIDEToolkit.FileType;
import edu.cmu.side.uima.UIMAToolkit;
import edu.cmu.side.uima.UIMAToolkit.DocumentList;
import edu.cmu.side.uima.type.SIDESegment;

public class Summary extends DataItem implements XMLable{

	private Recipe[] recipeArray;
//	private DocumentList documentList;
	public static final String tagName = "summary";

	private Summary(){}
	public Summary(Recipe[] recipeArray){
		this.recipeArray = recipeArray;
//		this.documentList = documentList;
	}
	
	public static Summary create(Element root){
		Summary summary = new Summary();
		summary.fromXML(root);
		return summary;
	}
	
	public TextRecipe[] getTextRecipeArray(){
		List<TextRecipe> textRecipeList = new ArrayList<TextRecipe>();
		
		for(int i=0; i<recipeArray.length; i++){
			if(!(recipeArray[i] instanceof TextRecipe)){ continue; }
			
			TextRecipe textRecipe = (TextRecipe)recipeArray[i];
			textRecipeList.add(textRecipe);
		}
		return textRecipeList.toArray(new TextRecipe[0]);
	}
	
	public TrainingResult[] getUsedTrainingResultArray(){
		List<TrainingResult> trainingResultList = new ArrayList<TrainingResult>();
		for(TextRecipe textRecipe : this.getTextRecipeArray()){
			CollectionsToolkit.addAll(trainingResultList, textRecipe.getTrainingResultArray());
		}
		return trainingResultList.toArray(new TrainingResult[0]);
	}
	
	@Override
	public FileType getFileType() {
		return FileType.summary;
	}

	public static Summary loadFile(File file) throws FileNotFoundException, IOException {
		System.out.println(file.getAbsolutePath());
		Element root = XMLBoss.XMLFromFile(file).getDocumentElement();
		return Summary.create(root);
	}

	public void save(File selectedFile) throws IOException{
		FileToolkit.writeTo(selectedFile, this.toXML());
	}
	public void fromXML(Element root){
		List<Recipe> recipeList = new ArrayList<Recipe>();
		for(Element element : XMLToolkit.getChildElements(root)){
			String tagName = element.getTagName();
			if(tagName.equals("recipes")){
				for(Element recipeElement : XMLToolkit.getChildElements(element)){
					Recipe recipe = Recipe.create(recipeElement);
					recipeList.add(recipe);
				}
			}
			else{
				this.itemsFromXML(element);
			}
		}
		
		recipeArray = recipeList.toArray(new Recipe[0]);
	}
	public String toXML(){
		StringBuilder builder = new StringBuilder();
		builder.append(this.itemsToXML());
		
		StringBuilder recipeStringBuilder = new StringBuilder();
		for(Recipe recipe: recipeArray){
			recipeStringBuilder.append(recipe.toXML());
		}
		builder.append(XMLToolkit.wrapContentWithTag(recipeStringBuilder, "recipes"));
//		builder.append(documentList.toXML());
		
		return XMLToolkit.wrapContentWithTag(builder, tagName).toString();
	}
	
	public Component summarize(DocumentList documentList) {
		List<SIDESegment> textRecipeResultList = new ArrayList<SIDESegment>();
		for(Recipe recipe : recipeArray){
			
			if(recipe instanceof TextRecipe){
				textRecipeResultList.addAll( (List<SIDESegment>)recipe.summarize(documentList) );
			}else{
				throw new UnsupportedOperationException();
			}
		}
		
		ListViewerPanel<String> listViewerPanel = new ListViewerPanel<String>(); 
		listViewerPanel.setItemArray(UIMAToolkit.toStringArray(textRecipeResultList));
		
		
		JScrollPane scrollPane = new JScrollPane(listViewerPanel);
		SwingToolkit.adjustScrollBar(scrollPane, JScrollBar.VERTICAL);
		return scrollPane;
//		TestDialog testDialog = new TestDialog(scrollPane, null, Dialog.ModalityType.APPLICATION_MODAL);
//		
//		testDialog.setSize(new Dimension(800,600));
//		testDialog.showDialog();
	}
	
	public Map<String,String> createColumnNameMap(DocumentList documentList) {
		TrainingResult[] trainingResultArray = this.getUsedTrainingResultArray();
		
		Map<String,String> renderingMap = new HashMap<String,String>();
		for(TrainingResult trainingResult : trainingResultArray){
			String subtypeName = trainingResult.getPredictionSubtypeNameArray(documentList)[1];
			renderingMap.put(subtypeName, "model: "+trainingResult.getDisplayText());
		}
		
		return renderingMap;
	}
}
